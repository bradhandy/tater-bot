package net.jackofalltrades.taterbot.service;

import com.google.common.cache.CacheLoader;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Component;

@Component
class ServiceCacheLoader extends CacheLoader<String, Service> {

    private final ServiceDao serviceDao;

    public ServiceCacheLoader(ServiceDao serviceDao) {
        this.serviceDao = serviceDao;
    }

    @Override
    public Service load(String serviceCode) throws Exception {
        try {
            return serviceDao.findService(serviceCode);
        } catch (IncorrectResultSizeDataAccessException e) {
            return Service.UNKNOWN_SERVICE;
        }
    }

}
