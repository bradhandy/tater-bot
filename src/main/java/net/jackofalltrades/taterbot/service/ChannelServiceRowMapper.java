package net.jackofalltrades.taterbot.service;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
class ChannelServiceRowMapper implements RowMapper<ChannelService> {

    @Override
    public ChannelService mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        String channelId = resultSet.getString("channel_id");
        String serviceCode = resultSet.getString("service_code");
        Service.Status status = Service.Status.fromCode(resultSet.getString("status"));

        // if the status date is null somehow, then default to the current date.
        Timestamp statusTimestamp = resultSet.getTimestamp("status_date");
        LocalDateTime statusDate = resultSet.wasNull() ? LocalDateTime.now() : statusTimestamp.toLocalDateTime();

        String userId = resultSet.getString("user_id");

        return new ChannelService(channelId, serviceCode, status, statusDate, userId);
    }

}
