package com.gsx.entity;

public class ChatMessageBody {
    /**
     * 发送者 userId（服务器从握手 Principal 里取，不信任前端传值）
     */
    private String fromUserId;
    /**
     * 发送者昵称（从 JWT claim 里带进来，给对端显示用）
     */
    private String fromNickName;
    /**
     * 发给谁：接收者的 userId（这条是前端填的）
     */
    private String aimUser;
    private String content;

    /**
     * 无参构造器必须保留：STOMP 收到 JSON 时，Jackson 要先 new 一个空对象，
     * 再靠下面的 setter 把字段填进去。没它反序列化直接报错。
     */
    public ChatMessageBody() {
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromNickName() {
        return fromNickName;
    }

    public void setFromNickName(String fromNickName) {
        this.fromNickName = fromNickName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAimUser() {
        return aimUser;
    }

    public void setAimUser(String aimUser) {
        this.aimUser = aimUser;
    }
}
