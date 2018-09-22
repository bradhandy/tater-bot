package net.jackofalltrades.taterbot.channel;

import org.springframework.jdbc.core.PreparedStatementSetter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;

class ChannelInsertPreparedStatementSetter implements PreparedStatementSetter {

    private final Channel channel;

    ChannelInsertPreparedStatementSetter(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, channel.getChannelId());
        preparedStatement.setString(2, channel.isMember() ? "Y" : "N");
        preparedStatement.setString(3, channel.getMemberReason());
        preparedStatement.setTimestamp(4, Timestamp.valueOf(channel.getMembershipDate()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelInsertPreparedStatementSetter that = (ChannelInsertPreparedStatementSetter) o;
        return Objects.equals(channel, that.channel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel);
    }

}
