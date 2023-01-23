package com.test;


import com.lz.mybatis.plugin.utils.StringUtils;
import com.test.mysql.*;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/***
 * 预发布环境回调接口地址：
 https://pre-callback.sinawallent.com/platform/callback
 https://pre-callback.sinawallent.com/platformweb/query
 */
public class PMysqlMain {

    public static String package_name = "com.api.model.entity";
    public static String package_name_model = "com.chengyi.user.dao";

    public static String save_path = "/Users/zhy/logs";

    public static String bean_path = "";
    public static String mapper_path = "";
    public static String mapper_xml_path = "";
    public static String service_path = "";
    public static String service_impl_path = "";


    public static String bean_package = "";
    public static String dao_package = "";
    public static String service_package = "";
    public static String service_impl_package = "";

    public static String mysql_url = "jdbc:mysql://115.236.186.82:4106?zeroDateTimeBehavior=convertToNull&serverTimezone=GMT%2B8&useSSL=false";
    public static String pre = "";
    public static String mysql_dbname = "pitpat";
    public static String mysql_username = "pitpat";
    public static String mysql_password = "pitpat";

    public static void initApi(String package_name) throws Exception {

        String path = ResourceUtils.getURL("classpath:").getPath();
        System.out.println("=========" + path);

        String[] a = path.split("pitpat-common");
        System.out.println(a[0]);
        PMysqlMain.bean_package = "com.linzi.pitpat.data.entity." + package_name;
        PMysqlMain.dao_package = "com.linzi.pitpat.data.dao." + package_name;
        PMysqlMain.service_package = "com.linzi.pitpat.data.service." + package_name;
        PMysqlMain.service_impl_package = "com.linzi.pitpat.data.service.impl." + package_name;

        PMysqlMain.bean_path = a[0] + "pitpat-common/pitpat-data/src/main/java/com/linzi/pitpat/data/entity/" + package_name;
        PMysqlMain.mapper_path = a[0] + "pitpat-common/pitpat-data/src/main/java/com/linzi/pitpat/data/dao/" + package_name;
        PMysqlMain.mapper_xml_path = a[0] + "pitpat-common/pitpat-data/src/main/resources/mapper/" + package_name;
        PMysqlMain.service_path = a[0] + "pitpat-common/pitpat-data/src/main/java/com/linzi/pitpat/data/service/" + package_name;
        PMysqlMain.service_impl_path = a[0] + "pitpat-common/pitpat-data/src/main/java/com/linzi/pitpat/data/service/impl/" + package_name;

        for (String file : Arrays.asList(PMysqlMain.bean_path, PMysqlMain.mapper_path, PMysqlMain.mapper_xml_path, PMysqlMain.service_path, PMysqlMain.service_impl_path)) {
            File file1 = new File(file);
            if (!file1.exists()) {
                file1.mkdirs();
            }
        }
    }

    @Test
    public void testInsert() throws Exception {
        String path = ResourceUtils.getURL("classpath:").getPath();
        System.out.println(path);
        String dir = null;
        if (StringUtils.isNotBlank(path)) {
            dir = path.split("target")[0];
        }
        PMysqlMain.save_path = dir + "src/test/tmp";
        String packageName = "activity";
        initApi(packageName);
        System.out.println(PMysqlMain.save_path);
        List<TablesBean> list = new ArrayList<TablesBean>();

        list.add(new TablesBean("zns_message_task"));
        list.add(new TablesBean("zns_message_task_msg"));
        list.add(new TablesBean("zns_message_task_user"));

        List<TablesBean> list2 = new ArrayList<TablesBean>();
        Map<String, String> map = MysqlUtil2ShowCreateTable.getComments();

        for (int i = 0; i < list.size(); i++) {
            TablesBean obj = list.get(i);
            String tableName = list.get(i).getTableName();
            List<FieldBean> itemList = MysqlUtil2ShowCreateTable.readTableDetail(tableName);
            obj.setFieldList(itemList);
            obj.setComment(map.get(tableName));
            list2.add(obj);
        }

        for (int i = 0; i < list2.size(); i++) {
            MysqlUtilTable2Bean.printEntity(list2.get(i));
        }

   /*     for (int i = 0; i < list2.size(); i++) {
            MysqlUtilTable2Contoller.printController(list2.get(i));
        }*/

        for (int i = 0; i < list2.size(); i++) {
            MysqlUtilTable2Mapper.printDao(list2.get(i));
        }

        for (int i = 0; i < list2.size(); i++) {
            MysqlUtilTable2Service.printService(list2.get(i));
            MysqlUtilTable2Service.printServiceImpl(list2.get(i));
        }

        for (int i = 0; i < list2.size(); i++) {
            MysqlUtilTable2XML.printXMLForMap(list2.get(i));
        }
    }

    @Test
    public void testUpdateEntity() throws Exception {
        String path = ResourceUtils.getURL("classpath:").getPath();
        System.out.println(path);
        String dir = null;
        if (StringUtils.isNotBlank(path)) {
            dir = path.split("target")[0];
        }
        String old = dir + "src/test/tmp";
        PMysqlMain.save_path = dir + "src/test/tmp";
        System.out.println(PMysqlMain.save_path);

        String packageName = "activity";
        initApi(packageName);

        String entityPath = dir + "/src/main/java/com/linzi/pitpat/api/entity";
        List<String> resultFileName = new ArrayList<>();
        File file = new File(entityPath);
        ergodic(file, resultFileName);

        Map<String, String> fileNameMap = new HashMap<>();
        for (String fileName : resultFileName) {
            System.out.println(fileName);
        }
        List<TablesBean> list = new ArrayList<TablesBean>();
        list.add(new TablesBean("zns_coupon"));
        Map<String, String> map = MysqlUtil2ShowCreateTable.getComments();
        for (int i = 0; i < list.size(); i++) {
            TablesBean obj = list.get(i);
            String tableName = list.get(i).getTableName();
            System.out.println("---------" + tableName);
            List<FieldBean> itemList = MysqlUtil2ShowCreateTable.readTableDetail(tableName);
            obj.setFieldList(itemList);
            obj.setComment(map.get(tableName));
            for (String fileName : resultFileName) {
                String className = fileName.replaceAll("\\.java", "");
                try {
                    System.out.println("xxxxxxxxxxxxxxxxxxxxxxx:" + className);
                    Class clazz = Class.forName(className);
                    String annTableName = getAnnotationValueByTypeName(clazz, "TableName");
                    if (StringUtils.isNotBlank(annTableName) && tableName.equals(annTableName)) {
                        System.out.println("annotationName is = " + annTableName);
                        String fileNames[] = className.split("\\.");
                        String xx = fileNames[fileNames.length - 1];
                        String paxx = className.replace("." + xx, "");
                        System.out.println(paxx);
                        PMysqlMain.package_name = paxx;
                        PMysqlMain.save_path = dir + "src/main/java/" + paxx.replaceAll("\\.", "/");
                        System.out.println("----------" + PMysqlMain.save_path);
                        break;
                    } else {
                        PMysqlMain.save_path = old;
                    }
                } catch (ClassNotFoundException e) {
                    System.out.println("=============" + className);
                    e.printStackTrace();

                }
            }
            MysqlUtilTable2Bean.printEntity(obj);
        }

    }

    public static <T> T getAnnotationValueByTypeName(Class type, String name) {
        Annotation[] annotations = type.getAnnotations();
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (name.equals(getAnnotationName(annotation))) {
                    return getAnnotationValue(annotation);
                }
            }
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

    public static List<String> ergodic(File file, List<String> resultFileName) {
        File[] files = file.listFiles();
        if (files == null) return resultFileName;// 判断目录下是不是空的
        for (File f : files) {
            if (f.isDirectory()) {// 判断是否文件夹
                ergodic(f, resultFileName);// 调用自身,查找子目录
            } else {
                String path = f.getPath();
                String paths[] = path.split("/src/main/java/");
                String className = paths[1].replaceAll("/", ".");
                resultFileName.add(className);
            }
        }
        return resultFileName;
    }


}
