package net.jackofalltrades.taterbot.service;

import com.google.common.base.Strings;
import org.springframework.jdbc.core.PreparedStatementCreator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

class ChannelServiceUpdatePreparedStatementCreator implements PreparedStatementCreator {

    private static final String UPDATE_WITH_UPDATE_USER_ID =
            "update channel_service set status = ?, status_date = ?, user_id = ? " +
                    "where channel_id = ? and service_code = ? and status = ? and status_date = ? and user_id = ?";
    private static final String UPDATE_WITHOUT_UPDATE_USER_ID =
            "update channel_service set status = ?, status_date = ?, user_id = ? " +
                    "where channel_id = ? and service_code = ? and status = ? and status_date = ? and user_id is null";

    private final ChannelService channelService;
    private final Service.Status channelServiceStatus;
    private final LocalDateTime channelServiceStatusDate;
    private final String updatingUser;

    ChannelServiceUpdatePreparedStatementCreator(ChannelService channelService, Service.Status channelServiceStatus,
            LocalDateTime channelServiceStatusDate, String updatingUser) {
        this.channelService = channelService;
        this.channelServiceStatus = channelServiceStatus;
        this.channelServiceStatusDate = channelServiceStatusDate;
        this.updatingUser = updatingUser;
    }

    ChannelServiceUpdatePreparedStatementCreator(ChannelService channelService, Service.Status channelServiceStatus,
            LocalDateTime channelServiceStatusDate) {
        this(channelService, channelServiceStatus, channelServiceStatusDate, null);
    }

    @Override
    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
        PreparedStatement preparedStatement;
        if (Strings.isNullOrEmpty(channelService.getUserId())) {
            preparedStatement = connection.prepareStatement(UPDATE_WITHOUT_UPDATE_USER_ID);
        } else {
            preparedStatement = connection.prepareStatement(UPDATE_WITH_UPDATE_USER_ID);
            preparedStatement.setString(8, channelService.getUserId());
        }

        preparedStatement.setString(1, channelServiceStatus.name().toLowerCase());
        preparedStatement.setTimestamp(2, Timestamp.valueOf(channelServiceStatusDate));

        if (Strings.isNullOrEmpty(updatingUser)) {
            preparedStatement.setNull(3, Types.VARCHAR);
        } else {
            preparedStatement.setString(3, updatingUser);
        }

        preparedStatement.setString(4, channelService.getChannelId());
        preparedStatement.setString(5, channelService.getServiceCode());
        preparedStatement.setString(6, channelService.getStatus().name().toLowerCase());
        preparedStatement.setTimestamp(7, Timestamp.valueOf(channelService.getStatusDate()));

        return preparedStatement;
    }

    ChannelService getChannelService() {
        return channelService;
    }

    Service.Status getChannelServiceStatus() {
        return channelServiceStatus;
    }

    LocalDateTime getChannelServiceStatusDate() {
        return channelServiceStatusDate;
    }

    String getUpdatingUser() {
        return updatingUser;
    }

}
