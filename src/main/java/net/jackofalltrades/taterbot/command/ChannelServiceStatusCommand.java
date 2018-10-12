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
import net.jackofalltrades.taterbot.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component(ChannelServiceStatusCommand.NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class ChannelServiceStatusCommand implements Command {

    static final Logger LOG = LoggerFactory.getLogger(ChannelServiceStatusCommand.class);
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'");
    static final String NAME = "service-status";

    private final LineMessagingClient lineMessagingClient;
    private final LoadingCache<String, Service> serviceCache;
    private final LoadingCache<ChannelServiceKey, ChannelService> channelServiceCache;
    private final LoadingCache<ChannelUserProfileKey, UserProfileResponse> channelUserProfileCache;

    private String serviceName;

    ChannelServiceStatusCommand(LineMessagingClient lineMessagingClient, LoadingCache<String, Service> serviceCache,
            LoadingCache<ChannelServiceKey, ChannelService> channelServiceCache,
            LoadingCache<ChannelUserProfileKey, UserProfileResponse> channelUserProfileCache) {
        this.lineMessagingClient = lineMessagingClient;
        this.serviceCache = serviceCache;
        this.channelServiceCache = channelServiceCache;
        this.channelUserProfileCache = channelUserProfileCache;
    }

    @Override
    public void execute() {
        if (!EventContext.isGroupEvent()) {
            return;
        }

        String channelId = EventContext.getGroupId().orNull();

        ChannelService channelService = channelServiceCache.getUnchecked(new ChannelServiceKey(channelId, serviceName));
        Service service = serviceCache.getUnchecked(channelService.getServiceCode());
        Optional<UserProfileResponse> userProfileResponse = Optional.absent();
        if (!Strings.isNullOrEmpty(channelService.getUserId())) {
            try {
                ChannelUserProfileKey channelUserKey = new ChannelUserProfileKey(channelId, channelService.getUserId());
                userProfileResponse = Optional.of(channelUserProfileCache.getUnchecked(channelUserKey));
            } catch (UncheckedExecutionException e) {
                LOG.info("Failed to retrieve channel user profile from cache.", e);
            }
        }

        String changedByString = userProfileResponse
                .transform((userProfile) -> String.format(" (changed by @%s)", userProfile.getDisplayName()))
                .or("");
        String message = String.format("'%s' service is %s as of %s.%s", service.getCode(),
                channelService.getStatus().name().toLowerCase(),
                DATE_TIME_FORMATTER.format(channelService.getStatusDate()), changedByString);

        lineMessagingClient.replyMessage(new ReplyMessage(EventContext.getReplyToken().orNull(), new TextMessage(message)));
    }

    @Override
    public String getName() {
        return NAME;
    }

    void setServiceName(String serviceName) {
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
        ChannelServiceStatusCommand that = (ChannelServiceStatusCommand) o;
        return Objects.equals(serviceName, that.serviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName);
    }

}
