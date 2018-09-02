package net.jackofalltrades.taterbot.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import java.util.concurrent.TimeUnit;

@SpringBootConfiguration
public class ServiceConfiguration {

    @Bean
    @Autowired
    LoadingCache<String, Service> serviceCache(ServiceCacheLoader serviceCacheLoader) {
        return CacheBuilder.newBuilder()
                .maximumSize(10)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(serviceCacheLoader);
    }

}
