package net.jackofalltrades.taterbot.service;

import org.springframework.jdbc.core.PreparedStatementSetter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

class ServiceStatusUpdatePreparedStatementSetter implements PreparedStatementSetter {

    private final Service service;
    private final Service.Status serviceStatus;
    private final LocalDateTime serviceStatusDate;

    ServiceStatusUpdatePreparedStatementSetter(Service service, Service.Status serviceStatus,
            LocalDateTime serviceStatusDate) {
        this.service = service;
        this.serviceStatus = serviceStatus;
        this.serviceStatusDate = serviceStatusDate;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, serviceStatus.name().toLowerCase());
        preparedStatement.setTimestamp(2, Timestamp.valueOf(serviceStatusDate));
        preparedStatement.setString(3, service.getCode());
        preparedStatement.setString(4, service.getStatus().name().toLowerCase());
        preparedStatement.setTimestamp(5, Timestamp.valueOf(service.getStatusDate()));
    }

    Service getService() {
        return service;
    }

    Service.Status getServiceStatus() {
        return serviceStatus;
    }

    LocalDateTime getServiceStatusDate() {
        return serviceStatusDate;
    }

}
