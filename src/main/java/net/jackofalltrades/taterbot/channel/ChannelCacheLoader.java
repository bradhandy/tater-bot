package net.jackofalltrades.taterbot.channel;

import com.google.common.cache.CacheLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ChannelCacheLoader extends CacheLoader<String, Channel> {

    private final ChannelDao channelDao;

    @Autowired
    ChannelCacheLoader(ChannelDao channelDao) {
        this.channelDao = channelDao;
    }

    @Override
    public Channel load(String channelId) throws Exception {
        return channelDao.findChannel(channelId);
    }

}
