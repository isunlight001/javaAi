package com.zhipu.demo.test;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BatchUserMapper {
    
    /**
     * 批量插入用户数据
     * @param users 用户列表
     * @return 插入的记录数
     */
    @Insert("<script>" +
            "INSERT INTO batch_user (name, age, email) VALUES " +
            "<foreach collection='users' item='user' separator=','>" +
            "(#{user.name}, #{user.age}, #{user.email})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("users") List<BatchUser> users);
}