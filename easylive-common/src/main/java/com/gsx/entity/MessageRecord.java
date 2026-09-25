package com.gsx.entity;

import java.time.LocalDateTime;

/**
 * 私聊消息记录实体，对应数据库表 message_records。
 * 语义：messageFrom 在 time 时刻给 messageTo 发了一条私聊（单条消息）。
 * <p>
 * 注意：本表没有主键，靠普通索引 idx_Message(message_from, message_to) 查会话；
 * 同一消息可重复落库（发重了就是发重了），不额外去重。
 */
public class MessageRecord {

    /*主键*/
    private int id;

    /** 私聊发送人，对应列 message_from，对应 user_info.user_id */
    private String messageFrom;

    /** 私聊接受人，对应列 message_to，对应 user_info.user_id */
    private String messageTo;

    /** 发送时间，对应列 time（datetime，无默认值，入库必须显式给） */
    private LocalDateTime time;

    /** 消息内容，对应列 message_content */
    private String messageContent;

    public String getMessageFrom() {
        return messageFrom;
    }

    public void setMessageFrom(String messageFrom) {
        this.messageFrom = messageFrom;
    }

    public String getMessageTo() {
        return messageTo;
    }

    public void setMessageTo(String messageTo) {
        this.messageTo = messageTo;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
