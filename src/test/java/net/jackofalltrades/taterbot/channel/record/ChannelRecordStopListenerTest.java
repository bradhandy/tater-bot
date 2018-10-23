package net.jackofalltrades.taterbot.channel.record;

import static net.jackofalltrades.taterbot.util.ReplyMessageAssertions.assertPushMessageForClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.profile.UserProfileResponse;
import net.jackofalltrades.taterbot.channel.profile.ChannelUserProfileKey;
import net.jackofalltrades.taterbot.channel.record.event.ChannelRecordStopEvent;
import net.jackofalltrades.taterbot.service.ChannelService;
import net.jackofalltrades.taterbot.service.ChannelServiceFactory;
import net.jackofalltrades.taterbot.service.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ChannelRecordStopListenerTest {

    static final String BASE_TRANSCRIPT_URL = "s3://my-bucket/";
    static final String BASE_DOWNLOAD_URL = "https://download/";

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private LineMessagingClient lineMessagingClient;

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private WritableResource writableResource;

    @Mock
    private OutputStream outputStream;

    @Spy
    private ChannelRecordRowMapper channelRecordRowMapper;

    @Mock
    private ResultSet resultSet;

    @Mock
    private LoadingCache<ChannelUserProfileKey, UserProfileResponse> userProfileCache;

    private ChannelRecordStopListener channelRecordStopListener;

    @BeforeEach
    void setUpChannelRecordStopListener() {
        ChannelRecordDao channelRecordDao = new ChannelRecordDao(jdbcTemplate, channelRecordRowMapper);
        channelRecordStopListener = new ChannelRecordStopListener(lineMessagingClient, resourceLoader, channelRecordDao,
                BASE_TRANSCRIPT_URL, BASE_DOWNLOAD_URL);
    }

    @Test
    void emptyFileShouldHaveNoContent() throws IOException {
        LocalDateTime channelServiceStatusTime = LocalDateTime.now().minus(10, ChronoUnit.MINUTES);
        ChannelService channelService = ChannelServiceFactory.createChannelServiceFactory("channelId", "record",
                Service.Status.ACTIVE, channelServiceStatusTime, "userId");
        ChannelRecordStopEvent channelRecordStopEvent = new ChannelRecordStopEvent(this, channelService);

        String expectedFileName = createExpectedFileName(channelService);
        LocalDateTime channelServiceStatusEndTime = LocalDateTime
                .ofInstant(Instant.ofEpochMilli(channelRecordStopEvent.getTimestamp()), ZoneId.systemDefault());
        doAnswer(new ChannelRecordProvider())
                .when(jdbcTemplate)
                .query(contains("from channel_record"), any(RowCallbackHandler.class), eq("channelId"),
                        eq(Timestamp.valueOf(channelServiceStatusTime)),
                        eq(Timestamp.valueOf(channelServiceStatusEndTime)));
        doReturn(writableResource).when(resourceLoader).getResource(BASE_TRANSCRIPT_URL + expectedFileName);
        doReturn(outputStream).when(writableResource).getOutputStream();

        channelRecordStopListener.onApplicationEvent(channelRecordStopEvent);

        assertPushMessageForClient(lineMessagingClient, channelService.getChannelId(),
                "Download the transcript @ https://download/" + expectedFileName);

        verify(resourceLoader).getResource(BASE_TRANSCRIPT_URL + expectedFileName);
        verify(writableResource).getOutputStream();
        verify(outputStream, never()).write(any(byte[].class));
        verify(outputStream).close();
    }

    @Test
    void fileShouldHaveContent() throws IOException {
        List<ChannelRecord> channelRecords = Lists.newArrayList(
                new ChannelRecord("channelId", "userId", null, "text", LocalDateTime.now(), "message"),
                new ChannelRecord("channelId", "resolvedUserId", "displayName", "text", LocalDateTime.now(), "msg"));

        LocalDateTime channelServiceStatusTime = LocalDateTime.now().minus(10, ChronoUnit.MINUTES);
        ChannelService channelService = ChannelServiceFactory.createChannelServiceFactory("channelId", "record",
                Service.Status.ACTIVE, channelServiceStatusTime, "userId");
        ChannelRecordStopEvent channelRecordStopEvent = new ChannelRecordStopEvent(this, channelService);

        String expectedFileName = createExpectedFileName(channelService);
        LocalDateTime channelServiceStatusEndTime = LocalDateTime
                .ofInstant(Instant.ofEpochMilli(channelRecordStopEvent.getTimestamp()), ZoneId.systemDefault());
        doAnswer(new ChannelRecordProvider(channelRecords))
                .when(jdbcTemplate)
                .query(contains("from channel_record"), any(RowCallbackHandler.class), eq("channelId"),
                        eq(Timestamp.valueOf(channelServiceStatusTime)),
                        eq(Timestamp.valueOf(channelServiceStatusEndTime)));
        doReturn(writableResource).when(resourceLoader).getResource(BASE_TRANSCRIPT_URL + expectedFileName);
        doReturn(outputStream).when(writableResource).getOutputStream();

        channelRecordStopListener.onApplicationEvent(channelRecordStopEvent);

        assertPushMessageForClient(lineMessagingClient, channelService.getChannelId(),
                "Download the transcript @ https://download/" + expectedFileName);

        verify(outputStream).write("[user name unavailable]   - message\n".getBytes(StandardCharsets.UTF_8));
        verify(outputStream).write("displayName               - msg\n".getBytes(StandardCharsets.UTF_8));
        verify(outputStream).close();
    }

    private String createExpectedFileName(ChannelService channelService) {
        long timeInMillis = channelService.getStatusDate().toInstant(ZoneOffset.UTC).toEpochMilli();
        return String.format("%s-%s-%d.txt", channelService.getChannelId(), channelService.getUserId(), timeInMillis);
    }

    private class ChannelRecordProvider implements Answer<Void> {

        private final List<ChannelRecord> channelRecords;

        private ChannelRecordProvider(List<ChannelRecord> channelRecords) {
            this.channelRecords = channelRecords;
        }

        private ChannelRecordProvider() {
            this(Lists.newArrayList());
        }

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            if (!channelRecords.isEmpty()) {
                ChannelRecord[] subsequentChannelRecords =
                        channelRecords.subList(1, channelRecords.size()).toArray(new ChannelRecord[0]);
                doReturn(channelRecords.get(0), subsequentChannelRecords)
                        .when(channelRecordRowMapper)
                        .mapRow(same(resultSet), anyInt());
            }

            RowCallbackHandler rowCallbackHandler = invocation.getArgument(1);
            for (int i = 0; i < channelRecords.size(); i++) {
                rowCallbackHandler.processRow(resultSet);
            }

            return null;
        }

    }

}
