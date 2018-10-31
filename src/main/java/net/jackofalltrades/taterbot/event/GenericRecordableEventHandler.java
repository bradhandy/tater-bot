package net.jackofalltrades.taterbot.event;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import net.jackofalltrades.taterbot.channel.record.ChannelRecordManager;

@LineMessageHandler
public class GenericRecordableEventHandler {

    private final ChannelRecordManager channelRecordManager;

    public GenericRecordableEventHandler(ChannelRecordManager channelRecordManager) {
        this.channelRecordManager = channelRecordManager;
    }

    @EventMapping
    public void recordStickerMessage(MessageEvent<StickerMessageContent> messageEvent) {
        channelRecordManager.recordEvent(messageEvent);
    }

    @EventMapping
    public void recordImageMessage(MessageEvent<ImageMessageContent> messageEvent) {
        channelRecordManager.recordEvent(messageEvent);
    }

}
