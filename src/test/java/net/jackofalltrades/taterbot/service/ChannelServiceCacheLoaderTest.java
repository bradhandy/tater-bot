package net.jackofalltrades.taterbot.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class ChannelServiceCacheLoaderTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ChannelServiceRowMapper channelServiceRowMapper;
    private ChannelServiceDao channelServiceDao;
    private ChannelServiceCacheLoader channelServiceCacheLoader;

    @BeforeEach
    void setUpChannelServiceCacheLoader() {
        channelServiceRowMapper = new ChannelServiceRowMapper();
        channelServiceDao = new ChannelServiceDao(jdbcTemplate, channelServiceRowMapper);
        channelServiceCacheLoader = new ChannelServiceCacheLoader(channelServiceDao);
    }

    @Test
    void channelServiceReturnedSuccessfully() throws Exception {
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.ACTIVE,
                LocalDateTime.now(), "userId");

        doReturn(channelService)
                .when(jdbcTemplate)
                .queryForObject(and(contains("select"), contains("from channel_service")),
                        (ChannelServiceRowMapper) notNull(), eq("channelId"), eq("service"));

        assertSame(channelService, channelServiceCacheLoader.load(new ChannelServiceKey("channelId", "service")),
                "The channel service does not match.");
    }

}
