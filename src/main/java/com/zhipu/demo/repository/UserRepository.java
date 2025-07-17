package com.zhipu.demo.repository;

import com.zhipu.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据用户名模糊查询
     */
    List<User> findByUsernameContaining(String username);
    
    /**
     * 自定义查询：查找描述中包含指定关键词的用户
     */
    @Query("SELECT u FROM User u WHERE u.description LIKE %:keyword%")
    List<User> findByDescriptionContaining(@Param("keyword") String keyword);
    
    /**
     * 统计用户总数
     */
    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();
} 