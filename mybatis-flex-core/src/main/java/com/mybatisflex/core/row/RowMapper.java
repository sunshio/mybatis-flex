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
package com.mybatisflex.core.row;

import com.mybatisflex.core.FlexConsts;
import com.mybatisflex.core.exception.FlexExceptions;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.provider.RowSqlProvider;
import com.mybatisflex.core.query.CPI;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.StringUtil;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.exceptions.TooManyResultsException;

import java.util.Collection;
import java.util.List;
import java.util.Map;


public interface RowMapper {

    //////insert //////

    /**
     * 插入 row 到数据表
     *
     * @param tableName 表名
     * @param row       数据内容，当设置有主键时，主键会自动填充
     * @return 执行影响的行数
     * @see RowSqlProvider#insert(Map)
     */
    @InsertProvider(value = RowSqlProvider.class, method = "insert")
    int insert(@Param(FlexConsts.TABLE_NAME) String tableName, @Param(FlexConsts.ROW) Row row);


    /**
     * 执行 insert sql 语句
     *
     * @param sql  insert sql 语句
     * @param args 参数
     * @return 执行影响的行数
     * @see Db#insertBySql(String, Object...)
     */
    @InsertProvider(value = RowSqlProvider.class, method = RowSqlProvider.METHOD_RAW_SQL)
    int insertBySql(@Param(FlexConsts.SQL) String sql, @Param(FlexConsts.SQL_ARGS) Object... args);


    /**
     * 批量插入 rows 到数据表
     * <p>
     * 注意，批量插入中，只会根据第一条 row 数据来构建 Sql 插入字段，若每条数据字段不一致，可能造成个别字段无法插入的情况
     *
     * @param tableName 表名
     * @param rows      数据内容，当设置有主键时，主键会自动填充
     * @return 执行影响的行数
     * @see RowSqlProvider#insertBatchWithFirstRowColumns(Map)
     */
    @InsertProvider(value = RowSqlProvider.class, method = "insertBatchWithFirstRowColumns")
    int insertBatchWithFirstRowColumns(@Param(FlexConsts.TABLE_NAME) String tableName, @Param(FlexConsts.ROWS) List<Row> rows);


    /////// delete /////

    /**
     * 执行 delete sql 语言
     *
     * @param sql  delete sql 语句
     * @param args 参数
     * @return 执行影响的行数
     */
    @DeleteProvider(value = RowSqlProvider.class, method = RowSqlProvider.METHOD_RAW_SQL)
    int deleteBySql(@Param(FlexConsts.SQL) String sql, @Param(FlexConsts.SQL_ARGS) Object... args);

    /**
     * 根据 id 删除数据
     *
     * @param tableName 表名
     * @param row       id 和 值的数据，可以通过 {@link Row#ofKey(String, Object)} 来创建
     * @return 执行影响的行数
     */
    default int deleteById(@Param(FlexConsts.TABLE_NAME) String tableName, @Param(FlexConsts.ROW) Row row) {
        return deleteById(tableName, StringUtil.join(",", row.obtainsPrimaryKeyStrings()), row.obtainsPrimaryValues());
    }

    /**
     * 根据 id 删除数据
     *
     * @param tableName  表名
     * @param primaryKey 主键，多个主键用英文逗号隔开
     * @param id         数据，多个主键时传入数组，例如 new Object[]{1,2}
     * @return 执行影响的行数
     * @see RowSqlProvider#deleteById(Map)
     */
    @DeleteProvider(value = RowSqlProvider.class, method = "deleteById")
    int deleteById(@Param(FlexConsts.TABLE_NAME) String tableName, @Param(FlexConsts.PRIMARY_KEY) String primaryKey, @Param(FlexConsts.PRIMARY_VALUE) Object id);


    /**
     * 根据 多个 id 值删除多条数据
     *
     * @param tableName  表名
     * @param primaryKey 主键
     * @param ids        id 的集合
     * @return 执行影响的行数
     * @see RowSqlProvider#deleteBatchByIds(Map)
     */
    @DeleteProvider(value = RowSqlProvider.class, method = "deleteBatchByIds")
    int deleteBatchByIds(@Param(FlexConsts.TABLE_NAME) String tableName, @Param(FlexConsts.PRIMARY_KEY) String primaryKey, @Param(FlexConsts.PRIMARY_VALUE) Collection<?> ids);


    /**
     * 根据 queryWrapper 构建 where 条件来删除数据
     *
     * @param tableName    表名
     * @param queryWrapper queryWrapper
     * @return 执行影响的行数
     * @see RowSqlProvider#deleteByQuery(Map)
     */
    @DeleteProvider(value = RowSqlProvider.class, method = "deleteByQuery")
    int deleteByQuery(@Param(FlexConsts.TABLE_NAME) String tableName, @Param(FlexConsts.QUERY) QueryWrapper queryWrapper);


    ////////update ////

    /**
     * 执行 update sql 语句
     *
     * @param sql  sql 语句
     * @param args 参数内容
     * @return 执行影响的行数
     */
    @UpdateProvider(value = RowSqlProvider.class, method = RowSqlProvider.METHOD_RAW_SQL)
    int updateBySql(@Param(FlexConsts.SQL) String sql, @Param(FlexConsts.SQL_ARGS) Object... args);


    /**
     * 根据主键来更新数据
     *
     * @param tableName 表名
     * @param row       数据，其必须包含主键数据列名和值
     * @return 执行影响的行数
     * @see RowSqlProvider#updateById(Map)
     */
    @UpdateProvider(value = RowSqlProvider.class, method = "updateById")
    int updateById(@Param(FlexConsts.TABLE_NAME) String tableName, @Param(FlexConsts.ROW) Row row);


    /**
     * 根据 queryWrapper 来构建 where 条件更新数据
     *
     * @param tableName    表名
     * @param data         更新数据
     * @param queryWrapper queryWrapper
     * @return 执行影响的行数
     * @see RowSqlProvider#updateByQuery(Map)
     */
    @UpdateProvider(value = RowSqlProvider.class, method = "updateByQuery")
    int updateByQuery(@Param(FlexConsts.TABLE_NAME) String tableName, @Param(FlexConsts.ROW) Row data, @Param(FlexConsts.QUERY) QueryWrapper queryWrapper);


    /**
     * 根据主键来批量更新数据
     * 注意：
     * 1、此方法需要在 mysql 等链接配置需要开启 allowMultiQueries=true
     * 2、更新成功返回的结果也可能为 0
     *
     * @param tableName 表名
     * @param rows      数据，其必须包含主键数据列名和值
     * @return 执行影响的行数
     * @see RowSqlProvider#updateBatchById(Map)
     */
    @UpdateProvider(value = RowSqlProvider.class, method = "updateBatchById")
    int updateBatchById(@Param(FlexConsts.TABLE_NAME) String tableName, @Param(FlexConsts.ROWS) List<Row> rows);

    ///////select /////

    /**
     * 通过原生 SQL 查询 1 条数据，要求数据必须返回 1 条内容，否则会报错
     *
     * @param sql  select sql 语句
     * @param args 参数
     * @return 返回一条数据
     */
    default Row selectOneBySql(String sql, Object... args) {
        List<Row> rows = selectListBySql(sql, args);
        if (rows == null || rows.isEmpty()) {
            return null;
        } else if (rows.size() == 1) {
            return rows.get(0);
        } else {
            /** 当返回多条数据时，抛出异常, 保持和 Mybatis DefaultSqlSession 的统一逻辑,
             * see: {@link org.apache.ibatis.session.defaults.DefaultSqlSession#selectOne(String, Object)} **/
            throw new TooManyResultsException("Expected one result (or null) to be returned by selectOneBySql(), but found: " + rows.size());
        }
    }

    /**
     * 通过主键来查询数据
     *
     * @param tableName 表名
     * @param row       主键和ID的描述，通过 {@link Row#ofKey(String, Object)} 来进行构建
     * @return 返回一条数据，或者 null
     */
    default Row selectOneById(String tableName, Row row) {
        return selectOneById(tableName, StringUtil.join(",", row.obtainsPrimaryKeyStrings()), row.obtainsPrimaryValues());
    }


    /**
     * 根据主键来查询数据
     *
     * @param tableName  表名
     * @param primaryKey 主键
     * @param id         id 值
     * @return row or null
     * @see RowSqlProvider#selectOneById(Map)
     */
    @SelectProvider(value = RowSqlProvider.class, method = "selectOneById")
    Row selectOneById(@Param(FlexConsts.TABLE_NAME) String tableName, @Param(FlexConsts.PRIMARY_KEY) String primaryKey, @Param(FlexConsts.PRIMARY_VALUE) Object id);


    /**
     * 根据 queryWrapper 来查询 1 条数据
     *
     * @param tableName    表名
     * @param queryWrapper queryWrapper
     * @return row or null
     */
    default Row selectOneByQuery(String tableName, QueryWrapper queryWrapper) {
        List<Row> rows = selectListByQuery(tableName, queryWrapper.limit(1));
        if (rows == null || rows.isEmpty()) {
            return null;
        } else {
            return rows.get(0);
        }
    }

    /**
     * 通过自定义 sql 来查询一个 Row 列表
     *
     * @param sql  自定义的 sql
     * @param args sql 参数
     * @return row 列表
     */
    @SelectProvider(value = RowSqlProvider.class, method = RowSqlProvider.METHOD_RAW_SQL)
    List<Row> selectListBySql(@Param(FlexConsts.SQL) String sql, @Param(FlexConsts.SQL_ARGS) Object... args);


    /**
     * 根据 queryWrapper 来查询一个 row 列表
     *
     * @param tableName    表名
     * @param queryWrapper queryWrapper
     * @return row 列表
     * @see RowSqlProvider#selectListByQuery(Map)
     */
    @SelectProvider(value = RowSqlProvider.class, method = "selectListByQuery")
    List<Row> selectListByQuery(@Param(FlexConsts.TABLE_NAME) String tableName, @Param(FlexConsts.QUERY) QueryWrapper queryWrapper);


    /**
     * 查询某张表的全部数据
     *
     * @param tableName 表名
     * @return row 列表
     */
    default List<Row> selectAll(@Param(FlexConsts.TABLE_NAME) String tableName) {
        return selectListByQuery(tableName, QueryWrapper.create());
    }

    /**
     * 通过 sql 查询某一个数据，sql 执行的结果应该只有 1 行 1 列
     * 若返回有多列，则只取第一列的值，若有多行，则会出现 TooManyResultsException 错误
     *
     * @param sql  sql
     * @param args sql 参数
     * @return object
     */
    @SelectProvider(value = RowSqlProvider.class, method = RowSqlProvider.METHOD_RAW_SQL)
    Object selectObject(@Param(FlexConsts.SQL) String sql, @Param(FlexConsts.SQL_ARGS) Object... args);


    /**
     * 通过 sql 查询多行数据，sql 执行的结果应该只有 1 列
     *
     * @param sql  sql 语句
     * @param args sql 参数
     * @return object list
     */
    @SelectProvider(value = RowSqlProvider.class, method = RowSqlProvider.METHOD_RAW_SQL)
    List<Object> selectObjectList(@Param(FlexConsts.SQL) String sql, @Param(FlexConsts.SQL_ARGS) Object... args);


    /**
     * 查询数据，一般用于 select count(*)... 的语言，也可用于执行的结果只有一个数值的其他 sql
     *
     * @param sql  sql 语句
     * @param args sql 参数
     * @return 返回数据
     */
    default long selectCount(String sql, Object... args) {
        Object object = selectObject(sql, args);
        if (object == null) {
            return 0;
        } else if (object instanceof Number) {
            return ((Number) object).longValue();
        } else {
            throw FlexExceptions.wrap("selectCount error, Can not get number value for sql: %s", sql);
        }
    }


    /**
     * 根据 queryWrapper 来查询数量
     *
     * @param tableName    表名
     * @param queryWrapper queryWrapper
     * @return 数量
     * @see RowSqlProvider#selectCountByQuery(Map)
     */
    @SelectProvider(value = RowSqlProvider.class, method = "selectCountByQuery")
    long selectCountByQuery(@Param(FlexConsts.TABLE_NAME) String tableName, @Param(FlexConsts.QUERY) QueryWrapper queryWrapper);


    /**
     * 分页查询数据
     *
     * @param tableName    表名
     * @param page         page 封装类
     * @param queryWrapper 条件
     * @return
     */
    default Page<Row> paginate(String tableName, Page<Row> page, QueryWrapper queryWrapper) {

        List<QueryColumn> groupByColumns = CPI.getGroupByColumns(queryWrapper);

        // 只有 totalRow 小于 0 的时候才会去查询总量
        // 这样方便用户做总数缓存，而非每次都要去查询总量
        // 一般的分页场景中，只有第一页的时候有必要去查询总量，第二页以后是不需要的
        if (page.getTotalRow() < 0) {

            //清除group by 去查询数据
            CPI.setGroupByColumns(queryWrapper, null);
            long count = selectCountByQuery(tableName, queryWrapper);
            page.setTotalRow(count);
        }

        if (page.getTotalRow() == 0 || page.getPageNumber() > page.getTotalPage()) {
            return page;
        }

        //恢复数量查询清除的 groupBy
        CPI.setGroupByColumns(queryWrapper, groupByColumns);
        int offset = page.getPageSize() * (page.getPageNumber() - 1);
        queryWrapper.limit(offset, page.getPageSize());
        List<Row> records = selectListByQuery(tableName, queryWrapper);
        page.setRecords(records);
        return page;
    }


}
