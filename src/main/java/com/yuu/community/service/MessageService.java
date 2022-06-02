package com.yuu.community.service;

import com.yuu.community.entity.Message;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

public interface MessageService {
    List<Message> findConversations(int userId, int offset, int limit);
    int findConversationCount(int userId);
    List<Message> findLetters(String conversationId, int offset, int limit);
    int findLetterCount(String conversationId);
    int findLetterUnreadCount(int userId, String conversationId);
    int addMessage(Message message);
    int readMessage(List<Integer> ids);

    Message findLatestNotice(int userId, String topic);
    int findNoticeCount(int userId, String topic);

    int findNoticeUnreadCount(int userId, String topic);

    List<Message> findNotices(int userId, String topic, int offset, int limit);
    //删除某条私信
    int deleteLetter(int id);

}
