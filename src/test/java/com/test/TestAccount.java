package com.test;

import com.lz.mybatis.plugin.annotations.By;
import com.lz.mybatis.plugin.annotations.TableId;
import com.lz.mybatis.plugin.annotations.TableName;

import java.util.Date;

/**
 * <p>
 * 菜单权限表
 * </p>*项目用户
 *
 * @author quyixiao
 * @since 2021-01-28
 */


@TableName("lz_test_account")
public class TestAccount implements java.io.Serializable {

    public static final String id_ = "com.test.TestAccount:id";
    public static final String is_delete = "com.test.TestAccount:is_delete";
    public static final String gmt_create = "com.test.TestAccount:gmt_create";
    public static final String gmt_modified = "com.test.TestAccount:gmt_modified";
    public static final String type_ = "com.test.TestAccount:type";
    public static final String account_id = "com.test.TestAccount:account_id";
    public static final String user_id = "com.test.TestAccount:user_id";
    public static final String real_name = "com.test.TestAccount:real_name";
    public static final String mobile_ = "com.test.TestAccount:mobile";
    public static final String username_ = "com.test.TestAccount:username";
    public static final String task_id = "com.test.TestAccount:task_id";
    public static final String staff_id = "com.test.TestAccount:staff_id";


    //主键id
    @TableId
    @By
    private Long id;
    //是否删除
    private Integer isDelete;
    //生成时间
    private Date gmtCreate;
    //修改时间
    private Date gmtModified;
    //0
    private Integer type;
    //版本号
    private Long accountId;
    //真实名称
    private String realName;
    //手机号码
    private String mobile;
    //用户名
    private String username;
    //任务 id
    private Long taskId;
    //员工 id
    private Long staffId;
    private Long userId;


    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public TestAccount() {
    }

    public TestAccount(String mobile, String username) {
        this.mobile = mobile;
        this.username = username;
    }

    /**
     * 主键id
     *
     * @return
     */
    public Long getId() {
        return id;
    }

    /**
     * 主键id
     *
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 是否删除
     *
     * @return
     */
    public Integer getIsDelete() {
        return isDelete;
    }

    /**
     * 是否删除
     *
     * @param isDelete
     */
    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    /**
     * 生成时间
     *
     * @return
     */
    public Date getGmtCreate() {
        return gmtCreate;
    }

    /**
     * 生成时间
     *
     * @param gmtCreate
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * 修改时间
     *
     * @return
     */
    public Date getGmtModified() {
        return gmtModified;
    }

    /**
     * 修改时间
     *
     * @param gmtModified
     */
    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    /**
     * 0
     *
     * @return
     */
    public Integer getType() {
        return type;
    }

    /**
     * 0
     *
     * @param type
     */
    public void setType(Integer type) {
        this.type = type;
    }


    /**
     * 真实名称
     *
     * @return
     */
    public String getRealName() {
        return realName;
    }

    /**
     * 真实名称
     *
     * @param realName
     */
    public void setRealName(String realName) {
        this.realName = realName;
    }

    /**
     * 手机号码
     *
     * @return
     */
    public String getMobile() {
        return mobile;
    }

    /**
     * 手机号码
     *
     * @param mobile
     */
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    /**
     * 用户名
     *
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     * 用户名
     *
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 任务 id
     *
     * @return
     */
    public Long getTaskId() {
        return taskId;
    }

    /**
     * 任务 id
     *
     * @param taskId
     */
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    /**
     * 员工 id
     *
     * @return
     */
    public Long getStaffId() {
        return staffId;
    }

    /**
     * 员工 id
     *
     * @param staffId
     */
    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

}