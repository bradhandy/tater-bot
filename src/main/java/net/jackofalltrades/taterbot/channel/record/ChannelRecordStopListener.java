package net.jackofalltrades.taterbot.channel.record;

import com.google.common.base.Optional;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.TextMessage;
import net.jackofalltrades.taterbot.channel.record.event.ChannelRecordStopEvent;
import net.jackofalltrades.taterbot.service.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Component
class ChannelRecordStopListener implements ApplicationListener<ChannelRecordStopEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ChannelRecordStopListener.class);

    private final LineMessagingClient lineMessagingClient;
    private final ResourceLoader resourceLoader;
    private final ChannelRecordDao channelRecordDao;
    private final String baseTranscriptUrl;
    private final String baseDownloadUrl;

    @Autowired
    public ChannelRecordStopListener(LineMessagingClient lineMessagingClient, ResourceLoader resourceLoader,
            ChannelRecordDao channelRecordDao, @Value("${transcript.url}") String baseTranscriptUrl,
            @Value("${transcript.download.url}") String baseDownloadUrl) {
        this.lineMessagingClient = lineMessagingClient;
        this.resourceLoader = resourceLoader;
        this.channelRecordDao = channelRecordDao;
        this.baseTranscriptUrl = baseTranscriptUrl;
        this.baseDownloadUrl = baseDownloadUrl;
    }

    @Override
    public void onApplicationEvent(ChannelRecordStopEvent event) {
        ChannelService channelService = event.getChannelService();
        LocalDateTime endTimestamp =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault());

        String transcriptFileName = createFileName(channelService);
        Resource transcriptResource = resourceLoader.getResource(baseTranscriptUrl + transcriptFileName);
        WritableResource writableTranscriptResource = (WritableResource) transcriptResource;
        try (OutputStream outputStream = writableTranscriptResource.getOutputStream()) {
            channelRecordDao.processChannelRecords(channelService.getChannelId(), channelService.getStatusDate(),
                    endTimestamp, new TextFileExporterChannelRecordProcessor(outputStream));
        } catch (IOException e) {
            LOG.error("Could not create transcript.", e);
        }

        TextMessage textMessage = new TextMessage("Download the transcript @ " + baseDownloadUrl + transcriptFileName);
        lineMessagingClient.pushMessage(new PushMessage(channelService.getChannelId(), textMessage));
    }

    private String createFileName(ChannelService channelService) {
        long timeInMillis = channelService.getStatusDate().toInstant(ZoneOffset.UTC).toEpochMilli();
        return String.format("%s-%s-%d.txt", channelService.getChannelId(), channelService.getUserId(), timeInMillis);
    }

    private static class TextFileExporterChannelRecordProcessor implements ChannelRecordProcessor {

        private final OutputStream outputStream;

        private TextFileExporterChannelRecordProcessor(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void processChannelRecord(ChannelRecord channelRecord) {
            try {
                String record = String.format("%-25s - %s\n",
                        Optional.fromNullable(channelRecord.getUserDisplayName()).or("[user name unavailable]"),
                        channelRecord.getMessage());
                outputStream.write(record.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {

            }
        }

    }

}
