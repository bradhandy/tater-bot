package net.jackofalltrades.taterbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
class ServiceDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Service> serviceRowMapper;

    @Autowired
    ServiceDao(JdbcTemplate jdbcTemplate, RowMapper<Service> serviceRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.serviceRowMapper = serviceRowMapper;
    }

    Service findService(String serviceCode) {
        return jdbcTemplate.queryForObject("select * from service where code = ?", serviceRowMapper, serviceCode);
    }

    boolean updateServiceStatus(Service service, Service.Status status) {
        int updatedRecordCount = jdbcTemplate.update(
                "update service set status = ? where code = ? and status = ? and status_date = ?",
                new ServiceStatusUpdatePreparedStatementSetter(service, status));
        if (updatedRecordCount > 1) {
            throw new IncorrectUpdateSemanticsDataAccessException(
                    String.format("Should have update 1 service, but %d were updated.", updatedRecordCount));
        }

        return updatedRecordCount == 1;
    }

}
