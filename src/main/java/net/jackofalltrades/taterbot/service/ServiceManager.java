package net.jackofalltrades.taterbot.service;

import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
public class ServiceManager {

    private LoadingCache<String, Service> serviceLoadingCache;
    private ServiceDao serviceDao;
    private ServiceHistoryDao serviceHistoryDao;

    public ServiceManager(@Qualifier("serviceCache") LoadingCache<String, Service> serviceLoadingCache,
            ServiceDao serviceDao, ServiceHistoryDao serviceHistoryDao) {
        this.serviceLoadingCache = serviceLoadingCache;
        this.serviceDao = serviceDao;
        this.serviceHistoryDao = serviceHistoryDao;
    }

    public Service findServiceByCode(String serviceCode) {
        return serviceLoadingCache.getUnchecked(serviceCode);
    }

    @Transactional(rollbackFor = IncorrectUpdateSemanticsDataAccessException.class)
    public void updateServiceStatus(Service service, Service.Status serviceStatus) {
        if (service.getStatus() == serviceStatus) {
            return;
        }

        LocalDateTime serviceStatusDate = LocalDateTime.now();
        ServiceHistory serviceHistory = createServiceHistory(service, serviceStatusDate);

        if (serviceDao.updateServiceStatus(service, serviceStatus, serviceStatusDate)) {
            serviceHistoryDao.insertServiceHistory(serviceHistory);
            serviceLoadingCache.refresh(service.getCode());
        }
    }

    private ServiceHistory createServiceHistory(Service service, LocalDateTime endDateTime) {
        return new ServiceHistory(service.getCode(), service.getDescription(), service.getStatus(),
                service.getInitialChannelStatus(), service.getStatusDate(), endDateTime);
    }

}
