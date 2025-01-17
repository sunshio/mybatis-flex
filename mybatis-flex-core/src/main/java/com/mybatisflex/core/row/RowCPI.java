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

import java.util.Collection;

/**
 * cross package invoker
 */
public class RowCPI {

    public static void keepModifyAttrs(Row row, Collection<String> attrs) {
        row.keepModifyAttrs(attrs);
    }

    public static Object[] obtainModifyValues(Row row) {
        return row.obtainModifyValues();
    }

    public static String[] obtainsPrimaryKeyStrings(Row row) {
        return row.obtainsPrimaryKeyStrings();
    }

    public static RowKey[] obtainsPrimaryKeys(Row row) {
        return row.obtainsPrimaryKeys();
    }

    public static Object[] obtainsPrimaryValues(Row row) {
        return row.obtainsPrimaryValues();
    }

    public static Object[] obtainAllModifyValues(Row row) {
        return row.obtainAllModifyValues();
    }

}
