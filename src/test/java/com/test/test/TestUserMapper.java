package com.test.test;

import com.lz.mybatis.plugin.annotations.*;
import com.lz.mybatis.plugin.entity.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TestUserMapper extends MyBaseMapper<TestUser> {

    //所有的查询条件，默认是 AND 和 = 关系，如果想在其他的关系，可以写相关的注解@OR ，或@Like
    TestUser selectTestUserById(Long id);

    List<TestUser> selectByUserNameMobile(String username, @OR String mobile);


    @LIMIT(10)
    List<TestUser> selectUserByRealName(@LIKE String realName, @LLIKE String mobile);

    //对于这种情况 taskId 和 staffId 传入的值可以是 null
    List<TestUser> selectByTaskId(@IsNull Long taskId, @IsNotNull Long staffId);

    List<TestUser> selectAll();

    Page<TestUser> selectPage(String username, String mobile, @CurrPage int currPage, @PageSize int pageSize);

    List<TestUser> selectByTaskRealNameMobile(@IsNotEmpty String mobile, @IsEmpty String realName);

    @Order({
            @By(value = {"id","mobile"}, type = OrderType.DESC),
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
    int deleteTestUserByIds(@IN("id") List<Long> ids);

    //【注意】千万不能这样写，这样写的话，是删除所有的数据
    void deleteBatch();

    List<TestUser> selectByUserNameMobile1(String username, String mobile);

    void testBatchUpdate(@Param("sql") String sql);

    void testBatchUpdatexx(List<TestUser> testUsers);

    @OrderByIdDesc
    List<TestUser> selectActGoldCoinByActAccountTypeStatusList(Long actAccountId,@IF Integer type,List<Integer> status);

    int updateUserAmount(@Subtract int amount, Long id);

}