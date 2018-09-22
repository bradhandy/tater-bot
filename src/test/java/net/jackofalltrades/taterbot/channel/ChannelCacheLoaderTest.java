package net.jackofalltrades.taterbot.channel;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class ChannelCacheLoaderTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ChannelDao channelDao;
    private ChannelRowMapper channelRowMapper;
    private ChannelCacheLoader channelCacheLoader;

    @BeforeEach
    void setUpChannelCacheLoader() {
        channelRowMapper = new ChannelRowMapper();
        channelDao = new ChannelDao(jdbcTemplate, channelRowMapper);
        channelCacheLoader = new ChannelCacheLoader(channelDao);
    }

    @Test
    void channelReturnedSuccessfully() throws Exception {
        Channel channel = new Channel("channelId", true, "Invited", LocalDateTime.now());

        doReturn(channel)
                .when(jdbcTemplate)
                .queryForObject(contains("from channel"), same(channelRowMapper), eq("channelId"));

        assertSame(channel, channelCacheLoader.load("channelId"), "The channel returned does not match.");
    }

}
