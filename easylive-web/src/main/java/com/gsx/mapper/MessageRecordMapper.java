package com.gsx.mapper;

import com.gsx.entity.MessageRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.SortedSet;

@Mapper
public interface MessageRecordMapper {

    @Select("SELECT * from message_record where " +
            "(message_from = #{SendUserId} and message_to = #{UserId} ) " +
            "or (message_from = #{UserId} and message_to = #{SendUserId}) " +
            "order by time desc limit 100  ")
    public List<MessageRecord> getMessageRecordsByUserId(String SendUserId, String UserId);

    @Insert("INSERT INTO message_record(message_from, message_to, message_content) " +
            "values (#{messageFrom},#{messageTo},#{messageContent})")
    void addMessageRecord(MessageRecord messageRecord);
}
