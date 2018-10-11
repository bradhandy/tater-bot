package net.jackofalltrades.taterbot.channel.record;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ChannelRecordProcessingRowCallbackHandlerTest {

    @Mock
    private ResultSet resultSet;

    @Mock
    private ChannelRecordProcessor channelRecordProcessor;

    private ChannelRecordProcessingRowCallbackHandler channelRecordProcessingRowCallbackHandler;

    @BeforeEach
    void setUpChannelRecordProcessingRowCallbackHandler() {
        ChannelRecordRowMapper channelRecordRowMapper = new ChannelRecordRowMapper();
        channelRecordProcessingRowCallbackHandler =
                new ChannelRecordProcessingRowCallbackHandler(channelRecordRowMapper, channelRecordProcessor);
    }

    @Test
    void callChannelProcessorForAllChannelRecords() throws SQLException {
        LocalDateTime messageTimestamp = LocalDateTime.now();
        List<ChannelRecord> expectedChannelRecords = Lists.newArrayList(
                new ChannelRecord("channelId", "userId", "displayName", "text", messageTimestamp, "message"),
                new ChannelRecord("channelId2", "userId2", "displayName2", "text", messageTimestamp, "message2"));
        doReturn("channelId", "channelId2").when(resultSet).getString("channel_id");
        doReturn("userId", "userId2").when(resultSet).getString("user_id");
        doReturn("displayName", "displayName2").when(resultSet).getString("user_display_name");
        doReturn("text").when(resultSet).getString("message_type");
        doReturn(Timestamp.valueOf(messageTimestamp)).when(resultSet).getTimestamp("message_timestamp");
        doReturn("message", "message2").when(resultSet).getString("message");

        expectedChannelRecords.forEach(channelRecord -> {
            try {
                channelRecordProcessingRowCallbackHandler.processRow(resultSet);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        verify(channelRecordProcessor, times(1)).processChannelRecord(expectedChannelRecords.get(0));
        verify(channelRecordProcessor, times(1)).processChannelRecord(expectedChannelRecords.get(1));
    }

}