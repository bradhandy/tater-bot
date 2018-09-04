package net.jackofalltrades.taterbot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import net.jackofalltrades.taterbot.util.MockitoParameterResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class ServiceManagerTest {

    public static final Service DATABASE_SERVICE =
            new Service("database", "description", Service.Status.ACTIVE, LocalDateTime.now(),
                    Service.Status.INACTIVE);
    @Mock
    private JdbcTemplate jdbcTemplate;

    private ServiceManager serviceManager;
    private ServiceHistoryDao serviceHistoryDao;
    private ServiceDao serviceDao;

    @BeforeEach
    void setUpServiceManager() {
        serviceHistoryDao = new ServiceHistoryDao(jdbcTemplate);
        serviceDao = new ServiceDao(jdbcTemplate, new ServiceRowMapper());
        ServiceCacheLoader serviceCacheLoader = new ServiceCacheLoader(serviceDao);
        LoadingCache<String, Service> serviceLoadingCache = CacheBuilder.newBuilder().build(serviceCacheLoader);
        serviceManager = new ServiceManager(serviceLoadingCache, serviceDao, serviceHistoryDao);
    }

    @Test
    void retrieveServiceFromCache() {
        doReturn(DATABASE_SERVICE).when(jdbcTemplate)
                .queryForObject(contains("select"), (ServiceRowMapper) notNull(), eq("database"));

        Service service = serviceManager.findServiceByCode("database");
        assertSame(DATABASE_SERVICE, service, "The service does not match.");

        service = serviceManager.findServiceByCode("database");
        assertSame(DATABASE_SERVICE, service, "The service does not match.");

        verify(jdbcTemplate, times(1)).queryForObject(contains("select"), (ServiceRowMapper) notNull(), eq("database"));
    }

    @Test
    @ExtendWith(MockitoParameterResolver.class)
    void statusUpdateShouldAddHistoryEntry(@Mock LoadingCache<String, Service> serviceLoadingCache) {
        serviceManager = new ServiceManager(serviceLoadingCache, serviceDao, serviceHistoryDao);

        ArgumentCaptor<ServiceStatusUpdatePreparedStatementSetter> statusUpdatePreparedStatementSetterArgumentCaptor =
                ArgumentCaptor.forClass(ServiceStatusUpdatePreparedStatementSetter.class);
        ArgumentCaptor<ServiceHistoryInsertPreparedStatementSetter>
                serviceHistoryInsertPreparedStatementSetterArgumentCaptor =
                ArgumentCaptor.forClass(ServiceHistoryInsertPreparedStatementSetter.class);

        doReturn(1).when(jdbcTemplate)
                .update(contains("update"), (ServiceStatusUpdatePreparedStatementSetter) notNull());
        doReturn(1).when(jdbcTemplate)
                .update(contains("insert"), (ServiceHistoryInsertPreparedStatementSetter) notNull());

        LocalDateTime comparisonDate = LocalDateTime.now();
        serviceManager.updateServiceStatus(DATABASE_SERVICE, Service.Status.INACTIVE);

        verify(serviceLoadingCache, times(1)).refresh(DATABASE_SERVICE.getCode());
        verify(jdbcTemplate, times(1))
                .update(contains("update"), statusUpdatePreparedStatementSetterArgumentCaptor.capture());
        verify(jdbcTemplate, times(1))
                .update(contains("insert"), serviceHistoryInsertPreparedStatementSetterArgumentCaptor.capture());

        ServiceStatusUpdatePreparedStatementSetter serviceStatusUpdatePreparedStatementSetter =
                statusUpdatePreparedStatementSetterArgumentCaptor.getValue();
        assertEquals(Service.Status.INACTIVE, serviceStatusUpdatePreparedStatementSetter.getServiceStatus(),
                "The new service status does not match.");
        assertFalse(comparisonDate.isAfter(serviceStatusUpdatePreparedStatementSetter.getServiceStatusDate()),
                "The new service status date should be on or after the control date.");

        ServiceHistoryInsertPreparedStatementSetter serviceHistoryInsertPreparedStatementSetter =
                serviceHistoryInsertPreparedStatementSetterArgumentCaptor.getValue();
        assertEquals(serviceStatusUpdatePreparedStatementSetter.getServiceStatusDate(),
                serviceHistoryInsertPreparedStatementSetter.getServiceHistory().getEndDate(),
                "The service history end date does not match the service status date.");
    }

    @Test
    void statusUpdateFails() {
        doReturn(0).when(jdbcTemplate)
                .update(contains("update"), (ServiceStatusUpdatePreparedStatementSetter) notNull());

        serviceManager.updateServiceStatus(DATABASE_SERVICE, Service.Status.INACTIVE);

        verify(jdbcTemplate, times(1))
                .update(contains("update"), (ServiceStatusUpdatePreparedStatementSetter) notNull());
        verify(jdbcTemplate, never())
                .update(contains("insert"), (ServiceHistoryInsertPreparedStatementSetter) notNull());
    }

    @Test
    void statusUpdateFailsDueToUpdatingTooManyRows() {
        doReturn(2).when(jdbcTemplate)
                .update(contains("update"), (ServiceStatusUpdatePreparedStatementSetter) notNull());

        assertThrows(IncorrectUpdateSemanticsDataAccessException.class,
                () -> serviceManager.updateServiceStatus(DATABASE_SERVICE, Service.Status.INACTIVE));
        verify(jdbcTemplate, times(1))
                .update(contains("update"), (ServiceStatusUpdatePreparedStatementSetter) notNull());
        verify(jdbcTemplate, never())
                .update(contains("insert"), (ServiceHistoryInsertPreparedStatementSetter) notNull());
    }

    @Test
    void retrieveServiceFromCacheFailure() {
        doThrow(new IncorrectResultSizeDataAccessException(1, 0)).when(jdbcTemplate)
                .queryForObject(notNull(), (ServiceRowMapper) notNull(), eq("notthere"));
        assertSame(Service.UNKNOWN_SERVICE, serviceManager.findServiceByCode("notthere"),
                "The service does not match.");
    }

    @Test
    void updatingServiceToSameStatusDoesNothing() {
        serviceManager.updateServiceStatus(DATABASE_SERVICE, Service.Status.ACTIVE);

        verify(jdbcTemplate, never()).update(anyString(), (PreparedStatementSetter) notNull());
    }

}
