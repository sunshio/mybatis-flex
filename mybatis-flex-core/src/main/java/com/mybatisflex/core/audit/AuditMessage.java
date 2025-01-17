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
package com.mybatisflex.core.audit;

import com.mybatisflex.core.mybatis.TypeHandlerObject;
import com.mybatisflex.core.util.DateUtil;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class AuditMessage implements Serializable {

    private String platform;
    private String module;
    private String url;
    private String bizId; //自定义业务ID

    private String user;
    private String userIp;
    private String hostIp;

    private String query;
    private List<Object> queryParams;
    private int queryCount;

    private long queryTime;     // Sql 执行的当前时间，单位毫秒
    private long elapsedTime;   // Sql 执行消耗的时间，单位毫秒

    private Map<String, Object> metas; //其他信息，元信息


    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<Object> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<Object> queryParams) {
        this.queryParams = queryParams;
    }

    public void addParams(Object... objects) {
        if (queryParams == null) {
            queryParams = new ArrayList<>();
        }
        for (Object object : objects) {
            if (object != null && (object.getClass().isArray()
                    || object.getClass() == int[].class
                    || object.getClass() == long[].class
                    || object.getClass() == short[].class
                    || object.getClass() == float[].class
                    || object.getClass() == double[].class)
            ) {
                for (int i = 0; i < Array.getLength(object); i++) {
                    addParams(Array.get(object, i));
                }
            } else if (object instanceof TypeHandlerObject) {
                try {
                    ((TypeHandlerObject) object).setParameter(createPreparedStatement(), 0);
                } catch (SQLException e) {
                    //ignore
                }
            } else {
                queryParams.add(object);
            }
        }
    }

    public String getFullSql() {
        String sql = getQuery();
        List<Object> params = getQueryParams();
        if (params != null) {
            for (Object value : params) {
                // null
                if (value == null) {
                    sql = sql.replaceFirst("\\?", "null");
                }
                // number
                else if (value instanceof Number || value instanceof Boolean) {
                    sql = sql.replaceFirst("\\?", value.toString());
                }
                // other
                else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("'");
                    if (value instanceof Date) {
                        sb.append(DateUtil.toDateTimeString((Date) value));
                    } else if (value instanceof LocalDateTime) {
                        sb.append(DateUtil.toDateTimeString(DateUtil.toDate((LocalDateTime) value)));
                    } else {
                        sb.append(value);
                    }
                    sb.append("'");
                    sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(sb.toString()));
                }
            }
        }
        return sql;
    }

    private PreparedStatement createPreparedStatement() {
        return (PreparedStatement) Proxy.newProxyInstance(
                AuditMessage.class.getClassLoader(),
                new Class[]{PreparedStatement.class}, (proxy, method, args) -> {
                    if (args != null && args.length == 2){
                        addParams(args[1]);
                    }
                    return null;
                });
    }

    public int getQueryCount() {
        return queryCount;
    }

    public void setQueryCount(int queryCount) {
        this.queryCount = queryCount;
    }

    public long getQueryTime() {
        return queryTime;
    }

    public void setQueryTime(long queryTime) {
        this.queryTime = queryTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Map<String, Object> getMetas() {
        return metas;
    }

    public void setMetas(Map<String, Object> metas) {
        this.metas = metas;
    }

    public void addMeta(String key, Object value) {
        if (metas == null) {
            metas = new HashMap<>();
        }
        metas.put(key, value);
    }

    @Override
    public String toString() {
        return "AuditMessage{" +
                "platform='" + platform + '\'' +
                ", module='" + module + '\'' +
                ", url='" + url + '\'' +
                ", bizId='" + bizId + '\'' +
                ", user='" + user + '\'' +
                ", userIp='" + userIp + '\'' +
                ", hostIp='" + hostIp + '\'' +
                ", query='" + query + '\'' +
                ", queryParams=" + queryParams +
                ", queryCount=" + queryCount +
                ", queryTime=" + queryTime +
                ", elapsedTime=" + elapsedTime +
                ", metas=" + metas +
                '}';
    }
}
