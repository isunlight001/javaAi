package com.zhipu.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/tools")
@Slf4j
/**
 * 匹配模式重命名文件夹和文件
 * 根据指定的匹配模式，将包含特定字符的文件和文件夹重命名
 * 例如：匹配模式为"_"，新名字为"-"，则会将包含"_"的文件/文件夹重命名为包含"-"的文件/文件夹
 * 支持递归处理子目录中的所有文件和文件夹     
 */
public class ToolsController {

    /**
   
     * @param params {dirPath: 绝对路径, pattern: 匹配模式, newName: 新名字}
     * @return 重命名前后对照表
     */
    @PostMapping("/rename-by-pattern")
    public ResponseEntity<Map<String, Object>> renameByPattern(@RequestBody Map<String, String> params) {
        String dirPath = params.get("dirPath");
        String pattern = params.get("pattern");
        String newName = params.get("newName");
        Map<String, Object> result = new HashMap<>();
        
        log.info("匹配重命名请求: dirPath={}, pattern={}, newName={}", dirPath, pattern, newName);
        
        if (dirPath == null || pattern == null || newName == null) {
            result.put("success", false);
            result.put("error", "参数不能为空");
            return ResponseEntity.ok(result);
        }
        
        File root = new File(dirPath);
        if (!root.exists() || !root.isDirectory()) {
            result.put("success", false);
            result.put("error", "目录不存在或不是文件夹: " + dirPath);
            return ResponseEntity.ok(result);
        }
        
        List<Map<String, String>> renameList = new ArrayList<>();
        int[] counter = {1};
        
        try {
            // 将通配符模式转换为正则表达式
            String regexPattern = convertWildcardToRegex(pattern);
            log.info("转换后的正则表达式: {}", regexPattern);
            Pattern compiledPattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
            
            // 递归查找并重命名匹配的文件和文件夹
            findAndRenameRecursive(root, compiledPattern, newName, renameList, counter);
            
            log.info("重命名完成，共处理 {} 个文件/文件夹", renameList.size());
            result.put("success", true);
            result.put("list", renameList);
            result.put("total", renameList.size());
        } catch (Exception e) {
            log.error("匹配重命名失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 将通配符模式转换为正则表达式
     */
    private String convertWildcardToRegex(String wildcard) {
        // 转义特殊字符
        String regex = wildcard.replace("\\", "\\\\")
                              .replace("^", "\\^")
                              .replace("$", "\\$")
                              .replace(".", "\\.")
                              .replace("[", "\\[")
                              .replace("]", "\\]")
                              .replace("(", "\\(")
                              .replace(")", "\\)")
                              .replace("+", "\\+")
                              .replace("?", "\\?")
                              .replace("|", "\\|");
        
        // 将通配符转换为正则表达式
        regex = regex.replace("*", ".*")  // * 匹配任意字符
                    .replace("?", ".");   // ? 匹配单个字符
        
        return "^" + regex + "$";  // 添加开始和结束标记
    }

    /**
     * 递归查找并重命名匹配的文件和文件夹
     */
    private void findAndRenameRecursive(File dir, Pattern pattern, String newName, 
                                      List<Map<String, String>> renameList, int[] counter) {
        // 先收集所有需要重命名的文件和文件夹
        List<File> toRename = new ArrayList<>();
        collectFilesToRename(dir, pattern, toRename);
        
        // 按路径深度排序，从深到浅重命名（避免路径变化影响）
        toRename.sort((f1, f2) -> {
            int depth1 = getPathDepth(f1);
            int depth2 = getPathDepth(f2);
            return Integer.compare(depth2, depth1); // 深度深的先处理
        });
        
        // 执行重命名
        for (File file : toRename) {
            String oldPath = file.getAbsolutePath();
            String extension = "";
            
            // 如果是文件，保留扩展名
            if (file.isFile()) {
                int lastDot = file.getName().lastIndexOf('.');
                if (lastDot > 0) {
                    extension = file.getName().substring(lastDot);
                }
            }
            
            // 构建新文件名
            String newFileName = newName + "_" + counter[0]++ + extension;
            File newFile = new File(file.getParentFile(), newFileName);
            
            // 执行重命名
            boolean renamed = file.renameTo(newFile);
            
            // 记录结果
            Map<String, String> map = new HashMap<>();
            map.put("old", oldPath);
            map.put("new", newFile.getAbsolutePath());
            map.put("success", String.valueOf(renamed));
            map.put("type", file.isDirectory() ? "文件夹" : "文件");
            renameList.add(map);
        }
    }
    
    /**
     * 收集需要重命名的文件和文件夹
     */
    private void collectFilesToRename(File dir, Pattern pattern, List<File> toRename) {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            // 检查当前文件/文件夹是否匹配模式
            if (pattern.matcher(file.getName()).matches()) {
                log.info("匹配到: {} ({})", file.getAbsolutePath(), file.isDirectory() ? "文件夹" : "文件");
                toRename.add(file);
            }
            
            // 递归处理子目录
            if (file.isDirectory()) {
                collectFilesToRename(file, pattern, toRename);
            }
        }
    }
    
    /**
     * 获取文件路径深度
     */
    private int getPathDepth(File file) {
        int depth = 0;
        File current = file;
        while (current.getParentFile() != null) {
            depth++;
            current = current.getParentFile();
        }
        return depth;
    }

    /**
     * 递归重命名所有子文件夹
     * @param params {dirPath: 绝对路径, newName: 新文件夹名}
     * @return 重命名前后对照表
     */
    @PostMapping("/rename-folders")
    public ResponseEntity<Map<String, Object>> renameFolders(@RequestBody Map<String, String> params) {
        String dirPath = params.get("dirPath");
        String newName = params.get("newName");
        Map<String, Object> result = new HashMap<>();
        if (dirPath == null || newName == null) {
            result.put("success", false);
            result.put("error", "参数不能为空");
            return ResponseEntity.ok(result);
        }
        File root = new File(dirPath);
        if (!root.exists() || !root.isDirectory()) {
            result.put("success", false);
            result.put("error", "目录不存在或不是文件夹");
            return ResponseEntity.ok(result);
        }
        List<Map<String, String>> renameList = new ArrayList<>();
        int[] counter = {1};
        try {
            renameFoldersRecursive(root, newName, renameList, counter);
            result.put("success", true);
            result.put("list", renameList);
        } catch (Exception e) {
            log.error("重命名文件夹失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 递归重命名
     */
    private void renameFoldersRecursive(File dir, String newName, List<Map<String, String>> renameList, int[] counter) {
        // 先收集所有需要重命名的文件夹
        List<File> foldersToRename = new ArrayList<>();
        collectFoldersToRename(dir, foldersToRename);
        
        // 按路径深度排序，从深到浅重命名
        foldersToRename.sort((f1, f2) -> {
            int depth1 = getPathDepth(f1);
            int depth2 = getPathDepth(f2);
            return Integer.compare(depth2, depth1); // 深度深的先处理
        });
        
        // 执行重命名
        for (File folder : foldersToRename) {
            String oldPath = folder.getAbsolutePath();
            File newFile = new File(folder.getParentFile(), newName + "_" + counter[0]++);
            boolean renamed = folder.renameTo(newFile);
            Map<String, String> map = new HashMap<>();
            map.put("old", oldPath);
            map.put("new", newFile.getAbsolutePath());
            map.put("success", String.valueOf(renamed));
            map.put("type", "文件夹");
            renameList.add(map);
        }
    }
    
    /**
     * 收集需要重命名的文件夹
     */
    private void collectFoldersToRename(File dir, List<File> foldersToRename) {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                // 先递归收集子目录
                collectFoldersToRename(file, foldersToRename);
                // 添加当前文件夹
                foldersToRename.add(file);
            }
        }
    }

    /**
     * 模式替换重命名文件和文件夹
     * 根据指定的匹配模式，将文件名中包含特定字符的文件和文件夹重命名
     * 例如：匹配模式为"_"，替换为"-"，则会将包含"_"的文件/文件夹重命名为包含"-"的文件/文件夹
     * 支持递归处理子目录中的所有文件和文件夹
     * @param params {dirPath: 绝对路径, pattern: 匹配模式, replacement: 替换模式}
     * @return 重命名前后对照表
     */
    @PostMapping("/rename-by-replacement")
    public ResponseEntity<Map<String, Object>> renameByReplacement(@RequestBody Map<String, String> params) {
        String dirPath = params.get("dirPath");
        String pattern = params.get("pattern");
        String replacement = params.get("replacement");
        Map<String, Object> result = new HashMap<>();
        
        log.info("模式替换重命名请求: dirPath={}, pattern={}, replacement={}", dirPath, pattern, replacement);
        
        if (dirPath == null || pattern == null || replacement == null) {
            result.put("success", false);
            result.put("error", "参数不能为空");
            return ResponseEntity.ok(result);
        }
        
        File root = new File(dirPath);
        if (!root.exists() || !root.isDirectory()) {
            result.put("success", false);
            result.put("error", "目录不存在或不是文件夹: " + dirPath);
            return ResponseEntity.ok(result);
        }
        
        List<Map<String, String>> renameList = new ArrayList<>();
        
        try {
            // 递归查找并重命名匹配的文件和文件夹
            findAndRenameByReplacementRecursive(root, pattern, replacement, renameList);
            
            log.info("模式替换重命名完成，共处理 {} 个文件/文件夹", renameList.size());
            result.put("success", true);
            result.put("list", renameList);
            result.put("total", renameList.size());
        } catch (Exception e) {
            log.error("模式替换重命名失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 递归查找并重命名匹配的文件和文件夹（模式替换方式）
     */
    private void findAndRenameByReplacementRecursive(File dir, String pattern, String replacement, 
                                                    List<Map<String, String>> renameList) {
        // 先收集所有需要重命名的文件和文件夹
        List<File> toRename = new ArrayList<>();
        collectFilesToRenameByPattern(dir, pattern, toRename);
        
        // 按路径深度排序，从深到浅重命名（避免路径变化影响）
        toRename.sort((f1, f2) -> {
            int depth1 = getPathDepth(f1);
            int depth2 = getPathDepth(f2);
            return Integer.compare(depth2, depth1); // 深度深的先处理
        });
        
        // 执行重命名
        for (File file : toRename) {
            String oldPath = file.getAbsolutePath();
            String oldName = file.getName();
            
            // 替换文件名中的模式
            String newName = oldName.replace(pattern, replacement);
            
            // 如果文件名没有变化，跳过
            if (oldName.equals(newName)) {
                continue;
            }
            
            File newFile = new File(file.getParentFile(), newName);
            
            // 检查新文件名是否已存在
            if (newFile.exists()) {
                log.warn("目标文件已存在，跳过重命名: {}", newFile.getAbsolutePath());
                Map<String, String> map = new HashMap<>();
                map.put("old", oldPath);
                map.put("new", newFile.getAbsolutePath());
                map.put("success", "false");
                map.put("type", file.isDirectory() ? "文件夹" : "文件");
                map.put("error", "目标文件已存在");
                renameList.add(map);
                continue;
            }
            
            // 执行重命名
            boolean renamed = file.renameTo(newFile);
            
            // 记录结果
            Map<String, String> map = new HashMap<>();
            map.put("old", oldPath);
            map.put("new", newFile.getAbsolutePath());
            map.put("success", String.valueOf(renamed));
            map.put("type", file.isDirectory() ? "文件夹" : "文件");
            renameList.add(map);
            
            log.info("重命名: {} -> {}", oldName, newName);
        }
    }
    
    /**
     * 收集需要重命名的文件和文件夹（模式替换方式）
     */
    private void collectFilesToRenameByPattern(File dir, String pattern, List<File> toRename) {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            // 检查当前文件/文件夹名称是否包含模式
            if (file.getName().contains(pattern)) {
                log.info("匹配到: {} ({})", file.getAbsolutePath(), file.isDirectory() ? "文件夹" : "文件");
                toRename.add(file);
            }
            
            // 递归处理子目录
            if (file.isDirectory()) {
                collectFilesToRenameByPattern(file, pattern, toRename);
            }
        }
    }
} 