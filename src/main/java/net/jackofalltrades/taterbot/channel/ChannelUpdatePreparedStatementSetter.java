package net.jackofalltrades.taterbot.channel;

import org.springframework.jdbc.core.PreparedStatementSetter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

class ChannelUpdatePreparedStatementSetter implements PreparedStatementSetter {

    private final Channel channel;
    private final boolean member;
    private final String memberReason;
    private final LocalDateTime membershipTime;

    ChannelUpdatePreparedStatementSetter(Channel channel, boolean member, String memberReason,
            LocalDateTime membershipTime) {
        this.channel = channel;
        this.member = member;
        this.memberReason = memberReason;
        this.membershipTime = membershipTime;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, member ? "Y" : "N");
        preparedStatement.setString(2, memberReason);
        preparedStatement.setTimestamp(3, Timestamp.valueOf(membershipTime));
        preparedStatement.setString(4, channel.getChannelId());
        preparedStatement.setString(5, channel.isMember() ? "Y" : "N");
        preparedStatement.setString(6, channel.getMemberReason());
        preparedStatement.setTimestamp(7, Timestamp.valueOf(channel.getMembershipDate()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelUpdatePreparedStatementSetter that = (ChannelUpdatePreparedStatementSetter) o;
        return member == that.member && Objects.equals(channel, that.channel) &&
                Objects.equals(memberReason, that.memberReason) && Objects.equals(membershipTime, that.membershipTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, member, memberReason, membershipTime);
    }

}
