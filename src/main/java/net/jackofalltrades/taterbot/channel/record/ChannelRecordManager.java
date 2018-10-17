package net.jackofalltrades.taterbot.channel.record;

import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.profile.UserProfileResponse;
import net.jackofalltrades.taterbot.channel.profile.ChannelUserProfileKey;
import net.jackofalltrades.taterbot.service.ChannelService;
import net.jackofalltrades.taterbot.service.ChannelServiceKey;
import net.jackofalltrades.taterbot.service.ChannelServiceManager;
import net.jackofalltrades.taterbot.service.ChannelServiceStatusException;
import net.jackofalltrades.taterbot.service.Service;
import net.jackofalltrades.taterbot.service.ServiceDisabledException;
import net.jackofalltrades.taterbot.service.ServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutionException;

@Component
public class ChannelRecordManager {

    private final ServiceManager serviceManager;
    private final ChannelServiceManager channelServiceManager;
    private final ChannelRecordDao channelRecordDao;
    private final LoadingCache<ChannelUserProfileKey, UserProfileResponse> channelUserProfileCache;

    @Autowired
    public ChannelRecordManager(ServiceManager serviceManager, ChannelServiceManager channelServiceManager,
            ChannelRecordDao channelRecordDao,
            LoadingCache<ChannelUserProfileKey, UserProfileResponse> channelUserProfileCache) {
        this.serviceManager = serviceManager;
        this.channelServiceManager = channelServiceManager;
        this.channelRecordDao = channelRecordDao;
        this.channelUserProfileCache = channelUserProfileCache;
    }

    public void disableChannelRecordService(String channelId, String userId) {
        ChannelServiceKey channelServiceKey = new ChannelServiceKey(channelId, Service.RECORD_SERVICE_CODE);
        ChannelService channelService = channelServiceManager.findChannelServiceByKey(channelServiceKey).orNull();
        if (channelService == null || channelService.getStatus() == Service.Status.DISABLED) {
            return;
        } else if (channelService.getStatus() == Service.Status.ACTIVE) {
            throw new ChannelRecordActiveException("Cannot disable channel record while active.");
        }

        channelServiceManager.updateChannelServiceStatus(channelService, Service.Status.DISABLED, userId);
        channelService = channelServiceManager.findChannelServiceByKey(channelServiceKey).orNull();
        if (channelService.getStatus() != Service.Status.DISABLED) {
            throw new ChannelServiceStatusException(channelService.getStatus(), Service.Status.DISABLED);
        }
    }

    public void enableChannelRecordService(String channelId, String userId) {
        ChannelServiceKey channelServiceKey = new ChannelServiceKey(channelId, Service.RECORD_SERVICE_CODE);
        ChannelService channelService = channelServiceManager.findChannelServiceByKey(channelServiceKey).orNull();
        if (channelService == null || channelService.getStatus() != Service.Status.DISABLED) {
            return;
        }

        Service service = serviceManager.findServiceByCode(Service.RECORD_SERVICE_CODE);
        channelServiceManager.updateChannelServiceStatus(channelService, service.getInitialChannelStatus(), userId);
        channelService = channelServiceManager.findChannelServiceByKey(channelServiceKey).orNull();
        if (channelService.getStatus() != service.getInitialChannelStatus()) {
            throw new ChannelServiceStatusException(channelService.getStatus(), service.getInitialChannelStatus());
        }
    }

    public void startChannelRecordService(String channelId, String userId) {
        Service service = serviceManager.findServiceByCode(Service.RECORD_SERVICE_CODE);
        if (service.getStatus() == Service.Status.DISABLED) {
            throw new ServiceDisabledException(service, true);
        }

        ChannelServiceKey channelServiceKey = new ChannelServiceKey(channelId, Service.RECORD_SERVICE_CODE);
        ChannelService channelService = channelServiceManager.findChannelServiceByKey(channelServiceKey).orNull();
        if (channelService == null || channelService.getStatus() == Service.Status.ACTIVE) {
            return;
        } else if (channelService.getStatus() == Service.Status.DISABLED) {
            throw new ServiceDisabledException(service, false);
        }

        channelServiceManager.updateChannelServiceStatus(channelService, Service.Status.ACTIVE, userId);
        channelService = channelServiceManager.findChannelServiceByKey(channelServiceKey).orNull();
        if (channelService.getStatus() != Service.Status.ACTIVE) {
            throw new ChannelServiceStatusException(channelService.getStatus(), Service.Status.ACTIVE);
        }
    }

    public void stopChannelRecordService(String channelId, String userId) {
        ChannelServiceKey channelServiceKey = new ChannelServiceKey(channelId, Service.RECORD_SERVICE_CODE);
        ChannelService channelService = channelServiceManager.findChannelServiceByKey(channelServiceKey).orNull();
        if (channelService == null || channelService.getStatus() != Service.Status.ACTIVE) {
            return;
        }

        channelServiceManager.updateChannelServiceStatus(channelService, Service.Status.INACTIVE, userId);
        channelService = channelServiceManager.findChannelServiceByKey(channelServiceKey).orNull();
        if (channelService.getStatus() != Service.Status.INACTIVE) {
            throw new ChannelServiceStatusException(channelService.getStatus(), Service.Status.INACTIVE);
        }
    }

    public void recordEvent(MessageEvent<TextMessageContent> textMessageEvent) {
        Source source = textMessageEvent.getSource();
        String channelId = source.getSenderId();
        if (recordingServiceIsEnabled(channelId)) {
            String userId = source.getUserId();
            Optional<UserProfileResponse> userProfileResponse = retrieveUserProfileInChannel(channelId, userId);
            ChannelRecord channelRecord = new ChannelRecord(channelId, userId,
                    userProfileResponse.transform((profileResponse) -> profileResponse.getDisplayName()).orNull(),
                    "text", LocalDateTime.ofInstant(textMessageEvent.getTimestamp(), ZoneOffset.UTC),
                    textMessageEvent.getMessage().getText());
            channelRecordDao.insertChannelRecord(channelRecord);
        }
    }

    private boolean recordingServiceIsEnabled(String channelId) {
        return channelServiceManager
                .findChannelServiceByKey(new ChannelServiceKey(channelId, Service.RECORD_SERVICE_CODE))
                .transform((channelService) -> channelService.getStatus() == Service.Status.ACTIVE)
                .or(false);
    }

    private Optional<UserProfileResponse> retrieveUserProfileInChannel(String channelId, String userId) {
        try {
            return Optional.of(channelUserProfileCache.get(new ChannelUserProfileKey(channelId, userId)));
        } catch (ExecutionException e) {
            return Optional.absent();
        }
    }

}
