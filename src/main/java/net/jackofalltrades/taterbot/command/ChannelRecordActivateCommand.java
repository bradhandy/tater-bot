package net.jackofalltrades.taterbot.command;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;
import net.jackofalltrades.taterbot.channel.record.ChannelRecordManager;
import net.jackofalltrades.taterbot.event.EventContext;
import net.jackofalltrades.taterbot.service.ChannelService;
import net.jackofalltrades.taterbot.service.ChannelServiceKey;
import net.jackofalltrades.taterbot.service.ChannelServiceManager;
import net.jackofalltrades.taterbot.service.Service;
import net.jackofalltrades.taterbot.service.ServiceDisabledException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(ChannelRecordActivateCommand.NAME)
class ChannelRecordActivateCommand implements Command {

    static final String NAME = "record-start";

    private final LineMessagingClient lineMessagingClient;
    private final ChannelServiceManager channelServiceManager;
    private final ChannelRecordManager channelRecordManager;

    @Autowired
    ChannelRecordActivateCommand(LineMessagingClient lineMessagingClient, ChannelServiceManager channelServiceManager,
            ChannelRecordManager channelRecordManager) {
        this.lineMessagingClient = lineMessagingClient;
        this.channelServiceManager = channelServiceManager;
        this.channelRecordManager = channelRecordManager;
    }

    @Override
    public void execute() {
        if (EventContext.isGroupEvent()) {
            processChannelRecordStart();
        }
    }

    private void processChannelRecordStart() {
        String channelId = EventContext.getGroupId().orNull();
        ChannelService channelService = channelServiceManager
                .findChannelServiceByKey(new ChannelServiceKey(channelId, Service.RECORD_SERVICE_CODE))
                .orNull();

        String replyToken = EventContext.getReplyToken().orNull();
        try {
            if (channelService.getStatus() == Service.Status.ACTIVE) {
                lineMessagingClient.replyMessage(new ReplyMessage(replyToken,
                        new TextMessage("'record' service is already active.")));
            } else {
                channelRecordManager.startChannelRecordService(channelId, EventContext.getUserId().orNull());
                lineMessagingClient.replyMessage(new ReplyMessage(replyToken,
                        new TextMessage("Recording active. Use 'taterbot record stop' to terminate recording.")));
            }
        } catch (ServiceDisabledException e) {
            lineMessagingClient.replyMessage(new ReplyMessage(replyToken, new TextMessage(e.getMessage())));
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

}
