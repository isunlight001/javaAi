package com.zhipu.demo.service;

import com.zhipu.demo.entity.User;
import com.zhipu.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务类
 */
@Service
@Slf4j
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 创建用户
     */
    public User createUser(User user) {
        log.info("创建用户: {}", user.getUsername());
        return userRepository.save(user);
    }
    
    /**
     * 根据ID查找用户
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * 根据用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * 获取所有用户
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * 根据用户名模糊查询
     */
    public List<User> findByUsernameContaining(String username) {
        return userRepository.findByUsernameContaining(username);
    }
    
    /**
     * 根据描述关键词查询
     */
    public List<User> findByDescriptionContaining(String keyword) {
        return userRepository.findByDescriptionContaining(keyword);
    }
    
    /**
     * 更新用户信息
     */
    public User updateUser(User user) {
        log.info("更新用户: {}", user.getUsername());
        return userRepository.save(user);
    }
    
    /**
     * 删除用户
     */
    public void deleteUser(Long id) {
        log.info("删除用户ID: {}", id);
        userRepository.deleteById(id);
    }
    
    /**
     * 统计用户总数
     */
    public long countUsers() {
        return userRepository.countAllUsers();
    }
    
    /**
     * 批量创建测试用户
     */
    public void createTestUsers() {
        if (userRepository.count() == 0) {
            log.info("创建测试用户数据");
            
            User user1 = new User();
            user1.setUsername("张三");
            user1.setEmail("zhangsan@example.com");
            user1.setDescription("Java开发工程师，擅长Spring Boot开发");
            userRepository.save(user1);
            
            User user2 = new User();
            user2.setUsername("李四");
            user2.setEmail("lisi@example.com");
            user2.setDescription("前端开发工程师，精通Vue.js和React");
            userRepository.save(user2);
            
            User user3 = new User();
            user3.setUsername("王五");
            user3.setEmail("wangwu@example.com");
            user3.setDescription("数据库管理员，熟悉MySQL和Oracle");
            userRepository.save(user3);
            
            log.info("测试用户数据创建完成");
        }
    }
} 