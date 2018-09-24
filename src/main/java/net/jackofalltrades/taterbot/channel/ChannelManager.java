package net.jackofalltrades.taterbot.channel;

import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.LeaveEvent;
import net.jackofalltrades.taterbot.event.EventTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ChannelManager {

    private final LoadingCache<String, Channel> channelCache;
    private final ChannelDao channelDao;
    private final ChannelHistoryDao channelHistoryDao;

    @Autowired
    public ChannelManager(LoadingCache<String, Channel> channelCache, ChannelDao channelDao,
            ChannelHistoryDao channelHistoryDao) {
        this.channelCache = channelCache;
        this.channelDao = channelDao;
        this.channelHistoryDao = channelHistoryDao;
    }

    public Optional<Channel> findChannelById(String channelId) {
        try {
            Channel channel = channelCache.getUnchecked(channelId);
            return Optional.of(channel);
        } catch (UncheckedExecutionException e) {
            if (e.getCause() instanceof IncorrectResultSizeDataAccessException) {
                return Optional.absent();
            }
            throw e;
        }
    }

    @EventTask(eventType = JoinEvent.class)
    public void joinChannel(String channelId) {
        joinChannel(channelId, "Invited", LocalDateTime.now());
    }

    @EventTask(eventType = LeaveEvent.class)
    public void leaveChannel(String channelId) {
        leaveChannel(channelId, "Kicked", LocalDateTime.now());
    }

    public void joinChannel(String channelId, String joinReason, LocalDateTime joinDate) {
        toggleChannelMembership(channelId, true, joinReason, joinDate);
    }

    public void leaveChannel(String channelId, String leaveReason, LocalDateTime leaveDate) {
        toggleChannelMembership(channelId, false, leaveReason, leaveDate);
    }

    private void toggleChannelMembership(String channelId, boolean member, String reason, LocalDateTime updateDate) {
        try {
            Channel channel = channelDao.findChannel(channelId);
            if ((channel.isMember() ^ member) && channelDao.updateChannel(channel, member, reason, updateDate)) {
                ChannelHistory channelHistory = new ChannelHistory(channel.getChannelId(), channel.isMember(),
                        channel.getMemberReason(), channel.getMembershipDate(), updateDate);
                channelHistoryDao.insertChannelHistory(channelHistory);
                channelCache.refresh(channelId);
            }
        } catch (IncorrectResultSizeDataAccessException e) {
            channelDao.insertChannel(new Channel(channelId, member, reason, updateDate));
        }
    }

    public List<ChannelHistory> findHistoryForChannelId(String channelId) {
        return channelHistoryDao.findHistoryForChannelId(channelId);
    }
}
