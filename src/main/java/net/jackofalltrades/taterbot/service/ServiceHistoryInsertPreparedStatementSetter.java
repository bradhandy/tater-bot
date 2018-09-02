package net.jackofalltrades.taterbot.service;

import org.springframework.jdbc.core.PreparedStatementSetter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

class ServiceHistoryInsertPreparedStatementSetter implements PreparedStatementSetter {

    private final ServiceHistory serviceHistory;

    ServiceHistoryInsertPreparedStatementSetter(ServiceHistory serviceHistory) {
        this.serviceHistory = serviceHistory;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, serviceHistory.getCode());
        preparedStatement.setString(2, serviceHistory.getDescription());
        preparedStatement.setString(3, serviceHistory.getStatus().name().toLowerCase());
        preparedStatement.setString(4, serviceHistory.getInitialChannelStatus().name().toLowerCase());
        preparedStatement.setTimestamp(5, Timestamp.valueOf(serviceHistory.getBeginDate()));
        preparedStatement.setTimestamp(6, Timestamp.valueOf(serviceHistory.getEndDate()));
    }

}
