package net.jackofalltrades.taterbot.service;

import com.google.common.base.Strings;
import org.springframework.jdbc.core.PreparedStatementCreator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

class ChannelServiceUpdatePreparedStatementCreator implements PreparedStatementCreator {

    private static final String UPDATE_WITH_UPDATE_USER_ID =
            "update channel_service set status = ?, user_id = ? " +
                    "where channel_id = ? and service_code = ? and status = ? and status_date = ? and user_id = ?";
    private static final String UPDATE_WITHOUT_UPDATE_USER_ID =
            "update channel_service set status = ?, user_id = ? " +
                    "where channel_id = ? and service_code = ? and status = ? and status_date = ? and user_id is null";

    private final ChannelService channelService;
    private final Service.Status channelServiceStatus;
    private final String updatingUser;

    ChannelServiceUpdatePreparedStatementCreator(ChannelService channelService, Service.Status channelServiceStatus,
            String updatingUser) {
        this.channelService = channelService;
        this.channelServiceStatus = channelServiceStatus;
        this.updatingUser = updatingUser;
    }

    ChannelServiceUpdatePreparedStatementCreator(ChannelService channelService, Service.Status channelServiceStatus) {
        this(channelService, channelServiceStatus, null);
    }

    @Override
    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
        PreparedStatement preparedStatement;
        if (Strings.isNullOrEmpty(channelService.getUserId())) {
            preparedStatement = connection.prepareStatement(UPDATE_WITHOUT_UPDATE_USER_ID);
        } else {
            preparedStatement = connection.prepareStatement(UPDATE_WITH_UPDATE_USER_ID);
            preparedStatement.setString(7, channelService.getUserId());
        }

        preparedStatement.setString(1, channelServiceStatus.name().toLowerCase());

        if (Strings.isNullOrEmpty(updatingUser)) {
            preparedStatement.setNull(2, Types.VARCHAR);
        } else {
            preparedStatement.setString(2, updatingUser);
        }

        preparedStatement.setString(3, channelService.getChannelId());
        preparedStatement.setString(4, channelService.getServiceCode());
        preparedStatement.setString(5, channelService.getStatus().name().toLowerCase());
        preparedStatement.setTimestamp(6, Timestamp.valueOf(channelService.getStatusDate()));

        return preparedStatement;
    }

    ChannelService getChannelService() {
        return channelService;
    }

    Service.Status getChannelServiceStatus() {
        return channelServiceStatus;
    }

    String getUpdatingUser() {
        return updatingUser;
    }

}
