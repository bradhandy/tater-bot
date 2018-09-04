package net.jackofalltrades.taterbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class ChannelServiceDao {

    private final JdbcTemplate jdbcTemplate;
    private final ChannelServiceRowMapper channelServiceRowMapper;

    @Autowired
    ChannelServiceDao(JdbcTemplate jdbcTemplate, ChannelServiceRowMapper channelServiceRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.channelServiceRowMapper = channelServiceRowMapper;
    }

    ChannelService findChannelService(ChannelServiceKey channelServiceKey) {
        return jdbcTemplate.queryForObject("select * from channel_service where channel_id = ? and service_code = ?",
                channelServiceRowMapper, channelServiceKey.getChannelId(), channelServiceKey.getServiceCode());
    }

}
