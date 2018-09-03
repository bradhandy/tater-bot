package net.jackofalltrades.taterbot.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@ExtendWith(MockitoExtension.class)
class ServiceStatusUpdatePreparedStatementSetterTest {

    @Mock
    private PreparedStatement preparedStatement;

    @Test
    void setWhereClauseFieldsCorrectly() throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime statusDate = now.minus(5, ChronoUnit.DAYS);
        ServiceStatusUpdatePreparedStatementSetter serviceStatusUpdatePreparedStatementSetter =
                new ServiceStatusUpdatePreparedStatementSetter(new Service("code",
                        "description", Service.Status.ACTIVE, statusDate, Service.Status.INACTIVE),
                        Service.Status.INACTIVE, now);

        serviceStatusUpdatePreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement).setString(1, "inactive");
        verify(preparedStatement).setTimestamp(2, Timestamp.valueOf(now));
        verify(preparedStatement).setString(3, "code");
        verify(preparedStatement).setString(4, "active");
        verify(preparedStatement).setTimestamp(5, Timestamp.valueOf(statusDate));
    }

}