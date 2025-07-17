package com.zhipu.demo.controller;

import com.zhipu.demo.entity.User;
import com.zhipu.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 创建用户
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        log.info("创建用户请求: {}", user.getUsername());
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }
    
    /**
     * 获取所有用户
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("获取所有用户");
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("获取用户ID: {}", id);
        Optional<User> user = userService.findById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 根据用户名查找用户
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        log.info("根据用户名查找用户: {}", username);
        Optional<User> user = userService.findByUsername(username);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 根据用户名模糊查询
     */
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsersByUsername(@RequestParam String username) {
        log.info("模糊查询用户名: {}", username);
        List<User> users = userService.findByUsernameContaining(username);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 根据描述关键词查询
     */
    @GetMapping("/search/description")
    public ResponseEntity<List<User>> searchUsersByDescription(@RequestParam String keyword) {
        log.info("根据描述关键词查询: {}", keyword);
        List<User> users = userService.findByDescriptionContaining(keyword);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        log.info("更新用户ID: {}", id);
        Optional<User> existingUser = userService.findById(id);
        if (existingUser.isPresent()) {
            user.setId(id);
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("删除用户ID: {}", id);
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats/count")
    public ResponseEntity<Long> getUserCount() {
        log.info("获取用户总数");
        long count = userService.countUsers();
        return ResponseEntity.ok(count);
    }
    
    /**
     * 创建测试用户数据
     */
    @PostMapping("/init-test-data")
    public ResponseEntity<String> initTestData() {
        log.info("初始化测试用户数据");
        userService.createTestUsers();
        return ResponseEntity.ok("测试用户数据创建成功");
    }
} 