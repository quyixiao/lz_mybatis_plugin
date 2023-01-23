package com.test.mysql;



import com.lz.mybatis.plugin.utils.BeanUtil;
import com.lz.mybatis.plugin.utils.OsUtil;
import com.lz.mybatis.plugin.utils.StringUtils;
import com.test.PMysqlMain;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MysqlUtilTable2Bean {
    /**
     * 打印entity的信息
     */
    public static void printEntity(TablesBean tableBean) {

        boolean hasDate = false;

        List<FieldBean> list = tableBean.getFieldList();

        StringBuffer bf = new StringBuffer();

        String realName = PMysqlMain.pre + tableBean.getSpaceName() + "";


        String fileName = "";


        if (OsUtil.isWindows()) {
            if (StringUtils.isNotBlank(PMysqlMain.bean_path)) {
                fileName = PMysqlMain.bean_path + "/" + realName + ".java";
            } else {
                //fileName = WindowMysqlMain.save_path + "/" + realName + ".java";
                fileName = PMysqlMain.save_path + "/" + realName + ".java";
            }
        } else {
            fileName = PMysqlMain.bean_path + "/" + realName + ".java";
        }

        // 定义声明
        for (FieldBean tb : list) {
            String temp = "";
            temp += "    public final static String " + append_(tb.getField()) + " = \"" + PMysqlMain.bean_package+"." + realName + ":" + tb.getField() + "\";    //" + tb.getComment() + "\n";
            bf.append(temp);
        }

        int i = 0;
        // 定义声明
        for (FieldBean tb : list) {
            String temp = "";
            temp += "    //" + tb.getComment() + "\n";
            if (i == 0) {
                temp += "    @TableId(value = \"" + tb.getField() + "\", type = IdType.AUTO)\n";
            }
            temp += "    private " + tb.getJavaType() + " " + tb.getJavaCode() + ";";
            i++;
            // System.out.println(temp);
            bf.append(temp).append("\n");


            if (!hasDate && "Date".equals(tb.getJavaType())) {
                hasDate = true;
            }
        }


        // 定义get set方法
        for (FieldBean tb : list) {
            String temp = "";
            temp += "    /**\n";
            temp += "     * " + tb.getComment() + " \n";
            temp += "     * @return\n";
            temp += "     */\n";
            temp += "    public " + tb.getJavaType() + " "
                    + tb.getJavaCodeForGet() + "() {\n";
            temp += "        return " + tb.getJavaCode() + ";\n";
            temp += "    }";
            // System.out.println(temp);

            bf.append(temp).append("\n");

            temp = "";
            temp += "    /**\n";
            temp += "     * " + tb.getComment() + " \n";
            temp += "     * @param " + tb.getJavaCode() + "\n";
            temp += "     */\n";
            temp += "    public void " + tb.getJavaCodeForSet() + "("
                    + tb.getJavaType() + " " + tb.getJavaCode() + ") {\n";
            temp += "        this." + tb.getJavaCode() + " = " + tb.getJavaCode()
                    + ";\n";
            temp += "    }\n";
            // System.out.println(temp);
            bf.append(temp).append("\n");
        }


        // 定义get set方法
        for (FieldBean tb : list) {
            if (!tb.getComment().startsWith("枚举")) {
                continue;
            }
            String mo = "Integer";

            bf.append("/** \n" +
                    "* " + tb.getComment() +
                    "*/ \n" +
                    "    public enum " + tb.getField().toUpperCase() + "_ENUM {\n");
            String[] comments = tb.getComment().split(":");
            boolean isStr = false;
            for (int j = 1; j < comments.length; j++) {
                String bs[] = comments[j].split(",");
                if (!StringUtils.checkStrIsNum(bs[1].trim())) {
                    isStr = true;
                }
            }
            for (int j = 1; j < comments.length; j++) {
                String bs[] = comments[j].split(",");
                if (isStr) {
                    if (j == comments.length - 1) {
                        bf.append("        ").append(bs[0].trim() + "(\"" + bs[1].trim() + "\", \"" + bs[2].trim() + "\");\n");
                    } else {
                        bf.append("        ").append(bs[0].trim() + "(\"" + bs[1].trim() + "\", \"" + bs[2].trim() + "\"),\n");
                    }
                } else {
                    if (j == comments.length - 1) {
                        bf.append("        ").append(bs[0].trim() + "(" + bs[1].trim() + ", \"" + bs[2].trim() + "\");\n");
                    } else {
                        bf.append("        ").append(bs[0].trim() + "(" + bs[1].trim() + ", \"" + bs[2].trim() + "\"),\n");
                    }
                }
            }
            if (isStr) {
                mo = "String";
            }

            bf.append("\n");
            bf.append("\n" +
                    "        private " + mo + " code;\n" +
                    "\n" +
                    "        private String desc;\n" +
                    "\n" +
                    "        " + tb.getField().toUpperCase() + "_ENUM(" + mo + " code, String desc) {\n" +
                    "            this.code = code;\n" +
                    "            this.desc = desc;\n" +
                    "        }\n" +
                    "\n" +
                    "        public " + mo + " getCode() {\n" +
                    "            return code;\n" +
                    "        }\n" +
                    "\n" +
                    "        public void setCode(" + mo + " code) {\n" +
                    "            this.code = code;\n" +
                    "        }\n" +
                    "\n" +
                    "        public String getDesc() {\n" +
                    "            return desc;\n" +
                    "        }\n" +
                    "\n" +
                    "        public void setDesc(String desc) {\n" +
                    "            this.desc = desc;\n" +
                    "        }\n" +
                    "    }\n");

        }


        bf.append("\n" +
                "    public " + realName + " copy(Object obj) {\n" +
                "        BeanUtil.copy(obj, this);\n" +
                "        return this;\n" +
                "    }\n");


        // 定义get set方法
        for (FieldBean tb : list) {
            String temp =
                    "    public " + realName + " copy" + BeanUtil.fistToUpperCase(tb.getJavaCode()) + "(Object obj) {\n" +
                            "        BeanUtil.copy(obj, this, \"" + tb.getJavaCode() + "\");\n" +
                            "        return this;\n" +
                            "    }";


            bf.append(temp).append("\n");

            temp = "\n" +
                    "    public " + realName + " copy" + BeanUtil.fistToUpperCase(tb.getJavaCode()) + "(Object obj, Object defaultValue) {\n" +
                    "        BeanUtil.copy(obj, this, \"" + tb.getJavaCode() + "\", defaultValue);\n" +
                    "        return this;\n" +
                    "    }";
            bf.append(temp).append("\n");
        }


        StringBuilder sb = new StringBuilder();

        sb.append("    @Override\n");
        sb.append("    public String toString() {\n");
        sb.append("        return \"" + realName + "{\" +\n");

        for (FieldBean tb : list) {
            String temp = "";
            sb.append("                \"," + tb.getJavaCode() + "=\" + " + tb.getJavaCode() + " +\n");
        }
        sb.append("                \"}\";\n");
        sb.append("    }\n");
        try {

            String content = "package " + PMysqlMain.bean_package + ";\n";
            content += "import com.baomidou.mybatisplus.annotation.IdType;\n" +
                    "import com.baomidou.mybatisplus.annotation.TableId;\n" +
                    "import com.baomidou.mybatisplus.annotation.TableName;\n" +
                    "import com.linzi.pitpat.base.utils.BeanUtil;\n" +
                    "import lombok.Data;\n" +
                    "import com.lz.mybatis.plugin.annotations.AS;\n" +
                    "\n" +
                    "import java.math.BigDecimal;\n" +
                    "import java.util.Date;";
            if (hasDate) {
                content += "import java.util.Date;" + "\n";
            }


            content += "/**\n";
            content += "*" + tableBean.getComment() + "\n";
            content += "* @author quyixiao\n";
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            content += "* @since " + format.format(new Date()) + "\n";
            content += "*/\n";

            content += "\n";
            content += "@Data\n";
            content += "@TableName(" + "\"" + tableBean.getTableName() + "\")\n";
            content += "@AS(\""+getAs(tableBean.getTableName())+"\")\n";


            content += "public class " + realName + " implements java.io.Serializable {\n" + bf.toString();
            content += sb.toString();
            content += "}";

            FileOutputStream fos = new FileOutputStream(fileName);

            Writer out = new OutputStreamWriter(fos, "UTF-8");
            out.write(content);
            out.close();
            fos.close();
            System.out.println("===" + realName + ".java" + "生成");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getAs(String value) {
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
        return sb.toString();
    }



    public static String append_(String value){
        if(!value.contains("_")){
            return value + "_";
        }
        return value;
    }
}
