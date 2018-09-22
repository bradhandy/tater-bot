package net.jackofalltrades.taterbot.channel;

import java.time.LocalDateTime;
import java.util.Objects;

public class Channel {

    private final String channelId;
    private final boolean member;
    private final String memberReason;
    private final LocalDateTime membershipDate;

    public Channel(String channelId, boolean member, String memberReason, LocalDateTime membershipDate) {
        this.channelId = channelId;
        this.member = member;
        this.memberReason = memberReason;
        this.membershipDate = membershipDate;
    }

    public String getChannelId() {
        return channelId;
    }

    public boolean isMember() {
        return member;
    }

    public String getMemberReason() {
        return memberReason;
    }

    public LocalDateTime getMembershipDate() {
        return membershipDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Channel channel = (Channel) o;
        return Objects.equals(channelId, channel.channelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId);
    }

}
