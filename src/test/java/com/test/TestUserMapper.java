package com.test;

import com.lz.mybatis.plugin.annotations.*;
import com.lz.mybatis.plugin.entity.Page;
import com.lz.mybatis.plugin.entity.UserInfo;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface TestUserMapper extends MyBaseMapper<TestUser> {

    //所有的查询条件，默认是 AND 和 = 关系，如果想在其他的关系，可以写相关的注解@OR ，或@Like
    TestUser selectTestUserById(Long id);

    List<TestUser> selectByUserNameMobile(String username, @OR String mobile);

    @LIMIT(10)
    List<TestUser> selectUserByRealName(@LIKE String realName, @LLIKE String mobile);



    @LIMIT(10)
    List<TestUser> selectUserByRealNameObject1(@LIKE String realName,@LBracket @LLIKE String mobile ,@OR @RBracket String password);



    @LIMIT(10)
    List<TestUser> selectUserByRealNameObject2(@LIKE String realName,@LBracket @LLIKE String mobile , UserInfo userInfo );

    @LIMIT(10)
    List<TestUser> selectUserByRealNameObject3(@LIKE String realName, @LLIKE String mobile , UserInfo userInfo );




    //对于这种情况 taskId 和 staffId 传入的值可以是 null
    List<TestUser> selectByTaskId(@IsNull Long taskId, @IsNotNull Long staffId);

    List<TestUser> selectAll();

    List<TestUser> selectByIF(@IFNull Long companyId);

    List<TestUser> selectByIN(@IN Long companyId);

    Page<TestUser> selectPage(String username, String mobile, @CurrPage int currPage, @PageSize int pageSize);

    List<TestUser> selectByTaskRealNameMobile(@IsNotEmpty String mobile, @IsEmpty String realName);

    @Order({
            @By(value = {"id", "mobile"}, type = OrderType.DESC),
            @By(value = {"username"}, type = OrderType.ASC),
    })
    List<TestUser> selectUserBy(String username, @LIKE String mobile, @OrderBy("id") String asc);

    int countUser(@LIKE String realName);

    Long insertTestUser(TestUser testUser);

    //如果id存在则更新，如果不存在，则插入
    Long insertOrUpdateTestUser(TestUser testUser);

    Long insertTestUser2(@Param("user") TestUser testUser);

    Long insertBatchTestUser(List<TestUser> testUsers);

    Long insertTestUserBatch(TestUser[] testUsers);

    //目前不支持批量更新
    int updateCoverTestUserById(TestUser testUser);

    //目前不支持批量更新
    int updateTestUserById(TestUser testUser);

    //默认使用最后一个作为更新条件
    int updateRealNameById(String realName, Long id);

    //根据用户批量更新
    void updateBatchTestUser(List<TestUser> users);

    //根据数组批量更新数据
    void updateBatchArray(TestUser[] toArray);

    //如果想写多个更新条件，在字段前面加 @by注解，值得注意的是，所有的方法参数名称都应该和数据库中的字段对应，在自动生成 sql时，
    // 会将驼峰参数名转化为数据库字段
    void updateTestUserUserNamePassword(String username, String mobile, @By Long id, @By Long taskId);

    @Realy
    int deleteTestUserById(Long id);

    // @In注解中的值，对应数据库列字段
    List<String> selectTestUserByIds(String userName, @IN @Row("id") List<TestUser> id);

    // @In注解中的值，对应数据库列字段
    int deleteTestUserByIds(@IN("id") List<Long> ids);

    //【注意】千万不能这样写，这样写的话，是删除所有的数据
    void deleteBatch();

    List<TestUser> selectByUserNameMobile1(String username, @IF String mobile);

    void testBatchUpdate(@Param("sql") String sql);

    void testBatchUpdatexx(List<TestUser> testUsers);

    // @Order({@By(value = "id" ,type = OrderType.DESC)})
    @OrderByIdDesc
    List<TestUser> selectActGoldCoinByActAccountTypeStatusList(Long actAccountId, @IF Integer type, List<Integer> status);

    int updateUserAmount(@Sub int amount, Long id);

    void updateCurRedPrtInvalidRedPrtById(@Sub BigDecimal curRedPrt, @Plus BigDecimal invalidRedPrt, Long id);


    List<TestUser> selectCompanyVisibleByCompanyId(Long companyId);



    @Mapping(value = "t.*,t1.account_id as AccountId,t2.borrow_id as bowrowId")
    @LeftJoinOns({
            @Item(value = TestAccount.class, as = "t1", on = "t.account_id = t1.id"),
            @Item(value = TestBorrow.class, as = "t2", on = "t.borrow_id = t2.id")})
    List<TestUser> selectUserAccountBorrowByLeftJoinOnsOld(@AS("t2") Long companyId);



    @Mapping(value = {"t.*", TestAccount.account_id, TestBorrow.borrow_id}, as = {"_", "accountId", "borrowId"})
    @LeftJoinOns({
            @Item(value = TestAccount.class, as = "t1", on = "t.account_id = t1.id"),
            @Item(value = TestBorrow.class, as = "t2", on = "t.borrow_id = t2.id")})
    List<TestUser> selectUserAccountBorrowByLeftJoinOnsNew(@Column(TestBorrow.company_id) Long companyId);



    @Mapping(value = {"t.*", TestAccount.account_id, TestBorrow.borrow_id},
            mk = {
                    @MK(key = TestAccount.account_id, value = "AccountId"),
                    @MK(key = TestBorrow.borrow_id, value = "bowrowId")
            })
    @LeftJoinOns({
            @Item(value = TestAccount.class, as = "t1", on = "t.account_id = t1.id"),
            @Item(value = TestBorrow.class, as = "t2", on = "t.borrow_id = t2.id")})
    List<TestUser> selectUserAccountBorrowByLeftJoinOnsNew1(@AS("t2") Long companyId);




    @Mapping(value = {"t.*", TestAccount.account_id, TestBorrow.borrow_id}, as = {"_", "accountId", "borrowId"})
    @LeftJoinOns({
            @Item(value = TestAccount.class, left = TestAccount.user_id, right = TestUser.id_),
            @Item(value = TestBorrow.class, left = TestBorrow.user_id, right = TestUser.id_)})
    List<TestUser> selectUserAccountBorrowByLeftJoinOnsNew2(@Column(TestBorrow.company_id) Long companyId);


    @Froms({
            @Item(value = TestAccount.class, as = "t1"),
            @Item(value = TestBorrow.class, as = "t2")})
    //@Where("t.account_id = t1.id and t.borrow_id=t2.id")
    @Where(condition = {
            @Item(left = TestAccount.user_id,opt = OptType.EQ, right = "1"),
            @Item(left = TestBorrow.mobile_,opt = OptType.EQ, right = "'18458591xx'")
    })
    List<TestUser> selectUserAccountBorrowByFrom(@Column("t1.companyxx") @IF Long companyId, @IF Long brrowId, @IF @IsNotNull String userName);


    @Max(TestUser.id_)
    BigDecimal selectUserAccountBorrowByMax( Long companyId,@OR@LBracket Long brrowId,@OR  String username,@RBracket @IF @IsNotNull String userName);


    @Max(TestUser.id_)
    @LeftJoinOns({
            @Item(value = TestAccount.class,left = TestAccount.account_id, right = TestUser.id_),
            @Item(value = TestBorrow.class, left = TestBorrow.borrow_id, right = TestUser.id_)})
    BigDecimal selectUserAccountBorrowByMax1(@Column @IF Long companyId, @IF Long brrowId, @IF @IsNotNull String userName);


    @Count
    Long selectUserAccountByCount(@Column(TestUser.branch_id) @IF Long companyId, @IF Long brrowId, @IF @IsNotNull String userName);


    @CountDistinct(TestUser.id_)
    Long selectUserAccountByCountNew(@Column(TestUser.branch_id) @IF Long companyId, @IF Long brrowId, @IF @IsNotNull String userName);


    @LeftJoinOns({
            @Item(value = TestAccount.class, as = "t1", on = "t.account_id = t1.id"),
            @Item(value = TestBorrow.class, as = "t2", on = "t.borrow_id = t2.id")})
    List<TestUser> selectPageInfo(Page page, @AS("t2") String userName);


    @OrderByIdDesc
    @OrderBy({" a.id desc ", "b.id asc "})
    @Order({
            @By(value = {TestUser.id_, TestUser.mobile_}, type = OrderType.DESC),
            @By(value = {TestUser.username_}, type = OrderType.ASC),
    })
    List<MyUserPhone> selectPageInfoXXX(Page page, MyUserPhone userPhone, @OrderBy("xx") String sort);



    @GroupBy(TestUser.username_)
    int countByProductIdGroupByUserId(@DateFormat("%Y-%m-%d") Date gmtCreate, Long productId, @IsNotEmpty String userId);

    int countByProductIdGroupByUserIdxxxxxx(@DateFormat("%Y-%m-%d") Date gmtCreate, Long productId, @IsNotEmpty String userId);


}