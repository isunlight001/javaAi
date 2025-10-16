package com.zhipu.demo.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BatchInsertDemo {
    
    public static void main(String[] args) {
        try {
            // 加载MyBatis配置文件
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            
            log.info("开始测试MyBatis批量插入功能");
            
            // 创建测试数据
            List<BatchUser> users = createTestData();
            
            // 创建SqlSession
            try (SqlSession session = sqlSessionFactory.openSession()) {
                // 获取Mapper
                BatchUserMapper userMapper = session.getMapper(BatchUserMapper.class);
                
                log.info("准备执行批量插入，数据条数: {}", users.size());
                
                // 执行批量插入来触发拦截器
                int result = userMapper.batchInsert(users);
                session.commit();
                
                log.info("批量插入完成，影响行数: {}", result);
            }
            
            log.info("MyBatis批量插入测试完成");
        } catch (Exception e) {
            log.error("运行过程中出现异常: ", e);
        }
    }
    
    /**
     * 创建测试数据
     * @return
     */
    private static List<BatchUser> createTestData() {
        List<BatchUser> users = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            BatchUser user = new BatchUser();
            user.setName("用户" + i);
            user.setAge(20 + i);
            user.setEmail("user" + i + "@example.com");
            users.add(user);
        }
        
        return users;
    }
}