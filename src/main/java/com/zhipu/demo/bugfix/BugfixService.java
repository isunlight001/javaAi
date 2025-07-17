package com.zhipu.demo.bugfix;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.zhipu.demo.service.ZhipuAiService;
import com.zhipu.demo.bugfix.ai.AiModelFactory;
import com.zhipu.demo.bugfix.ai.AiModelService;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class BugfixService {
    @Autowired
    private AiModelFactory aiModelFactory;
    
    // 会话记忆存储，使用ConcurrentHashMap保证线程安全
    private final ConcurrentHashMap<String, ConversationSession> sessionStore = new ConcurrentHashMap<>();
    
    // 会话类，存储对话历史
    private static class ConversationSession {
        private String sessionId;
        private List<ConversationMessage> messages;
        private LocalDateTime createTime;
        private LocalDateTime lastUpdateTime;
        
        public ConversationSession(String sessionId) {
            this.sessionId = sessionId;
            this.messages = new ArrayList<>();
            this.createTime = LocalDateTime.now();
            this.lastUpdateTime = LocalDateTime.now();
        }
        
        public void addMessage(String role, String content) {
            messages.add(new ConversationMessage(role, content, LocalDateTime.now()));
            lastUpdateTime = LocalDateTime.now();
        }
        
        public List<ConversationMessage> getMessages() {
            return messages;
        }
        
        public String getSessionId() {
            return sessionId;
        }
        
        public LocalDateTime getCreateTime() {
            return createTime;
        }
        
        public LocalDateTime getLastUpdateTime() {
            return lastUpdateTime;
        }
        
        public int getMessageCount() {
            return messages.size();
        }
    }
    
    // 对话消息类
    private static class ConversationMessage {
        private String role; // user 或 assistant
        private String content;
        private LocalDateTime timestamp;
        
        public ConversationMessage(String role, String content, LocalDateTime timestamp) {
            this.role = role;
            this.content = content;
            this.timestamp = timestamp;
        }
        
        public String getRole() {
            return role;
        }
        
        public String getContent() {
            return content;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
    
    public String analyzeLog(String serialNo) throws Exception {
        // 1. 查询ES获取日志（此处为模拟）
        String log = queryLogFromES(serialNo);
        // 2. 调用大模型分析（此处为模拟）
        String suggestion = callLLM(log);
        return suggestion;
    }
    
    public Map<String, Object> analyzeLogWithTokenInfo(String serialNo) throws Exception {
        // 1. 查询ES获取日志（此处为模拟）
        String log = queryLogFromES(serialNo);
        // 2. 调用大模型分析并获取token信息
        Map<String, Object> result = callLLMWithTokenInfo(log);
        return result;
    }
    
    public Map<String, Object> analyzeLogWithTokenInfo(String serialNo, String modelType) throws Exception {
        // 1. 查询ES获取日志（此处为模拟）
        String log = queryLogFromES(serialNo);
        // 2. 调用大模型分析并获取token信息
        Map<String, Object> result = callLLMWithTokenInfo(log, modelType);
        return result;
    }
    
    public Map<String, Object> analyzeLogWithMemory(String serialNo, String sessionId) throws Exception {
        // 1. 查询ES获取日志（此处为模拟）
        String log = queryLogFromES(serialNo);
        
        // 2. 获取或创建会话
        ConversationSession session = getOrCreateSession(sessionId);
        
        // 3. 构建带记忆的对话上下文
        String enhancedPrompt = buildConversationContext(log, session);
        
        // 4. 调用大模型分析并获取token信息
        Map<String, Object> result = callLLMWithTokenInfoAndMemory(enhancedPrompt, session);
        
        return result;
    }
    
    public Map<String, Object> analyzeLogWithMemory(String serialNo, String sessionId, String modelType) throws Exception {
        // 1. 查询ES获取日志（此处为模拟）
        String log = queryLogFromES(serialNo);
        
        // 2. 获取或创建会话
        ConversationSession session = getOrCreateSession(sessionId);
        
        // 3. 构建带记忆的对话上下文
        String enhancedPrompt = buildConversationContext(log, session);
        
        // 4. 调用大模型分析并获取token信息
        Map<String, Object> result = callLLMWithTokenInfoAndMemory(enhancedPrompt, session, modelType);
        
        return result;
    }
    
    /**
     * 获取或创建会话
     */
    private ConversationSession getOrCreateSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            // 生成新的会话ID
            sessionId = UUID.randomUUID().toString();
            log.info("创建新会话，会话ID: {}", sessionId);
        }
        
        return sessionStore.computeIfAbsent(sessionId, k -> {
            log.info("初始化新会话: {}", k);
            return new ConversationSession(k);
        });
    }
    
    /**
     * 构建带记忆的对话上下文
     */
    private String buildConversationContext(String currentLog, ConversationSession session) {
        StringBuilder context = new StringBuilder();
        
        // 添加基础提示词
        context.append("# 角色与能力设定（CRISPE框架）  \n");
        context.append("你是一位资深系统运维专家，拥有10年日志分析与故障排查经验，擅长通过ES日志和大模型技术定位复杂系统缺陷。你的任务是：  \n");
        context.append("1. **精准检索**：根据流水号提取关联日志，还原完整事务链路。  \n");
        context.append("2. **根因分析**：识别异常模式（如级联超时、空指针等），定位缺陷代码位置。  \n");
        context.append("3. **修复建议**：提供代码级优化方案或架构改进策略。  \n");
        context.append("4. **会话记忆**：基于之前的对话历史，提供连续性的分析和建议。  \n");
        context.append("\n");
        
        // 添加会话信息
        context.append("# 会话信息  \n");
        context.append("- **会话ID**: ").append(session.getSessionId()).append("  \n");
        context.append("- **会话创建时间**: ").append(session.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("  \n");
        context.append("- **当前对话轮次**: ").append(session.getMessageCount() / 2 + 1).append("  \n");
        context.append("- **历史消息数**: ").append(session.getMessageCount()).append("  \n");
        context.append("\n");
        
        // 添加历史对话记录（最多保留10轮）
        List<ConversationMessage> messages = session.getMessages();
        if (!messages.isEmpty()) {
            context.append("# 历史对话记录  \n");
            context.append("以下是之前的对话历史，请基于这些信息提供连续性的分析：  \n");
            context.append("\n");
            
            // 只保留最近10轮对话（20条消息）
            int startIndex = Math.max(0, messages.size() - 20);
            for (int i = startIndex; i < messages.size(); i++) {
                ConversationMessage msg = messages.get(i);
                context.append("**").append(msg.getRole().equals("user") ? "用户" : "AI助手").append("** (")
                       .append(msg.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("):  \n");
                context.append(msg.getContent()).append("  \n");
                context.append("\n");
            }
        }
        
        // 添加当前分析要求
        context.append("# 当前分析要求  \n");
        context.append("- **流水号**：`[用户提供]`  \n");
        context.append("- **日志范围**：最近24小时（可调整）  \n");
        context.append("- **关键字段**：`error_code`、`service_name`、`timestamp`  \n");
        context.append("\n");
        
        // 添加上下文与洞察
        context.append("# 上下文与洞察（Insight）  \n");
        context.append("- **ES索引结构**：`logs-<日期>`，按`trace_id`倒排索引，冷热数据分离存储。  \n");
        context.append("- **典型缺陷模式**：  \n");
        context.append("  - `级联超时`：服务调用链连续超时（如`ServiceA → ServiceB`）  \n");
        context.append("  - `空指针`：未校验入参或DB返回空（如`NullPointerException at UserService.java:58`）  \n");
        context.append("  - `线程阻塞`：线程池满警告（`ThreadPoolExecutor rejected`）  \n");
        context.append("\n");
        
        // 添加输出要求
        context.append("# 输出要求（Output Indicator）  \n");
        context.append("- **结构化输出**：按以下JSON格式返回：  \n");
        context.append("  ```json\n");
        context.append("  {\n");
        context.append("    \"status\": \"success/partial/fail\",\n");
        context.append("    \"log_summary\": \"按时间排序的日志摘要（最多5条关键日志）\",\n");
        context.append("    \"defect_type\": \"超时/空指针/数据冲突...\",\n");
        context.append("    \"root_cause\": {\n");
        context.append("      \"code_file\": \"缺陷文件路径\",\n");
        context.append("      \"line_number\": \"行号\",\n");
        context.append("      \"context\": \"缺陷代码片段\"\n");
        context.append("    },\n");
        context.append("    \"solutions\": [\n");
        context.append("      {\n");
        context.append("        \"type\": \"hotfix/arch\",\n");
        context.append("        \"description\": \"具体修复步骤\",\n");
        context.append("        \"confidence\": 0.9  // 置信度评分\n");
        context.append("      }\n");
        context.append("    ],\n");
        context.append("    \"session_context\": \"基于历史对话的上下文分析\"\n");
        context.append("  }\n");
        context.append("  ```\n");
        context.append("\n");
        
        context.append("请分析以下日志内容：\n").append(currentLog);
        
        return context.toString();
    }
    
    /**
     * 带记忆的AI调用
     */
    private Map<String, Object> callLLMWithTokenInfoAndMemory(String prompt, ConversationSession session) throws Exception {
        return callLLMWithTokenInfoAndMemory(prompt, session, null);
    }
    
    /**
     * 带记忆的AI调用
     */
    private Map<String, Object> callLLMWithTokenInfoAndMemory(String prompt, ConversationSession session, String modelType) throws Exception {
        long startTime = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // 获取AI模型服务
        AiModelService aiService = modelType != null ? 
            aiModelFactory.getAiModelService(modelType) : 
            aiModelFactory.getAiModelService();
        
        // 调用前日志
        log.info("=== Bugfix AI调用开始（会话记忆模式） ===");
        log.info("使用模型: {}", aiService.getModelName());
        log.info("会话ID: {}", session.getSessionId());
        log.info("时间: {}", timestamp);
        log.info("历史消息数: {}", session.getMessageCount());
        log.info("提示词长度: {} 字符", prompt.length());
        log.info("预估输入token数: {}", estimateTokenCount(prompt));
        
        try {
            // 添加用户消息到会话历史
            session.addMessage("user", "分析日志: " + prompt.substring(prompt.lastIndexOf("请分析以下日志内容：\n") + "请分析以下日志内容：\n".length()));
            
            String response = aiService.chat(prompt);
            
            // 添加AI响应到会话历史
            session.addMessage("assistant", response);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 计算token信息
            int inputTokens = estimateTokenCount(prompt);
            int outputTokens = estimateTokenCount(response);
            int totalTokens = inputTokens + outputTokens;
            
            // 调用后日志
            log.info("=== Bugfix AI调用完成（会话记忆模式） ===");
            log.info("会话ID: {}", session.getSessionId());
            log.info("响应时间: {} ms", duration);
            log.info("响应内容长度: {} 字符", response.length());
            log.info("预估输出token数: {}", outputTokens);
            log.info("预估总token消耗: {}", totalTokens);
            log.info("当前会话消息数: {}", session.getMessageCount());
            double speed = (double) response.length() / (duration / 1000.0);
            log.info("平均响应速度: {:.2f} 字符/秒", speed);
            
            // 检查是否需要清理旧会话
            cleanupOldSessions();
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("suggestion", response);
            result.put("sessionId", session.getSessionId());
            result.put("messageCount", session.getMessageCount());
            
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("inputTokens", inputTokens);
            tokenInfo.put("outputTokens", outputTokens);
            tokenInfo.put("totalTokens", totalTokens);
            tokenInfo.put("responseTime", duration);
            tokenInfo.put("speed", String.format("%.2f", speed));
            
            result.put("tokenInfo", tokenInfo);
            
            return result;
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 异常日志
            log.error("=== Bugfix AI调用失败（会话记忆模式） ===");
            log.error("会话ID: {}", session.getSessionId());
            log.error("响应时间: {} ms", duration);
            log.error("异常信息: {}", e.getMessage(), e);
            
            throw e;
        }
    }
    
    /**
     * 清理过期会话（超过1小时未活动的会话）
     */
    private void cleanupOldSessions() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        sessionStore.entrySet().removeIf(entry -> {
            ConversationSession session = entry.getValue();
            boolean shouldRemove = session.getLastUpdateTime().isBefore(oneHourAgo);
            if (shouldRemove) {
                log.info("清理过期会话: {} (最后活动时间: {})", 
                    session.getSessionId(), 
                    session.getLastUpdateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            return shouldRemove;
        });
        
        log.info("当前活跃会话数: {}", sessionStore.size());
    }
    
    /**
     * 获取会话信息
     */
    public Map<String, Object> getSessionInfo(String sessionId) {
        ConversationSession session = sessionStore.get(sessionId);
        if (session == null) {
            return null;
        }
        
        Map<String, Object> info = new HashMap<>();
        info.put("sessionId", session.getSessionId());
        info.put("createTime", session.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        info.put("lastUpdateTime", session.getLastUpdateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        info.put("messageCount", session.getMessageCount());
        info.put("conversationRounds", session.getMessageCount() / 2);
        
        return info;
    }
    
    /**
     * 清除指定会话
     */
    public boolean clearSession(String sessionId) {
        ConversationSession removed = sessionStore.remove(sessionId);
        if (removed != null) {
            log.info("清除会话: {}", sessionId);
            return true;
        }
        return false;
    }
    
    /**
     * 获取所有活跃会话
     */
    public List<Map<String, Object>> getAllSessions() {
        List<Map<String, Object>> sessions = new ArrayList<>();
        for (ConversationSession session : sessionStore.values()) {
            sessions.add(getSessionInfo(session.getSessionId()));
        }
        return sessions;
    }
    
    /**
     * 获取支持的模型列表
     */
    public String[] getSupportedModels() {
        return aiModelFactory.getSupportedModels();
    }
    
    private String queryLogFromES(String serialNo) {
        // 模拟ES查询，返回典型Spring Boot异常挡板日志
        return "2024-05-01 10:23:45.123 ERROR 12345 --- [nio-8080-exec-1] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.lang.NullPointerException] with root cause\n" +
               "\njava.lang.NullPointerException: Cannot invoke \"String.length()\" because \"str\" is null\n" +
               "\tat com.zhipu.demo.bugfix.BugfixService.testMethod(BugfixService.java:42)\n" +
               "\tat com.zhipu.demo.bugfix.BugfixController.analyze(BugfixController.java:18)\n" +
               "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
               "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n" +
               "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n" +
               "\tat java.lang.reflect.Method.invoke(Method.java:498)\n" +
               "\tat org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:205)\n" +
               "\tat org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:150)\n" +
               "\tat org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:117)\n" +
               "\tat org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1039)\n" +
               "\tat org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:942)\n" +
               "\tat org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1005)\n" +
               "\tat org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:908)\n" +
               "\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:660)\n" +
               "\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:741)\n";
    }
    
    private String callLLM(String logContent) throws Exception {
        long startTime = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // 获取AI模型服务
        AiModelService aiService = aiModelFactory.getAiModelService();
        
        // 调用前日志
        log.info("=== Bugfix AI调用开始 ===");
        log.info("使用模型: {}", aiService.getModelName());
        log.info("时间: {}", timestamp);
        log.info("日志内容长度: {} 字符", logContent.length());
        log.info("预估输入token数: {}", estimateTokenCount(logContent));
        
        try {
            // 调用智普AI服务分析日志
            String prompt = "# 角色与能力设定（CRISPE框架）  \n" +
                    "你是一位资深系统运维专家，拥有10年日志分析与故障排查经验，擅长通过ES日志和大模型技术定位复杂系统缺陷。你的任务是：  \n" +
                    "1. **精准检索**：根据流水号提取关联日志，还原完整事务链路。  \n" +
                    "2. **根因分析**：识别异常模式（如级联超时、空指针等），定位缺陷代码位置。  \n" +
                    "3. **修复建议**：提供代码级优化方案或架构改进策略。  \n" +
                    "\n" +
                    "# 输入指令（Statement）  \n" +
                    "- **流水号**：`[用户提供]`  \n" +
                    "- **日志范围**：最近24小时（可调整）  \n" +
                    "- **关键字段**：`error_code`、`service_name`、`timestamp`  \n" +
                    "\n" +
                    "# 上下文与洞察（Insight）  \n" +
                    "- **ES索引结构**：`logs-<日期>`，按`trace_id`倒排索引，冷热数据分离存储。  \n" +
                    "- **典型缺陷模式**：  \n" +
                    "  - `级联超时`：服务调用链连续超时（如`ServiceA → ServiceB`）  \n" +
                    "  - `空指针`：未校验入参或DB返回空（如`NullPointerException at UserService.java:58`）  \n" +
                    "  - `线程阻塞`：线程池满警告（`ThreadPoolExecutor rejected`）  \n" +
                    "\n" +
                    "# 输出要求（Output Indicator）  \n" +
                    "- **结构化输出**：按以下JSON格式返回：  \n" +
                    "  ```json\n" +
                    "  {\n" +
                    "    \"status\": \"success/partial/fail\",\n" +
                    "    \"log_summary\": \"按时间排序的日志摘要（最多5条关键日志）\",\n" +
                    "    \"defect_type\": \"超时/空指针/数据冲突...\",\n" +
                    "    \"root_cause\": {\n" +
                    "      \"code_file\": \"缺陷文件路径\",\n" +
                    "      \"line_number\": \"行号\",\n" +
                    "      \"context\": \"缺陷代码片段\"\n" +
                    "    },\n" +
                    "    \"solutions\": [\n" +
                    "      {\n" +
                    "        \"type\": \"hotfix/arch\",\n" +
                    "        \"description\": \"具体修复步骤\",\n" +
                    "        \"confidence\": 0.9  // 置信度评分\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "  ```\n" +
                    "\n" +
                    "请分析以下日志内容：\n" + logContent;
            
            log.info("提示词长度: {} 字符", prompt.length());
            log.info("预估总输入token数: {}", estimateTokenCount(prompt));
            
            String response = aiService.chat(prompt);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 调用后日志
            log.info("=== Bugfix AI调用完成 ===");
            log.info("响应时间: {} ms", duration);
            log.info("响应内容长度: {} 字符", response.length());
            log.info("预估输出token数: {}", estimateTokenCount(response));
            log.info("预估总token消耗: {}", estimateTokenCount(prompt) + estimateTokenCount(response));
            double speed = (double) response.length() / (duration / 1000.0);
            log.info("平均响应速度: {:.2f} 字符/秒", speed);
            
            return response;
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 异常日志
            log.error("=== Bugfix AI调用失败 ===");
            log.error("响应时间: {} ms", duration);
            log.error("异常信息: {}", e.getMessage(), e);
            
            throw e;
        }
    }
    
    private Map<String, Object> callLLMWithTokenInfo(String logContent) throws Exception {
        return callLLMWithTokenInfo(logContent, null);
    }
    
    private Map<String, Object> callLLMWithTokenInfo(String logContent, String modelType) throws Exception {
        long startTime = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // 获取AI模型服务
        AiModelService aiService = modelType != null ? 
            aiModelFactory.getAiModelService(modelType) : 
            aiModelFactory.getAiModelService();
        
        // 调用前日志
        log.info("=== Bugfix AI调用开始 ===");
        log.info("使用模型: {}", aiService.getModelName());
        log.info("时间: {}", timestamp);
        log.info("日志内容长度: {} 字符", logContent.length());
        log.info("预估输入token数: {}", estimateTokenCount(logContent));
        
        try {
            // 调用智普AI服务分析日志
            String prompt = "# 角色与能力设定（CRISPE框架）  \n" +
                    "你是一位资深系统运维专家，拥有10年日志分析与故障排查经验，擅长通过ES日志和大模型技术定位复杂系统缺陷。你的任务是：  \n" +
                    "1. **精准检索**：根据流水号提取关联日志，还原完整事务链路。  \n" +
                    "2. **根因分析**：识别异常模式（如级联超时、空指针等），定位缺陷代码位置。  \n" +
                    "3. **修复建议**：提供代码级优化方案或架构改进策略。  \n" +
                    "\n" +
                    "# 输入指令（Statement）  \n" +
                    "- **流水号**：`[用户提供]`  \n" +
                    "- **日志范围**：最近24小时（可调整）  \n" +
                    "- **关键字段**：`error_code`、`service_name`、`timestamp`  \n" +
                    "\n" +
                    "# 上下文与洞察（Insight）  \n" +
                    "- **ES索引结构**：`logs-<日期>`，按`trace_id`倒排索引，冷热数据分离存储。  \n" +
                    "- **典型缺陷模式**：  \n" +
                    "  - `级联超时`：服务调用链连续超时（如`ServiceA → ServiceB`）  \n" +
                    "  - `空指针`：未校验入参或DB返回空（如`NullPointerException at UserService.java:58`）  \n" +
                    "  - `线程阻塞`：线程池满警告（`ThreadPoolExecutor rejected`）  \n" +
                    "\n" +
                    "# 输出要求（Output Indicator）  \n" +
                    "- **结构化输出**：按以下JSON格式返回：  \n" +
                    "  ```json\n" +
                    "  {\n" +
                    "    \"status\": \"success/partial/fail\",\n" +
                    "    \"log_summary\": \"按时间排序的日志摘要（最多5条关键日志）\",\n" +
                    "    \"defect_type\": \"超时/空指针/数据冲突...\",\n" +
                    "    \"root_cause\": {\n" +
                    "      \"code_file\": \"缺陷文件路径\",\n" +
                    "      \"line_number\": \"行号\",\n" +
                    "      \"context\": \"缺陷代码片段\"\n" +
                    "    },\n" +
                    "    \"solutions\": [\n" +
                    "      {\n" +
                    "        \"type\": \"hotfix/arch\",\n" +
                    "        \"description\": \"具体修复步骤\",\n" +
                    "        \"confidence\": 0.9  // 置信度评分\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "  ```\n" +
                    "\n" +
                    "请分析以下日志内容：\n" + logContent;
            
            log.info("提示词长度: {} 字符", prompt.length());
            log.info("预估总输入token数: {}", estimateTokenCount(prompt));
            
            String response = aiService.chat(prompt);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 计算token信息
            int inputTokens = estimateTokenCount(prompt);
            int outputTokens = estimateTokenCount(response);
            int totalTokens = inputTokens + outputTokens;
            
            // 调用后日志
            log.info("=== Bugfix AI调用完成 ===");
            log.info("响应时间: {} ms", duration);
            log.info("响应内容长度: {} 字符", response.length());
            log.info("预估输出token数: {}", outputTokens);
            log.info("预估总token消耗: {}", totalTokens);
            double speed = (double) response.length() / (duration / 1000.0);
            log.info("平均响应速度: {:.2f} 字符/秒", speed);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("suggestion", response);
            
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("inputTokens", inputTokens);
            tokenInfo.put("outputTokens", outputTokens);
            tokenInfo.put("totalTokens", totalTokens);
            tokenInfo.put("responseTime", duration);
            tokenInfo.put("speed", String.format("%.2f", speed));
            
            result.put("tokenInfo", tokenInfo);
            
            return result;
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 异常日志
            log.error("=== Bugfix AI调用失败 ===");
            log.error("响应时间: {} ms", duration);
            log.error("异常信息: {}", e.getMessage(), e);
            
            throw e;
        }
    }
    
    /**
     * 估算token数量（简单估算：1个token约等于4个字符）
     */
    private int estimateTokenCount(String text) {
        if (text == null) return 0;
        return (int) Math.ceil(text.length() / 4.0);
    }
} 