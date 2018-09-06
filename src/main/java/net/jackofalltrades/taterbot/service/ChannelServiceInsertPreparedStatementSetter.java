package net.jackofalltrades.taterbot.service;

import com.google.common.base.Strings;
import org.springframework.jdbc.core.PreparedStatementSetter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

class ChannelServiceInsertPreparedStatementSetter implements PreparedStatementSetter {

    private final ChannelService channelService;

    ChannelServiceInsertPreparedStatementSetter(ChannelService channelService) {
        this.channelService = channelService;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, channelService.getChannelId());
        preparedStatement.setString(2, channelService.getServiceCode());
        preparedStatement.setString(3, channelService.getStatus().name().toLowerCase());
        preparedStatement.setTimestamp(4, Timestamp.valueOf(channelService.getStatusDate()));

        if (Strings.isNullOrEmpty(channelService.getUserId())) {
            preparedStatement.setNull(5, Types.VARCHAR);
        } else {
            preparedStatement.setString(5, channelService.getUserId());
        }
    }

    ChannelService getChannelService() {
        return channelService;
    }

}
