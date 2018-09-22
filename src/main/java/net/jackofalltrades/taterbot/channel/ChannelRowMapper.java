package net.jackofalltrades.taterbot.channel;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Component
class ChannelRowMapper implements RowMapper<Channel> {

    @Override
    public Channel mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        String channelId = resultSet.getString("channel_id");
        boolean member = "Y".equals(resultSet.getString("member"));
        String memberReason = resultSet.getString("member_reason");
        LocalDateTime membershipDate = resultSet.getTimestamp("membership_date").toLocalDateTime();

        return new Channel(channelId, member, memberReason, membershipDate);
    }

}
