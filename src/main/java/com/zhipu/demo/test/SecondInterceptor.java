package com.zhipu.demo.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
@Slf4j
public class SecondInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        log.info("第二个拦截器：开始执行");
        
        // 执行下一个拦截器或目标方法
        Object result = invocation.proceed();
        
        log.info("第二个拦截器：执行结束");
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
}