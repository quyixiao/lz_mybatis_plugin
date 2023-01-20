/*
 * Copyright (c) 2011-2021, baomidou (jobob@qq.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lz.mybatis.plugin.plugins.support;


import com.lz.mybatis.plugin.annotations.OrderBy;
import com.lz.mybatis.plugin.annotations.TableId;
import com.lz.mybatis.plugin.annotations.TableName;
import com.lz.mybatis.plugin.plugins.annotation.KeySequence;
import com.lz.mybatis.plugin.plugins.annotation.TableLogic;
import com.lz.mybatis.plugin.plugins.toolkit.ClassUtils;
import com.lz.mybatis.plugin.plugins.toolkit.CollectionUtils;
import com.lz.mybatis.plugin.plugins.toolkit.ExceptionUtils;
import com.lz.mybatis.plugin.utils.StringUtils;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.reflection.ReflectorFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

/**
 * <p>
 * 实体类反射表辅助类
 * </p>
 *
 * @author hubin sjy
 * @since 2016-09-09
 */
public class TableInfoHelper {

    private static final Log logger = LogFactory.getLog(TableInfoHelper.class);

    /**
     * 储存反射类表信息
     */
    private static final Map<Class<?>, TableInfo> TABLE_INFO_CACHE = new ConcurrentHashMap<>();

    /**
     * 储存表名对应的反射类表信息
     */
    private static final Map<String, TableInfo> TABLE_NAME_INFO_CACHE = new ConcurrentHashMap<>();

    /**
     * 默认表主键名称
     */
    private static final String DEFAULT_ID_NAME = "id";

    /**
     * <p>
     * 获取实体映射表信息
     * </p>
     *
     * @param clazz 反射实体类
     * @return 数据库表反射信息
     */
    public static TableInfo getTableInfo(Class<?> clazz) {
        if (clazz == null || ReflectionKit.isPrimitiveOrWrapper(clazz) || clazz == String.class || clazz.isInterface()) {
            return null;
        }
        // https://github.com/baomidou/mybatis-plus/issues/299
        Class<?> targetClass = ClassUtils.getUserClass(clazz);
        TableInfo tableInfo = TABLE_INFO_CACHE.get(targetClass);
        if (null != tableInfo) {
            return tableInfo;
        }
        //尝试获取父类缓存
        Class<?> currentClass = clazz;
        while (null == tableInfo && Object.class != currentClass) {
            currentClass = currentClass.getSuperclass();
            tableInfo = TABLE_INFO_CACHE.get(ClassUtils.getUserClass(currentClass));
        }
        //把父类的移到子类中来
        if (tableInfo != null) {
            TABLE_INFO_CACHE.put(targetClass, tableInfo);
        }
        return tableInfo;
    }

    /**
     * <p>
     * 根据表名获取实体映射表信息
     * </p>
     *
     * @param tableName 表名
     * @return 数据库表反射信息
     */
    public static TableInfo getTableInfo(String tableName) {
        if (StringUtils.isBlank(tableName)) {
            return null;
        }
        return TABLE_NAME_INFO_CACHE.get(tableName);
    }

    /**
     * <p>
     * 获取所有实体映射表信息
     * </p>
     *
     * @return 数据库表反射信息集合
     */
    public static List<TableInfo> getTableInfos() {
        return Collections.unmodifiableList(new ArrayList<>(TABLE_INFO_CACHE.values()));
    }




    /**
     * <p>
     * 初始化 表数据库类型,表名,resultMap
     * </p>
     *
     * @param clazz        实体类
     * @param globalConfig 全局配置
     * @param tableInfo    数据库表反射信息
     * @return 需要排除的字段名
     */
    private static String[] initTableName(Class<?> clazz, GlobalConfig globalConfig, TableInfo tableInfo) {
        /* 数据库全局配置 */
        GlobalConfig.DbConfig dbConfig = globalConfig.getDbConfig();
        TableName table = clazz.getAnnotation(TableName.class);

        String tableName = clazz.getSimpleName();
        String tablePrefix = dbConfig.getTablePrefix();
        String schema = dbConfig.getSchema();
        boolean tablePrefixEffect = true;
        String[] excludeProperty = null;

        if (table != null) {
            if (StringUtils.isNotBlank(table.value())) {
                tableName = table.value();
                if (StringUtils.isNotBlank(tablePrefix) && !table.keepGlobalPrefix()) {
                    tablePrefixEffect = false;
                }
            } else {
                tableName = initTableNameWithDbConfig(tableName, dbConfig);
            }
            if (StringUtils.isNotBlank(table.schema())) {
                schema = table.schema();
            }
            /* 表结果集映射 */
            if (StringUtils.isNotBlank(table.resultMap())) {
                tableInfo.setResultMap(table.resultMap());
            }
            tableInfo.setAutoInitResultMap(table.autoResultMap());
            excludeProperty = table.excludeProperty();
        } else {
            tableName = initTableNameWithDbConfig(tableName, dbConfig);
        }

        String targetTableName = tableName;
        if (StringUtils.isNotBlank(tablePrefix) && tablePrefixEffect) {
            targetTableName = tablePrefix + targetTableName;
        }
        if (StringUtils.isNotBlank(schema)) {
            targetTableName = schema + StringPool.DOT + targetTableName;
        }

        tableInfo.setTableName(targetTableName);

        /* 开启了自定义 KEY 生成器 */
        if (CollectionUtils.isNotEmpty(dbConfig.getKeyGenerators())) {
            tableInfo.setKeySequence(clazz.getAnnotation(KeySequence.class));
        }
        return excludeProperty;
    }

    /**
     * 根据 DbConfig 初始化 表名
     *
     * @param className 类名
     * @param dbConfig  DbConfig
     * @return 表名
     */
    private static String initTableNameWithDbConfig(String className, GlobalConfig.DbConfig dbConfig) {
        String tableName = className;
        // 开启表名下划线申明
        if (dbConfig.isTableUnderline()) {
            tableName = StringUtils.camelToUnderline(tableName);
        }
        // 大写命名判断
        if (dbConfig.isCapitalMode()) {
            tableName = tableName.toUpperCase();
        } else {
            // 首字母小写
            tableName = StringUtils.firstToLowerCase(tableName);
        }
        return tableName;
    }

    /**
     * <p>
     * 初始化 表主键,表字段
     * </p>
     *
     * @param clazz        实体类
     * @param globalConfig 全局配置
     * @param tableInfo    数据库表反射信息
     */
    private static void initTableFields(Class<?> clazz, GlobalConfig globalConfig, TableInfo tableInfo, List<String> excludeProperty) {
        /* 数据库全局配置 */
        GlobalConfig.DbConfig dbConfig = globalConfig.getDbConfig();
        ReflectorFactory reflectorFactory = tableInfo.getConfiguration().getReflectorFactory();
        Reflector reflector = reflectorFactory.findForClass(clazz);
        List<Field> list = getAllFields(clazz);
        // 标记是否读取到主键
        boolean isReadPK = false;
        // 是否存在 @TableId 注解
        boolean existTableId = isExistTableId(list);
        // 是否存在 @TableLogic 注解
        boolean existTableLogic = isExistTableLogic(list);
        // 是否存在 @OrderBy 注解
        boolean existOrderBy = isExistOrderBy(list);

        List<TableFieldInfo> fieldList = new ArrayList<>(list.size());
        for (Field field : list) {
            if (excludeProperty.contains(field.getName())) {
                continue;
            }

            /* 主键ID 初始化 */
            if (existTableId) {
                TableId tableId = field.getAnnotation(TableId.class);
                if (tableId != null) {
                    if (isReadPK) {
                        throw ExceptionUtils.mpe("@TableId can't more than one in Class: \"%s\".", clazz.getName());
                    } else {
                        initTableIdWithAnnotation(dbConfig, tableInfo, field, tableId, reflector);
                        isReadPK = true;
                        continue;
                    }
                }
            } else if (!isReadPK) {
                isReadPK = initTableIdWithoutAnnotation(dbConfig, tableInfo, field, reflector);
                if (isReadPK) {
                    continue;
                }
            }
            final TableField tableField = field.getAnnotation(TableField.class);

            /* 有 @TableField 注解的字段初始化 */
            if (tableField != null) {
                fieldList.add(new TableFieldInfo(dbConfig, tableInfo, field, tableField, reflector, existTableLogic, existOrderBy));
                continue;
            }

            /* 无 @TableField  注解的字段初始化 */
            fieldList.add(new TableFieldInfo(dbConfig, tableInfo, field, reflector, existTableLogic, existOrderBy));
        }

        /* 字段列表 */
        tableInfo.setFieldList(fieldList);

        /* 未发现主键注解，提示警告信息 */
        if (!isReadPK) {
            logger.warn(String.format("Can not find table primary key in Class: \"%s\".", clazz.getName()));
        }
    }

    /**
     * <p>
     * 判断主键注解是否存在
     * </p>
     *
     * @param list 字段列表
     * @return true 为存在 @TableId 注解;
     */
    public static boolean isExistTableId(List<Field> list) {
        return list.stream().anyMatch(field -> field.isAnnotationPresent(TableId.class));
    }

    /**
     * <p>
     * 判断逻辑删除注解是否存在
     * </p>
     *
     * @param list 字段列表
     * @return true 为存在 @TableId 注解;
     */
    public static boolean isExistTableLogic(List<Field> list) {
        return list.stream().anyMatch(field -> field.isAnnotationPresent(TableLogic.class));
    }

    /**
     * <p>
     * 判断排序注解是否存在
     * </p>
     *
     * @param list 字段列表
     * @return true 为存在 @TableId 注解;
     */
    public static boolean isExistOrderBy(List<Field> list) {
        return list.stream().anyMatch(field -> field.isAnnotationPresent(OrderBy.class));
    }

    /**
     * <p>
     * 主键属性初始化
     * </p>
     *
     * @param dbConfig  全局配置信息
     * @param tableInfo 表信息
     * @param field     字段
     * @param tableId   注解
     * @param reflector Reflector
     */
    private static void initTableIdWithAnnotation(GlobalConfig.DbConfig dbConfig, TableInfo tableInfo,
                                                  Field field, TableId tableId, Reflector reflector) {
        boolean underCamel = tableInfo.isUnderCamel();
        final String property = field.getName();
        if (field.getAnnotation(TableField.class) != null) {
            logger.warn(String.format("This \"%s\" is the table primary key by @TableId annotation in Class: \"%s\",So @TableField annotation will not work!",
                    property, tableInfo.getEntityType().getName()));
        }
        /* 主键策略（ 注解 > 全局 ） */
        // 设置 Sequence 其他策略无效
        //if (IdType.NONE == tableId.type()) {
            tableInfo.setIdType(dbConfig.getIdType());
        //} else {
//            tableInfo.setIdType(tableId.type());
//        }

        /* 字段 */
        String column = property;
        if (StringUtils.isNotBlank(tableId.value())) {
            column = tableId.value();
        } else {
            // 开启字段下划线申明
            if (underCamel) {
                column = StringUtils.camelToUnderline(column);
            }
            // 全局大写命名
            if (dbConfig.isCapitalMode()) {
                column = column.toUpperCase();
            }
        }
        final Class<?> keyType = reflector.getGetterType(property);
        if (keyType.isPrimitive()) {
            logger.warn(String.format("This primary key of \"%s\" is primitive !不建议如此请使用包装类 in Class: \"%s\"",
                    property, tableInfo.getEntityType().getName()));
        }
        tableInfo.setKeyRelated(checkRelated(underCamel, property, column))
                .setKeyColumn(column)
                .setKeyProperty(property)
                .setKeyType(keyType);
    }

    /**
     * <p>
     * 主键属性初始化
     * </p>
     *
     * @param tableInfo 表信息
     * @param field     字段
     * @param reflector Reflector
     * @return true 继续下一个属性判断，返回 continue;
     */
    private static boolean initTableIdWithoutAnnotation(GlobalConfig.DbConfig dbConfig, TableInfo tableInfo,
                                                        Field field, Reflector reflector) {
        final String property = field.getName();
        if (DEFAULT_ID_NAME.equalsIgnoreCase(property)) {
            if (field.getAnnotation(TableField.class) != null) {
                logger.warn(String.format("This \"%s\" is the table primary key by default name for `id` in Class: \"%s\",So @TableField will not work!",
                        property, tableInfo.getEntityType().getName()));
            }
            String column = property;
            if (dbConfig.isCapitalMode()) {
                column = column.toUpperCase();
            }
            final Class<?> keyType = reflector.getGetterType(property);
            if (keyType.isPrimitive()) {
                logger.warn(String.format("This primary key of \"%s\" is primitive !不建议如此请使用包装类 in Class: \"%s\"",
                        property, tableInfo.getEntityType().getName()));
            }
            tableInfo.setKeyRelated(checkRelated(tableInfo.isUnderCamel(), property, column))
                    .setIdType(dbConfig.getIdType())
                    .setKeyColumn(column)
                    .setKeyProperty(property)
                    .setKeyType(keyType);
            return true;
        }
        return false;
    }

    /**
     * 判定 related 的值
     * <p>
     * 为 true 表示不符合规则
     *
     * @param underCamel 驼峰命名
     * @param property   属性名
     * @param column     字段名
     * @return related
     */
    public static boolean checkRelated(boolean underCamel, String property, String column) {
        column = StringUtils.getTargetColumn(column);
        String propertyUpper = property.toUpperCase(Locale.ENGLISH);
        String columnUpper = column.toUpperCase(Locale.ENGLISH);
        if (underCamel) {
            // 开启了驼峰并且 column 包含下划线
            return !(propertyUpper.equals(columnUpper) ||
                    propertyUpper.equals(columnUpper.replace(StringPool.UNDERSCORE, StringPool.EMPTY)));
        } else {
            // 未开启驼峰,直接判断 property 是否与 column 相同(全大写)
            return !propertyUpper.equals(columnUpper);
        }
    }

    /**
     * <p>
     * 获取该类的所有属性列表
     * </p>
     *
     * @param clazz 反射类
     * @return 属性集合
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fieldList = ReflectionKit.getFieldList(ClassUtils.getUserClass(clazz));
        return fieldList.stream()
                .filter(field -> {
                    /* 过滤注解非表字段属性 */
                    TableField tableField = field.getAnnotation(TableField.class);
                    return (tableField == null || tableField.exist());
                }).collect(toList());
    }


}
