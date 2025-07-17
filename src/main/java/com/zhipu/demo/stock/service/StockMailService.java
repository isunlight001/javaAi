package com.zhipu.demo.stock.service;

import com.zhipu.demo.stock.model.StockPickResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockMailService {
    private static final Logger log = LoggerFactory.getLogger(StockMailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${stock.mail.to:}")
    private String toEmail;
    
    public void sendStockPickResult(List<StockPickResult> results) {
        if (results == null || results.isEmpty()) {
            log.info("没有选股结果，跳过邮件发送");
            return;
        }
        
        if (toEmail == null || toEmail.trim().isEmpty()) {
            log.warn("未配置收件人邮箱，跳过邮件发送");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("股票选股结果 - " + results.size() + "只股票");
            
            StringBuilder content = new StringBuilder();
            content.append("今日选股结果：\n\n");
            
            for (int i = 0; i < results.size(); i++) {
                StockPickResult result = results.get(i);
                content.append(i + 1).append(". ").append(result.toString()).append("\n");
            }
            
            content.append("\n选股时间：").append(java.time.LocalDateTime.now());
            
            message.setText(content.toString());
            
            mailSender.send(message);
            log.info("选股结果邮件发送成功，收件人: {}, 股票数量: {}", toEmail, results.size());
            
        } catch (Exception e) {
            log.error("发送选股结果邮件失败", e);
        }
    }
    
    public void sendRiskAlert(String stockCode, String reason) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            log.warn("未配置收件人邮箱，跳过风险提醒邮件发送");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("股票风险提醒 - " + stockCode);
            message.setText("股票代码: " + stockCode + "\n风险原因: " + reason + "\n提醒时间: " + java.time.LocalDateTime.now());
            
            mailSender.send(message);
            log.info("风险提醒邮件发送成功，股票代码: {}", stockCode);
            
        } catch (Exception e) {
            log.error("发送风险提醒邮件失败，股票代码: {}", stockCode, e);
        }
    }
} 