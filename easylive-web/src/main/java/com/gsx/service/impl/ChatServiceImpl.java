package com.gsx.service.impl;

import com.gsx.entity.MessageRecord;
import com.gsx.handler.UserHolder;
import com.gsx.mapper.MessageRecordMapper;
import com.gsx.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    MessageRecordMapper messageRecordMapper;

    public ChatServiceImpl(@Autowired MessageRecordMapper messageRecordMapper) {
        this.messageRecordMapper = messageRecordMapper;
    }

    @Override
    public List<MessageRecord> getMessageRecords(String userId) {
        return messageRecordMapper.getMessageRecordsByUserId(UserHolder.getUserId(),userId);
    }

    @Override
    public void addMessageRecord(MessageRecord messageRecord) {
        messageRecordMapper.addMessageRecord(messageRecord);
    }


}
