package net.jackofalltrades.taterbot.channel.profile;

import java.util.Objects;

public class ChannelUserProfileKey {

    private final String channelId;
    private final String userId;

    public ChannelUserProfileKey(String channelId, String userId) {
        this.channelId = channelId;
        this.userId = userId;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelUserProfileKey that = (ChannelUserProfileKey) o;
        return Objects.equals(channelId, that.channelId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, userId);
    }

}

