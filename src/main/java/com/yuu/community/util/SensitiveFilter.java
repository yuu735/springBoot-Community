package com.yuu.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger= LoggerFactory.getLogger(SensitiveFilter.class);
    //替换敏感词的字符
    private static final String REPLACEMENT="***";
    //root节点
    private TrieNode rootNode=new TrieNode();

    @PostConstruct      //表示这是一个初始化方法
    public void init(){
       // 当这个bean第一次被调用时就初始化
        try(InputStream is=this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader reader=new BufferedReader(new InputStreamReader(is));
            ){
            //读取敏感词
            String keyword;
            while((keyword=reader.readLine())!=null){
                //添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e){
            logger.error("加载铭感次文件失败"+e.getMessage());
        }
    }

    //将一个敏感词添加到前缀树中
    private void addKeyword(String keyword){
        TrieNode tempNode=rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c=keyword.charAt(i);
            TrieNode subNode=tempNode.getSubNode(c);    //检查子节点是否存在
            if(subNode==null){  //不存在就建立
                //初始化子节点
                subNode=new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            //指向子节点进入下一轮循环
            tempNode=subNode;
            //走到单词结尾了
            if(i==keyword.length()-1){
                tempNode.setKeyWordEnd(true);   //设置结束标识
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤的文本
     * @return  过滤后的文本
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        //进行过滤，需要三个指针
        //指针1
        TrieNode tempNode=rootNode;
        //指针2
        int begin=0;
        //指针3
        int position=0;
        //修改的结果
        StringBuilder sb=new StringBuilder();

        //遍历text
        while(position<text.length()) {
            char c=text.charAt(position);
            //跳过符号：
            if(isSymbol(c)){
                //若指针1处于根节点,将此符号记录结果，指针2向下走(从下一个算开始
               if(tempNode==rootNode){
                   sb.append(c);
                   begin++;
               }
                //System.out.println("是符号哦跳过");
                position++;//无论符号在开头或中间，指针3都往下遍历
                continue;
            }
            //检查子节点
            tempNode=tempNode.getSubNode(c);    //检查前缀树里面的敏感词
            if(tempNode==null){
                //以begin开头的字符串不是敏感词（不在前缀树中）
                sb.append(text.charAt(begin));
                //进入下一个位置
                position=++begin;   //begin先++然后再赋值给position
                //重新指向根节点
                tempNode=rootNode;
                //到达结束标识表示发现了一个敏感词
            }else if(tempNode.isKeyWordEnd()){
                //敏感词begin开头position结尾的字符串替换掉
                sb.append(REPLACEMENT);
                //进入下一个位置
                begin=++position;
                tempNode=rootNode;
            }else {
                //目前有符合的嫌疑敏感词但前缀树中的敏感词还没查到结束标识
                //继续检查下一个字符
                position++;
            }
        }
        System.out.println(sb.toString());
        //最后一批字符串不是敏感词的时候还没有记录到结果中就结束循环了
        sb.append(text.substring(begin));
        return sb.toString();

    }
    //判断是否为符号
    private  boolean isSymbol(Character c){
        //0x2E80-0x9FFF是东亚字符范围例如日语韩文
        //符号=不是合法的字符并且不是东亚字符
        return (!CharUtils.isAsciiAlphanumeric(c) && (c<0x2E80 || c>0x9FFF));//isAsciiAlphanumeric判断是否为合法的字符
    }

    //前缀树
    private class TrieNode{
        //关键词结束标识(是不是一个单词结尾)
        private boolean isKeyWordEnd=false;

        //当前节点的子节点，一个节点的孩子可能是多个因此用Map封装
        private Map<Character,TrieNode> subNodes=new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }
        //添加子节点
        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }
        //获得指定的子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c); //根据key获取value
        }
    }
}
