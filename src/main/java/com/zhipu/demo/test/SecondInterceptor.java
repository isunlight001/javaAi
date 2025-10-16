package com.zhipu.demo.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
@Slf4j
public class SecondInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        log.info("第二个拦截器：开始执行");
        
        // 获取MappedStatement
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        
        // 获取SQL信息
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String sql = boundSql.getSql().replaceAll("\\s+", " ");
        
        log.info("拦截到的SQL: {}", sql);
        log.info("参数个数: {}", getParameterCount(boundSql));
        
        // 执行下一个拦截器或目标方法
        Object result = invocation.proceed();
        
        log.info("第二个拦截器：执行结束，结果: {}", result);
        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以设置一些属性
        log.info("第二个拦截器：设置属性 - {}", properties);
    }
    
    /**
     * 获取参数个数
     * @param boundSql
     * @return
     */
    private int getParameterCount(BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        if (parameterObject == null) {
            return 0;
        }
        
        // 对于批量插入，参数通常是一个List
        if (parameterObject instanceof java.util.List) {
            return ((java.util.List<?>) parameterObject).size();
        }
        
        return 1;
    }
}