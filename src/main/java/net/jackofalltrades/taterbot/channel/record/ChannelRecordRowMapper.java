package net.jackofalltrades.taterbot.channel.record;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Component
class ChannelRecordRowMapper implements RowMapper<ChannelRecord> {

    @Override
    public ChannelRecord mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        String channelId = resultSet.getString("channel_id");
        String userId = resultSet.getString("user_id");
        String userDisplayName = resultSet.getString("user_display_name");
        String messageType = resultSet.getString("message_type");
        LocalDateTime messageTimestamp = resultSet.getTimestamp("message_timestamp").toLocalDateTime();
        String message = resultSet.getString("message");

        return new ChannelRecord(channelId, userId, userDisplayName, messageType, messageTimestamp, message);
    }

}
