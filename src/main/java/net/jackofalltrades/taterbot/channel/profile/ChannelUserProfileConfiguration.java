package net.jackofalltrades.taterbot.channel.profile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.linecorp.bot.model.profile.UserProfileResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

@Configuration
class ChannelUserProfileConfiguration {

    @Bean
    @Autowired
    public LoadingCache<ChannelUserProfileKey, UserProfileResponse> channelUserProfileCache(
            CacheLoader<ChannelUserProfileKey, UserProfileResponse> channelUserProfileLoader) {
        return CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(20, TimeUnit.MINUTES)
                .refreshAfterWrite(10, TimeUnit.MINUTES)
                .build(channelUserProfileLoader);
    }

}
