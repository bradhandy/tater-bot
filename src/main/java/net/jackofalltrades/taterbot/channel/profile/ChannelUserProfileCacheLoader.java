package net.jackofalltrades.taterbot.channel.profile;

import com.google.common.cache.CacheLoader;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.profile.UserProfileResponse;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
class ChannelUserProfileCacheLoader extends CacheLoader<ChannelUserProfileKey, UserProfileResponse> {

    private final LineMessagingClient lineMessagingClient;

    public ChannelUserProfileCacheLoader(LineMessagingClient lineMessagingClient) {
        this.lineMessagingClient = lineMessagingClient;
    }

    @Override
    public UserProfileResponse load(ChannelUserProfileKey key) throws Exception {
        return lineMessagingClient
                .getGroupMemberProfile(key.getChannelId(), key.getUserId())
                .get(5, TimeUnit.SECONDS);
    }

}
