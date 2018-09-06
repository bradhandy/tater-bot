package net.jackofalltrades.taterbot.service;

import com.google.common.cache.CacheLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ChannelServiceCacheLoader extends CacheLoader<ChannelServiceKey, ChannelService> {

    private final ChannelServiceDao channelServiceDao;

    @Autowired
    public ChannelServiceCacheLoader(ChannelServiceDao channelServiceDao) {
        this.channelServiceDao = channelServiceDao;
    }

    @Override
    public ChannelService load(ChannelServiceKey channelServiceKey) throws Exception {
        return channelServiceDao.findChannelService(channelServiceKey);
    }

}
