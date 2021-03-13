import com.lz.mybatis.plugin.utils.SqlParseUtils;

public class Test {

    public static void main(String[] args) {
        String sql = "<script>1111 \n" +
                "        insert into lz_test_user(\n" +
                "        <trim suffixOverrides=\",\">\n" +
                "            <if test=\"id != null\">id, </if>\n" +
                "            <if test=\"isDelete != null\">is_delete, </if>\n" +
                "            <if test=\"gmtCreate != null\">gmt_create, </if>\n" +
                "            <if test=\"gmtModified != null\">gmt_modified, </if>\n" +
                "            <if test=\"type != null\">type, </if>\n" +
                "            <if test=\"branchId != null\">branch_id, </if>\n" +
                "            <if test=\"realName != null and realName != '' \">real_name, </if>\n" +
                "            <if test=\"mobile != null and mobile != '' \">mobile, </if>\n" +
                "            <if test=\"username != null and username != '' \">username, </if>\n" +
                "            <if test=\"taskId != null\">task_id, </if>\n" +
                "            <if test=\"staffId != null\">staff_id, </if>\n" +
                "        </trim>\n" +
                "        )values(\n" +
                "        <trim suffixOverrides=\",\">\n" +
                "            <if test=\"id != null\">#{id}, </if>\n" +
                "            <if test=\"isDelete != null\">#{isDelete}, </if>\n" +
                "            <if test=\"gmtCreate != null\">#{gmtCreate}, </if>\n" +
                "            <if test=\"gmtModified != null\">#{gmtModified}, </if>\n" +
                "            <if test=\"type != null\">#{type}, </if>\n" +
                "            <if test=\"branchId != null\">#{branchId}, </if>\n" +
                "            <if test=\"realName != null and realName != '' \">#{realName}, </if>\n" +
                "            <if test=\"mobile != null and mobile != '' \">#{mobile}, </if>\n" +
                "            <if test=\"username != null and username != '' \">#{username}, </if>\n" +
                "            <if test=\"taskId != null\">#{taskId}, </if>\n" +
                "            <if test=\"staffId != null\">#{staffId}, </if>\n" +
                "        </trim>\n" +
                "        )\n" +
                "11111</script>";
        System.out.println(SqlParseUtils.removeScript(sql));
    }

}
