/**
 * Copyright (c) 2022-2023, Mybatis-Flex (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mybatisflex.core.query;


import com.mybatisflex.core.dialect.IDialect;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class QueryCondition implements Serializable {

    public static final String LOGIC_LIKE = "LIKE";
    public static final String LOGIC_GT = ">";
    public static final String LOGIC_GE = ">=";
    public static final String LOGIC_LT = "<";
    public static final String LOGIC_LE = "<=";
    public static final String LOGIC_EQUALS = "=";
    public static final String LOGIC_NOT_EQUALS = "!=";

    public static final String LOGIC_IS_NULL = "IS NULL";
    public static final String LOGIC_IS_NOT_NULL = "IS NOT NULL";

    public static final String LOGIC_IN = "IN";
    public static final String LOGIC_NOT_IN = "NOT IN";
    public static final String LOGIC_BETWEEN = "BETWEEN";
    public static final String LOGIC_NOT_BETWEEN = "NOT BETWEEN";


    protected QueryColumn column;
    protected String logic;
    protected Object value;
    protected boolean effective = true;

    //当前条件的上个条件
    protected QueryCondition before;
    //当前条件的上个下一个
    protected QueryCondition next;
    //两个条件直接的连接符
    protected SqlConnector connector;


    public static QueryCondition createEmpty() {
        return new QueryCondition().when(false);
    }


    public static QueryCondition create(String table, String column, String logic, Object value) {
        QueryCondition condition = new QueryCondition();
        condition.setColumn(new QueryColumn(table, column));
        condition.setLogic(logic);
        condition.setValue(value);
        return condition;
    }

    public static QueryCondition create(QueryColumn queryColumn, Object value) {
        return create(queryColumn, LOGIC_EQUALS, value);
    }

    public static QueryCondition create(QueryColumn queryColumn, String logic, Object value) {
        QueryCondition condition = new QueryCondition();
        condition.setColumn(queryColumn);
        condition.setLogic(logic);
        condition.setValue(value);
        return condition;
    }

    public QueryCondition() {
    }

    public QueryColumn getColumn() {
        return column;
    }

    public void setColumn(QueryColumn column) {
        this.column = column;
    }

    public Object getValue() {
        return checkEffective() ? value : null;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getLogic() {
        return logic;
    }

    public void setLogic(String logic) {
        this.logic = logic;
    }


    public QueryCondition when(boolean effective) {
        this.effective = effective;
        return this;
    }

    public void when(Supplier<Boolean> fn) {
        Boolean effective = fn.get();
        this.effective = (effective != null && effective);
    }

    public <T> QueryCondition when(Predicate<T> fn){
        Object val = this.value;
        if (LOGIC_LIKE.equals(logic) && val instanceof String) {
            String valStr = (String) val;
            if (valStr.startsWith("%")) {
                valStr = valStr.substring(1);
            }
            if (valStr.endsWith("%")) {
                valStr = valStr.substring(0, valStr.length() - 1);
            }
            val = valStr;
        }
        this.effective = fn.test((T) val);
        return this;
    }

    public boolean checkEffective() {
        return effective;
    }


    public QueryCondition and(String sql) {
        return and(new StringQueryCondition(sql));
    }

    public QueryCondition and(String sql, Object... params) {
        return and(new StringQueryCondition(sql, params));
    }

    public QueryCondition and(QueryCondition nextCondition) {
        return new Brackets(this).and(nextCondition);
    }

    public QueryCondition or(String sql) {
        return or(new StringQueryCondition(sql));
    }

    public QueryCondition or(String sql, Object... params) {
        return or(new StringQueryCondition(sql, params));
    }

    public QueryCondition or(QueryCondition nextCondition) {
        return new Brackets(this).or(nextCondition);
    }

    protected void connect(QueryCondition nextCondition, SqlConnector connector) {
        if (this.next != null) {
            this.next.connect(nextCondition, connector);
        } else {
            this.next = nextCondition;
            this.connector = connector;
            nextCondition.before = this;
        }
    }

    public String toSql(List<QueryTable> queryTables, IDialect dialect) {
        StringBuilder sql = new StringBuilder();
        //检测是否生效
        if (checkEffective()) {
            QueryCondition effectiveBefore = getEffectiveBefore();
            if (effectiveBefore != null) {
                sql.append(effectiveBefore.connector);
            }
            sql.append(getColumn().toConditionSql(queryTables, dialect));
            sql.append(" ").append(logic).append(" ");
            if (value instanceof QueryColumn) {
                sql.append(((QueryColumn) value).toConditionSql(queryTables, dialect));
            }
            //子查询
            else if (value instanceof QueryWrapper) {
                sql.append("(").append(dialect.buildSelectSql((QueryWrapper) value)).append(")");
            }
            //原生sql
            else if (value instanceof RawValue) {
                sql.append(((RawValue) value).getContent());
            }
            //正常查询，构建问号
            else {
                appendQuestionMark(sql);
            }
        }

        if (this.next != null) {
            return sql + next.toSql(queryTables, dialect);
        }

        return sql.toString();
    }


    protected QueryCondition getEffectiveBefore() {
        if (before != null && before.checkEffective()) {
            return before;
        } else if (before != null) {
            return before.getEffectiveBefore();
        } else {
            return null;
        }
    }


    protected void appendQuestionMark(StringBuilder sqlBuilder) {
        if (LOGIC_IS_NULL.equals(logic)
                || LOGIC_IS_NOT_NULL.equals(logic)
                || value instanceof QueryColumn
                || value instanceof QueryWrapper
                || value instanceof RawValue) {
            //do nothing
        }

        //between, not between
        else if (LOGIC_BETWEEN.equals(logic) || LOGIC_NOT_BETWEEN.equals(logic)) {
            sqlBuilder.append(" ? AND ? ");
        }
        //in, not in
        else if (LOGIC_IN.equals(logic) || LOGIC_NOT_IN.equals(logic)) {
            int paramsCount = calculateValueArrayCount();
            sqlBuilder.append('(');
            for (int i = 0; i < paramsCount; i++) {
                sqlBuilder.append('?');
                if (i != paramsCount - 1) {
                    sqlBuilder.append(',');
                }
            }
            sqlBuilder.append(')');
        } else {
            sqlBuilder.append(" ? ");
        }
    }


    private int calculateValueArrayCount() {
        Object[] values = (Object[]) value;
        int paramsCount = 0;
        for (Object object : values) {
            if (object != null && (object.getClass().isArray()
                    || object.getClass() == int[].class
                    || object.getClass() == long[].class
                    || object.getClass() == short[].class
                    || object.getClass() == float[].class
                    || object.getClass() == double[].class)) {
                paramsCount += Array.getLength(object);
            } else {
                paramsCount++;
            }
        }
        return paramsCount;
    }

    @Override
    public String toString() {
        return "QueryCondition{" +
                "column=" + column +
                ", logic='" + logic + '\'' +
                ", value=" + value +
                ", effective=" + effective +
                '}';
    }
}
