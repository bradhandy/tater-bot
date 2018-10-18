package net.jackofalltrades.taterbot.command;

import com.google.common.base.Strings;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;
import net.jackofalltrades.taterbot.channel.record.ChannelRecordManager;
import net.jackofalltrades.taterbot.channel.record.event.ChannelRecordStopEvent;
import net.jackofalltrades.taterbot.event.EventContext;
import net.jackofalltrades.taterbot.service.ChannelService;
import net.jackofalltrades.taterbot.service.ChannelServiceKey;
import net.jackofalltrades.taterbot.service.ChannelServiceManager;
import net.jackofalltrades.taterbot.service.Service;
import net.jackofalltrades.taterbot.service.ServiceDisabledException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component(ChannelRecordInactivateCommand.NAME)
class ChannelRecordInactivateCommand implements Command {

    static final String NAME = "record-stop";

    private final LineMessagingClient lineMessagingClient;
    private final ChannelServiceManager channelServiceManager;
    private final ChannelRecordManager channelRecordManager;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    ChannelRecordInactivateCommand(LineMessagingClient lineMessagingClient, ChannelServiceManager channelServiceManager,
            ChannelRecordManager channelRecordManager, ApplicationEventPublisher applicationEventPublisher) {
        this.lineMessagingClient = lineMessagingClient;
        this.channelServiceManager = channelServiceManager;
        this.channelRecordManager = channelRecordManager;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void execute() {
        if (EventContext.isGroupEvent()) {
            processChannelRecordStop();
        }
    }

    private void processChannelRecordStop() {
        String channelId = EventContext.getGroupId().orNull();
        ChannelService channelService = channelServiceManager
                .findChannelServiceByKey(new ChannelServiceKey(channelId, Service.RECORD_SERVICE_CODE))
                .orNull();
        if (channelService == null) {
            sendReplyMessage("Cannot inactivate non-existent service.");
            return;
        }

        try {
            if (channelService.getStatus() == Service.Status.INACTIVE) {
                sendReplyMessage("'record' service is already inactive.");
            } else {
                String userId = EventContext.getUserId().orNull();
                if (Strings.isNullOrEmpty(userId)) {
                    sendReplyMessage("Cannot inactivate recording, because I don't know who you are.");
                } else if (activeChannelWithUserMismatch(channelService, userId)) {
                    sendReplyMessage("Cannot inactivate recording, because you did not start the recording session.");
                } else {
                    channelRecordManager.stopChannelRecordService(channelId, EventContext.getUserId().orNull());
                    applicationEventPublisher.publishEvent(new ChannelRecordStopEvent(this, channelService));
                    sendReplyMessage("Recording inactive.  Preparing transcript...");
                }
            }
        } catch (ServiceDisabledException e) {
            sendReplyMessage(e.getMessage());
        }
    }

    private void sendReplyMessage(String message) {
        lineMessagingClient.replyMessage(new ReplyMessage(EventContext.getReplyToken().orNull(), new TextMessage(message)));
    }

    private boolean activeChannelWithUserMismatch(ChannelService channelService, String userId) {
        return channelService.getStatus() == Service.Status.ACTIVE &&
                channelService.getUserId() != null && !userId.equals(channelService.getUserId());
    }

    @Override
    public String getName() {
        return NAME;
    }

}
