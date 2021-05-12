package com.lz.mybatis.plugin.utils;


import com.lz.mybatis.plugin.annotations.*;
import com.lz.mybatis.plugin.config.CustomerMapperBuilder;
import com.lz.mybatis.plugin.entity.ByInfo;
import com.lz.mybatis.plugin.entity.OrderByInfo;
import com.lz.mybatis.plugin.entity.Page;
import com.lz.mybatis.plugin.entity.ParameterInfo;
import com.lz.mybatis.plugin.utils.t.PluginTuple;
import com.lz.mybatis.plugin.utils.t.Tuple2;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;


public class SqlParseUtils {
    public final static String IS_DELETE = "is_delete";
    public final static String GMT_MODIFIED = "gmtModified";
    public final static String TABLE_ID = "TableId";
    public final static String BY = "By";
    public static String TAB = "    ";
    private static final List<Class<?>> primitiveTypes = new ArrayList<>(8);

    static {
        primitiveTypes.add(Boolean.class);
        primitiveTypes.add(Byte.class);
        primitiveTypes.add(Character.class);
        primitiveTypes.add(Double.class);
        primitiveTypes.add(Float.class);
        primitiveTypes.add(Integer.class);
        primitiveTypes.add(Long.class);
        primitiveTypes.add(Short.class);

        primitiveTypes.add(boolean.class);
        primitiveTypes.add(byte.class);
        primitiveTypes.add(char.class);
        primitiveTypes.add(double.class);
        primitiveTypes.add(float.class);
        primitiveTypes.add(int.class);
        primitiveTypes.add(long.class);
        primitiveTypes.add(short.class);
        primitiveTypes.add(String.class);
        primitiveTypes.add(Date.class);
        primitiveTypes.add(java.sql.Date.class);

        primitiveTypes.addAll(Arrays.asList(new Class<?>[]{
                boolean[].class, byte[].class, char[].class, double[].class,
                float[].class, int[].class, long[].class, short[].class}));

        primitiveTypes.addAll(Arrays.asList(new Class<?>[]{
                Boolean[].class, Byte[].class, Character[].class, Double[].class,
                Float[].class, Integer[].class, Long[].class, Short[].class, String[].class}));
    }

    private static List<String> tableColumns = Arrays.asList(new String[]{"id", "is_delete", "gmt_create", "gmt_modified", "type", "branch_id", "real_name", "mobile", "username", "task_id", "staff_id"});
    private static List<String> primaryC = Arrays.asList(new String[]{"id"});

    public static PluginTuple testSelect(Class clazz, String methodName) {
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;
        return parse("lz_test_user", primaryC, tableColumns, sqlCommandType, getMethod(clazz, methodName), null);

    }

    public static PluginTuple testInsert(Class clazz, String methodName) {
        SqlCommandType sqlCommandType = SqlCommandType.INSERT;
        return parse("lz_test_user", primaryC, tableColumns, sqlCommandType, getMethod(clazz, methodName), null);
    }

    public static PluginTuple testUpdate(Class clazz, String methodName) {
        SqlCommandType sqlCommandType = SqlCommandType.UPDATE;
        return parse("lz_test_user", primaryC, tableColumns, sqlCommandType, getMethod(clazz, methodName), null);
    }


    public static PluginTuple testDelete(Class clazz, String methodName) {
        SqlCommandType sqlCommandType = SqlCommandType.DELETE;
        return parse("lz_test_user", primaryC, tableColumns, sqlCommandType, getMethod(clazz, methodName), null);
    }

    public static PluginTuple testCount(Class clazz, String methodName) {
        SqlCommandType sqlCommandType = SqlCommandType.UNKNOWN;
        return parse("lz_test_user", primaryC, tableColumns, sqlCommandType, getMethod(clazz, methodName), null);
    }

    public static PluginTuple parse(String tableName, List<String> primaryColumns, List<String> tableColumns,
                                    SqlCommandType sqlCommandType, Method method, Class entityType) {
        DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        StringBuilder sb = new StringBuilder();
        if (SqlCommandType.SELECT.equals(sqlCommandType)) {
            if (method.getName().startsWith("count")) {
                return parseCount(tableName, tableColumns, parameterNames, method);
            } else if (method.getReturnType().equals(Page.class)) {
                return parseSelectPage(tableName, tableColumns, parameterNames, method, entityType);
            } else {
                return parseSelect(tableName, tableColumns, parameterNames, method);
            }
        } else if (SqlCommandType.INSERT.equals(sqlCommandType)) {
            if (method.getName().startsWith("insertOrUpdate")) {
                Tuple2<Boolean, String> tupleInsert = parseInsert(tableName, parameterNames, method).getData();
                String insertSql = removeScript(tupleInsert.getSecond());
                Tuple2<Boolean, String> tupleUpdate = parseUpdate(tableName, parameterNames, method).getData();
                String updateSql = removeScript(tupleUpdate.getSecond());
                StringBuilder sBuild = new StringBuilder();
                sBuild.append("<script> ").append("\n");
                sBuild.append("<choose>").append("\n");
                sBuild.append("<when ");
                if (primaryColumns == null || primaryColumns.size() == 0) {
                    primaryColumns = primaryC;
                }
                sBuild.append("test=\"");
                for (int i = 0; i < primaryColumns.size(); i++) {
                    if (i != 0) {
                        sBuild.append(" AND ");
                    }
                    sBuild.append(primaryColumns.get(i) + " != null ");
                }
                sBuild.append("\"");
                sBuild.append(" >").append("\n");
                sBuild.append(updateSql);
                sBuild.append(" </when>").append("\n");
                sBuild.append(" <otherwise>").append("\n");
                sBuild.append(insertSql);
                sBuild.append(" </otherwise>").append("\n");
                sBuild.append(" </choose>").append("\n");
                sBuild.append(" </script>").append("\n");
                return new PluginTuple(false, sBuild.toString());
            }
            return parseInsert(tableName, parameterNames, method);
        } else if (SqlCommandType.UPDATE.equals(sqlCommandType)) {
            return parseUpdate(tableName, parameterNames, method);
        } else if (SqlCommandType.DELETE.equals(sqlCommandType)) {
            return parseDelete(tableName, tableColumns, parameterNames, method);
        }
        return new PluginTuple(true, sb.toString());
    }


    private static PluginTuple parseSelectPage(String tableName, List<String> tableColumns, String[] parameterNames,
                                               Method method, Class entityType) {
        Class parameterTypes[] = method.getParameterTypes();
        ParameterInfo[] parameterInfos = getMethodParameterInfoByAnnotation(method);
        StringBuilder sql = new StringBuilder();
        StringBuilder resultMap = new StringBuilder();
        int paramCount = method.getParameterTypes() != null ? method.getParameterTypes().length : 0;
        String resultMapId = tableName + method.getName() + paramCount + "ResultMapId";
        Class clazz = findReturnGenericType(method, 0); //获取返回的第0们位置的泛型类型
        if (clazz != null) {
            entityType = clazz;
        }

        String entityName = entityType != null ? entityType.getName() : "";
        Field fields[] = entityType.getDeclaredFields();
        resultMap.append("<resultMap id=\"" + resultMapId + "\" type=\"com.lz.mybatis.plugin.entity.Page\">\n" +
                "        <id property=\"totalCount\" column=\"totalCount\" />\n" +
                "        <id property=\"pageCount\" column=\"pageCount\"></id>\n" +
                "        <id property=\"pageSize\" column=\"pageSize\"></id>\n" +
                "        <id property=\"currPage\" column=\"currPage\"></id>\n" +
                "        <collection property=\"list\" ofType=\"" + entityName + "\">\n");
        for (Field field : fields) {
            String realFieldName = getRealFieldName(field);
            String column = StringUtils.getDataBaseColumn(realFieldName);
            if ("id".equals(column)) {
                resultMap.append("            <id column=\"id\" property=\"id\"/>\n");
            } else {
                resultMap.append("            <result column=\"" + column + "\" property=\"" + realFieldName + "\"/>\n");
            }
        }
        resultMap.append("        </collection>\n" +
                "    </resultMap>");

        sql.append("<script> \n");
        sql.append(" <choose>");
        sql.append(" <when test=\"currPage=='0'.toString()\">\n" +
                "                <bind name=\"key_offset\" value=\"(currPage)*pageSize\"></bind>\n" +
                "            </when>\n" +
                "            <otherwise>\n" +
                "                <bind name=\"key_offset\" value=\"(currPage-1)*pageSize\"></bind>\n" +
                "            </otherwise>");
        sql.append("</choose>");
        sql.append(" select\n" +
                "        t1.totalCount,\n" +
                "        t1.pageCount,\n" +
                "        t1.pageSize,\n" +
                "        t1.currPage,\n");


        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String realFieldName = getRealFieldName(field);
            String column = StringUtils.getDataBaseColumn(realFieldName);
            sql.append("        t2." + column);
            if (i < fields.length - 1) {
                sql.append(",\n");
            }
        }

        String pageSize = "";
        String currPage = "";
        if (parameterInfos != null) {
            for (int i = 0; i < parameterInfos.length; i++) {
                ParameterInfo parameterInfo = parameterInfos[i];
                if (parameterInfo.isCurrPage()) {
                    currPage = parameterInfo.getCurrPage();
                    if (StringUtils.isEmpty(currPage)) {
                        currPage = parameterNames[i];
                    }
                }
                if (parameterInfo.isPageSize()) {
                    pageSize = parameterInfo.getPageSize();
                    if (StringUtils.isEmpty(pageSize)) {
                        pageSize = parameterNames[i];
                    }
                }
            }
        }
        sql.append("        from\n" +
                "        (select 1 as xxx, totalCount,\n" +
                "        (totalCount + #{" + pageSize + "} - 1) /  #{" + pageSize + "} as pageCount,\n" +
                "        #{" + pageSize + "} as pageSize,\n" +
                "        if(#{" + currPage + "}=0,1,#{" + currPage + "}) as currPage \n" +   // 如果传入的currPage为0，转变成1
                "        from \n" +
                "        (select count(*) as totalCount from ");
        sql.append(" ").append(tableName).append(" ");
        String sqlCondition = doGetSqlCondition(parameterTypes, parameterInfos, parameterNames);
        sql.append(sqlCondition);
        sql.append(") a) as t1  left join \n" +
                "        (");
        sql.append("select *,1 as xxx from ").append(tableName).append(" ").append(sqlCondition);
        sql.append(getOrderBySql(method, parameterInfos, parameterNames));
        // limit (#{currPage}-1)*#{pageSize},#{pageSize}
        sql.append(" limit ").append("#{key_offset},#{" + pageSize + "}");
        sql.append(") as t2  on t1.xxx = t2.xxx \n");
        sql.append("</script>");
        return new PluginTuple(true, sql.toString(), "", resultMapId, resultMap.toString());
    }

    public static PluginTuple parseSelect(String tableName, List<String> tableColumns, String[] parameterNames, Method method) {
        Class parameterTypes[] = method.getParameterTypes();
        ParameterInfo[] parameterInfos = getMethodParameterInfoByAnnotation(method);
        StringBuilder sql = new StringBuilder();
        sql.append("<script> \n");
        sql.append(TAB).append("SELECT").append(" * ").append("FROM ").append(tableName);
        sql.append(doGetSqlCondition(parameterTypes, parameterInfos, parameterNames));
        sql.append(getOrderBySql(method, parameterInfos, parameterNames));
        sql.append(getLimit(method));
        sql.append(" \n</script>");
        return new PluginTuple(true, sql.toString().trim());
    }

    public static String doGetSqlCondition(Class parameterTypes[], ParameterInfo[] parameterInfos, String[] parameterNames) {
        StringBuilder sql = new StringBuilder();
        if (parameterTypes != null && parameterTypes.length > 0) {
            sql.append(" WHERE ");
            for (int i = 0; i < parameterTypes.length; i++) {//遍历所有的参数
                sql.append(" ").append(getCondition("", parameterTypes, parameterInfos, parameterNames, i));
            }
        } else {
            sql.append(" WHERE 1 = 1 ");
        }
        if (tableColumns.contains(IS_DELETE)) {
            if (!sql.toString().trim().endsWith("AND")) {
                sql.append(" AND ");
            }
            sql.append(" IS_DELETE = 0 ");
        }
        return sql.toString();
    }

    public static PluginTuple parseInsert(String tableName, String[] parameterNames, Method method) {
        StringBuilder bf = new StringBuilder("<script> ").append("\n");
        Class paramterType = method.getParameterTypes()[0];
        String realTableName = SqlParseUtils.getAnnotationValueByTypeName(paramterType, CustomerMapperBuilder.TABLENAME);
        if (StringUtils.isNotEmpty(realTableName)) {
            tableName = realTableName;
        }
        Field fields[] = null;
        if (isAssignableFromCollection(paramterType)) {                    //如果是 list集合
            //泛型的参数类型(如果只有一个参数，那么就取第一个)
            Type[] types = method.getGenericParameterTypes();
            ParameterizedType pType = (ParameterizedType) types[0];
            Type type = pType.getActualTypeArguments()[0];
            try {
                Class clazz = Class.forName(type.getTypeName());
                fields = clazz.getDeclaredFields();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (paramterType.isArray()) {
            Class clazz = paramterType.getComponentType();
            fields = clazz.getDeclaredFields();
        } else {
            fields = paramterType.getDeclaredFields();
        }

        fields = sortFields(fields);
        if (isAssignableFromCollection(paramterType) || paramterType.isArray()) {                    //如果是 list集合,或数组
            String collectionValue = paramterType.isArray() ? "array" : "list";
            bf.append(TAB).append(TAB).append("insert into ").append(tableName).append("(").append("\n");
            bf.append(TAB).append(TAB).append(TAB).append("<trim suffixOverrides=\",\">").append("\n");
            for (Field field : fields) {
                String realFieldName = getRealFieldName(field);
                String column = StringUtils.getDataBaseColumn(realFieldName);
                if ("id".equals(column) || "is_delete".equals(column) || "gmt_create".equals(column) || "gmt_modified".equals(column)) {
                    continue;
                }
                bf.append(TAB).append(TAB).append(TAB).append(TAB);
                bf.append(column).append(", ").append("\n");
            }
            if (tableColumns.contains("is_delete")) {
                bf.append(TAB).append(TAB).append(TAB).append(TAB).append("is_delete,").append("\n");
            }
            if (tableColumns.contains("gmt_create")) {
                bf.append(TAB).append(TAB).append(TAB).append(TAB).append("gmt_create,").append("\n");
            }
            if (tableColumns.contains("gmt_modified")) {
                bf.append(TAB).append(TAB).append(TAB).append(TAB).append("gmt_modified,").append("\n");
            }
            bf.append(TAB).append(TAB).append(TAB).append("</trim>\n");
            bf.append(TAB).append(TAB).append(")values").append("\n");
            bf.append(TAB).append(TAB).append("<foreach collection=\"" + collectionValue + "\" item=\"item\" index=\"i\"  separator=\",\">").append("\n");
            bf.append(TAB).append(TAB).append(TAB).append("(").append("\n");
            bf.append(TAB).append(TAB).append(TAB).append("<trim suffixOverrides=\",\">").append("\n");
            for (Field field : fields) {
                String realFieldName = getRealFieldName(field);
                String column = StringUtils.getDataBaseColumn(realFieldName);
                if ("id".equals(column) || "is_delete".equals(column) || "gmt_create".equals(column) || "gmt_modified".equals(column)) {
                    continue;
                }
                bf.append(TAB).append(TAB).append(TAB).append(TAB);
                bf.append("#{").append("item.").append(realFieldName).append("},").append("\n");
            }
            if (tableColumns.contains("is_delete")) {
                bf.append(TAB).append(TAB).append(TAB).append(TAB).append("0,").append("\n");
            }
            if (tableColumns.contains("gmt_create")) {
                bf.append(TAB).append(TAB).append(TAB).append(TAB).append("now(),").append("\n");
            }
            if (tableColumns.contains("gmt_modified")) {
                bf.append(TAB).append(TAB).append(TAB).append(TAB).append("now(),").append("\n");
            }
            bf.append(TAB).append(TAB).append(TAB).append("</trim>\n");
            bf.append(TAB).append(TAB).append(TAB).append(")").append("\n");
            bf.append(TAB).append(TAB).append("</foreach>").append("\n");
            bf.append("</script>");
            return new PluginTuple(false, bf.toString());
        } else {
            String paramPre = "";
            bf.append(TAB).append(TAB).append("insert into ").append(tableName).append("(").append("\n");
            bf.append(TAB).append(TAB).append("<trim suffixOverrides=\",\">").append("\n");
            for (Field field : fields) {
                bf.append(TAB).append(TAB).append(TAB);
                String realFieldName = getRealFieldName(field);
                bf.append(getIfNotNullByType(field.getType(), paramPre + realFieldName));
                bf.append(StringUtils.getDataBaseColumn(realFieldName)).append(", </if>").append("\n");
            }
            bf.append(TAB).append(TAB).append("</trim>\n");
            bf.append(TAB).append(TAB).append(")values(").append("\n");
            bf.append(TAB).append(TAB).append("<trim suffixOverrides=\",\">").append("\n");
            for (Field field : fields) {
                String realFieldName = getRealFieldName(field);
                bf.append(TAB).append(TAB).append(TAB);
                bf.append(getIfNotNullByType(field.getType(), paramPre + realFieldName));
                bf.append("#{").append(paramPre + realFieldName).append("}, </if>").append("\n");
            }
            bf.append(TAB).append(TAB).append("</trim>\n");
            bf.append(TAB).append(TAB).append(")").append("\n");
            bf.append("</script>");
            return new PluginTuple(false, bf.toString(), paramPre);
        }
    }

    public static boolean isAssignableFromCollection(Class clazz) {
        if (Collection.class.isAssignableFrom(clazz)) {
            return true;
        }
        return false;
    }

    public static PluginTuple parseUpdate(String tableName, String[] parameterNames, Method method) {
        StringBuilder bf = new StringBuilder("<script> ").append("\n");
        Class paramterType = method.getParameterTypes()[0];
        String realTableName = SqlParseUtils.getAnnotationValueByTypeName(paramterType, CustomerMapperBuilder.TABLENAME);
        if (StringUtils.isNotEmpty(realTableName)) {
            tableName = realTableName;
        }
        Field fields[] = null;
        if (isAssignableFromCollection(paramterType)) {                    //如果是 list集合
            //泛型的参数类型(如果只有一个参数，那么就取第一个)
            Type[] types = method.getGenericParameterTypes();
            ParameterizedType pType = (ParameterizedType) types[0];
            Type type = pType.getActualTypeArguments()[0];
            try {
                Class clazz = Class.forName(type.getTypeName());
                fields = clazz.getDeclaredFields();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (paramterType.isArray()) {
            Class clazz = paramterType.getComponentType();
            fields = clazz.getDeclaredFields();
        } else {
            fields = paramterType.getDeclaredFields();
        }
        if (isAssignableFromCollection(paramterType) || paramterType.isArray()) {
            String collection = paramterType.isArray() ? "array" : "list";
            bf.append(TAB).append(TAB).append("update").append(" ").append(tableName).append("\n");
            bf.append(TAB).append(TAB).append("<trim prefix=\"set\" suffixOverrides=\",\">").append("\n");
            for (Field field : fields) {
                String realFieldName = getRealFieldName(field);
                String column = StringUtils.getDataBaseColumn(realFieldName);
                if ("id".equals(column)) {
                    continue;
                }
                bf.append("             <trim prefix=\"" + column + " = case id\" suffix=\"end,\">\n" +
                        "                <foreach collection=\"" + collection + "\" item=\"item\">\n" +
                        "                     <if test=\"item." + realFieldName + "!=null\">\n" +
                        "                        when #{item.id} then #{item." + realFieldName + "}\n" +
                        "                     </if>\n" +
                        "                </foreach>\n" +
                        "            </trim>\n");
            }
            bf.append("             </trim>");
            bf.append("         <where>\n" +
                    "               id in\n" +
                    "               <foreach collection=\"" + collection + "\" separator=\",\" item=\"item\" open=\"(\" close=\")\">\n" +
                    "                   #{item.id}\n" +
                    "               </foreach>\n" +
                    "           </where>");

            bf.append("\n");
            bf.append("</script>");
            return new PluginTuple(false, bf.toString());
        } else if (!isBasicDataTypes(paramterType)) { //如果不是基本数据类型,且对于只有一个对象的时候
            String pre = "";
            bf.append(TAB).append(TAB).append("update").append("\n");
            bf.append(TAB).append(TAB).append(TAB).append(tableName).append("\n");
            bf.append(TAB).append(TAB).append("<trim prefix=\"set\" suffixOverrides=\",\">").append("\n");
            boolean flag = false;
            Map<String, String> map = new LinkedHashMap<>();
            for (Field field : fields) {
                String realFieldName = getRealFieldName(field);
                if (GMT_MODIFIED.equals(realFieldName)) {
                    flag = true;
                    continue;
                }
                if (hasAnnotation(field, BY)) {
                    String[] by = getAnnotationValueByTypeName(field, BY);
                    String tableId = "";
                    if (by != null && by.length > 0) {
                        tableId = by[0];
                    }
                    map.put(StringUtils.isNotEmpty(tableId) ? tableId : StringUtils.getDataBaseColumn(realFieldName), pre + realFieldName);
                    continue;
                }
                bf.append(TAB).append(TAB).append(TAB);
                bf.append(getIfNotNullByType(field.getType(), pre + realFieldName));
                bf.append(StringUtils.getDataBaseColumn(realFieldName)).append(" = ");
                bf.append("#{").append(realFieldName).append("}, </if>").append("\n");
            }
            bf.append(TAB).append(TAB).append("</trim>").append("\n");
            if (flag) {
                bf.append(TAB).append(TAB).append(",gmt_modified = now()").append("\n");
            }
            bf.append(TAB).append(TAB).append(" where ");
            if (map.size() > 0) {
                int i = 0;
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    if (i > 0) {
                        bf.append(" AND ");
                    }
                    bf.append(entry.getKey()).append(" = #{").append(entry.getValue()).append("}");
                    i++;
                }
            } else {
                bf.append(" id = #{" + pre + "id} ");
            }
            bf.append("\n");
            bf.append("</script>");
            return new PluginTuple(false, bf.toString());
        } else {
            ParameterInfo[] parameterInfos = getMethodParameterInfoByAnnotation(method);
            bf.append(TAB).append(TAB).append("UPDATE ").append(tableName).append(" SET ");
            boolean hasBy = false;
            for (ParameterInfo paramInfo : parameterInfos) {
                if (paramInfo.isBy()) {
                    hasBy = true;
                }
            }
            int flag = 0;
            int i = 0;
            for (ParameterInfo paramInfo : parameterInfos) {
                if (!hasBy && flag >= parameterInfos.length - 1) {     //保留最后一个参数作为where条件
                    break;
                }
                if (!paramInfo.isBy()) {
                    if (flag > 0) {
                        bf.append(" , ");
                    }
                    String conditionName = getConditionName(paramInfo, parameterNames[i]);
                    bf.append(StringUtils.getDataBaseColumn(conditionName)).append(" = ").append("#{").append(conditionName).append("}");
                    flag++;
                }
                i++;
            }
            if (parameterInfos.length > flag) {         //表示有 where 条件
                bf.append(" where ");
                Class<?>[] parameterTypes = method.getParameterTypes();
                Class[] childParameterTypes = new Class[parameterInfos.length - flag];
                ParameterInfo childParameterInfos[] = new ParameterInfo[parameterInfos.length - flag];
                String[] childParameterNames = new String[parameterInfos.length - flag];
                int k = 0;
                int y = 0;
                if (hasBy) {
                    for (ParameterInfo paramInfo : parameterInfos) {
                        if (paramInfo.isBy()) {
                            childParameterNames[y] = parameterNames[k];
                            childParameterInfos[y] = paramInfo;
                            childParameterTypes[y] = parameterTypes[k];
                            y++;
                        }
                        k++;
                    }
                } else {                            //取最后一个元素来更新
                    childParameterNames[y] = parameterNames[parameterInfos.length - 1];
                    childParameterInfos[y] = parameterInfos[parameterInfos.length - 1];
                    childParameterTypes[y] = parameterTypes[parameterInfos.length - 1];
                }
                for (int x = 0; x < parameterInfos.length - flag; x++) {
                    bf.append(" ").append(getCondition("", childParameterTypes, childParameterInfos, childParameterNames, x));
                }
            }
            bf.append("\n");
            bf.append("</script>");
            return new PluginTuple(true, bf.toString());
        }
    }

    public static PluginTuple parseDelete(String tableName, List<String> tableColumns, String[] parameterNames, Method method) {
        StringBuilder bf = new StringBuilder("<script> ").append("\n");
        if (parameterNames != null && parameterNames.length > 0) {
            Class paramterType = method.getParameterTypes()[0];
            String realTableName = SqlParseUtils.getAnnotationValueByTypeName(paramterType, CustomerMapperBuilder.TABLENAME);
            if (StringUtils.isNotEmpty(realTableName)) {
                tableName = realTableName;
            }
        }
        //如果有Realy 注解，直接从数据库中删除数据
        if (!hasAnnotation(method, "Realy") && tableColumns.contains("is_delete")) {
            bf.append(TAB).append("UPDATE ").append(tableName).append(" SET IS_DELETE = 1 ");
        } else {
            bf.append(TAB).append("DELETE FROM ").append(tableName);
        }
        if (parameterNames != null && parameterNames.length > 0) {
            bf.append(TAB).append(" WHERE ");
            Class parameterTypes[] = method.getParameterTypes();
            ParameterInfo[] parameterInfos = getMethodParameterInfoByAnnotation(method);
            for (int i = 0; i < parameterTypes.length; i++) {//遍历所有的参数
                bf.append(" ").append(getCondition("", parameterTypes, parameterInfos, parameterNames, i));
            }
        }
        bf.append("\n");
        bf.append("</script>");
        return new PluginTuple(true, bf.toString());
    }


    public static PluginTuple parseCount(String tableName, List<String> tableColumns, String[] parameterNames, Method method) {
        StringBuilder bf = new StringBuilder("<script> ").append("\n");
        if (parameterNames != null && parameterNames.length > 0) {
            Class paramterType = method.getParameterTypes()[0];
            String realTableName = SqlParseUtils.getAnnotationValueByTypeName(paramterType, CustomerMapperBuilder.TABLENAME);
            if (StringUtils.isNotEmpty(realTableName)) {
                tableName = realTableName;
            }
        }
        bf.append(TAB).append("SELECT COUNT(*) FROM ").append(tableName);
        if (parameterNames != null && parameterNames.length > 0) {
            bf.append(TAB).append(" WHERE ");
            Class parameterTypes[] = method.getParameterTypes();
            ParameterInfo[] parameterInfos = getMethodParameterInfoByAnnotation(method);
            for (int i = 0; i < parameterTypes.length; i++) {//遍历所有的参数
                bf.append(" ").append(getCondition("", parameterTypes, parameterInfos, parameterNames, i));
            }
        }
        bf.append("\n");
        bf.append("</script>");
        return new PluginTuple(true, bf.toString());
    }


    public static String getCondition(String conditionNamePre, Class[] parameterTypes, ParameterInfo parameterInfos[], String[] parameterNames, int i) {
        StringBuilder condition = new StringBuilder();
        condition.append(getIfOrIfNullPre(parameterTypes, parameterInfos, parameterNames, i));
        if (parameterInfos[i].isPageSize() || parameterInfos[i].isCurrPage()
                || parameterInfos[i].isOrderBy()) { //如果是 pageSize 或 currPage, orderBy  注解修饰的变量，不做处理
            return "";
        }
        if (i != 0) {
            if (parameterInfos[i].isOr()) {
                condition.append(" OR ");
            } else {
                condition.append(" AND ");
            }
        }
        if (!isBasicDataTypes(parameterTypes[i]) && !Collection.class.isAssignableFrom(parameterTypes[i])) {//如果参数不是基本数据类型
            return notBasicDataTypeHandler(parameterTypes, parameterInfos, parameterNames, i);
        }
        String column = getColumName(parameterInfos[i], parameterNames[i]);
        String conditionName = conditionNamePre + getConditionName(parameterInfos[i], parameterNames[i]);           //设置变量前缀
        if (parameterInfos[i].isEmpty()) {
            condition.append("(").append(column).append(" IS NULL OR ").append(column).append(" = '' ").append(")");
        } else if (parameterInfos[i].isNotEmpty()) {
            condition.append("(").append(column).append(" IS NOT NULL OR ").append(column).append(" != '' ").append(")");
        } else if (parameterInfos[i].isNull()) {
            condition.append(column).append(" IS NULL ");
        } else if (parameterInfos[i].isNotNull()) {
            condition.append(column).append(" IS NOT NULL ");
        } else if (parameterInfos[i].isNe()) {
            condition.append(getEQNEGTLTGELE(parameterInfos, parameterTypes, column, conditionName, "!=", i));
        } else if (parameterInfos[i].isGt()) {
            condition.append(getEQNEGTLTGELE(parameterInfos, parameterTypes, column, conditionName, " > ", i));
        } else if (parameterInfos[i].isLt()) {
            condition.append(getEQNEGTLTGELE(parameterInfos, parameterTypes, column, conditionName, " <![CDATA[ < ]]> ", i));
        } else if (parameterInfos[i].isGe()) {
            condition.append(getEQNEGTLTGELE(parameterInfos, parameterTypes, column, conditionName, " >= ", i));
        } else if (parameterInfos[i].isLe()) {
            condition.append(getEQNEGTLTGELE(parameterInfos, parameterTypes, column, conditionName, " <![CDATA[ <= ]]> ", i));
        } else if (parameterInfos[i].isIn()) {
            String inParam = parameterInfos[i].getColumn();
            if (StringUtils.isEmpty(inParam)) {
                inParam = column;
            }
            condition.append(inParam).append(" IN ");
            if (isStringTypes(parameterTypes[i])) {
                condition.append("(${").append(conditionName).append("})");
            } else {
                condition.append("\n").append("<foreach collection=\"" + conditionName + "\" item=\"item\" index=\"index\" separator=\",\" open=\"(\" close=\")\">").append("\n");
                condition.append("  #{item}").append("\n");
                condition.append("</foreach>").append("\n");
            }
        } else if (parameterInfos[i].isNotIn()) {
            String notInParam = parameterInfos[i].getColumn();
            if (StringUtils.isEmpty(notInParam)) {
                notInParam = column;
            }
            condition.append(notInParam).append(" NOT IN ");
            if (isStringTypes(parameterTypes[i])) {
                condition.append("(${").append(conditionName).append("})");
            } else {
                condition.append("\n").append("<foreach collection=\"" + conditionName + "\" item=\"item\" index=\"index\" separator=\",\" open=\"(\" close=\")\">").append("\n");
                condition.append("  #{item}").append("\n");
                condition.append("</foreach>").append("\n");
            }
        } else if (parameterInfos[i].isLike()) {
            condition.append(column).append(" LIKE CONCAT('%',#{" + conditionName + "},'%') ");
        } else if (parameterInfos[i].isLLike()) {
            condition.append(column).append(" LIKE CONCAT('',#{" + conditionName + "},'%') ");
        } else if (parameterInfos[i].isRLike()) {
            condition.append(column).append(" LIKE CONCAT('%',#{" + conditionName + "},'') ");
        } else if (isAssignableFromCollection(parameterTypes[i])) {
            String inParam = parameterInfos[i].getColumn();
            if (StringUtils.isEmpty(inParam)) {
                inParam = column;
            }
            condition.append(inParam).append(" IN ");
            condition.append("\n").append("<foreach collection=\"" + conditionName + "\" item=\"item\" index=\"index\" separator=\",\" open=\"(\" close=\")\">").append("\n");
            condition.append("  #{item}").append("\n");
            condition.append("</foreach>").append("\n");
        } else {
            condition.append(getEQNEGTLTGELE(parameterInfos, parameterTypes, column, conditionName, "=", i));
        }
        return condition.toString().trim();
    }

    private static String notBasicDataTypeHandler(Class[] parameterTypes, ParameterInfo parameterInfos[], String[] parameterNames, int i) {// 如果是不个对象，获取对象的所对应的sql
        StringBuilder sql = new StringBuilder();
        Field fields[] = parameterTypes[i].getDeclaredFields();
        fields = sortFields(fields);
        Annotation annotations[] = parameterTypes[i].getAnnotations();
        boolean isAnd = true;
        for (Annotation annotation : annotations) {
            String annotationName = getAnnotationName(annotation);
            if ("OR".equals(annotationName)) {
                isAnd = false;
                break;
            }
        }
        sql.append(parameterInfos[i].isOr() ? " OR " : " AND ");
        sql.append(" ( ");
        Class[] childParameterTypes = new Class[fields.length];
        ParameterInfo childParameterInfos[] = new ParameterInfo[fields.length];
        String[] childParameterNames = new String[fields.length];
        for (int f = 0; f < fields.length; f++) {
            childParameterTypes[f] = fields[f].getType();
            ParameterInfo parameterInfo = getParameterInfo(fields[f]);
            if (!parameterInfo.isOr() && !parameterInfo.isAnd()) { //如果属性没有配置 OR 或 AND注解，则使用对象类型的的注解
                if (isAnd) {
                    parameterInfo.setAnd(true);
                } else {
                    parameterInfo.setOr(true);
                }
            }
            childParameterInfos[f] = parameterInfo;
            childParameterNames[f] = fields[f].getName();
        }
        for (int k = 0; k < fields.length; k++) {
            sql.append(" ").append(getCondition(getConditionName(parameterInfos[i], parameterNames[i]) + ".", childParameterTypes, childParameterInfos, childParameterNames, k));
        }
        sql.append(" ) ");
        return sql.toString();
    }


    public static ParameterInfo getParameterInfo(Class clazz) {
        ParameterInfo parameterInfo = new ParameterInfo();
        for (Annotation annotation : clazz.getAnnotations()) {
            fillParameterInfo(parameterInfo, annotation);
        }
        return parameterInfo;
    }


    public static String getRealFieldName(Field field) {
        String value = getAnnotationValueByTypeName(field, "Param");
        return StringUtils.isNotEmpty(value) ? value : field.getName();
    }


    public static ParameterInfo getParameterInfo(Field field) {
        ParameterInfo parameterInfo = new ParameterInfo();
        parameterInfo.setAnd(false);                //因为 isAnd默认为 true， 这里要还原设置
        for (Annotation annotation : field.getAnnotations()) {
            fillParameterInfo(parameterInfo, annotation);
        }
        return parameterInfo;
    }

    public static String getEQNEGTLTGELE(ParameterInfo[] parameterInfos, Class[] parameterTypes, String column, String conditionName, String flag, int i) {
        StringBuilder condition = new StringBuilder();
        String columnName = ifNullGetDefault(parameterInfos[i].getColumn(), column);
        if (isDateTypes(parameterTypes[i])) {
            condition.append(" DATE_FORMAT(" + columnName + ", '" + ifNullGetDefault(parameterInfos[i].getDateFormatParam(), "%Y-%m-%d %H:%i:%S") + "')  " + flag +
                    "  DATE_FORMAT(#{" + conditionName + "}, '" + ifNullGetDefault(parameterInfos[i].getDateFormatParam(), "%Y-%m-%d %H:%i:%S") + "')");
        } else {
            condition.append(columnName).append(" ").append(flag).append(" #{").append(conditionName).append("}");
        }
        return condition.toString();
    }

    public static String getIfOrIfNullPre(Class[] parameterTypes, ParameterInfo[] parameterInfos, String parameterNames[], int i) {
        Class parameterType = parameterTypes[i];
        String parameterName = parameterNames[i];
        StringBuilder sb = new StringBuilder();
        if (parameterInfos[i].isIF()) {
            List<String> values = parameterInfos[i].getIfParams();
            if (values != null && values.size() > 0) {
                sb.append(getIfPreByValues(parameterTypes, parameterNames, parameterInfos, values, i));
            } else {
                sb.append(getIfNotNullByType(parameterType, parameterName));
            }
        } else if (parameterInfos[i].isIfNull()) {
            List<String> values = parameterInfos[i].getIfParams();
            if (values != null && values.size() > 0) {
                sb.append(getIfNullPreByValues(parameterTypes, parameterNames, parameterInfos, values, i));
            } else {
                if (isStringTypes(parameterType)) {
                    sb.append("<if test=\"" + parameterName + " == null OR " + parameterName + " == '' \">");
                } else {
                    sb.append("<if test=\"" + parameterName + " == null\">");
                }
            }
        }
        return sb.toString();
    }

    public static String getIfNotNullByType(Class parameterType, String parameterName) {
        StringBuilder sb = new StringBuilder();
        if (isStringTypes(parameterType)) {
            sb.append("<if test=\"" + parameterName + " != null and " + parameterName + " != '' \">");
        } else {
            sb.append("<if test=\"" + parameterName + " != null\">");
        }
        return sb.toString();
    }

    public static String getIfPreByValues(Class[] parameterTypes, String parameterNames[], ParameterInfo[] parameterInfos, List<String> values, int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("<if test=\"");
        int k = 0;
        for (String value : values) {
            if (k > 0) {
                sb.append(" and ");
            }
            for (int j = 0; j < parameterNames.length; j++) {
                if (value.equals(parameterNames[j])) {
                    String conditionName = getConditionName(parameterInfos[j], parameterNames[j]);
                    if (isStringTypes(parameterTypes[j])) {
                        sb.append("(").append(conditionName + "!= null").append(conditionName + " != ''").append(")");
                    } else {
                        sb.append("(").append(conditionName + "!= null").append(")");
                    }
                }
            }
            k++;
        }
        sb.append("\">");
        return sb.toString();
    }


    public static String getIfNullPreByValues(Class[] parameterTypes, String parameterNames[], ParameterInfo[] parameterInfos, List<String> values, int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("<if test=\"");
        int k = 0;
        for (String value : values) {
            if (k > 0) {
                sb.append(" OR ");
            }
            for (int j = 0; j < parameterNames.length; j++) {
                if (value.equals(parameterNames[j])) {
                    String conditionName = getConditionName(parameterInfos[j], parameterNames[j]);
                    if (isStringTypes(parameterTypes[j])) {
                        sb.append("(").append(conditionName + "== null").append(conditionName + " == ''").append(")");
                    } else {
                        sb.append("(").append(conditionName + "== null").append(")");
                    }
                }
            }
            k++;
        }
        sb.append("\">");
        return sb.toString();
    }


    public static String getColumName(ParameterInfo parameterInfo, String parameterName) {
        StringBuilder condition = new StringBuilder();
        if (StringUtils.isNotEmpty(parameterInfo.getColumn())) {
            condition.append(parameterInfo.getColumn());
        } else {
            condition.append(StringUtils.getDataBaseColumn(parameterName));
        }
        return condition.toString();
    }


    public static String ifNullGetDefault(String priority, String defaultValue) {
        if (StringUtils.isNotEmpty(priority)) {
            return priority;
        }
        return defaultValue;
    }


    public static String getConditionName(ParameterInfo parameterInfo, String parameterName) {
        StringBuilder condition = new StringBuilder();
        if (StringUtils.isNotEmpty(parameterInfo.getParam())) {
            condition.append(parameterInfo.getParam());
        } else {
            condition.append(parameterName);
        }
        return condition.toString();
    }

    public static String getOrderBySql(Method method, ParameterInfo[] parameterInfos, String[] parameterNames) {
        StringBuilder sql = new StringBuilder();
        List<OrderByInfo> orderByInfos = getMethodOrderByListByMethod(method);
        boolean flag = true;
        if (parameterInfos != null && parameterInfos.length > 0) {
            for (int i = 0; i < parameterInfos.length; i++) {
                ParameterInfo parameterInfo = parameterInfos[i];
                if (parameterInfo.isOrderBy()) {
                    if (flag) {
                        flag = false;
                        sql.append(" ORDER BY ");
                    }
                    String[] bys = parameterInfo.getBys();
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < bys.length; j++) {
                        if (j > 0) {
                            sb.append(",");
                        }
                        sb.append(bys[j]);
                    }
                    sql.append(sb.toString()).append(" ").append("${").append(parameterNames[i]).append("}").append(",");
                }
            }
        }
        if (orderByInfos != null && orderByInfos.size() > 0) {
            if (flag) {
                sql.append(" ORDER BY ");
            }
            int k = 0;
            for (OrderByInfo orderByInfo : orderByInfos) {
                int j = 0;
                if (k > 0) {
                    sql.append(",");
                }
                for (String by : orderByInfo.getBy()) {
                    if (j > 0) {
                        sql.append(" , ");
                    }
                    sql.append(by);
                    j++;
                }
                if (OrderType.DESC.equals(orderByInfo.getOrderType())) {
                    sql.append(" DESC ");
                } else {
                    sql.append(" ASC ");
                }
                k++;
            }
        }
        String temp = sql.toString();
        if (temp.endsWith(",")) {
            temp = temp.substring(0, temp.length() - 1);
        }
        return temp;
    }

    public static String getLimit(Method method) {
        StringBuilder sql = new StringBuilder();
        LIMIT limit = method.getAnnotation(LIMIT.class);
        if (limit != null) {
            int index = limit.value();
            sql.append(" LIMIT ").append(index);
            if (limit.offset() > 0) {
                sql.append(",").append(limit.offset());
            }
        }
        return sql.toString();
    }

    public static ParameterInfo[] getMethodParameterInfoByAnnotation(Method method) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        if (parameterAnnotations == null || parameterAnnotations.length == 0) {
            return null;
        }
        ParameterInfo[] parameterInfos = new ParameterInfo[parameterAnnotations.length];
        int i = 0;
        for (Annotation[] parameterAnnotation : parameterAnnotations) {
            ParameterInfo parameterInfo = new ParameterInfo();
            for (Annotation annotation : parameterAnnotation) {
                fillParameterInfo(parameterInfo, annotation);
            }
            parameterInfos[i] = parameterInfo;
            i++;
        }
        return parameterInfos;
    }

    public static void fillParameterInfo(ParameterInfo parameterInfo, Annotation annotation) {
        String annotationName = getAnnotationName(annotation);
        Object obj = getAnnotationValue(annotation);
        String value = "";
        if (!(obj instanceof String)) {
            value = obj + "";
        } else {
            value = obj.toString();
        }
        if ("OR".equals(annotationName)) {              //默认为 AND 关系
            parameterInfo.setOr(true);
        } else if ("AND".equals(annotationName)) {
            parameterInfo.setAnd(true);
        } else if ("Param".equals(annotationName)) {
            parameterInfo.setParam(value);
        } else if ("Column".equals(annotationName)) {
            parameterInfo.setColumn(value);
        } else if ("EQ".equals(annotationName)) {
            parameterInfo.setEq(true);
            parameterInfo.setColumn(value);
        } else if ("NE".equals(annotationName)) {
            parameterInfo.setNe(true);
            parameterInfo.setColumn(value);
        } else if ("GT".equals(annotationName)) {
            parameterInfo.setGt(true);
            parameterInfo.setColumn(value);
        } else if ("LT".equals(annotationName)) {
            parameterInfo.setLt(true);
            parameterInfo.setColumn(value);
        } else if ("GE".equals(annotationName)) {
            parameterInfo.setGe(true);
            parameterInfo.setColumn(value);
        } else if ("LE".equals(annotationName)) {
            parameterInfo.setLe(true);
            parameterInfo.setColumn(value);
        } else if ("LIKE".equals(annotationName)) {
            parameterInfo.setLike(true);
            parameterInfo.setColumn(value);
        } else if ("LLIKE".equals(annotationName)) {
            parameterInfo.setLLike(true);
            parameterInfo.setColumn(value);
        } else if ("RLIKE".equals(annotationName)) {
            parameterInfo.setRLike(true);
            parameterInfo.setColumn(value);
        } else if ("IN".equals(annotationName)) {
            parameterInfo.setIn(true);
            parameterInfo.setColumn(value);
        } else if ("notIn".equals(annotationName)) {
            parameterInfo.setNotIn(true);
            parameterInfo.setColumn(value);
        } else if ("IsEmpty".equals(annotationName)) {
            parameterInfo.setEmpty(true);
            parameterInfo.setColumn(value);
        } else if ("IsNotEmpty".equals(annotationName)) {
            parameterInfo.setNotEmpty(true);
            parameterInfo.setColumn(value);
        } else if ("IsNull".equals(annotationName)) {
            parameterInfo.setNull(true);
            parameterInfo.setColumn(value);
        } else if ("IsNotNull".equals(annotationName)) {
            parameterInfo.setNotNull(true);
            parameterInfo.setColumn(value);
        } else if ("IF".equals(annotationName)) {
            parameterInfo.setIF(true);
            List<String> list = parameterInfo.getIfParams();
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(value);
            parameterInfo.setIfParams(list);
        } else if ("IFNull".equals(annotationName)) {
            parameterInfo.setIfNull(true);
            List<String> list = parameterInfo.getIfNullParams();
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(value);
            parameterInfo.setIfNullParams(list);
        } else if ("DateFormat".equals(annotationName)) {
            parameterInfo.setDateFormat(true);
            parameterInfo.setDateFormatParam(value);
        } else if ("By".equals(annotationName)) {
            parameterInfo.setBy(true);
        } else if ("CurrPage".equals(annotationName)) {
            parameterInfo.setCurrPage(true);
            parameterInfo.setCurrPage(value);
        } else if ("PageSize".equals(annotationName)) {
            parameterInfo.setPageSize(true);
            parameterInfo.setPageSize(value);
        } else if ("OrderBy".equals(annotationName)) {
            parameterInfo.setOrderBy(true);
            parameterInfo.setBys(getAnnotationValue(annotation));
        }
    }

    public static List<ByInfo> getMethodParameterByByMethod(Method method) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        if (parameterAnnotations == null || parameterAnnotations.length == 0) {
            return null;
        }
        List<ByInfo> by = new ArrayList<>();
        for (Annotation[] parameterAnnotation : parameterAnnotations) {
            for (Annotation annotation : parameterAnnotation) {
                String annotationName = getAnnotationName(annotation);
                String value = getAnnotationValue(annotation);
                if ("By".equals(annotationName)) {
                    ByInfo byInfo = new ByInfo(value, null);
                    by.add(byInfo);
                }
            }
        }
        return by;
    }

    public static List<OrderByInfo> getMethodOrderByListByMethod(Method method) {
        List<OrderByInfo> byList = new ArrayList<>();
        Order orderBy = method.getAnnotation(Order.class);
        if (orderBy != null) {
            By[] bys = getAnnotationValue(orderBy);
            if (bys != null && bys.length > 0) {
                for (By by : bys) {
                    String[] value = getAnnotationValue(by);
                    OrderType type = getAnnotationValueByMethodName(by, "type");
                    OrderByInfo info = new OrderByInfo(value, type);
                    byList.add(info);
                }
            }
        }
        return byList;
    }

    private static boolean isBasicDataTypes(Class clazz) {
        return primitiveTypes.contains(clazz) ? true : false;
    }

    private static boolean isStringTypes(Class clazz) {
        Set<Class> classSet = new HashSet<>();
        classSet.add(String.class);
        return classSet.contains(clazz);
    }


    private static boolean isDateTypes(Class clazz) {
        Set<Class> classSet = new HashSet<>();
        classSet.add(Date.class);
        classSet.add(java.sql.Date.class);
        return classSet.contains(clazz);
    }


    public static <T> T getAnnotationValue(Annotation annotation) {
        try {
            Method method = annotation.getClass().getMethod("value");
            if (method != null) {
                T paramName = (T) method.invoke(annotation);
                return paramName;
            }
        } catch (NoSuchMethodException e) {

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static <T> T getAnnotationValueByTypeName(Class type, String name) {
        Annotation[] annotations = type.getAnnotations();
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (name.equals(SqlParseUtils.getAnnotationName(annotation))) {
                    return SqlParseUtils.getAnnotationValue(annotation);
                }
            }
        }
        return null;
    }


    public static <T> T getAnnotationValueByTypeName(Field type, String name) {
        Annotation[] annotations = type.getAnnotations();
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (name.equals(SqlParseUtils.getAnnotationName(annotation))) {
                    return SqlParseUtils.getAnnotationValue(annotation);
                }
            }
        }
        return null;
    }

    public static boolean hasAnnotation(Field type, String name) {
        Annotation[] annotations = type.getAnnotations();
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (name.equals(SqlParseUtils.getAnnotationName(annotation))) {
                    return true;
                }
            }
        }
        return false;
    }


    public static boolean hasAnnotation(Method method, String name) {
        Annotation[] annotations = method.getAnnotations();
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (name.equals(SqlParseUtils.getAnnotationName(annotation))) {
                    return true;
                }
            }
        }
        return false;
    }


    public static boolean hasAnnotation(Class type, String name) {
        Annotation[] annotations = type.getAnnotations();
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (name.equals(SqlParseUtils.getAnnotationName(annotation))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static <T> T getAnnotationValueByMethodName(Annotation annotation, String methodName) {
        try {
            Method method = annotation.getClass().getMethod(methodName);
            if (method != null) {
                T paramName = (T) method.invoke(annotation);
                return paramName;
            }
        } catch (NoSuchMethodException e) {

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String getAnnotationName(Annotation annotation) {
        String annotionStr = annotation.toString();
        int a = annotionStr.indexOf("(", 0);
        if (a != -1) {
            annotionStr = annotionStr.substring(0, a);
            String strs[] = annotionStr.split("\\.");
            if (strs != null && strs.length > 0) {
                return strs[strs.length - 1];
            }
        }
        return annotionStr;
    }


    public static Field[] sortFields(Field[] fields) {
        // 用来存放所有的属性域
        List<Field> fieldList = new ArrayList<>();
        for (Field field : fields) {
            fieldList.add(field);
        }
        // 这个比较排序的语法依赖于java 1.8
        fieldList.sort(Comparator.comparingInt(
                f -> {
                    if (f.getAnnotation(Index.class) != null) {
                        return f.getAnnotation(Index.class).value();
                    }
                    return 999;
                }
        ));
        return fieldList.toArray(new Field[fieldList.size()]);
    }

    public static String captureName(String name) {
        char[] cs = name.toCharArray();
        cs[0] += 32;
        return String.valueOf(cs);
    }


    public static Method[] sortMethods(Method[] methods) {
        // 用来存放所有的属性域
        List<Method> methodList = new ArrayList<>();
        // 过滤带有注解的Field
        for (Method m : methods) {
            String mname = m.getName();
            if ((mname.startsWith("get") || mname.startsWith("Get")) && !"getClass".equals(m.getName())) {
                Class clas = m.getDeclaringClass();
                String a = mname.substring(3);
                a = captureName(a);
                try {
                    Field field = clas.getDeclaredField(a);
                    if (field.getAnnotation(Index.class) != null) {
                        methodList.add(m);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }

        }

        // 这个比较排序的语法依赖于java 1.8
        methodList.sort(Comparator.comparingInt(
                m -> {
                    String mname = m.getName();
                    Class clas = m.getDeclaringClass();
                    String a = mname.substring(3);
                    a = captureName(a);
                    try {
                        Field field = clas.getDeclaredField(a);
                        int sort = field.getAnnotation(Index.class).value();
                        return sort;
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                    return 9999;
                }
        ));
        return methodList.toArray(new Method[methodList.size()]);
    }


    public static Method getMethod(Class clazz, String methodName) {
        Method methods[] = clazz.getMethods();
        for (Method method : methods) {
            if (methodName.equals(method.getName())) {
                return method;
            }
        }
        return null;
    }

    public static String findTableName(Class<?> type) {
        String tableName = null;
        //获取接口定义上的泛型类型
        //一个类可能实现多个接口,每个接口上定义的泛型类型都可取到
        Type[] interfacesTypes = type.getGenericInterfaces();
        for (Type t : interfacesTypes) {
            Type[] genericType2 = ((ParameterizedType) t).getActualTypeArguments();
            for (Type t2 : genericType2) {
                try {
                    Class c = Class.forName(t2.getTypeName());
                    tableName = SqlParseUtils.getAnnotationValueByTypeName(c, CustomerMapperBuilder.TABLENAME);
                    if (StringUtils.isNotEmpty(tableName)) {
                        break;
                    }
                } catch (Exception e) {
                }
            }
        }
        return tableName;
    }


    public static Class findEntityType(Class<?> type) {
        //获取接口定义上的泛型类型
        //一个类可能实现多个接口,每个接口上定义的泛型类型都可取到
        Type[] interfacesTypes = type.getGenericInterfaces();
        for (Type t : interfacesTypes) {
            Type[] genericType2 = ((ParameterizedType) t).getActualTypeArguments();
            for (Type t2 : genericType2) {
                try {
                    return Class.forName(t2.getTypeName());
                } catch (Exception e) {
                }
            }
        }
        return null;
    }


    public static Map<Integer, String> getParamsName(Method method) {
        final Class<?>[] paramTypes = method.getParameterTypes();
        DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        final Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Map<Integer, String> names = new HashMap<>();
        for (Integer i = 0; i < paramTypes.length; i++) {
            if (isSpecialParameter(paramTypes[i])) {
                continue;
            }
            for (Annotation annotation : paramAnnotations[i]) {
                if (annotation instanceof Param) {
                    names.put(i, ((Param) annotation).value());
                    break;
                }
            }
            if (StringUtils.isEmpty(names.get(i))) {
                names.put(i, parameterNames[i]);
            }
        }
        return names;
    }


    private static boolean isSpecialParameter(Class<?> clazz) {
        return RowBounds.class.isAssignableFrom(clazz) || ResultHandler.class.isAssignableFrom(clazz);
    }


    public static <T> T setFieldValue(Object target, String name, Object value) {
        Field field = getField(target.getClass(), name);
        if (field != null) {
            try {
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static <T> T getFieldValue(Object target, String name) {
        Field field = getField(target.getClass(), name);
        if (field != null) {
            try {
                field.setAccessible(true);
                return (T) field.get(target);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Field getField(Class clazz, String name) {
        Field fields[] = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (name.equals(field.getName())) {
                return field;
            }
        }
        return null;
    }

    public static String removeScript(String sql) {
        sql = sql.trim();
        if (sql.startsWith("<script>")) {
            sql = sql.substring("<script>".length());
        }
        if (sql.endsWith("</script>")) {
            sql = sql.substring(0, sql.length() - "</script>".length());
        }
        return sql;
    }

    public static Class findReturnGenericType(Method method, int i) {
        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;//如果包含泛型
            Type[] types = parameterizedType.getActualTypeArguments();//获取真实类型
            try {
                Type type = types[i];
                return Class.forName(type.getTypeName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}
