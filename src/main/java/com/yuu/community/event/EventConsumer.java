package com.yuu.community.event;

import com.alibaba.fastjson.JSONObject;
import com.yuu.community.entity.DiscussPost;
import com.yuu.community.entity.Event;
import com.yuu.community.entity.Message;
import com.yuu.community.service.DiscussPostService;
import com.yuu.community.service.Impl.ElasticsearchService;
import com.yuu.community.service.MessageService;
import com.yuu.community.util.Constant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer {
    private static final Logger logger= LoggerFactory.getLogger(EventConsumer.class);
    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticsearchService elasticsearchService;

    @KafkaListener(topics = {Constant.TOPIC_COMMENT, Constant.TOPIC_LIKE, Constant.TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record){
        if(record==null || record.value()==null){
            logger.error("消息内容为空");
            return;
        }
        //转为event对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }
        // 发送站内通知
        Message message = new Message();
        message.setFromId(Constant.SYSTEM_USER_ID); //系统id为1
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        //数据转换:系统要显示给用户看的必要信息
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            //不为空,将event的key和value存到content中
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }
    //消费发帖事件
    @KafkaListener(topics = {Constant.TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if(record==null || record.value()==null){
            logger.error("消息内容为空");
            return;
        }
        //转为event对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }
        //从事件的消息里得到帖子id，查到对应的帖子
        DiscussPost post=discussPostService.findDiscussPostById(event.getEntityId());
        //存到es服务器里面
        elasticsearchService.saveDiscussPost(post);
    }

    //消费删除事件
    @KafkaListener(topics = {Constant.TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        if(record==null || record.value()==null){
            logger.error("消息内容为空");
            return;
        }
        //转为event对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }
        //从事件的消息里得到帖子id，查到对应的帖子
        //存到es服务器里面
        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }
}
