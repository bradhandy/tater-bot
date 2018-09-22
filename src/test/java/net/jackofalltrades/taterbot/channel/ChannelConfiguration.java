package net.jackofalltrades.taterbot.channel;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import java.util.concurrent.TimeUnit;

@SpringBootConfiguration
@EnableTransactionManagement
class ChannelConfiguration {

    @Bean
    @Autowired
    LoadingCache<String, Channel> channelCache(ChannelCacheLoader channelCacheLoader) {
        return CacheBuilder.newBuilder()
                .maximumSize(30)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(channelCacheLoader);
    }

}
