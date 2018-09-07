package net.jackofalltrades.taterbot.service;

import com.google.common.base.Strings;
import org.springframework.jdbc.core.PreparedStatementSetter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

class ChannelServiceHistoryPreparedStatementSetter implements PreparedStatementSetter {

    private final ChannelServiceHistory channelServiceHistory;

    public ChannelServiceHistoryPreparedStatementSetter(ChannelServiceHistory channelServiceHistory) {
        this.channelServiceHistory = channelServiceHistory;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, channelServiceHistory.getChannelId());
        preparedStatement.setString(2, channelServiceHistory.getService());
        preparedStatement.setString(3, channelServiceHistory.getStatus().name().toLowerCase());
        preparedStatement.setTimestamp(4, Timestamp.valueOf(channelServiceHistory.getBeginDate()));
        preparedStatement.setTimestamp(5, Timestamp.valueOf(channelServiceHistory.getEndDate()));

        if (Strings.isNullOrEmpty(channelServiceHistory.getUserId())) {
            preparedStatement.setNull(6, Types.VARCHAR);
        } else {
            preparedStatement.setString(6, channelServiceHistory.getUserId());
        }
    }

    ChannelServiceHistory getChannelServiceHistory() {
        return channelServiceHistory;
    }

}
