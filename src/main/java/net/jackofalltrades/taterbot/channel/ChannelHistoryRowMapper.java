package net.jackofalltrades.taterbot.channel;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Component
class ChannelHistoryRowMapper implements RowMapper<ChannelHistory> {

    @Override
    public ChannelHistory mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        String channelId = resultSet.getString("channel_id");
        boolean member = resultSet.getString("member").equals("Y");
        String memberReason = resultSet.getString("member_reason");
        LocalDateTime beginDate = resultSet.getTimestamp("begin_date").toLocalDateTime();
        LocalDateTime endDate = resultSet.getTimestamp("end_date").toLocalDateTime();

        return new ChannelHistory(channelId, member, memberReason, beginDate, endDate);
    }

}
