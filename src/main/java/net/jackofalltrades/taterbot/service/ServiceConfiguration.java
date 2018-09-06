package net.jackofalltrades.taterbot.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.util.concurrent.TimeUnit;

@SpringBootConfiguration
@EnableTransactionManagement
public class ServiceConfiguration {

    @Bean
    @Autowired
    LoadingCache<String, Service> serviceCache(ServiceCacheLoader serviceCacheLoader) {
        return CacheBuilder.newBuilder()
                .maximumSize(10)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(serviceCacheLoader);
    }

    @Bean
    @Autowired
    LoadingCache<ChannelServiceKey, ChannelService> channelServiceCache(ChannelServiceCacheLoader channelServiceCacheLoader) {
        return CacheBuilder.newBuilder()
                .maximumSize(30)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(channelServiceCacheLoader);
    }

}
