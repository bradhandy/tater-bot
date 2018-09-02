package net.jackofalltrades.taterbot.service;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
class ServiceRowMapper implements RowMapper<Service> {

    @Override
    public Service mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        String code = resultSet.getString("code");
        String description = resultSet.getString("description");
        Service.Status status = Service.Status.fromCode(resultSet.getString("status"));

        // if status timestamp is null for some reason, default to the current time.
        Timestamp statusTimestamp = resultSet.getTimestamp("status_date");
        LocalDateTime statusDate = (resultSet.wasNull()) ? LocalDateTime.now() : statusTimestamp.toLocalDateTime();

        Service.Status initialChannelStatus = Service.Status.fromCode(resultSet.getString("initial_channel_status"));

        return new Service(code, description, status, statusDate, initialChannelStatus);
    }

}
