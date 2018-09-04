package net.jackofalltrades.taterbot.service;

import static org.junit.jupiter.api.Assertions.assertSame;
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
class ChannelServiceDaoTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ChannelServiceDao channelServiceDao;

    @BeforeEach
    void setUpChannelServiceDao() {
        channelServiceDao = new ChannelServiceDao(jdbcTemplate, new ChannelServiceRowMapper());
    }

    @Test
    void retrieveByChannelServiceKey() {
        ChannelServiceKey channelServiceKey = new ChannelServiceKey("channelId", "service");
        ChannelService databaseChannelService = new ChannelService("channelId", "service", Service.Status.ACTIVE,
                LocalDateTime.now(), "userId");

        doReturn(databaseChannelService).when(jdbcTemplate)
                .queryForObject(eq("select * from channel_service where channel_id = ? and service_code = ?"),
                        (ChannelServiceRowMapper) notNull(), eq("channelId"), eq("service"));

        ChannelService channelService = channelServiceDao.findChannelService(channelServiceKey);
        assertSame(databaseChannelService, channelService, "The channel service did not match.");
    }

}
