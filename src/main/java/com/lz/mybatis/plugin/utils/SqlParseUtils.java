package com.lz.mybatis.plugin.utils;


import com.lz.mybatis.plugin.annotations.*;
import com.lz.mybatis.plugin.config.CustomerMapperBuilder;
import com.lz.mybatis.plugin.entity.*;
import com.lz.mybatis.plugin.utils.t.PluginTuple;
import com.lz.mybatis.plugin.utils.t.Tuple2;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;


public class SqlParseUtils {
    public final static String IS_DELETE = "is_delete";
    public final static String GMT_MODIFIED = "gmtModified";
    public final static String GMT_CREATE = "gmtCreate";

    public final static String TABLE_ID = "TableId";
    public final static String BY = "By";
    public final static String ID = "id";
    public final static ThreadLocal<SqlContext> THREAD_LOCAL = new ThreadLocal();

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
        primitiveTypes.add(BigDecimal.class);

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

    public static TableBaseInfo tableBaseInfo = new TableBaseInfo("id", "is_delete", "gmt_create", "gmt_modified");

    public static List<String> primaryC = Arrays.asList(new String[]{"id"});


    public static PluginTuple testSql(Class clazz, String methodName) {
        if (methodName.startsWith("select")) {
            return testSelect(clazz, methodName);
        } else if (methodName.startsWith("update")) {
            return testUpdate(clazz, methodName);
        } else if (methodName.startsWith("delete")) {
            return testDelete(clazz, methodName);
        } else if (methodName.startsWith("insert")) {
            return testInsert(clazz, methodName);
        } else if (methodName.startsWith("count")) {
            return testCount(clazz, methodName);
        }
        return null;
    }

    public static PluginTuple testSelect(Class clazz, String methodName) {
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;
        EntryInfo entryInfo = SqlParseUtils.findTableName(clazz);
        return parse(entryInfo, primaryC, tableBaseInfo, sqlCommandType, getMethod(clazz, methodName), null);
    }

    public static PluginTuple testInsert(Class clazz, String methodName) {
        SqlCommandType sqlCommandType = SqlCommandType.INSERT;
        EntryInfo entryInfo = SqlParseUtils.findTableName(clazz);
        return parse(entryInfo, primaryC, tableBaseInfo, sqlCommandType, getMethod(clazz, methodName), null);
    }

    public static PluginTuple testUpdate(Class clazz, String methodName) {
        SqlCommandType sqlCommandType = SqlCommandType.UPDATE;
        EntryInfo entryInfo = SqlParseUtils.findTableName(clazz);
        return parse(entryInfo, primaryC, tableBaseInfo, sqlCommandType, getMethod(clazz, methodName), null);
    }

    public static PluginTuple testDelete(Class clazz, String methodName) {
        SqlCommandType sqlCommandType = SqlCommandType.DELETE;
        EntryInfo entryInfo = SqlParseUtils.findTableName(clazz);
        return parse(entryInfo, primaryC, tableBaseInfo, sqlCommandType, getMethod(clazz, methodName), null);
    }

    public static PluginTuple testCount(Class clazz, String methodName) {
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;
        EntryInfo entryInfo = SqlParseUtils.findTableName(clazz);
        return parse(entryInfo, primaryC, tableBaseInfo, sqlCommandType, getMethod(clazz, methodName), null);
    }


    public static SqlContext getSqlContext() {
        SqlContext sqlContext = THREAD_LOCAL.get();
        if (sqlContext == null) {
            sqlContext = new SqlContext();
            THREAD_LOCAL.set(sqlContext);
        }
        return sqlContext;
    }

    public static PluginTuple parse(EntryInfo entryInfo, List<String> primaryColumns, TableBaseInfo tableInfo,
                                    SqlCommandType sqlCommandType, Method method, Class entityType) {
        StringBuilder sb = new StringBuilder();
        try {
            getSqlContext().setPrimaryEntryInfo(entryInfo);
            getSqlContext().asList.add(entryInfo.getAs());
            String tableAlas = getAlias(method);
            if (StringUtils.isNotBlank(tableAlas)) {
                entryInfo.setAs(tableAlas);
                getSqlContext().setPrimaryEntryInfo(entryInfo);
                getSqlContext().asList.add(entryInfo.getAs());
            }

            if (primaryColumns == null || primaryColumns.size() == 0) {
                primaryColumns = primaryC;
            }
            if (tableInfo == null) {
                tableInfo = tableBaseInfo;
            }
            DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
            String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
            String tableName = entryInfo.getTableName();
            if (SqlCommandType.SELECT.equals(sqlCommandType)) {
                if (method.getName().startsWith("count")) {
                    return parseCount(entryInfo, tableInfo, parameterNames, method);
                } else if (method.getReturnType().equals(Page.class)) {
                    return parseSelectPage(tableName, tableInfo, parameterNames, method, entityType);
                } else {
                    return parseSelect(false, entryInfo, tableInfo, parameterNames, method);
                }
            } else if (SqlCommandType.INSERT.equals(sqlCommandType)) {
                if (method.getName().startsWith("insertOrUpdate")) {
                    Tuple2<Boolean, String> tupleInsert = parseInsert(tableName, parameterNames, method, tableInfo).getData();
                    String insertSql = removeScript(tupleInsert.getSecond());
                    Tuple2<Boolean, String> tupleUpdate = parseUpdate(tableName, parameterNames, method, tableInfo).getData();
                    String updateSql = removeScript(tupleUpdate.getSecond());
                    StringBuilder sBuild = new StringBuilder();
                    sBuild.append("<script> ").append("\n");
                    sBuild.append("<choose>").append("\n");
                    sBuild.append("<when ");

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
                return parseInsert(tableName, parameterNames, method, tableInfo);
            } else if (SqlCommandType.UPDATE.equals(sqlCommandType)) {
                return parseUpdate(tableName, parameterNames, method, tableInfo);
            } else if (SqlCommandType.DELETE.equals(sqlCommandType)) {
                return parseDelete(tableName, tableInfo, parameterNames, method);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            THREAD_LOCAL.remove();
        }
        return new PluginTuple(true, sb.toString());
    }

    public static String getAs(String value) {
        int i = 0;
        String as = "t";
        while (getSqlContext().asList.contains(as)) {
            i++;
            as = "t" + i;
        }
        return as;
    }

    public static String getAsBak(String value) {
        StringBuilder sb = new StringBuilder();
        char values[] = value.toCharArray();
        sb.append(values[0]);
        boolean flag = false;
        for (int i = 1; i < value.length(); i++) {
            char c = values[i];
            if (flag) {
                flag = false;
                sb.append(c);
            }
            if (c == '_') {
                flag = true;
            }
        }
        String as = sb.toString();
        String old = as;
        int i = 0;
        while (getSqlContext().asList.contains(as)) {
            i++;
            as = old + "_" + i;
        }

        return as;
    }


    @Deprecated
    private static PluginTuple parseSelectPage(String tableName, TableBaseInfo tableInfo, String[] parameterNames, Method method, Class entityType) {
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
            if (tableInfo.getId().equals(column)) {
                resultMap.append("            <id column=\"" + tableInfo.getId() + "\" property=\"" + tableInfo.getId() + "\"/>\n");
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
        String sqlCondition = doGetSqlCondition("", "", parameterTypes, parameterInfos, parameterNames, tableInfo, method);
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

    public static PluginTuple parseSelect(boolean isCount, EntryInfo entryInfo, TableBaseInfo tableInfo, String[] parameterNames, Method method) {
        Class parameterTypes[] = method.getParameterTypes();
        ParameterInfo[] parameterInfos = getMethodParameterInfoByAnnotation(method);
        StringBuilder sql = new StringBuilder();
        // 如果没有配置别名，则使用默认别名
        String alias ="";
        String tableName = entryInfo.getTableName();
        String wheir = "";
        if (methodHasAnnotation(method, Froms.class)) {
            alias = entryInfo.getAs();
            alias = StringUtils.isEmpty(alias) ? "t" : alias;       //默认当前表的别名为 t
            sql.append(" FROM ").append(tableName).append(" ").append(alias);
            Tuple2<String, String> tuple2 = getFromsBySql(method, parameterInfos, parameterNames, tableInfo);
            sql.append(tuple2.getFirst());
            wheir += getWhere(method) + " " + tuple2.getSecond();
            getSqlContext().isMultiTable = true;
        } else if (methodHasAnnotation(method, LeftJoinOns.class)) {
            alias = entryInfo.getAs();
            alias = StringUtils.isEmpty(alias) ? "t" : alias;
            sql.append(" FROM ").append(tableName).append(" ").append(alias);
            Tuple2<String, String> tuple2 = getLeftJoinOnsBySql(method, parameterInfos, parameterNames, tableInfo);
            sql.append(tuple2.getFirst());
            wheir += getWhere(method) + " " + tuple2.getSecond();
            getSqlContext().isMultiTable = true;
        } else {
            sql.append(" FROM ").append(tableName);
        }
        sql.append(doGetSqlCondition(wheir, alias, parameterTypes, parameterInfos, parameterNames, tableInfo, method));
        sql.append(getGroupBy(method));
        sql.append(getHaving(method));
        sql.append(getOrderBySql(method, parameterInfos, parameterNames));
        sql.append(getLimit(method));
        sql.append(" \n</script>");
        sql.insert(0, getMapper(isCount, method));
        return new PluginTuple(true, sql.toString().trim());
    }

    public static String getMapper(boolean isCount, Method method) {
        StringBuilder sql = new StringBuilder();
        sql.append("<script> \n");
        sql.append(TAB).append("SELECT");
        String avg = getAvg(method);
        String max = getMax(method);
        String min = getMin(method);
        String sum = getSum(method);
        String countDistinct = getCountDistince(method);
        String mapping = getMapping(method);
        if (isCount) {
            sql.append(" IFNULL(COUNT(*),0) ");
        } else if (methodHasAnnotation(method, Count.class)) {
            sql.append(" IFNULL(COUNT(").append(getCount(method)).append("),0) ");
        } else if (StringUtils.isNotEmpty(countDistinct)) {
            sql.append(" IFNULL(COUNT(DISTINCT").append("(").append(countDistinct).append(")),0) ");
        } else if (StringUtils.isNotEmpty(avg)) {
            sql.append(" IFNULL(AVG").append("(").append(avg).append("),0) ");
        } else if (StringUtils.isNotEmpty(max)) {
            sql.append(" IFNULL(MAX").append("(").append(max).append("),0) ");
        } else if (StringUtils.isNotEmpty(min)) {
            sql.append(" IFNULL(MIN").append("(").append(min).append("),0) ");
        } else if (StringUtils.isNotEmpty(sum)) {
            sql.append(" IFNULL(SUM").append("(").append(sum).append("),0) ");
        } else if (StringUtils.isNotEmpty(mapping)) {
            sql.append(" ").append(mapping).append(" ");
        } else {
            sql.append(" * ");
        }
        return sql.toString();
    }

    public static String doGetSqlCondition(String wheir, String alias, Class parameterTypes[], ParameterInfo[] parameterInfos, String[] parameterNames, TableBaseInfo tableInfo, Method method) {
        StringBuilder sql = new StringBuilder();
        if (StringUtils.isNotEmpty(alias)) {
            alias = alias + ".";
        }
        if (parameterTypes != null && parameterTypes.length > 0) {
            sql.append(" WHERE ");
            if (StringUtils.isNotEmpty(tableInfo.getIsDelete())) {
                sql.append(" " + alias + tableInfo.getIsDelete() + " = 0 ");
                appendWhere(wheir, sql);
            }
            for (int i = 0; i < parameterTypes.length; i++) {//遍历所有的参数
                sql.append("\n").append(" ").append(getCondition(sql, alias, "", parameterTypes, parameterInfos, parameterNames, i, method));
            }
        } else {
            if (StringUtils.isNotEmpty(tableInfo.getIsDelete())) {
                sql.append(" WHERE " + alias + tableInfo.getIsDelete() + " = 0 ");
                appendWhere(wheir, sql);
            }
        }
        return sql.toString();
    }


    public static void appendWhere(String wheir, StringBuilder sql) {
        if (StringUtils.isNotEmpty(wheir)) {
            if (wheir.trim().toLowerCase().startsWith("and") || wheir.trim().toLowerCase().startsWith("or")) {
                sql.append(wheir);
            } else {
                sql.append("\n AND ").append(wheir);
            }
        }
    }

    public static PluginTuple parseInsert(String tableName, String[] parameterNames, Method method, TableBaseInfo tableInfo) {
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
                if ("serialVersionUID".equals(realFieldName)) {
                    continue;
                }
                String column = StringUtils.getDataBaseColumn(realFieldName);
                if (column.equals(tableInfo.getId()) || column.equals(tableInfo.getIsDelete())
                        || column.equals(tableInfo.getGmtCreate()) || column.equals(tableInfo.getGmtModified())) {
                    continue;
                }

                bf.append(TAB).append(TAB).append(TAB).append(TAB);
                bf.append(column).append(", ").append("\n");
            }


            if (StringUtils.isNotEmpty(tableInfo.getIsDelete())) {
                bf.append(TAB).append(TAB).append(TAB).append(TAB).append(tableInfo.getIsDelete() + ",").append("\n");
            }
            if (StringUtils.isNotEmpty(tableInfo.getGmtCreate())) {
                bf.append(TAB).append(TAB).append(TAB).append(TAB).append(tableInfo.getGmtCreate() + ",").append("\n");
            }
            if (StringUtils.isNotEmpty(tableInfo.getGmtModified())) {
                bf.append(TAB).append(TAB).append(TAB).append(TAB).append(tableInfo.getGmtModified() + ",").append("\n");
            }

            bf.append(TAB).append(TAB).append(TAB).append("</trim>\n");
            bf.append(TAB).append(TAB).append(")values").append("\n");
            bf.append(TAB).append(TAB).append("<foreach collection=\"" + collectionValue + "\" item=\"item\" index=\"i\"  separator=\",\">").append("\n");
            bf.append(TAB).append(TAB).append(TAB).append("(").append("\n");
            bf.append(TAB).append(TAB).append(TAB).append("<trim suffixOverrides=\",\">").append("\n");
            for (Field field : fields) {


                String realFieldName = getRealFieldName(field);
                String column = StringUtils.getDataBaseColumn(realFieldName);
                if (column.equals(tableInfo.getId()) || column.equals(tableInfo.getIsDelete())
                        || column.equals(tableInfo.getGmtCreate()) || column.equals(tableInfo.getGmtModified())) {
                    continue;
                }
                bf.append(TAB).append(TAB).append(TAB).append(TAB);
                bf.append("#{").append("item.").append(realFieldName).append("},").append("\n");
            }

            if (StringUtils.isNotEmpty(tableInfo.getIsDelete())) {
                bf.append(TAB).append(TAB).append(TAB).append(TAB).append("0,").append("\n");
            }
            if (StringUtils.isNotEmpty(tableInfo.getGmtCreate())) {
                bf.append(TAB).append(TAB).append(TAB).append(TAB).append("now(),").append("\n");
            }
            if (StringUtils.isNotEmpty(tableInfo.getGmtModified())) {
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
                if ("serialVersionUID".equals(realFieldName)) {
                    continue;
                }
                bf.append(getIfNotNullByType(field.getType(), paramPre + realFieldName));
                bf.append(StringUtils.getDataBaseColumn(realFieldName)).append(", </if>").append("\n");
            }
            bf.append(TAB).append(TAB).append("</trim>\n");
            bf.append(TAB).append(TAB).append(")values(").append("\n");
            bf.append(TAB).append(TAB).append("<trim suffixOverrides=\",\">").append("\n");
            for (Field field : fields) {
                String realFieldName = getRealFieldName(field);
                bf.append(TAB).append(TAB).append(TAB);
                if ("serialVersionUID".equals(realFieldName)) {
                    continue;
                }
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

    public static PluginTuple parseUpdate(String tableName, String[] parameterNames, Method method, TableBaseInfo tableInfo) {
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

        if (isAssignableFromCollection(paramterType) || paramterType.isArray()) {
            String collection = paramterType.isArray() ? "array" : "list";
            bf.append(TAB).append(TAB).append("update").append(" ").append(tableName).append("\n");
            bf.append(TAB).append(TAB).append("<trim prefix=\"set\" suffixOverrides=\",\">").append("\n");
            for (Field field : fields) {
                String realFieldName = getRealFieldName(field);
                if ("serialVersionUID".equals(realFieldName)) {
                    continue;
                }
                String column = StringUtils.getDataBaseColumn(realFieldName);
                if (tableInfo.getId().equals(column)) {
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
                if ("serialVersionUID".equals(realFieldName)) {
                    continue;
                }
                if (tableInfo.getJavaCodeGmtModified().equals(realFieldName)) {
                    flag = true;
                    continue;
                }
                if (ID.equals(realFieldName)) {
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
                //updateCoverXXX方法处理
                if (method.getName().startsWith("updateCover")) {
                    bf.append(StringUtils.getDataBaseColumn(realFieldName)).append(" = ").append("#{").append(realFieldName).append("}, ").append("\n");
                } else {
                    bf.append(getIfNotNullByType(field.getType(), pre + realFieldName));
                    bf.append(StringUtils.getDataBaseColumn(realFieldName)).append(" = ");
                    bf.append("#{").append(realFieldName).append("}, </if>").append("\n");
                }
            }
            bf.append(TAB).append(TAB).append("</trim>").append("\n");
            if (flag) {
                bf.append(TAB).append(TAB).append("," + tableInfo.getGmtModified() + " = now()").append("\n");
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
                bf.append(" " + tableInfo.getId() + " = #{" + pre + "" + tableInfo.getId() + "} ");
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
                    String datasourceName = StringUtils.getDataBaseColumn(conditionName);
                    if (StringUtils.isNotEmpty(paramInfo.getColumn())) {
                        datasourceName = StringUtils.getDataBaseColumn(paramInfo.getColumn());
                    }
                    if (paramInfo.isPlus()) {
                        bf.append(datasourceName).append(" = ").append(datasourceName).append(" + ").append("#{").append(conditionName).append("}");
                    } else if (paramInfo.isSub()) {
                        bf.append(datasourceName).append(" = ").append(datasourceName).append(" - ").append("#{").append(conditionName).append("}");
                    } else if (paramInfo.isMul()) {
                        bf.append(datasourceName).append(" = ").append(datasourceName).append(" * ").append("#{").append(conditionName).append("}");
                    } else if (paramInfo.isDiv()) {
                        bf.append(datasourceName).append(" = ").append(datasourceName).append(" / ").append("#{").append(conditionName).append("}");
                    } else {
                        bf.append(StringUtils.getDataBaseColumn(conditionName)).append(" = ").append("#{").append(conditionName).append("}");
                    }
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
                    bf.append(" ").append(getCondition(bf, "", "", childParameterTypes, childParameterInfos, childParameterNames, x, method));
                }
            }
            bf.append("\n");
            bf.append("</script>");
            return new PluginTuple(true, bf.toString());
        }
    }

    public static PluginTuple parseDelete(String tableName, TableBaseInfo tableInfo, String[] parameterNames, Method method) {
        StringBuilder bf = new StringBuilder("<script> ").append("\n");
        if (parameterNames != null && parameterNames.length > 0) {
            Class paramterType = method.getParameterTypes()[0];
            String realTableName = SqlParseUtils.getAnnotationValueByTypeName(paramterType, CustomerMapperBuilder.TABLENAME);
            if (StringUtils.isNotEmpty(realTableName)) {
                tableName = realTableName;
            }
        }


        //如果有Realy 注解，直接从数据库中删除数据
        if (!hasAnnotation(method, "Realy") && StringUtils.isNotEmpty(tableInfo.getIsDelete())) {
            bf.append(TAB).append("UPDATE ").append(tableName).append(" SET " + tableInfo.getIsDelete() + " = 1 ");
        } else {
            bf.append(TAB).append("DELETE FROM ").append(tableName);
        }


        if (parameterNames != null && parameterNames.length > 0) {
            bf.append(TAB).append(" WHERE ");
            Class parameterTypes[] = method.getParameterTypes();
            ParameterInfo[] parameterInfos = getMethodParameterInfoByAnnotation(method);
            for (int i = 0; i < parameterTypes.length; i++) {//遍历所有的参数
                bf.append(" ").append(getCondition(bf, "", "", parameterTypes, parameterInfos, parameterNames, i, method));
            }
        }
        bf.append("\n");
        bf.append("</script>");
        return new PluginTuple(true, bf.toString());
    }


    public static PluginTuple parseCount(EntryInfo tableName, TableBaseInfo tableInfo, String[] parameterNames, Method method) {
        return parseSelect(true, tableName, tableInfo, parameterNames, method);
    }

    public static String getCondition(StringBuilder sb, String columPre, String conditionNamePre,
                                      Class[] parameterTypes, ParameterInfo parameterInfos[], String[] parameterNames, int i, Method method) {
        String simpleName = parameterTypes[i].getSimpleName(); //如果是分页对象，不当作条件
        if ("Page".equals(simpleName) || "IPage".equals(simpleName)) {
            return "";
        }
        if (parameterInfos[i].isExclude()) {
            return "";
        }

        if (parameterInfos[i].isPageSize() || parameterInfos[i].isCurrPage()
                || parameterInfos[i].isOrderBy()) { //如果是 pageSize 或 currPage, orderBy  注解修饰的变量，不做处理
            return "";
        }

        StringBuilder condition = new StringBuilder();
        Tuple2<Boolean, String> ifResult = getIfOrIfNullPre(conditionNamePre, parameterTypes, parameterInfos, parameterNames, i);
        boolean isLBracket = true;
        if (ifResult.getFirst()) {
            if (parameterInfos[i].isLBracket()) {
                condition.append(" ( \n ").append(ifResult.getSecond());                //加括号
                isLBracket = false;
            } else {
                condition.append(" \n ").append(ifResult.getSecond());
            }
        }

        String preSql = sb.toString().trim().toLowerCase();
        if (!preSql.endsWith("where") && !preSql.endsWith("and") && !preSql.endsWith("or") && !preSql.endsWith("(")) {
            if (parameterInfos[i].isLBracket() && isLBracket) {                             //如果是左括号
                if (parameterInfos[i].isOr()) {
                    condition.append(" OR ( ");
                } else {
                    condition.append(" AND ( ");
                }
            } else {
                if (parameterInfos[i].isOr()) {
                    condition.append(" OR ");
                } else {
                    condition.append(" AND ");
                }
            }
        }

        if (!isBasicDataTypes(parameterTypes[i]) && !Collection.class.isAssignableFrom(parameterTypes[i])) {//如果参数不是基本数据类型
            return notBasicDataTypeHandler(condition, parameterTypes, parameterInfos, parameterNames, i, method);
        }

        String column = getColumName(parameterInfos[i].isAlias(), parameterInfos[i], parameterNames[i], method);
        //如果字段有别名
        if (parameterInfos[i].isAlias()) {
            column = parameterInfos[i].getAliasValue()[0] + "." + column;
        } else {
            if (column.indexOf(".") == -1) {
                column = columPre + column;
            }
        }
        String conditionName = conditionNamePre + getConditionName(parameterInfos[i], parameterNames[i]);           //设置变量前缀
        if (parameterInfos[i].isEmpty()) {
            condition.append("(").append(column).append(" IS NULL OR ").append(column).append(" = '' ").append(")");
        } else if (parameterInfos[i].isNotEmpty()) {
            condition.append("(").append(column).append(" IS NOT NULL AND ").append(column).append(" != '' ").append(")");
        } else if (parameterInfos[i].isNull()) {
            condition.append(column).append(" IS NULL ");
        } else if (parameterInfos[i].isNotNull()) {
            condition.append(column).append(" IS NOT NULL ");
        } else if (parameterInfos[i].isNe()) {
            condition.append(getEQNEGTLTGELE(parameterInfos, parameterTypes, column, conditionName, "!=", i));
        } else if (parameterInfos[i].isGt()) {
            condition.append(getEQNEGTLTGELE(parameterInfos, parameterTypes, column, conditionName, " <![CDATA[ > ]]>", i));
        } else if (parameterInfos[i].isLt()) {
            condition.append(getEQNEGTLTGELE(parameterInfos, parameterTypes, column, conditionName, " <![CDATA[ < ]]> ", i));
        } else if (parameterInfos[i].isGe()) {
            condition.append(getEQNEGTLTGELE(parameterInfos, parameterTypes, column, conditionName, " <![CDATA[ >=]]>", i));
        } else if (parameterInfos[i].isLe()) {
            condition.append(getEQNEGTLTGELE(parameterInfos, parameterTypes, column, conditionName, " <![CDATA[ <= ]]> ", i));
        } else if (parameterInfos[i].isIn()) {
            String inParam = parameterInfos[i].getParam();
            if (StringUtils.isEmpty(inParam)) {
                inParam = column;
            }
            if (isStringTypes(parameterTypes[i])) {
                condition.append(StringUtils.getDataBaseColumn(inParam)).append(" IN ");
                condition.append("(${").append(conditionName).append("})");
            } else if (isAssignableFromCollection(parameterTypes[i]) && !isBasicDataTypes(getActualType(method, i))) {
                condition.append("<choose>\n" +
                        "            <when test=\"" + conditionName + "!=null and " + conditionName + ".size()> 0\">\n");
                condition.append(StringUtils.getDataBaseColumn(inParam)).append(" IN ");
                condition.append("\n").append("<foreach collection=\"" + conditionName + "\" item=\"item\" index=\"index\" separator=\",\" open=\"(\" close=\")\">").append("\n");
                condition.append("  #{item." + parameterInfos[i].getRowValue() + "}").append("\n");
                condition.append("</foreach>").append("\n");
                condition.append("            </when>\n" +
                        "            <otherwise>\n");
                condition.append(" 1 = 0 \n ");
                condition.append("            </otherwise>\n" +
                        "        </choose>\n");
            } else {
                condition.append(StringUtils.getDataBaseColumn(inParam)).append(" IN ");
                condition.append("\n").append("<foreach collection=\"" + conditionName + "\" item=\"item\" index=\"index\" separator=\",\" open=\"(\" close=\")\">").append("\n");
                condition.append("  #{item}").append("\n");
                condition.append("</foreach>").append("\n");
            }
        } else if (parameterInfos[i].isNotIn()) {
            String notInParam = parameterInfos[i].getColumn();
            if (StringUtils.isEmpty(notInParam)) {
                notInParam = column;
            }
            condition.append(StringUtils.getDataBaseColumn(notInParam)).append(" NOT IN ");
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
        if (ifResult.getFirst()) {
            condition.append("\n </if>").append("\n");
        }
        if (parameterInfos[i].isRBracket()) {
            condition.append(" ) ");
        }
        return condition.toString().trim();
    }

    private static String notBasicDataTypeHandler(StringBuilder oldSql, Class[] parameterTypes, ParameterInfo parameterInfos[], String[] parameterNames, int i, Method method) {// 如果是不个对象，获取对象的所对应的sql
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
            sql.append(" ").append(getCondition(sql, "", getConditionName(parameterInfos[i], parameterNames[i]) + ".", childParameterTypes, childParameterInfos, childParameterNames, k, method));
        }
        return sql.toString();
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
        String columnName = ifNullGetDefault(getEntryColum(parameterInfos[i].getColumn()), column);
        if (isDateTypes(parameterTypes[i]) || parameterInfos[i].isDateFormat()) {
            if (parameterInfos[i].isDateFormat()) {
                String dateformate = ifNullGetDefault(parameterInfos[i].getDateFormatParam(), "%Y-%m-%d %H:%i:%S");
                condition.append("DATE_FORMAT(" + columnName + ", '" + dateformate + "')  "
                        + flag +
                        "  DATE_FORMAT(#{" + conditionName + "}, '" + dateformate + "')");
            } else {
                condition.append(" " + columnName + " " + flag + " #{" + conditionName + "} ");
            }
        } else {
            condition.append(columnName).append(" ").append(flag).append(" #{").append(conditionName).append("}");
        }
        return condition.toString();
    }


    public static Tuple2<Boolean, String> getIfOrIfNullPre(String conditionNamePre, Class[] parameterTypes, ParameterInfo[] parameterInfos, String parameterNames[], int i) {
        Class parameterType = parameterTypes[i];
        String parameterName = parameterNames[i];
        if (StringUtils.isNotEmpty(conditionNamePre)) {
            parameterName = conditionNamePre + parameterName;
        }
        StringBuilder sb = new StringBuilder();
        boolean flag = false;
        if (parameterInfos[i].isIF()) {
            flag = true;
            List<String> values = parameterInfos[i].getIfParams();
            if (values != null && values.size() > 0) {
                sb.append(getIfPreByValues(parameterTypes, parameterNames, parameterInfos, values, i));
            } else {
                sb.append(getIfNotNullByType(parameterType, parameterName));
            }
        } else if (parameterInfos[i].isIfNull()) {
            flag = true;
            List<String> values = parameterInfos[i].getIfNullParams();
            if (values != null && values.size() > 0) {
                sb.append(getIfNullPreByValues(parameterTypes, parameterNames, parameterInfos, values, i));
            } else {
                if (isStringTypes(parameterType)) {
                    sb.append(" \n <if test=\"" + parameterName + " == null OR " + parameterName + " == '' \">");
                } else {
                    sb.append(" \n <if test=\"" + parameterName + " == null\">");
                }
                sb.append(" \n ").append(TAB);
            }
        }
        return new Tuple2<>(flag, sb.toString());
    }

    public static String getIfNotNullByType(Class parameterType, String parameterName) {
        StringBuilder sb = new StringBuilder();
        if (isStringTypes(parameterType)) {
            sb.append(" \n ").append(" <if test=\"" + parameterName + " != null and " + parameterName + " != '' \">");
        } else {
            sb.append(" \n ").append("  <if test=\"" + parameterName + " != null\">");
        }
        sb.append(" \n ").append(TAB);
        return sb.toString();
    }

    public static String getIfPreByValues(Class[] parameterTypes, String parameterNames[], ParameterInfo[] parameterInfos, List<String> values, int i) {
        StringBuilder sb = new StringBuilder();
        sb.append(" \n <if test=\"");
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
        sb.append(" \n ").append(TAB);
        ;
        return sb.toString();
    }

    public static String getIfNullPreByValues(Class[] parameterTypes, String parameterNames[], ParameterInfo[] parameterInfos, List<String> values, int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n<if test=\"");
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
        sb.append(" \n ").append(TAB);
        ;
        return sb.toString();
    }

    public static String getColumName(boolean hasAlias, ParameterInfo parameterInfo, String parameterName, Method method) {
        StringBuilder condition = new StringBuilder();
        if (StringUtils.isNotEmpty(parameterInfo.getColumn())) {
            String colum = parameterInfo.getColumn();
            if (colum.contains(":")) {
                String colums[] = colum.split(":");
                colum = colums[0];
                String as = null;
                try {
                    Class clazz = Class.forName(colum);
                    if (clazz == getSqlContext().primaryEntryInfo.getClazz()) {
                        as = getSqlContext().primaryEntryInfo.getAs();
                    } else {
                        for (EntryInfo entryInfo : getSqlContext().getOtherEntryInfo()) {
                            if (clazz == entryInfo.getClazz()) {
                                as = entryInfo.getAs();
                            }
                        }
                    }
                    // 获取别名+ 数据库列名
                    condition.append(as + "." + colums[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                condition.append(parameterInfo.getColumn());
            }
        } else {
            //如果已经有别名注解
            if (hasAlias) {
                condition.append(StringUtils.getDataBaseColumn(parameterName));
            } else {
                boolean isSelect = getSqlContext().isMultiTable;
                if (isSelect) {
                    condition.append(getSqlContext().primaryEntryInfo.getAs() + "." + StringUtils.getDataBaseColumn(parameterName));
                } else {
                    condition.append(StringUtils.getDataBaseColumn(parameterName));
                }
            }
        }
        return condition.toString();
    }


    public static boolean methodHasAnnotation(Method method, Class... annos) {
        for (Class ann : annos) {
            Annotation annotation = method.getAnnotation(ann);
            if (annotation != null) {
                return true;
            }
        }
        return false;
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

    public static Tuple2<String, String> getLeftJoinOnsBySql(Method method, ParameterInfo[] parameterInfos, String[] parameterNames, TableBaseInfo tableInfo) {
        StringBuilder sql = new StringBuilder();
        StringBuilder sql2 = new StringBuilder();
        List<ItemInfo> orderByInfos = getLeftJoinOnsItemsListByMethod(method);
        int i = 0;
        for (ItemInfo itemInfo : orderByInfos) {
            String tableName = itemInfo.getTableName();
            sql.append("\n LEFT JOIN ").append(tableName).append(" ").append(itemInfo.getAs()).append(" ON ").append(itemInfo.getOn());
            if (i > 0) {
                sql2.append("\n AND ");
            }
            sql2.append(itemInfo.getAs()).append(".").append(tableInfo.getIsDelete() + " = 0");
            i++;
        }
        return new Tuple2<>(sql.toString(), sql2.toString());
    }

    public static Tuple2<String, String> getFromsBySql(Method method, ParameterInfo[] parameterInfos, String[] parameterNames, TableBaseInfo tableInfo) {
        StringBuilder sql = new StringBuilder();
        List<ItemInfo> orderByInfos = getFromsItemsListByMethod(method);
        StringBuilder sql2 = new StringBuilder();
        int i = 0;
        for (ItemInfo itemInfo : orderByInfos) {
            EntryInfo entryInfo = SqlParseUtils.findTableName(itemInfo.getClazz());
            String as = itemInfo.getAs();
            if (StringUtils.isBlank(as)) {
                as = entryInfo.getAs();
            }
            String tableName = entryInfo.getTableName();
            sql.append(" , ").append(tableName).append(" ").append(as);
            if (i > 0) {
                sql2.append("\n AND ");
            }
            sql2.append(as).append(".").append(tableInfo.getIsDelete() + " = 0 ");
            i++;

            getSqlContext().getOtherEntryInfo().add(new EntryInfo(tableName, as, itemInfo.getClazz()));
            getSqlContext().asList.add(as);
        }
        return new Tuple2<>(sql.toString(), sql2.toString());
    }

    public static String getOrderBySql(Method method, ParameterInfo[] parameterInfos, String[] parameterNames) {
        StringBuilder sql = new StringBuilder();
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
                    for (int j = 0; j < bys.length; j++) {
                        if (isNotEndOrderBy(sql)) {
                            sql.append(",");
                        }
                        sql.append(getEntryColum(bys[j])).append(" ").append("${").append(parameterNames[i]).append("}");
                    }
                }
            }
        }

        if (isOrderBy(method)) {
            OrderBy orderBy = method.getAnnotation(OrderBy.class);
            String[] bys = getAnnotationValue(orderBy);
            OrderType type[] = getAnnotationValueByMethodName(orderBy, "type");
            if (bys != null && bys.length > 0) {
                if (flag) {
                    flag = false;
                    sql.append(" ORDER BY ");
                }

                for (int i = 0; i < bys.length; i++) {
                    String b = bys[i];
                    if (isNotEndOrderBy(sql)) {
                        sql.append(",");
                    }
                    String order = getEntryColum(b);
                    sql.append(order);
                    if (!order.trim().toLowerCase().contains("asc") ||
                            !order.trim().toLowerCase().contains("desc")) {
                        if (type != null && i < type.length) {
                            OrderType orderType = type[i];
                            if (orderType.equals(OrderType.ASC)) {
                                sql.append(" ASC");
                            } else {
                                sql.append(" DESC");
                            }
                        } else {
                            sql.append(" DESC");
                        }
                    }
                }
            }
        }

        List<OrderByInfo> orderByInfos = getMethodOrderByListByMethod(method);
        if (orderByInfos != null && orderByInfos.size() > 0) {
            if (flag) {
                flag = false;
                sql.append(" ORDER BY ");
            }
            for (OrderByInfo orderByInfo : orderByInfos) {
                for (String by : orderByInfo.getBy()) {
                    if (isNotEndOrderBy(sql)) {
                        sql.append(",");
                    }
                    sql.append(getEntryColum(by));
                    if (OrderType.DESC.equals(orderByInfo.getOrderType())) {
                        sql.append(" DESC ");
                    } else {
                        sql.append(" ASC ");
                    }
                }
            }
        }

        if (isOrderByIdDesc(method)) {
            if (flag) {
                sql.append(" ORDER BY ");
            }
            if (isNotEndOrderBy(sql)) {
                sql.append(",");
            }
            sql.append(" id DESC ");
        }

        if (isOrderByIdDescLimit_1(method)) {
            if (flag) {
                sql.append(" ORDER BY ");
            }

            if (isNotEndOrderBy(sql)) {
                sql.append(",");
            }
            sql.append(" id DESC LIMIT 1 ");
        }

        String temp = sql.toString();
        if (temp.endsWith(",")) {
            temp = temp.substring(0, temp.length() - 1);
        }
        return temp;
    }

    public static boolean isNotEndOrderBy(StringBuilder sql) {
        return !sql.toString().trim().endsWith("ORDER BY");
    }

    public static String getAvg(Method method) {
        Avg avg = method.getAnnotation(Avg.class);
        if (avg != null) {
            return getEntryColum(avg.value());
        }
        return null;
    }


    public static String getSum(Method method) {
        Sum avg = method.getAnnotation(Sum.class);
        if (avg != null) {
            return getEntryColum(avg.value());
        }
        return null;
    }

    public static String getMax(Method method) {
        Max avg = method.getAnnotation(Max.class);
        if (avg != null) {
            return getEntryColum(avg.value());
        }
        return null;
    }

    public static String getMapping(Method method) {
        Mapping avg = method.getAnnotation(Mapping.class);
        if (avg != null) {
            String value[] = avg.value();
            if (value.length == 1) {
                if (value[0].contains(":")) {
                    String vs[] = value[0].split(",");
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < vs.length; i++) {
                        sb.append(getEntryColum(vs[i]));
                        if (i < vs.length - 1) {
                            sb.append(",");
                        }
                    }
                    return " " + sb.toString() + " ";
                } else {
                    return " " + value[0] + " ";
                }
            }
            StringBuilder sb = new StringBuilder();
            String as[] = avg.as();
            MK[] mks = avg.mk();
            if (as != null && as.length > 0) {
                for (int i = 0; i < value.length; i++) { //如果是AS ，则需要一一对应
                    String a = as[i];
                    sb.append(getEntryColum(value[i]));
                    if (!"_".equals(a)) {
                        sb.append(" ").append("as").append(" ").append(a);
                    }
                    if (i < value.length - 1) {
                        sb.append(",");
                    }
                }
            } else if (mks != null && mks.length > 0) {
                for (int i = 0; i < value.length; i++) { //如果是AS ，则需要一一对应
                    String v = getEntryColum(value[i]);
                    sb.append(v);
                    for (int j = 0; j < mks.length; j++) {
                        String key = getEntryColum(mks[j].key());
                        if (v.equals(key)) {
                            sb.append(" ").append("as").append(" ").append(mks[j].value());
                        }
                    }
                    if (i < value.length - 1) {
                        sb.append(",");
                    }
                }
            } else {
                for (int i = 0; i < value.length; i++) { //如果是AS ，则需要一一对应
                    String v = getEntryColum(value[i]);
                    sb.append(v);
                    if (i < value.length - 1) {
                        sb.append(",");
                    }
                }
            }
            return sb.toString();
        }
        return "";
    }

    public static String getGroupBy(Method method) {
        GroupBy avg = method.getAnnotation(GroupBy.class);
        if (avg != null) {
            String a = "\n GROUP BY ";
            String[] xx = avg.value();
            for (int i = 0; i < xx.length; i++) {
                if (i == 0) {
                    a += getEntryColum(xx[i]);
                } else {
                    a += "," + getEntryColum(xx[i]);
                }
            }
            return a;
        }
        return "";
    }


    public static String getHaving(Method method) {
        Having avg = method.getAnnotation(Having.class);
        if (avg != null) {
            return " HAVING " + getEntryColum(avg.value()) + " ";
        }
        return "";
    }


    public static String getMin(Method method) {
        Min avg = method.getAnnotation(Min.class);
        if (avg != null) {
            return getEntryColum(avg.value());
        }
        return null;
    }

    public static String getAlias(Method method) {
        AS avg = method.getAnnotation(AS.class);
        if (avg != null) {
            return getEntryColum(avg.value()[0]);
        }
        return "";
    }


    public static String getCount(Method method) {
        Count avg = method.getAnnotation(Count.class);
        if (avg != null) {
            return getEntryColum(avg.value());
        }
        return "*";
    }

    public static String getCountDistince(Method method) {
        CountDistinct avg = method.getAnnotation(CountDistinct.class);
        if (avg != null) {
            return getEntryColum(avg.value());
        }
        return "";
    }

    public static String getEntryColum(String value) {
        if (StringUtils.isNotBlank(value) && value.contains(":")) {
            String colums[] = value.split(":");
            if(!getSqlContext().isMultiTable){
                return colums[1];
            }
            try {
                Class clazz = getSqlContext().primaryEntryInfo.getClazz();
                Class c = Class.forName(colums[0]);
                if (clazz == c) {
                    return getSqlContext().primaryEntryInfo.getAs() + "." + colums[1];
                } else {
                    for (EntryInfo entryInfo : getSqlContext().otherEntryInfo) {
                        if (entryInfo.getClazz() == c) {
                            return entryInfo.getAs() + "." + colums[1];
                        }
                    }
                }
                EntryInfo entryInfo = findTableName(c);
                return entryInfo.getAs() + "." + colums[1];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    public static boolean methodHasAnnotation(Method method, Class clazz) {
        Object avg = method.getAnnotation(clazz);
        if (avg != null) {
            return true;
        }
        return false;
    }

    public static String getWhere(Method method) {
        Where avg = method.getAnnotation(Where.class);
        if (avg == null) {
            return "";
        }
        String value = avg.value();
        Item[] items = avg.condition();
        if (StringUtils.isNotBlank(value)) {
            if (avg.value().trim().toLowerCase().endsWith("and")) {
                return " " + avg.value() + " ";
            } else {
                return " " + avg.value() + " AND";
            }
        } else if (items != null && items.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < items.length; i++) {
                Item item = items[i];
                String[] lefts = item.left();
                String[] rights = item.right();
                for (int j = 0; j < lefts.length; j++) {
                    sb.append(getWhiers(i == 0 && j == 0, lefts[j], item.opt(), rights[j]));
                }
            }
            sb.append(" AND");
            return sb.toString();
        }
        return "";
    }

    //GE, // >=
    //    GT, // >
    //    EQ, // =
    //    LE, // <=
    //    LT, // <
    //    LIKE,//
    //    LLIKE,          // 左like
    //    RLIKE,      //右like
    //    NE,         // 不等于;
    public static String getWhiers(boolean flag, String left, OptType optType, String right) {
        StringBuilder sb = new StringBuilder();
        if (!flag) {
            sb.append(" AND ");
        }
        sb.append(getEntryColum(left));
        if (OptType.GE.equals(optType)) {
            sb.append(" >= ").append(right);
        } else if (OptType.GT.equals(optType)) {
            sb.append(" > ").append(right);
        } else if (OptType.LE.equals(optType)) {
            sb.append(" <= ").append(right);
        } else if (OptType.LT.equals(optType)) {
            sb.append(" < ").append(right);
        } else if (OptType.LIKE.equals(optType)) {
            sb.append(" LIKE CONCAT('%',#{" + right + "},'%') ");
        } else if (OptType.LLIKE.equals(optType)) {
            sb.append(" LIKE CONCAT('%',#{" + right + "},'') ");
        } else if (OptType.RLIKE.equals(optType)) {
            sb.append(" LIKE CONCAT('',#{" + right + "},'%') ");
        } else if (OptType.NE.equals(optType)) {
            sb.append(" != ").append(right);
        } else {
            sb.append(" = ").append(right);
        }
        sb.append("\n");
        return sb.toString();
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
        } else if ("LBracket".equals(annotationName)) {
            parameterInfo.setLBracket(true);
            parameterInfo.setColumn(value);
        } else if ("RBracket".equals(annotationName)) {
            parameterInfo.setRBracket(true);
            parameterInfo.setColumn(value);
        } else if ("IN".equals(annotationName)) {
            parameterInfo.setIn(true);
            parameterInfo.setColumn(value);
        } else if ("Row".equals(annotationName)) {
            parameterInfo.setRowValue(value);
        } else if ("NotIn".equals(annotationName)) {
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
        } else if ("Plus".equals(annotationName)) {
            parameterInfo.setPlus(true);
            parameterInfo.setColumn(value);
        } else if ("Sub".equals(annotationName)) {
            parameterInfo.setSub(true);
            parameterInfo.setColumn(value);
        } else if ("Mul".equals(annotationName)) {
            parameterInfo.setMul(true);
            parameterInfo.setColumn(value);
        } else if ("Div".equals(annotationName)) {
            parameterInfo.setDiv(true);
            parameterInfo.setColumn(value);
        } else if ("AS".equals(annotationName)) {
            parameterInfo.setAlias(true);
            parameterInfo.setAliasValue((String[]) obj);
        } else if ("OrderByIdDesc".equals(annotationName)) {
            parameterInfo.setOrderByIdDesc(true);
        } else if ("OrderByIdDescLimit_1".equals(annotationName)) {
            parameterInfo.setOrderByIdDescLimit_1(true);
        } else if ("Exclude".equals(annotationName)) {
            parameterInfo.setExclude(true);
        } else if ("IF".equals(annotationName)) {
            parameterInfo.setIF(true);
            List<String> list = parameterInfo.getIfParams();
            if (list == null) {
                list = new ArrayList<>();
            }
            if (obj != null) {
                if (obj instanceof String[]) {
                    for (String s : (String[]) obj) {
                        list.add(s);
                    }
                }
            }
            parameterInfo.setIfParams(list);
        } else if ("IFNull".equals(annotationName)) {
            parameterInfo.setIfNull(true);
            List<String> list = parameterInfo.getIfNullParams();
            if (list == null) {
                list = new ArrayList<>();
            }
            if (obj != null) {
                if (obj instanceof String[]) {
                    for (String s : (String[]) obj) {
                        list.add(s);
                    }
                }
            }
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

    public static boolean isOrderByIdDesc(Method method) {
        List<OrderByInfo> byList = new ArrayList<>();
        OrderByIdDesc orderBy = method.getAnnotation(OrderByIdDesc.class);
        if (orderBy != null) {
            return true;
        }
        return false;
    }


    public static boolean isOrderByIdDescLimit_1(Method method) {
        List<OrderByInfo> byList = new ArrayList<>();
        OrderByIdDescLimit_1 orderBy = method.getAnnotation(OrderByIdDescLimit_1.class);
        if (orderBy != null) {
            return true;
        }
        return false;
    }

    public static boolean isOrderBy(Method method) {
        OrderBy orderBy = method.getAnnotation(OrderBy.class);
        if (orderBy != null) {
            return true;
        }
        return false;
    }


    public static String[] getMethodOrderArrayByMethod(Method method) {
        OrderBy orderBy = method.getAnnotation(OrderBy.class);
        if (orderBy != null) {
            String[] bys = getAnnotationValue(orderBy);
            return bys;
        }
        return null;
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

    public static List<ItemInfo> getFromsItemsListByMethod(Method method) {
        List<ItemInfo> byList = new ArrayList<>();
        Froms orderBy = method.getAnnotation(Froms.class);
        if (orderBy != null) {
            Item[] bys = getAnnotationValue(orderBy);
            if (bys != null && bys.length > 0) {
                for (Item by : bys) {
                    Class[] value = getAnnotationValue(by);
                    String as = getAnnotationValueByMethodName(by, "as");
                    byList.add(new ItemInfo(value[0], as));
                }
            }
        }
        return byList;
    }


    public static List<ItemInfo> getLeftJoinOnsItemsListByMethod(Method method) {
        List<ItemInfo> byList = new ArrayList<>();
        LeftJoinOns orderBy = method.getAnnotation(LeftJoinOns.class);
        if (orderBy != null) {
            Item[] bys = getAnnotationValue(orderBy);
            if (bys != null && bys.length > 0) {
                for (Item by : bys) {
                    Class[] value = getAnnotationValue(by);
                    EntryInfo entryInfo = SqlParseUtils.findTableName(value[0]);
                    String as = getAnnotationValueByMethodName(by, "as");
                    if (StringUtils.isBlank(as)) {
                        as = getAs(entryInfo.getTableName());
                    }
                    entryInfo.setAs(as);
                    getSqlContext().otherEntryInfo.add(entryInfo);
                    getSqlContext().asList.add(as);
                }

                for (int j = 0; j < bys.length; j++) {
                    Item by = bys[j];
                    EntryInfo entryInfo = getSqlContext().otherEntryInfo.get(j);
                    String on = getAnnotationValueByMethodName(by, "on");
                    if (StringUtils.isBlank(on)) {
                        String left[] = getAnnotationValueByMethodName(by, "left");
                        String right[] = getAnnotationValueByMethodName(by, "right");
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < left.length; i++) {
                            String leftStr = getEntryColum(left[i]);
                            String rightStr = getEntryColum(right[i]);
                            sb.append(leftStr).append(" = ").append(rightStr);
                            if (i < left.length - 1) {
                                sb.append(" AND ");
                            }
                        }
                        on = sb.toString();
                    }
                    byList.add(new ItemInfo(entryInfo.getClazz(), entryInfo.getAs(), on, entryInfo.getTableName()));
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


    public static <T> T getAnnotationFieldValueByFieldName(Annotation annotation, String fieldName) {
        try {
            Method method = annotation.getClass().getMethod(fieldName);
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
            if (Modifier.isFinal(field.getModifiers())) {    //如果是静态类型
                continue;
            }
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

    public static Class getActualType(Method method, int index) {
        Type[] types = method.getGenericParameterTypes();
        ParameterizedType pType = (ParameterizedType) types[index];
        //获取方法参数泛型类型，那么就为Date
        Type type = pType.getActualTypeArguments()[0];
        try {
            return Class.forName(type.getTypeName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static EntryInfo findTableName(Class<?> type) {
        //获取接口定义上的泛型类型
        //一个类可能实现多个接口,每个接口上定义的泛型类型都可取到
        EntryInfo entryInfo = null;
        String tableName = SqlParseUtils.getAnnotationValueByTypeName(type, CustomerMapperBuilder.TABLENAME);
        String as = SqlParseUtils.getAnnotationValueByTypeName(type, CustomerMapperBuilder.AS);
        if (StringUtils.isNotEmpty(tableName)) {
            entryInfo = new EntryInfo(tableName, as, type);
        } else {
            Type[] interfacesTypes = type.getGenericInterfaces();
            for (Type t : interfacesTypes) {
                Type[] genericType2 = ((ParameterizedType) t).getActualTypeArguments();
                for (Type t2 : genericType2) {
                    try {
                        Class c = Class.forName(t2.getTypeName());
                        tableName = SqlParseUtils.getAnnotationValueByTypeName(c, CustomerMapperBuilder.TABLENAME);
                        as = SqlParseUtils.getAnnotationValueByTypeName(c, CustomerMapperBuilder.AS);
                        if (StringUtils.isNotEmpty(tableName)) {
                            entryInfo = new EntryInfo(tableName, as, c);
                            break;
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }
        // 如果没有配置as注解，则通过表名得到
        if (StringUtils.isBlank(entryInfo.getAs())) {
            entryInfo.setAs(getAs(entryInfo.getTableName()));
        }
        return entryInfo;
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

    public static PluginTuple getTableInfo(Class clazz) {
        TableBaseInfo tableBaseInfo = new TableBaseInfo();
        List<String> primaryColumns = new ArrayList<>();
        Field fields[] = clazz.getDeclaredFields();
        fields = SqlParseUtils.sortFields(fields);
        for (Field field : fields) {
            for (Annotation annotation : field.getAnnotations()) {
                String annotationName = SqlParseUtils.getAnnotationName(annotation);
                Object obj = SqlParseUtils.getAnnotationValue(annotation);
                String column = StringUtils.getDataBaseColumn(SqlParseUtils.getRealFieldName(field));
                if (obj != null && StringUtils.isNotEmpty(obj.toString())) {
                    column = obj.toString();
                }
                if ("TableId".equals(annotationName)) {
                    primaryColumns.add(column);
                    tableBaseInfo.setId(column);
                } else if ("GmtCreate".equals(annotationName)) {
                    tableBaseInfo.setGmtCreate(column);
                } else if ("IsDelete".equals(annotationName)) {
                    tableBaseInfo.setIsDelete(column);
                } else if ("GmtModified".equals(annotationName)) {
                    tableBaseInfo.setGmtModified(column);
                }
            }
        }

        if (primaryColumns.size() == 0) {
            primaryColumns = SqlParseUtils.primaryC;
        }
        if (StringUtils.isEmpty(tableBaseInfo.getGmtModified())) {
            tableBaseInfo = SqlParseUtils.tableBaseInfo;
        }
        return new PluginTuple(primaryColumns, tableBaseInfo);
    }


    public static String colomn2JavaCode(String field) {
        String javaCode = field;

        javaCode = javaCode.toLowerCase();
        javaCode = javaCode.trim();

        if (javaCode.contains("_")) {
            String[] codes = javaCode.split("_");
            if (codes.length > 1) {
                for (int i = 1; i < codes.length; i++) {
                    codes[i] = (codes[i].substring(0, 1)).toUpperCase()
                            + codes[i].substring(1);
                }
                javaCode = "";
                for (int i = 0; i < codes.length; i++) {
                    javaCode += codes[i];
                }
            }
            return javaCode;

        }
        return field;
    }


}
