package net.jackofalltrades.taterbot.channel;

import org.springframework.jdbc.core.PreparedStatementSetter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;

class ChannelHistoryInsertPreparedStatementSetter implements PreparedStatementSetter {

    private final ChannelHistory channelHistory;

    ChannelHistoryInsertPreparedStatementSetter(ChannelHistory channelHistory) {
        this.channelHistory = channelHistory;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, channelHistory.getChannelId());
        preparedStatement.setString(2, channelHistory.isMember() ? "Y" : "N");
        preparedStatement.setString(3, channelHistory.getMemberReason());
        preparedStatement.setTimestamp(4, Timestamp.valueOf(channelHistory.getBeginDate()));
        preparedStatement.setTimestamp(5, Timestamp.valueOf(channelHistory.getEndDate()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelHistoryInsertPreparedStatementSetter that = (ChannelHistoryInsertPreparedStatementSetter) o;
        return Objects.equals(channelHistory, that.channelHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelHistory);
    }

}
