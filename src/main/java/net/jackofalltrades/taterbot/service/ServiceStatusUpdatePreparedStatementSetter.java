package net.jackofalltrades.taterbot.service;

import org.springframework.jdbc.core.PreparedStatementSetter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

class ServiceStatusUpdatePreparedStatementSetter implements PreparedStatementSetter {

    private final Service.Status serviceStatus;
    private final Service service;

    ServiceStatusUpdatePreparedStatementSetter(Service service, Service.Status serviceStatus) {
        this.serviceStatus = serviceStatus;
        this.service = service;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, serviceStatus.name().toLowerCase());
        preparedStatement.setString(2, service.getCode());
        preparedStatement.setString(3, service.getStatus().name().toLowerCase());
        preparedStatement.setTimestamp(4, Timestamp.valueOf(service.getStatusDate()));
    }

}
