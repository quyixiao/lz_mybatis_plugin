package com.test.mysql;




import com.lz.mybatis.plugin.utils.StringUtils;
import com.test.PMysqlMain;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MysqlUtilTable2Service {


    public static String TAB = "	";

    public static void printService(TablesBean tableBean) {
        String realName = PMysqlMain.pre + tableBean.getSpaceName();
        String realName2 = PMysqlMain.pre + tableBean.getSpaceName() + "";
        String fileName = PMysqlMain.save_path + "/" + realName + "Service.java";


        if (StringUtils.isNotBlank(PMysqlMain.service_path)) {
            fileName = PMysqlMain.service_path + "/" + realName + "Service.java";
        }


        try {
            String content = "package " + PMysqlMain.service_package + ";\n";


            content += "/**\n";
            content += "* <p>\n";
            content += "* " + tableBean.getComment() + " 服务类\n";
            content += "* </p>\n";
            content += "*\n";
            content += "* @author quyixiao\n";
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            content += "* @since " + format.format(new Date()) + "\n";
            content += "*/\n";

            content += "import com.baomidou.mybatisplus.extension.service.IService;\n" +
                    "import " + PMysqlMain.bean_package + "." + tableBean.getSpaceName() + ";\n";
            content += "public interface " + tableBean.getSpaceName() + "Service extends IService<" + tableBean.getSpaceName() + "> {\n";
            content += "\n";
            content += "\n";
            content += "\n";


            content += TAB + realName + " select" + realName + "ById(Long id);";
            content += "\n";
            content += "\n";
            content += "\n";

            content += TAB + "Long insert" + realName + "(" + realName + " " + tableBean.getJavaName() + ");";
            content += "\n";
            content += "\n";
            content += "\n";


            content += TAB + "Long insertOrUpdate" + realName + "(" + realName + " " + tableBean.getJavaName() + ");";
            content += "\n";
            content += "\n";
            content += "\n";

            content += TAB + "int update" + realName + "ById(" + realName + " " + tableBean.getJavaName() + ");";
            content += "\n";
            content += "\n";
            content += "\n";

            content += TAB + "int updateCover" + realName + "ById(" + realName + " " + tableBean.getJavaName() + ");";
            content += "\n";
            content += "\n";
            content += "\n";

            content += TAB + "int delete" + tableBean.getSpaceName() + "ById(Long id);";
            content += "\n";
            content += "\n";
            content += "\n";


            content += "}";

            FileOutputStream fos = new FileOutputStream(fileName);
            Writer out = new OutputStreamWriter(fos, "UTF-8");
            out.write(content);
            out.close();
            fos.close();

            System.out.println("===" + realName + "service.java" + "生成");
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            // FileWriter writer = new FileWriter(fileName, false);
            // writer.write(content);
            // writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String captureName(String name) {
        if (StringUtils.isNotBlank(name) && name.length() > 0) {
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        return name;

    }

    public static void printServiceImpl(TablesBean tableBean) {
        String realName = PMysqlMain.pre + tableBean.getSpaceName();

        String realName2 = PMysqlMain.pre + tableBean.getSpaceName() + "";

        String javaName = "" + captureName(tableBean.getJavaName());
        String fileName = PMysqlMain.save_path + "/" + realName + "ServiceImpl.java";


        if (StringUtils.isNotBlank(PMysqlMain.service_impl_path)) {
            fileName = PMysqlMain.service_impl_path + "/" + realName + "ServiceImpl.java";
        }

        try {
            String content = "package " + PMysqlMain.service_impl_package + ";\n";


            content += "/**\n";
            content += "* <p>\n";
            content += "* " + tableBean.getComment() + " 服务类\n";
            content += "* </p>\n";
            content += "*\n";
            content += "* @author quyixiao\n";
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            content += "* @since " + format.format(new Date()) + "\n";
            content += "*/\n";

            content += "\n";
            content += "import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;\n" +
                    "import " + PMysqlMain.dao_package + "." + tableBean.getSpaceName() + "Dao;\n" +
                    "import " + PMysqlMain.bean_package + "." + tableBean.getSpaceName() + ";\n" +
                    "import " + PMysqlMain.service_package + "." + tableBean.getSpaceName() + "Service;\n" +
                    "import org.springframework.beans.factory.annotation.Autowired;\n" +
                    "import org.springframework.stereotype.Service;\n";

            content += "@Service\n";
            content += "public class " + tableBean.getSpaceName() + "ServiceImpl extends ServiceImpl<" + tableBean.getSpaceName() + "Dao, " + tableBean.getSpaceName() + "> implements " + tableBean.getSpaceName() + "Service {\n";
            content += "\n";
            content += "\n";
            content += "    @Autowired\n";
            content += "\tprivate " + realName + "Dao " + MysqlUtil.getFirstToLower(javaName) + "Dao;" + "\n";
            content += "\n";
            content += "\n";
            content += "\n";

            content += TAB + "@Override\n";
            content += TAB + "public " + realName + " select" + realName + "ById(Long id){\n";
            content += TAB + TAB + "return " + tableBean.getJavaName() + "Dao." + "select" + realName + "ById(id);\n";
            content += TAB + "}\n";
            content += "\n";
            content += "\n";
            content += "\n";
            content += TAB + "@Override\n";
            content += TAB + "public Long insert" + realName + "(" + realName + " " + tableBean.getJavaName() + "){\n";
            content += TAB + TAB + "return " + tableBean.getJavaName() + "Dao." + "insert" + realName + "(" + tableBean.getJavaName() + ");\n";
            content += TAB + "}\n";
            content += "\n";
            content += "\n";
            content += "\n";


            content += TAB + "@Override\n";
            content += TAB + "public Long insertOrUpdate" + realName + "(" + realName + " " + tableBean.getJavaName() + "){\n";
            content += TAB + TAB + "return " + tableBean.getJavaName() + "Dao." + "insertOrUpdate" + realName + "(" + tableBean.getJavaName() + ");\n";
            content += TAB + "}\n";
            content += "\n";
            content += "\n";
            content += "\n";


            content += TAB + "@Override\n";
            content += TAB + "public int update" + realName + "ById(" + realName + " " + tableBean.getJavaName() + "){\n";
            content += TAB + TAB + "return " + tableBean.getJavaName() + "Dao." + "update" + realName + "ById(" + tableBean.getJavaName() + ");\n";
            content += TAB + "}\n";

            content += "\n";
            content += "\n";
            content += "\n";
            content += TAB + "@Override\n";
            content += TAB + "public int updateCover" + realName + "ById(" + realName + " " + tableBean.getJavaName() + "){\n";
            content += TAB + TAB + "return " + tableBean.getJavaName() + "Dao." + "updateCover" + realName + "ById(" + tableBean.getJavaName() + ");\n";
            content += TAB + "}\n";
            content += "\n";
            content += "\n";
            content += "\n";
            content += TAB + "@Override\n";
            content += TAB + "public int delete" + tableBean.getSpaceName() + "ById(Long id){\n";
            content += TAB + TAB + "return " + tableBean.getJavaName() + "Dao." + "delete" + tableBean.getSpaceName() + "ById(id);\n";
            content += TAB + "}\n";
            content += "\n";
            content += "\n";
            content += "\n";


            content += "}" + "\n";
            FileOutputStream fos = new FileOutputStream(fileName);
            Writer out = new OutputStreamWriter(fos, "UTF-8");
            out.write(content);
            out.close();
            fos.close();

            System.out.println("===" + realName + "ServiceImpl.java" + "生成");
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            // FileWriter writer = new FileWriter(fileName, false);
            // writer.write(content);
            // writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
