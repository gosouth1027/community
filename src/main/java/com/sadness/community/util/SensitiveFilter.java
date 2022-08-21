package com.sadness.community.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2022/6/8 15:48
 * @Author SadAndBeautiful
 */
@Slf4j
@Component
public class SensitiveFilter {

    // 敏感词替换的内容
    private static final String REPLACEMENT = "***";

    // 定义根节点
    private TrieNode rootNode = new TrieNode();

    /**
     * 过滤敏感词
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {

        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 定义三个指针分别指向前缀树，待过滤字符的起始
        TrieNode temp = rootNode;
        int begin = 0;
        int position = 0;

        // 用来保存结果
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            Character c = text.charAt(i);
            // 过滤特殊符号
            if (isSymbol(c)) {
                // 前缀树索引指向根节点，字符指针直接后移
                if (temp == rootNode) {
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            // 不是特殊符号，看前缀树中是否有该符号
            // 前缀树中没有，直接加入sb中，字符指针后移, 前缀树指向根节点
            temp =  temp.getChild(c);
            if (temp == null) {
                sb.append(c);
                temp = rootNode;
                position = ++begin;
            } else if(temp.isKeywordEnd == true) { // 前缀树中有，且为敏感词的结尾，敏感词代替，字符指针后移，前缀树指向根节点
                sb.append(REPLACEMENT);
                temp = rootNode;
                begin = ++position;
            } else { // 前缀树中有，且不为敏感词的结尾，字符指针后移，继续判断
                position++;
            }
        }
        // 剩下的字符计入sb
        sb.append(text.substring(begin));

        return sb.toString();
    }

    /**
     * 判断字符是否为特殊符号
     */
    private boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    // 根据敏感词初始化前缀树
    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                )
        {
            String keyword;
            while ((keyword = br.readLine()) != null) {
                this.addKeyword(keyword);
            }
        } catch (Exception e) {
            log.error("读取敏感词文件失败！" + e.getMessage());
        }
    }

    // 将敏感词添加到前缀树
    private void addKeyword(String keyword) {
        // temp结点指向根节点
        TrieNode temp = rootNode;
        // 遍历字符串
        for (int i = 0; i < keyword.length(); i++) {
            Character c = keyword.charAt(i);
            // 查看当前节点是否有该字符的子节点
            TrieNode child = rootNode.getChild(c);
            // 没有则添加
            if (child == null) {
                child = new TrieNode();
                temp.setChildren(c, child);
            }
            // 判断是都为结尾字符
            if (i == keyword.length() - 1) {
                child.setKeywordEnd(true);
            }
            // temp指向下一个结点，即添加的结点
            temp = child;
        }
    }

    // 定义前缀树结点
    private class TrieNode {

        // 记录当前结点是否为敏感词的结尾
        private boolean isKeywordEnd = false;
        // 结点的子节点
        private Map<Character, TrieNode> children = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public TrieNode getChild(Character c) {
            return children.get(c);
        }

        public void setChildren(Character c, TrieNode node) {
            children.put(c, node);
        }
    }

}
