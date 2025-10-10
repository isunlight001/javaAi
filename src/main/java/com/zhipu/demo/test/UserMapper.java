package com.zhipu.demo.test;

import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper {
    
    @Select("SELECT * FROM connection_pool_test")
    List<User> findAll();
}