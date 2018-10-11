package net.jackofalltrades.taterbot.channel.record;

import com.google.common.base.Strings;
import org.springframework.jdbc.core.PreparedStatementCreator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;

class ChannelRecordInsertPreparedStatementCreator implements PreparedStatementCreator {

    private final ChannelRecord channelRecord;

    ChannelRecordInsertPreparedStatementCreator(ChannelRecord channelRecord) {
        this.channelRecord = channelRecord;
    }

    @Override
    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(createSqlStatement());
        int currentParameterIndex = 1;

        preparedStatement.setString(currentParameterIndex++, channelRecord.getChannelId());
        if (!Strings.isNullOrEmpty(channelRecord.getUserId())) {
            preparedStatement.setString(currentParameterIndex++, channelRecord.getUserId());
            if (!Strings.isNullOrEmpty(channelRecord.getUserDisplayName())) {
                preparedStatement.setString(currentParameterIndex++, channelRecord.getUserDisplayName());
            }
        }

        preparedStatement.setString(currentParameterIndex++, channelRecord.getMessageType());
        preparedStatement.setTimestamp(currentParameterIndex++, Timestamp.valueOf(channelRecord.getMessageTimestamp()));
        preparedStatement.setString(currentParameterIndex, channelRecord.getMessage());

        return preparedStatement;
    }

    private String createSqlStatement() {
        StringBuilder insertStatement = new StringBuilder(
                "insert into channel_record (channel_id, message_type, message_timestamp, message) values (?, ?, ?, ?)");
        if (!Strings.isNullOrEmpty(channelRecord.getUserId())) {
            insertStatement.insert(insertStatement.indexOf("message_type,"), "user_id, ");
            insertStatement.insert(insertStatement.indexOf("?,"), "?, ");

            if (!Strings.isNullOrEmpty(channelRecord.getUserDisplayName())) {
                insertStatement.insert(insertStatement.indexOf("message_type,"), "user_display_name, ");
                insertStatement.insert(insertStatement.indexOf("?,"), "?, ");
            }
        }

        return insertStatement.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelRecordInsertPreparedStatementCreator that = (ChannelRecordInsertPreparedStatementCreator) o;
        return Objects.equals(channelRecord, that.channelRecord);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelRecord);
    }

}
