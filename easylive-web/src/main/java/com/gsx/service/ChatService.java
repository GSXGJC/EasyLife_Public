package com.gsx.service;

import com.gsx.entity.MessageRecord;

import java.util.List;

public interface ChatService {
    List<MessageRecord> getMessageRecords(String userId);

    void addMessageRecord(MessageRecord messageRecord);
}
