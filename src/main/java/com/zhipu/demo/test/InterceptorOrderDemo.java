package com.zhipu.demo.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
public class InterceptorOrderDemo {
    
    public static void main(String[] args) {
        try {
            // 加载MyBatis配置文件
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            
            log.info("开始测试MyBatis拦截器执行顺序");
            
            // 创建SqlSession
            try (SqlSession session = sqlSessionFactory.openSession()) {
                // 获取Mapper
                UserMapper userMapper = session.getMapper(UserMapper.class);
                
                log.info("准备执行SQL查询");
                
                // 执行查询来触发拦截器
                List<User> users = userMapper.findAll();
                log.info("查询到 {} 条用户记录", users.size());
            }
            
            log.info("MyBatis拦截器执行顺序测试完成");
        } catch (Exception e) {
            log.error("运行过程中出现异常: ", e);
        }
    }
}