package net.jackofalltrades.taterbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class ServiceHistoryDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ServiceHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    void insertServiceHistory(ServiceHistory serviceHistory) {
        int recordsInserted = jdbcTemplate.update(
                "insert into service_history (code, description, status, initial_channel_status, begin_date, end_date) " +
                        "values (?, ?, ?, ?, ?, ?)",
                new ServiceHistoryInsertPreparedStatementSetter(serviceHistory));

        if (recordsInserted != 1) {
            throw new IncorrectUpdateSemanticsDataAccessException(
                    String.format("Should have inserted 1 service history, but inserted %d.", recordsInserted));
        }
    }

}
