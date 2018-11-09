package net.jackofalltrades.taterbot.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class ChannelRowMapperTest {

    @Mock
    private ResultSet resultSet;

    private ChannelRowMapper channelRowMapper;

    @BeforeEach
    void setUpChannelRowMapper() {
        channelRowMapper = new ChannelRowMapper();
    }

    @Test
    void createChannelWithMembershipFromResult() throws SQLException {
        LocalDateTime membershipDate = LocalDateTime.now();

        doReturn("channelId").when(resultSet).getString("channel_id");
        doReturn("Y").when(resultSet).getString("member");
        doReturn("Invited").when(resultSet).getString("member_reason");
        doReturn(Timestamp.valueOf(membershipDate)).when(resultSet).getTimestamp("membership_date");

        Channel channel = channelRowMapper.mapRow(resultSet, 1);

        assertNotNull(channel, "There should be have been a channel returned.");
        assertEquals("channelId", channel.getChannelId(), "The channel id does not match.");
        assertTrue(channel.isMember(), "The channel membership flag should be true.");
        assertEquals("Invited", channel.getMemberReason(), "The membership reason does not match.");
        assertEquals(membershipDate, channel.getMembershipDate(), "The membership date does not match.");
    }

    @Test
    void createChannelWithoutMembershipFromResult() throws SQLException {
        LocalDateTime membershipDate = LocalDateTime.now();

        doReturn("channelId").when(resultSet).getString("channel_id");
        doReturn("N").when(resultSet).getString("member");
        doReturn("Invited").when(resultSet).getString("member_reason");
        doReturn(Timestamp.valueOf(membershipDate)).when(resultSet).getTimestamp("membership_date");

        Channel channel = channelRowMapper.mapRow(resultSet, 1);

        assertNotNull(channel, "There should be have been a channel returned.");
        assertEquals("channelId", channel.getChannelId(), "The channel id does not match.");
        assertFalse(channel.isMember(), "The channel membership flag should be true.");
        assertEquals("Invited", channel.getMemberReason(), "The membership reason does not match.");
        assertEquals(membershipDate, channel.getMembershipDate(), "The membership date does not match.");
    }

}