package net.jackofalltrades.taterbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
class ChannelServiceDao {

    private final JdbcTemplate jdbcTemplate;
    private final ChannelServiceRowMapper channelServiceRowMapper;
    private final StringColumnListResultSetExtractor stringColumnListResultSetExtractor;

    @Autowired
    ChannelServiceDao(JdbcTemplate jdbcTemplate, ChannelServiceRowMapper channelServiceRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.channelServiceRowMapper = channelServiceRowMapper;
        this.stringColumnListResultSetExtractor = new StringColumnListResultSetExtractor("code");
    }

    ChannelService findChannelService(ChannelServiceKey channelServiceKey) {
        return jdbcTemplate.queryForObject("select * from channel_service where channel_id = ? and service_code = ?",
                channelServiceRowMapper, channelServiceKey.getChannelId(), channelServiceKey.getServiceCode());
    }

    void insertChannelService(ChannelService channelService) {
        int recordsInserted = jdbcTemplate.update(
                "insert into channel_service (channel_id, service_code, status, status_date, user_id) " +
                        "values (?, ?, ?, ?, ?)",
                new ChannelServiceInsertPreparedStatementSetter(channelService));

        if (recordsInserted != 1) {
            throw new IncorrectUpdateSemanticsDataAccessException(
                    String.format("Expected to insert 1 channel service record, but inserted %d.", recordsInserted));
        }
    }

    boolean updateChannelServiceStatus(ChannelService channelService, Service.Status channelServiceStatus,
            LocalDateTime channelServiceStatusDate, String updatingUser) {
        int recordsUpdated = jdbcTemplate.update(new ChannelServiceUpdatePreparedStatementCreator(channelService,
                channelServiceStatus, channelServiceStatusDate, updatingUser));
        if (recordsUpdated > 1) {
            throw new IncorrectUpdateSemanticsDataAccessException(
                    String.format("Expected to update 1 channel service record, but updated %d records.",
                            recordsUpdated));
        }

        return recordsUpdated == 1;
    }

    List<String> findMissingServicesForChannel(String channelId) {
        return jdbcTemplate.query(
                "select code " +
                        "from service left outer join (select * from channel_service where channel_id = ?) cs " +
                        "       on (cs.service_code = service.code) " +
                        "where cs.service_code is null", stringColumnListResultSetExtractor,
                channelId);
    }
}
