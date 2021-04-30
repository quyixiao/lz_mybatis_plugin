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
            // 有些 mybatis Configuration 的 useGeneratedKeys 字段没有默认设置为true，为了保险起见，还是调用一下setUseGeneratedKeys
            // 设置其默认值为 true
            Method method = Configuration.class.getMethod("setUseGeneratedKeys", boolean.class);
            if (method != null) {
                // invoke  setUseGeneratedKeys set value true
                method.invoke(configuration, true);          //默认不需要写@Param注解，就能在 sql 中引用方法参数名称
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 获取 Datasource 构建jdbcTemplate ，主要用途是用来在项目启动时获取数据库表中的所有字段
        if (jdbcTemplate == null) {
            final Environment environment = configuration.getEnvironment();
            DataSource dataSource = environment.getDataSource();
            jdbcTemplate = new JdbcTemplate(dataSource);
        }

        // 获取表名,看Mapper的继承类中有没有配置泛型，如果配置泛型，看泛型对象是否有@TableName注解，如果有@TableName注解
        // 获取@TableName注解的 value 作为表名称
        tableName = SqlParseUtils.findTableName(type);
        entityType = SqlParseUtils.findEntityType(type);    //找到实体名称
        if (StringUtils.isEmpty(tableName)) { //
            tableName = SqlParseUtils.getAnnotationValueByTypeName(type, TABLENAME);
        }
        //如果表名为空，则直接退出
        if (StringUtils.isEmpty(tableName)) {
            return;
        }
        //通过 jdbc 获取表信息，主要是表的主键列 和 表的所有列
        Tuple2<List<String>, List<String>> tableInfos = getTableInfo(jdbcTemplate, tableName).getData();
        primaryColumns = tableInfos.getFirst();
        tableColumns = tableInfos.getSecond();
        // 初始化 Select ,Insert ,Update,Delete 注解，如：
        // 我们在 Mapper 中定义了一个方法 selectUser(String username) ; 但是方法上确配置了@Delete注解
        // 那么最后动态生成的 sql  是 delete from user where username = #{username}
        // 如果selectUser没有配置 @Delete 注解，那么生成的 sql 是 select * from user where username = #{username}
        // Select ,Insert ,Update,Delete 注解的作用主要是指定方法的是查询 ，更新，插入，还是删除
        // 默认情况下，方法名以：
        // select 开头的方法是 查询操作
        // update 开头的方法是 更新操作
        // insert 开头的方法是 插入操作
        // delete 开头的方法是 删除操作
        customerAnnotationTypes.add(Select.class);
        customerAnnotationTypes.add(Insert.class);
        customerAnnotationTypes.add(Update.class);
        customerAnnotationTypes.add(Delete.class);
    }

    public void parse() {
        // 获取 *Mapper.java 中的所有方法
        Method[] methods = type.getMethods();
        // 设置名称空间
        assistant.setCurrentNamespace(type.getName());
        // PluginTuple 是一个java元组，类似于 python 中的元组
        List<PluginTuple> pluginTuples = new ArrayList<>();
        for (Method method : methods) {
            try {
                String methodName = method.getName();
                // 以currentNamespace + . + methodName 构建 id ，在 mybatis 源码中也是这样构建 id的
                // 这个 id 在 configuration 中唯一,以 id为key,保存 mappedStatement
                String id = assistant.applyCurrentNamespace(methodName, false);
                MappedStatement mappedStatement = null;
                try {
                    mappedStatement = configuration.getMappedStatement(id);
                } catch (Exception e) {
                }
                // 如果 Mapper 中的方法没有对应的mappedStatement, 并且方法不是桥接方法，则为此方法生成 mappedStatement
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
        // 这个方法的目的主要是解决：
        // 如： 在 A.xml 的 selectXXX 方法引用了一个 B.xml 的 <resultMap .../> 的 id，但是在解析 A.xml 时，B.xml还没有被解析
        // 这个时候，解析器会将selectXXX方法的信息存储于 configuration 的 incompleteMethods属性中。
        // 当 C.xml 己经解析完了，会查看configuration 的 incompleteMethods属性中是否有未初始化完成的 method
        // 如果有，则继续去完成 selectXXX 的 mappedStatement 的创建，如果还是失败，将抛出 IncompleteElementException异常，
        // selectXXX信息会继续留incompleteMethods中等待下一个 *.xml 的解析，
        // 直到selectXXX引用的 resultMap所在的 B.xml 被解析完成 ，此时创建selectXXX的mappedStatement将不会抛出异常，
        // 并且将selectXXX方法的信息从从configuration 的 incompleteMethods属性移除
        // selectXXX的mappedStatement就己经创建完成，并将创建好的mappedStatement保存到configuration的mappedStatements属性中
        parsePendingMethods();

        Object mapperRegistry = configuration.getMapperRegistry();
        if ("MybatisMapperRegistry".equals(mapperRegistry.getClass().getSimpleName())) {
            myBatisBaomidouService.init(pluginTuples, configuration, type);
        } else if (mapperRegistry instanceof MapperRegistry) {
            originMyBatis(pluginTuples);
        }

    }


    /**
     * 没有引用第三方mybatis的处理方式
     */
    public void originMyBatis(List<PluginTuple> pluginTuples) {
        try {
            MapperRegistry mapperRegistry = configuration.getMapperRegistry();
            // 获取MapperRegistry的knownMappers属性值
            Map<Class<?>, MapperProxyFactory<?>> knownMappers = SqlParseUtils.getFieldValue(mapperRegistry, "knownMappers");
            //根据 Mapper 类型获取MapperProxyFactory
            MapperProxyFactory mapperProxyFactory = knownMappers.get(type);
            // 从mapperProxyFactory的缓存中获取Map<Method, MapperMethod>
            Map<Method, MapperMethod> methodCache = mapperProxyFactory.getMethodCache();
            for (PluginTuple pluginTuple : pluginTuples) {
                Tuple2<Boolean, Method> data = pluginTuple.getData();
                Method method = data.getSecond();
                MapperMethod mapperMethod = methodCache.get(method);
                // 如果方法对应的 mapperMethod 为空，则创建MapperMethod
                if (mapperMethod == null) {
                    if (mapperProxyFactory.getMapperInterface() != null) {
                        mapperMethod = new MapperMethod(mapperProxyFactory.getMapperInterface(), method, configuration);
                    } else {
                        mapperMethod = new MapperMethod(type, method, configuration);
                    }
                    // 获取mapperMethod的method方法属性
                    MapperMethod.MethodSignature methodSignature = SqlParseUtils.getFieldValue(mapperMethod, "method");
                    // 反射调用，获取methodSignature的paramNameResolver属性
                    ParamNameResolver paramNameResolver = SqlParseUtils.getFieldValue(methodSignature, "paramNameResolver");
                    // 调用 paramNameResolver的hasParamAnnotation方法， data.getFirst()为 boolean 类型
                    // hasParamAnnotation 的作用，设置当前方法有没有使用@Param 注解
                    // 1. 对于普通的查询方法如 selectUserById(Long id) 这种情况，默认data.getFirst(),表示使用@Param注解
                    // 最后生成的 sql 为 select * from user where username = #{username},可以设置hasParamAnnotation值为true
                    // 的好处就是省去了在 selectUserById(Long id) 方法中@Param注解的书写。
                    // 本来要写成 selectUserById(@Param Long id)的，现在只需要写成selectUserById(Long id)
                    // 2. 而对于insert(User user )方法,MyBatis本身是支持这样写的，因此，我们这里设置hasParamAnnotation为 false
                    // 设置 true 和 false 由生成sql时控制
                    // 如果在insert(User user )中也设置hasParamAnnotation为 true
                    // insert(id ,username ,password ) values (user.id,user.username,user.password)
                    // hasParamAnnotation为设置为 false，则生成的 sql 是
                    // insert (id ,username,password ) values (id,username,password),签于方便，我选择了后者
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
        String sql = "SELECT COLUMN_NAME columnName, DATA_TYPE dataType, COLUMN_COMMENT columnComment,COLUMN_KEY columnKey FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + tableName + "'";
        List<TableInfo> tableInfos = jdbcTemplate.query(sql, new TableRowMapper());
        List<String> tableColumns = new ArrayList<>();
        List<String> primaryColumns = new ArrayList<>();
        if (tableInfos != null && tableInfos.size() > 0) {
            for (TableInfo tableInfo : tableInfos) {
                tableColumns.add(tableInfo.getColumnName());
                //
                if ("PRI".equals(tableInfo.getColumnKey())) {           // 获取主键
                    primaryColumns.add(tableInfo.getColumnName());
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
        if (StringUtils.isNotEmpty(tupleInfo.getFourth())) {  //表示需要添加 Mapper
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
