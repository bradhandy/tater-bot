package net.jackofalltrades.taterbot.command;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import net.jackofalltrades.taterbot.channel.profile.ChannelUserProfileKey;
import net.jackofalltrades.taterbot.event.EventContext;
import net.jackofalltrades.taterbot.service.ChannelService;
import net.jackofalltrades.taterbot.service.ChannelServiceKey;
import net.jackofalltrades.taterbot.service.ChannelServiceManager;
import net.jackofalltrades.taterbot.service.Service;
import net.jackofalltrades.taterbot.service.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component(ServiceStatusCommand.NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class ServiceStatusCommand implements Command, ServiceNameAware {

    static final Logger LOG = LoggerFactory.getLogger(ServiceStatusCommand.class);
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'");
    static final String NAME = "service-status";

    private final LineMessagingClient lineMessagingClient;
    private final ServiceManager serviceManager;
    private final ChannelServiceManager channelServiceManager;
    private final LoadingCache<ChannelUserProfileKey, UserProfileResponse> channelUserProfileCache;

    private String serviceName;

    ServiceStatusCommand(LineMessagingClient lineMessagingClient, ServiceManager serviceManager,
            ChannelServiceManager channelServiceManager,
            LoadingCache<ChannelUserProfileKey, UserProfileResponse> channelUserProfileCache) {
        this.lineMessagingClient = lineMessagingClient;
        this.serviceManager = serviceManager;
        this.channelServiceManager = channelServiceManager;
        this.channelUserProfileCache = channelUserProfileCache;
    }

    @Override
    public void execute() {
        if (!EventContext.isGroupEvent()) {
            return;
        }

        if (Optional.fromNullable(serviceName).isPresent()) {
            String channelId = EventContext.getGroupId().orNull();
            Optional<ChannelService> optionalChannelService =
                    channelServiceManager.findChannelServiceByKey(new ChannelServiceKey(channelId, serviceName));
            String channelServiceStatusMessage = optionalChannelService.transform(this::createChannelServiceStatusMessage)
                    .or(String.format("'%s' is an invalid service for this channel.", serviceName));

            lineMessagingClient.replyMessage(
                    new ReplyMessage(EventContext.getReplyToken().orNull(), new TextMessage(channelServiceStatusMessage)));
        }
    }

    private String createChannelServiceStatusMessage(ChannelService channelService) {
        Service service = serviceManager.findServiceByCode(channelService.getServiceCode());
        Optional<UserProfileResponse> userProfileResponse = Optional.absent();
        if (!Strings.isNullOrEmpty(channelService.getUserId())) {
            try {
                ChannelUserProfileKey channelUserKey =
                        new ChannelUserProfileKey(channelService.getChannelId(), channelService.getUserId());
                userProfileResponse = Optional.of(channelUserProfileCache.getUnchecked(channelUserKey));
            } catch (UncheckedExecutionException e) {
                LOG.info("Failed to retrieve channel user profile from cache.", e);
            }
        }

        String changedByString = userProfileResponse
                .transform((userProfile) -> String.format(" (changed by @%s)", userProfile.getDisplayName()))
                .or("");
        return String.format("'%s' service is %s as of %s.%s", service.getCode(),
                channelService.getStatus().name().toLowerCase(),
                DATE_TIME_FORMATTER.format(channelService.getStatusDate()), changedByString);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceStatusCommand that = (ServiceStatusCommand) o;
        return Objects.equals(serviceName, that.serviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName);
    }

}
