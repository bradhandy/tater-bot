package net.jackofalltrades.taterbot.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@ExtendWith(MockitoExtension.class)
class ServiceHistoryInsertPreparedStatementSetterTest {

    @Mock
    private PreparedStatement preparedStatement;

    @Test
    void serviceHistoryDataSetCorrectly() throws SQLException {
        LocalDateTime statusEndDate = LocalDateTime.now();
        LocalDateTime statusBeginDate = statusEndDate.minus(5, ChronoUnit.MINUTES);
        ServiceHistory serviceHistory = new ServiceHistory("code", "description", Service.Status.INACTIVE,
                Service.Status.ACTIVE, statusBeginDate, statusEndDate);

        ServiceHistoryInsertPreparedStatementSetter serviceHistoryInsertPreparedStatementSetter =
                new ServiceHistoryInsertPreparedStatementSetter(serviceHistory);
        serviceHistoryInsertPreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement).setString(1, "code");
        verify(preparedStatement).setString(2, "description");
        verify(preparedStatement).setString(3, "inactive");
        verify(preparedStatement).setString(4, "active");
        verify(preparedStatement).setTimestamp(5, Timestamp.valueOf(statusBeginDate));
        verify(preparedStatement).setTimestamp(6, Timestamp.valueOf(statusEndDate));
    }

}