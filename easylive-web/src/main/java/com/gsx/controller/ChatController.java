package com.gsx.controller;

import com.gsx.Utils.ResultUtil.Result;
import com.gsx.entity.ChatMessageBody;
import com.gsx.entity.MessageRecord;
import com.gsx.mapper.FollowMapper;
import com.gsx.security.UserPrincipal;
import com.gsx.service.ChatService;
import com.gsx.service.impl.ChatServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;


@RestController
public class ChatController {

    private final ChatServiceImpl chatServiceImpl;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final FollowMapper followMapper;

    public ChatController(@Autowired ChatServiceImpl chatServiceImpl, @Autowired SimpMessagingTemplate simpMessagingTemplate,
                          @Autowired FollowMapper followMapper) {
        this.chatServiceImpl = chatServiceImpl;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.followMapper = followMapper;
    }

    @MessageMapping("/chat")
    public void sendMessage(@Payload ChatMessageBody chatMessageBody,
                            UserPrincipal principal) {

        // 防御：没有接收者或内容为空就不处理
        String aimUser = chatMessageBody.getAimUser();
        String content = chatMessageBody.getContent();
        if (aimUser == null || aimUser.isEmpty() || content == null || content.isEmpty()) {
            return;
        }

        String senderUserId = principal.getName();

        // 私信门槛（服务端强制）：1) 禁止自言自语；2) 发送者必须已关注接收者，
        // 否则静默丢弃（不回消息、不报错）。配合前端名单 = 我关注的人，构成"关注后才能私信"。
        if (senderUserId.equals(aimUser)) {
            return;
        }
        if (followMapper.countFollow(senderUserId, aimUser) == 0) {
            return;
        }

        // 谁发的以握手鉴权出来的 Principal 为准，不能信前端传的 fromUserId，防止伪造
        ChatMessageBody msg = new ChatMessageBody();
        msg.setFromUserId(senderUserId);
        msg.setFromNickName(principal.getNickName());
        msg.setAimUser(aimUser);
        msg.setContent(content);

        MessageRecord rcd = new MessageRecord();
        rcd.setMessageTo(aimUser);
        rcd.setMessageFrom(senderUserId);
        rcd.setMessageContent(content);

        // 定向发给 aimUser：拼成 /user/{aimUser}/queue/chat，由 /user 前缀映射到该用户的活动会话
        simpMessagingTemplate.convertAndSendToUser(aimUser, "/queue/chat", msg);

        chatServiceImpl.addMessageRecord(rcd);
    }

    @GetMapping("/chat/chatRecords/{userId}")
    public Result<List<MessageRecord>> getChatRecords(@PathVariable("userId") String userId){
        return Result.success(chatServiceImpl.getMessageRecords(userId));
    }
}
