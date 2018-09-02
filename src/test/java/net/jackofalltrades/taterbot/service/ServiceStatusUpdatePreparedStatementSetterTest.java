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

@ExtendWith(MockitoExtension.class)
class ServiceStatusUpdatePreparedStatementSetterTest {

    @Mock
    private PreparedStatement preparedStatement;

    @Test
    void setWhereClauseFieldsCorrectly() throws SQLException {
        LocalDateTime statusDate = LocalDateTime.now();
        ServiceStatusUpdatePreparedStatementSetter serviceStatusUpdatePreparedStatementSetter =
                new ServiceStatusUpdatePreparedStatementSetter(new Service("code",
                        "description", Service.Status.ACTIVE, statusDate, Service.Status.INACTIVE),
                        Service.Status.INACTIVE);

        serviceStatusUpdatePreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement).setString(1, "inactive");
        verify(preparedStatement).setString(2, "code");
        verify(preparedStatement).setString(3, "active");
        verify(preparedStatement).setTimestamp(4, Timestamp.valueOf(statusDate));
    }

}