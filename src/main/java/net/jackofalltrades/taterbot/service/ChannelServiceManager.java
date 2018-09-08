package net.jackofalltrades.taterbot.service;

import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ChannelServiceManager {

    private final LoadingCache<ChannelServiceKey, ChannelService> channelServiceCache;
    private final ChannelServiceDao channelServiceDao;
    private final ChannelServiceHistoryDao channelServiceHistoryDao;
    private final LoadingCache<String, Service> serviceCache;

    public ChannelServiceManager(LoadingCache<ChannelServiceKey, ChannelService> channelServiceCache,
            ChannelServiceDao channelServiceDao, ChannelServiceHistoryDao channelServiceHistoryDao,
            LoadingCache<String, Service> serviceCache) {
        this.channelServiceCache = channelServiceCache;
        this.channelServiceDao = channelServiceDao;
        this.channelServiceHistoryDao = channelServiceHistoryDao;
        this.serviceCache = serviceCache;
    }

    public Optional<ChannelService> findChannelServiceByKey(ChannelServiceKey channelServiceKey) {
        try {
            ChannelService channelService = channelServiceCache.getUnchecked(channelServiceKey);
            return Optional.of(channelService);
        } catch (UncheckedExecutionException e) {
            if (e.getCause() instanceof IncorrectResultSizeDataAccessException) {
                return Optional.absent();
            }
            throw e;
        }
    }

    public void addMissingServicesToChannel(String channelId) {
        List<String> missingServiceCodes = channelServiceDao.findMissingServicesForChannel(channelId);
        for (String missingServiceCode : missingServiceCodes) {
            Service service = serviceCache.getUnchecked(missingServiceCode);
            ChannelService channelService = new ChannelService(channelId, service.getCode(),
                    service.getInitialChannelStatus(), LocalDateTime.now(), null);

            channelServiceDao.insertChannelService(channelService);
        }
    }

}
