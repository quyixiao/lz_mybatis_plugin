package com.lz.mybatis.plugin.config;


import com.lz.mybatis.plugin.entity.TableInfo;
import com.lz.mybatis.plugin.mapper.TableRowMapper;
import com.lz.mybatis.plugin.service.MyBatisBaomidouService;
import com.lz.mybatis.plugin.utils.SqlParseUtils;
import com.lz.mybatis.plugin.utils.StringUtils;
import com.lz.mybatis.plugin.utils.t.PluginTuple;
import com.lz.mybatis.plugin.utils.t.Tuple1;
import com.lz.mybatis.plugin.utils.t.Tuple2;
import com.lz.mybatis.plugin.utils.t.Tuple5;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.binding.MapperProxyFactory;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.ResultMapResolver;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.builder.annotation.MethodResolver;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * @author quyixiao
 */
public class CustomerMapperBuilder extends MapperAnnotationBuilder {

    private final Set<Class<? extends Annotation>> customerAnnotationTypes = new HashSet<Class<? extends Annotation>>();

    private Configuration configuration = null;
    private MapperBuilderAssistant assistant = null;
    private Class<?> type = null;
    private static JdbcTemplate jdbcTemplate = null;
    private String tableName;
    private List<String> tableColumns;
    private List<String> primaryColumns;
    public final static String TABLENAME = "TableName";
    public MyBatisBaomidouService myBatisBaomidouService;
    protected final TypeAliasRegistry typeAliasRegistry;
    private Class entityType;

    public CustomerMapperBuilder(Configuration configuration, Class<?> type, MyBatisBaomidouService myBatisBaomidouService) {
        super(configuration, type);
        String resource = type.getName().replace('.', '/') + ".java (best guess)";
        this.assistant = new MapperBuilderAssistant(configuration, resource);
        this.configuration = configuration;
        this.type = type;
        this.myBatisBaomidouService = myBatisBaomidouService;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
        try {
            // ?????? mybatis Configuration ??? useGeneratedKeys ???????????????????????????true??????????????????????????????????????????setUseGeneratedKeys
            // ????????????????????? true
            Method method = Configuration.class.getMethod("setUseGeneratedKeys", boolean.class);
            if (method != null) {
                // invoke  setUseGeneratedKeys set value true
                method.invoke(configuration, true);          //??????????????????@Param?????????????????? sql ???????????????????????????
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ?????? Datasource ??????jdbcTemplate ??????????????????????????????????????????????????????????????????????????????
        if (jdbcTemplate == null) {
            final Environment environment = configuration.getEnvironment();
            DataSource dataSource = environment.getDataSource();
            jdbcTemplate = new JdbcTemplate(dataSource);
        }

        // ????????????,???Mapper????????????????????????????????????????????????????????????????????????????????????@TableName??????????????????@TableName??????
        // ??????@TableName????????? value ???????????????
        tableName = SqlParseUtils.findTableName(type);
        entityType = SqlParseUtils.findEntityType(type);    //??????????????????
        if (StringUtils.isEmpty(tableName)) { //
            tableName = SqlParseUtils.getAnnotationValueByTypeName(type, TABLENAME);
        }
        //????????????????????????????????????
        if (StringUtils.isEmpty(tableName)) {
            return;
        }
        //?????? jdbc ?????????????????????????????????????????? ??? ???????????????
        Tuple2<List<String>, List<String>> tableInfos = getTableInfo(jdbcTemplate, tableName).getData();
        primaryColumns = tableInfos.getFirst();
        tableColumns = tableInfos.getSecond();
        // ????????? Select ,Insert ,Update,Delete ???????????????
        // ????????? Mapper ???????????????????????? selectUser(String username) ; ???????????????????????????@Delete??????
        // ??????????????????????????? sql  ??? delete from user where username = #{username}
        // ??????selectUser???????????? @Delete ???????????????????????? sql ??? select * from user where username = #{username}
        // Select ,Insert ,Update,Delete ???????????????????????????????????????????????? ?????????????????????????????????
        // ?????????????????????????????????
        // select ?????????????????? ????????????
        // update ?????????????????? ????????????
        // insert ?????????????????? ????????????
        // delete ?????????????????? ????????????
        customerAnnotationTypes.add(Select.class);
        customerAnnotationTypes.add(Insert.class);
        customerAnnotationTypes.add(Update.class);
        customerAnnotationTypes.add(Delete.class);
    }

    public void parse() {
        // ?????? *Mapper.java ??????????????????
        Method[] methods = type.getMethods();
        // ??????????????????
        assistant.setCurrentNamespace(type.getName());
        // PluginTuple ?????????java?????????????????? python ????????????
        List<PluginTuple> pluginTuples = new ArrayList<>();
        for (Method method : methods) {
            try {
                String methodName = method.getName();
                // ???currentNamespace + . + methodName ?????? id ?????? mybatis ??????????????????????????? id???
                // ?????? id ??? configuration ?????????,??? id???key,?????? mappedStatement
                String id = assistant.applyCurrentNamespace(methodName, false);
                MappedStatement mappedStatement = null;
                try {
                    mappedStatement = configuration.getMappedStatement(id);
                } catch (Exception e) {
                }
                // ?????? Mapper ???????????????????????????mappedStatement, ?????????????????????????????????????????????????????? mappedStatement
                if (!method.isBridge() && mappedStatement == null) {
                    Tuple1<Boolean> one = parseStatement(method).getData();
                    pluginTuples.add(new PluginTuple(one.getFirst(), method));
                }
            } catch (IncompleteElementException e) {
                configuration.addIncompleteMethod(new MethodResolver(this, method));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // ???????????????????????????????????????
        // ?????? ??? A.xml ??? selectXXX ????????????????????? B.xml ??? <resultMap .../> ??? id?????????????????? A.xml ??????B.xml??????????????????
        // ??????????????????????????????selectXXX???????????????????????? configuration ??? incompleteMethods????????????
        // ??? C.xml ??????????????????????????????configuration ??? incompleteMethods??????????????????????????????????????? method
        // ?????????????????????????????? selectXXX ??? mappedStatement ?????????????????????????????????????????? IncompleteElementException?????????
        // selectXXX??????????????????incompleteMethods?????????????????? *.xml ????????????
        // ??????selectXXX????????? resultMap????????? B.xml ??????????????? ???????????????selectXXX???mappedStatement????????????????????????
        // ?????????selectXXX?????????????????????configuration ??? incompleteMethods????????????
        // selectXXX???mappedStatement??????????????????????????????????????????mappedStatement?????????configuration???mappedStatements?????????
        parsePendingMethods();

        Object mapperRegistry = configuration.getMapperRegistry();
        if ("MybatisMapperRegistry".equals(mapperRegistry.getClass().getSimpleName())) {
            myBatisBaomidouService.init(pluginTuples, configuration, type);
        } else if (mapperRegistry instanceof MapperRegistry) {
            originMyBatis(pluginTuples);
        }

    }


    /**
     * ?????????????????????mybatis???????????????
     */
    public void originMyBatis(List<PluginTuple> pluginTuples) {
        try {
            MapperRegistry mapperRegistry = configuration.getMapperRegistry();
            // ??????MapperRegistry???knownMappers?????????
            Map<Class<?>, MapperProxyFactory<?>> knownMappers = SqlParseUtils.getFieldValue(mapperRegistry, "knownMappers");
            //?????? Mapper ????????????MapperProxyFactory
            MapperProxyFactory mapperProxyFactory = knownMappers.get(type);
            // ???mapperProxyFactory??????????????????Map<Method, MapperMethod>
            Map<Method, MapperMethod> methodCache = mapperProxyFactory.getMethodCache();
            for (PluginTuple pluginTuple : pluginTuples) {
                Tuple2<Boolean, Method> data = pluginTuple.getData();
                Method method = data.getSecond();
                MapperMethod mapperMethod = methodCache.get(method);
                // ????????????????????? mapperMethod ??????????????????MapperMethod
                if (mapperMethod == null) {
                    if (mapperProxyFactory.getMapperInterface() != null) {
                        mapperMethod = new MapperMethod(mapperProxyFactory.getMapperInterface(), method, configuration);
                    } else {
                        mapperMethod = new MapperMethod(type, method, configuration);
                    }
                    // ??????mapperMethod???method????????????
                    MapperMethod.MethodSignature methodSignature = SqlParseUtils.getFieldValue(mapperMethod, "method");
                    // ?????????????????????methodSignature???paramNameResolver??????
                    ParamNameResolver paramNameResolver = SqlParseUtils.getFieldValue(methodSignature, "paramNameResolver");
                    // ?????? paramNameResolver???hasParamAnnotation????????? data.getFirst()??? boolean ??????
                    // hasParamAnnotation ?????????????????????????????????????????????@Param ??????
                    // 1. ?????????????????????????????? selectUserById(Long id) ?????????????????????data.getFirst(),????????????@Param??????
                    // ??????????????? sql ??? select * from user where username = #{username},????????????hasParamAnnotation??????true
                    // ??????????????????????????? selectUserById(Long id) ?????????@Param??????????????????
                    // ??????????????? selectUserById(@Param Long id)???????????????????????????selectUserById(Long id)
                    // 2. ?????????insert(User user )??????,MyBatis?????????????????????????????????????????????????????????hasParamAnnotation??? false
                    // ?????? true ??? false ?????????sql?????????
                    // ?????????insert(User user )????????????hasParamAnnotation??? true
                    // insert(id ,username ,password ) values (user.id,user.username,user.password)
                    // hasParamAnnotation???????????? false??????????????? sql ???
                    // insert (id ,username,password ) values (id,username,password),?????????????????????????????????
                    SqlParseUtils.setFieldValue(paramNameResolver, "hasParamAnnotation", data.getFirst());
                    methodCache.put(method, mapperMethod);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*
    public void baomidouMyBatis(List<PluginTuple> pluginTuples,Configuration configuration,Class type) {
        try {
            MybatisMapperRegistry mapperRegistry = (MybatisMapperRegistry) configuration.getMapperRegistry();
            Map<Class<?>, MybatisMapperProxyFactory<?>> knownMappers = SqlParseUtils.getFieldValue(mapperRegistry, "knownMappers");
            MybatisMapperProxyFactory mapperProxyFactory = knownMappers.get(type);
            Map<Method, MybatisMapperMethod> methodCache = mapperProxyFactory.getMethodCache();
            for (PluginTuple pluginTuple : pluginTuples) {
                Tuple2<Boolean, Method> data = pluginTuple.getData();
                Method method = data.getSecond();
                MybatisMapperMethod mapperMethod = methodCache.get(method);
                if (mapperMethod == null) {
                    if (mapperProxyFactory.getMapperInterface() != null) {
                        mapperMethod = new MybatisMapperMethod(mapperProxyFactory.getMapperInterface(), method, configuration);
                    } else {
                        mapperMethod = new MybatisMapperMethod(type, method, configuration);
                    }
                    MapperMethod.MethodSignature methodSignature = SqlParseUtils.getFieldValue(mapperMethod, "method");
                    ParamNameResolver paramNameResolver = SqlParseUtils.getFieldValue(methodSignature, "paramNameResolver");
                    SqlParseUtils.setFieldValue(paramNameResolver, "hasParamAnnotation", data.getFirst());
                    methodCache.put(method, mapperMethod);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private PluginTuple buildSqlSourceFromStrings(Method method, Class<?> parameterTypeClass, LanguageDriver languageDriver, SqlCommandType sqlCommandType) {
        Tuple5<Boolean, String, String, String, String> data = SqlParseUtils.parse(tableName, primaryColumns, tableColumns, sqlCommandType, method, entityType).getData();
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, data.getSecond().trim(), parameterTypeClass);
        if(myBatisBaomidouService !=null){
            myBatisBaomidouService.info( type.getSimpleName() + "." + method.getName()  +"\t" +  data.getSecond());
        }
        return new PluginTuple(data.getFirst(), sqlSource, data.getThird(), data.getFourth(), data.getFifth());
    }

    public PluginTuple getTableInfo(JdbcTemplate jdbcTemplate, String tableName) {
        String sql = "SELECT COLUMN_NAME columnName, DATA_TYPE dataType, COLUMN_COMMENT" +
                "  columnComment,COLUMN_KEY columnKey FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + tableName + "'";

        List<TableInfo> tableInfos = jdbcTemplate.query(sql, new TableRowMapper());
        List<String> tableColumns = new ArrayList<>();
        List<String> primaryColumns = new ArrayList<>();
        if (tableInfos != null && tableInfos.size() > 0) {
            for (TableInfo tableInfo : tableInfos) {
                if(!tableColumns.contains(tableInfo.getColumnName())){
                    tableColumns.add(tableInfo.getColumnName());
                }
                if ("PRI".equals(tableInfo.getColumnKey())) {           // ????????????
                    if(!primaryColumns.contains(tableInfo.getColumnName())){
                        primaryColumns.add(tableInfo.getColumnName());
                    }
                }
            }
        }
        return new PluginTuple(primaryColumns, tableColumns);
    }


    private SqlSource buildSqlSourceFromStrings(String[] strings, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        final StringBuilder sql = new StringBuilder();
        for (String fragment : strings) {
            sql.append(fragment);
            sql.append(" ");
        }
        return languageDriver.createSqlSource(configuration, sql.toString().trim(), parameterTypeClass);
    }

    PluginTuple parseStatement(Method method) throws Exception {
        Class<?> parameterTypeClass = getParameterType(method);
        LanguageDriver languageDriver = getLanguageDriver(method);
        SqlCommandType sqlCommandType = getSqlCommandType(method);
        PluginTuple data = getSqlSourceFromAnnotations(method, parameterTypeClass, languageDriver, sqlCommandType);
        Tuple5<Boolean, SqlSource, String, String, String> tupleInfo = data.getData();
        if (StringUtils.isNotEmpty(tupleInfo.getFourth())) {  //?????????????????? Mapper
            XPathParser xPathParser = new XPathParser(tupleInfo.getFifth());
            List<XNode> xNodeList = xPathParser.evalNodes("/resultMap");
            resultMapElement(xNodeList.get(0));
        }
        SqlSource sqlSource = tupleInfo.getSecond();
        String keyPropertyPre = "";
        if (StringUtils.isNotEmpty(tupleInfo.getThird())) {
            keyPropertyPre = tupleInfo.getThird();
        }
        if (sqlSource != null) {
            Options options = method.getAnnotation(Options.class);
            final String mappedStatementId = type.getName() + "." + method.getName();
            Integer fetchSize = null;
            Integer timeout = null;
            StatementType statementType = StatementType.PREPARED;
            ResultSetType resultSetType = ResultSetType.FORWARD_ONLY;

            boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
            boolean flushCache = !isSelect;
            boolean useCache = isSelect;

            KeyGenerator keyGenerator;
            String keyProperty = keyPropertyPre + "id";
            String keyColumn = null;
            if (SqlCommandType.INSERT.equals(sqlCommandType) || SqlCommandType.UPDATE.equals(sqlCommandType)) {
                // first check for SelectKey annotation - that overrides everything else
                SelectKey selectKey = method.getAnnotation(SelectKey.class);
                KeyGenerator jdbc3KeyGenerator = SqlCommandType.INSERT.equals(sqlCommandType) ?
                        Jdbc3KeyGenerator.INSTANCE : CustomerJdbc3KeyGenerator.INSTANCE;
                if (selectKey != null) {
                    keyGenerator = handleSelectKeyAnnotation(selectKey, mappedStatementId, getParameterType(method), languageDriver);
                    keyProperty = selectKey.keyProperty();
                } else if (options == null) {
                    keyGenerator = configuration.isUseGeneratedKeys() ? jdbc3KeyGenerator : NoKeyGenerator.INSTANCE;
                } else {
                    keyGenerator = options.useGeneratedKeys() ? jdbc3KeyGenerator : NoKeyGenerator.INSTANCE;
                    keyProperty = options.keyProperty();
                    keyColumn = options.keyColumn();
                }
            } else {
                keyGenerator = NoKeyGenerator.INSTANCE;
            }
            if (options != null) {
                if (Options.FlushCachePolicy.TRUE.equals(options.flushCache())) {
                    flushCache = true;
                } else if (Options.FlushCachePolicy.FALSE.equals(options.flushCache())) {
                    flushCache = false;
                }
                useCache = options.useCache();
                fetchSize = options.fetchSize() > -1 || options.fetchSize() == Integer.MIN_VALUE ? options.fetchSize() : null; //issue #348
                timeout = options.timeout() > -1 ? options.timeout() : null;
                statementType = options.statementType();
                resultSetType = options.resultSetType();
            }
            String resultMapId = null;
            if (StringUtils.isEmpty(tupleInfo.getFourth())) {
                ResultMap resultMapAnnotation = method.getAnnotation(ResultMap.class);
                if (resultMapAnnotation != null) {
                    String[] resultMaps = resultMapAnnotation.value();
                    StringBuilder sb = new StringBuilder();
                    for (String resultMap : resultMaps) {
                        if (sb.length() > 0) {
                            sb.append(",");
                        }
                        sb.append(resultMap);
                    }
                    resultMapId = sb.toString();
                } else if (isSelect) {
                    resultMapId = parseResultMap(method);
                }
            } else {
                resultMapId = tupleInfo.getFourth();
            }

            assistant.addMappedStatement(
                    mappedStatementId,
                    sqlSource,
                    statementType,
                    sqlCommandType,
                    fetchSize,
                    timeout,
                    // ParameterMapID
                    null,
                    parameterTypeClass,
                    resultMapId,
                    getReturnType(method),
                    resultSetType,
                    flushCache,
                    useCache,
                    // TODO gcode issue #577
                    false,
                    keyGenerator,
                    keyProperty,
                    keyColumn,
                    // DatabaseID
                    null,
                    languageDriver,
                    // ResultSets
                    options != null ? nullOrEmpty(options.resultSets()) : null);
        }
        return data;
    }

    private org.apache.ibatis.mapping.ResultMap resultMapElement(XNode resultMapNode) throws Exception {
        return resultMapElement(resultMapNode, Collections.<ResultMapping>emptyList());
    }

    private org.apache.ibatis.mapping.ResultMap resultMapElement(XNode resultMapNode, List<ResultMapping> additionalResultMappings) throws Exception {
        ErrorContext.instance().activity("processing " + resultMapNode.getValueBasedIdentifier());
        String id = resultMapNode.getStringAttribute("id",
                resultMapNode.getValueBasedIdentifier());
        String type = resultMapNode.getStringAttribute("type",
                resultMapNode.getStringAttribute("ofType",
                        resultMapNode.getStringAttribute("resultType",
                                resultMapNode.getStringAttribute("javaType"))));
        String extend = resultMapNode.getStringAttribute("extends");
        Boolean autoMapping = resultMapNode.getBooleanAttribute("autoMapping");
        Class<?> typeClass = resolveClass(type);
        Discriminator discriminator = null;
        List<ResultMapping> resultMappings = new ArrayList<ResultMapping>();
        resultMappings.addAll(additionalResultMappings);
        List<XNode> resultChildren = resultMapNode.getChildren();
        for (XNode resultChild : resultChildren) {
            if ("constructor".equals(resultChild.getName())) {
                processConstructorElement(resultChild, typeClass, resultMappings);
            } else if ("discriminator".equals(resultChild.getName())) {
                discriminator = processDiscriminatorElement(resultChild, typeClass, resultMappings);
            } else {
                ArrayList<ResultFlag> flags = new ArrayList<ResultFlag>();
                if ("id".equals(resultChild.getName())) {
                    flags.add(ResultFlag.ID);
                }
                resultMappings.add(buildResultMappingFromContext(resultChild, typeClass, flags));
            }
        }
        ResultMapResolver resultMapResolver = new ResultMapResolver(assistant, id, typeClass, extend, discriminator, resultMappings, autoMapping);
        try {
            return resultMapResolver.resolve();
        } catch (IncompleteElementException e) {
            configuration.addIncompleteResultMap(resultMapResolver);
            throw e;
        }
    }

    private Discriminator processDiscriminatorElement(XNode context, Class<?> resultType, List<ResultMapping> resultMappings) throws Exception {
        String column = context.getStringAttribute("column");
        String javaType = context.getStringAttribute("javaType");
        String jdbcType = context.getStringAttribute("jdbcType");
        String typeHandler = context.getStringAttribute("typeHandler");
        Class<?> javaTypeClass = resolveClass(javaType);
        @SuppressWarnings("unchecked")
        Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);
        JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
        Map<String, String> discriminatorMap = new HashMap<String, String>();
        for (XNode caseChild : context.getChildren()) {
            String value = caseChild.getStringAttribute("value");
            String resultMap = caseChild.getStringAttribute("resultMap", processNestedResultMappings(caseChild, resultMappings));
            discriminatorMap.put(value, resultMap);
        }
        return assistant.buildDiscriminator(resultType, column, javaTypeClass, jdbcTypeEnum, typeHandlerClass, discriminatorMap);
    }

    private ResultMapping buildResultMappingFromContext(XNode context, Class<?> resultType, List<ResultFlag> flags) throws Exception {
        String property;
        if (flags.contains(ResultFlag.CONSTRUCTOR)) {
            property = context.getStringAttribute("name");
        } else {
            property = context.getStringAttribute("property");
        }
        String column = context.getStringAttribute("column");
        String javaType = context.getStringAttribute("javaType");
        String jdbcType = context.getStringAttribute("jdbcType");
        String nestedSelect = context.getStringAttribute("select");
        String nestedResultMap = context.getStringAttribute("resultMap",
                processNestedResultMappings(context, Collections.<ResultMapping>emptyList()));
        String notNullColumn = context.getStringAttribute("notNullColumn");
        String columnPrefix = context.getStringAttribute("columnPrefix");
        String typeHandler = context.getStringAttribute("typeHandler");
        String resultSet = context.getStringAttribute("resultSet");
        String foreignColumn = context.getStringAttribute("foreignColumn");
        boolean lazy = "lazy".equals(context.getStringAttribute("fetchType", configuration.isLazyLoadingEnabled() ? "lazy" : "eager"));
        Class<?> javaTypeClass = resolveClass(javaType);
        @SuppressWarnings("unchecked")
        Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);
        JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
        return assistant.buildResultMapping(resultType, property, column, javaTypeClass, jdbcTypeEnum, nestedSelect, nestedResultMap, notNullColumn, columnPrefix, typeHandlerClass, flags, resultSet, foreignColumn, lazy);
    }

    private String processNestedResultMappings(XNode context, List<ResultMapping> resultMappings) throws Exception {
        if ("association".equals(context.getName())
                || "collection".equals(context.getName())
                || "case".equals(context.getName())) {
            if (context.getStringAttribute("select") == null) {
                org.apache.ibatis.mapping.ResultMap resultMap = resultMapElement(context, resultMappings);
                return resultMap.getId();
            }
        }
        return null;
    }


    private void processConstructorElement(XNode resultChild, Class<?> resultType, List<ResultMapping> resultMappings) throws Exception {
        List<XNode> argChildren = resultChild.getChildren();
        for (XNode argChild : argChildren) {
            List<ResultFlag> flags = new ArrayList<ResultFlag>();
            flags.add(ResultFlag.CONSTRUCTOR);
            if ("idArg".equals(argChild.getName())) {
                flags.add(ResultFlag.ID);
            }
            resultMappings.add(buildResultMappingFromContext(argChild, resultType, flags));
        }
    }

    protected JdbcType resolveJdbcType(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return JdbcType.valueOf(alias);
        } catch (IllegalArgumentException e) {
            throw new BuilderException("Error resolving JdbcType. Cause: " + e, e);
        }
    }

    protected Class<?> resolveClass(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return resolveAlias(alias);
        } catch (Exception e) {
            throw new BuilderException("Error resolving class. Cause: " + e, e);
        }
    }

    protected Class<?> resolveAlias(String alias) {
        return typeAliasRegistry.resolveAlias(alias);
    }

    private SqlCommandType getSqlCommandType(Method method) {
        Class<? extends Annotation> type = getSqlAnnotationType(method);
        if (type != null) {
            if (type == Insert.class) {
                return SqlCommandType.INSERT;
            } else if (type == Update.class) {
                return SqlCommandType.UPDATE;
            } else if (type == Delete.class) {
                return SqlCommandType.DELETE;
            }
        } else {
            String methodName = method.getName();
            if (methodName.startsWith("get") || method.getName().startsWith("select") || method.getName().startsWith("count")) {
                return SqlCommandType.SELECT;
            } else if (methodName.startsWith("update")) {
                return SqlCommandType.UPDATE;
            } else if (methodName.startsWith("delete")) {
                return SqlCommandType.DELETE;
            } else if (methodName.startsWith("insert") || methodName.startsWith("add")) {
                return SqlCommandType.INSERT;
            }
        }
        return SqlCommandType.UNKNOWN;
    }

    private Class<?> getParameterType(Method method) {
        Class<?> parameterType = null;
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> currentParameterType : parameterTypes) {
            if (!RowBounds.class.isAssignableFrom(currentParameterType) && !ResultHandler.class.isAssignableFrom(currentParameterType)) {
                if (parameterType == null) {
                    parameterType = currentParameterType;
                } else {
                    // issue #135
                    parameterType = MapperMethod.ParamMap.class;
                }
            }
        }
        return parameterType;
    }

    private LanguageDriver getLanguageDriver(Method method) {
        return assistant.getLanguageDriver(null);
    }


    private PluginTuple getSqlSourceFromAnnotations(Method method, Class<?> parameterType, LanguageDriver languageDriver, SqlCommandType sqlCommandType) {
        try {
            return buildSqlSourceFromStrings(method, parameterType, languageDriver, sqlCommandType);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuilderException("Could not find value method on SQL annotation.  Cause: " + e, e);
        }
    }

    private Class<? extends Annotation> getSqlAnnotationType(Method method) {
        return chooseAnnotationType(method, customerAnnotationTypes);
    }


    private Class<? extends Annotation> chooseAnnotationType(Method method, Set<Class<? extends Annotation>> types) {
        for (Class<? extends Annotation> type : types) {
            Annotation annotation = method.getAnnotation(type);
            if (annotation != null) {
                return type;
            }
        }
        return null;
    }


    private KeyGenerator handleSelectKeyAnnotation(SelectKey selectKeyAnnotation, String baseStatementId, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        String id = baseStatementId + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        Class<?> resultTypeClass = selectKeyAnnotation.resultType();
        StatementType statementType = selectKeyAnnotation.statementType();
        String keyProperty = selectKeyAnnotation.keyProperty();
        String keyColumn = selectKeyAnnotation.keyColumn();
        boolean executeBefore = selectKeyAnnotation.before();

        // defaults
        boolean useCache = false;
        KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;
        Integer fetchSize = null;
        Integer timeout = null;
        boolean flushCache = false;
        String parameterMap = null;
        String resultMap = null;
        ResultSetType resultSetTypeEnum = null;

        SqlSource sqlSource = buildSqlSourceFromStrings(selectKeyAnnotation.statement(), parameterTypeClass, languageDriver);
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;

        assistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType, fetchSize, timeout, parameterMap, parameterTypeClass, resultMap, resultTypeClass, resultSetTypeEnum,
                flushCache, useCache, false,
                keyGenerator, keyProperty, keyColumn, null, languageDriver, null);

        id = assistant.applyCurrentNamespace(id, false);

        MappedStatement keyStatement = configuration.getMappedStatement(id, false);
        SelectKeyGenerator answer = new SelectKeyGenerator(keyStatement, executeBefore);
        configuration.addKeyGenerator(id, answer);
        return answer;
    }


    private String parseResultMap(Method method) {
        Class<?> returnType = getReturnType(method);
        ConstructorArgs args = method.getAnnotation(ConstructorArgs.class);
        Results results = method.getAnnotation(Results.class);
        TypeDiscriminator typeDiscriminator = method.getAnnotation(TypeDiscriminator.class);
        String resultMapId = generateResultMapName(method);
        applyResultMap(resultMapId, returnType, argsIf(args), resultsIf(results), typeDiscriminator);
        return resultMapId;
    }


    private Class<?> getReturnType(Method method) {
        Class<?> returnType = method.getReturnType();
        Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, type);
        if (resolvedReturnType instanceof Class) {
            returnType = (Class<?>) resolvedReturnType;
            if (returnType.isArray()) {
                returnType = returnType.getComponentType();
            }
            // gcode issue #508
            if (void.class.equals(returnType)) {
                ResultType rt = method.getAnnotation(ResultType.class);
                if (rt != null) {
                    returnType = rt.value();
                }
            }
        } else if (resolvedReturnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) resolvedReturnType;
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            if (Collection.class.isAssignableFrom(rawType) || Cursor.class.isAssignableFrom(rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    Type returnTypeParameter = actualTypeArguments[0];
                    if (returnTypeParameter instanceof Class<?>) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // (gcode issue #443) actual type can be a also a parameterized type
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    } else if (returnTypeParameter instanceof GenericArrayType) {
                        Class<?> componentType = (Class<?>) ((GenericArrayType) returnTypeParameter).getGenericComponentType();
                        // (gcode issue #525) support List<byte[]>
                        returnType = Array.newInstance(componentType, 0).getClass();
                    }
                }
            } else if (method.isAnnotationPresent(MapKey.class) && Map.class.isAssignableFrom(rawType)) {
                // (gcode issue 504) Do not look into Maps if there is not MapKey annotation
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 2) {
                    Type returnTypeParameter = actualTypeArguments[1];
                    if (returnTypeParameter instanceof Class<?>) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // (gcode issue 443) actual type can be a also a parameterized type
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    }
                }
            }
        }

        return returnType;
    }


    private String generateResultMapName(Method method) {
        Results results = method.getAnnotation(Results.class);
        if (results != null && !results.id().isEmpty()) {
            return type.getName() + "." + results.id();
        }
        StringBuilder suffix = new StringBuilder();
        for (Class<?> c : method.getParameterTypes()) {
            suffix.append("-");
            suffix.append(c.getSimpleName());
        }
        if (suffix.length() < 1) {
            suffix.append("-void");
        }
        return type.getName() + "." + method.getName() + suffix;
    }


    private void applyResultMap(String resultMapId, Class<?> returnType, Arg[] args, Result[] results, TypeDiscriminator discriminator) {
        List<ResultMapping> resultMappings = new ArrayList<ResultMapping>();
        applyConstructorArgs(args, returnType, resultMappings);
        applyResults(results, returnType, resultMappings);
        Discriminator disc = applyDiscriminator(resultMapId, returnType, discriminator);
        // TODO add AutoMappingBehaviour
        assistant.addResultMap(resultMapId, returnType, null, disc, resultMappings, null);
        createDiscriminatorResultMaps(resultMapId, returnType, discriminator);
    }


    private Result[] resultsIf(Results results) {
        return results == null ? new Result[0] : results.value();
    }

    private Arg[] argsIf(ConstructorArgs args) {
        return args == null ? new Arg[0] : args.value();
    }


    private void applyConstructorArgs(Arg[] args, Class<?> resultType, List<ResultMapping> resultMappings) {
        for (Arg arg : args) {
            List<ResultFlag> flags = new ArrayList<ResultFlag>();
            flags.add(ResultFlag.CONSTRUCTOR);
            if (arg.id()) {
                flags.add(ResultFlag.ID);
            }
            @SuppressWarnings("unchecked")
            Class<? extends TypeHandler<?>> typeHandler = (Class<? extends TypeHandler<?>>)
                    (arg.typeHandler() == UnknownTypeHandler.class ? null : arg.typeHandler());
            ResultMapping resultMapping = assistant.buildResultMapping(
                    resultType,
                    nullOrEmpty(arg.name()),
                    nullOrEmpty(arg.column()),
                    arg.javaType() == void.class ? null : arg.javaType(),
                    arg.jdbcType() == JdbcType.UNDEFINED ? null : arg.jdbcType(),
                    nullOrEmpty(arg.select()),
                    nullOrEmpty(arg.resultMap()),
                    null,
                    null,
                    typeHandler,
                    flags,
                    null,
                    null,
                    false);
            resultMappings.add(resultMapping);
        }
    }


    private void applyResults(Result[] results, Class<?> resultType, List<ResultMapping> resultMappings) {
        for (Result result : results) {
            List<ResultFlag> flags = new ArrayList<ResultFlag>();
            if (result.id()) {
                flags.add(ResultFlag.ID);
            }
            @SuppressWarnings("unchecked")
            Class<? extends TypeHandler<?>> typeHandler = (Class<? extends TypeHandler<?>>)
                    ((result.typeHandler() == UnknownTypeHandler.class) ? null : result.typeHandler());
            ResultMapping resultMapping = assistant.buildResultMapping(
                    resultType,
                    nullOrEmpty(result.property()),
                    nullOrEmpty(result.column()),
                    result.javaType() == void.class ? null : result.javaType(),
                    result.jdbcType() == JdbcType.UNDEFINED ? null : result.jdbcType(),
                    hasNestedSelect(result) ? nestedSelectId(result) : null,
                    null,
                    null,
                    null,
                    typeHandler,
                    flags,
                    null,
                    null,
                    isLazy(result));
            resultMappings.add(resultMapping);
        }
    }


    private Discriminator applyDiscriminator(String resultMapId, Class<?> resultType, TypeDiscriminator discriminator) {
        if (discriminator != null) {
            String column = discriminator.column();
            Class<?> javaType = discriminator.javaType() == void.class ? String.class : discriminator.javaType();
            JdbcType jdbcType = discriminator.jdbcType() == JdbcType.UNDEFINED ? null : discriminator.jdbcType();
            @SuppressWarnings("unchecked")
            Class<? extends TypeHandler<?>> typeHandler = (Class<? extends TypeHandler<?>>)
                    (discriminator.typeHandler() == UnknownTypeHandler.class ? null : discriminator.typeHandler());
            Case[] cases = discriminator.cases();
            Map<String, String> discriminatorMap = new HashMap<String, String>();
            for (Case c : cases) {
                String value = c.value();
                String caseResultMapId = resultMapId + "-" + value;
                discriminatorMap.put(value, caseResultMapId);
            }
            return assistant.buildDiscriminator(resultType, column, javaType, jdbcType, typeHandler, discriminatorMap);
        }
        return null;
    }


    private void createDiscriminatorResultMaps(String resultMapId, Class<?> resultType, TypeDiscriminator discriminator) {
        if (discriminator != null) {
            for (Case c : discriminator.cases()) {
                String caseResultMapId = resultMapId + "-" + c.value();
                List<ResultMapping> resultMappings = new ArrayList<ResultMapping>();
                // issue #136
                applyConstructorArgs(c.constructArgs(), resultType, resultMappings);
                applyResults(c.results(), resultType, resultMappings);
                // TODO add AutoMappingBehaviour
                assistant.addResultMap(caseResultMapId, c.type(), resultMapId, null, resultMappings, null);
            }
        }
    }


    private String nullOrEmpty(String value) {
        return value == null || value.trim().length() == 0 ? null : value;
    }

    private boolean hasNestedSelect(Result result) {
        if (result.one().select().length() > 0 && result.many().select().length() > 0) {
            throw new BuilderException("Cannot use both @One and @Many annotations in the same @Result");
        }
        return result.one().select().length() > 0 || result.many().select().length() > 0;
    }


    private String nestedSelectId(Result result) {
        String nestedSelect = result.one().select();
        if (nestedSelect.length() < 1) {
            nestedSelect = result.many().select();
        }
        if (!nestedSelect.contains(".")) {
            nestedSelect = type.getName() + "." + nestedSelect;
        }
        return nestedSelect;
    }


    private boolean isLazy(Result result) {
        boolean isLazy = configuration.isLazyLoadingEnabled();
        if (result.one().select().length() > 0 && FetchType.DEFAULT != result.one().fetchType()) {
            isLazy = result.one().fetchType() == FetchType.LAZY;
        } else if (result.many().select().length() > 0 && FetchType.DEFAULT != result.many().fetchType()) {
            isLazy = result.many().fetchType() == FetchType.LAZY;
        }
        return isLazy;
    }


    private void parsePendingMethods() {
        Collection<MethodResolver> incompleteMethods = configuration.getIncompleteMethods();
        synchronized (incompleteMethods) {
            Iterator<MethodResolver> iter = incompleteMethods.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().resolve();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // This method is still missing a resource
                }
            }
        }
    }

}
