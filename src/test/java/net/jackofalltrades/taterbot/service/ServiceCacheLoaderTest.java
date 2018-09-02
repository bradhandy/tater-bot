package net.jackofalltrades.taterbot.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class ServiceCacheLoaderTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ServiceRowMapper serviceRowMapper;
    private ServiceDao serviceDao;
    private ServiceCacheLoader serviceCacheLoader;

    @BeforeEach
    void setUpServiceCacheLoader() {
        serviceRowMapper = new ServiceRowMapper();
        serviceDao = new ServiceDao(jdbcTemplate, serviceRowMapper);
        serviceCacheLoader = new ServiceCacheLoader(serviceDao);
    }

    @Test
    void serviceReturnedSuccessfully() throws Exception {
        Service expectedService = new Service("code", "description", Service.Status.ENABLED, LocalDateTime.now(),
                Service.Status.INACTIVE);

        doReturn(expectedService).when(jdbcTemplate).queryForObject(notNull(), same(serviceRowMapper), eq("code"));

        assertSame(expectedService, serviceCacheLoader.load("code"), "The service could not be loaded.");
    }

}
