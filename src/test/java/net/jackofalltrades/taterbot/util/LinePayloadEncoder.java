package net.jackofalltrades.taterbot.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import com.linecorp.bot.model.event.CallbackRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Signs the payload to be sent to the callback endpoint.
 *
 * @author bhandy
 */
@Component
public class LinePayloadEncoder {

    private final ObjectMapper objectMapper;
    private final String channelSecret;

    @Autowired
    public LinePayloadEncoder(ObjectMapper objectMapper, @Value("${line.bot.channelSecret}") String channelSecret) {
        this.objectMapper = objectMapper;
        this.channelSecret = channelSecret;
    }

    public String encodePayload(CallbackRequest callbackRequest) throws JsonProcessingException {
        return Base64.getEncoder().encodeToString(
                Hashing.hmacSha256(channelSecret.getBytes(StandardCharsets.UTF_8))
                        .hashBytes(objectMapper.writeValueAsBytes(callbackRequest))
                        .asBytes());
    }

    public String formatPayload(CallbackRequest callbackRequest) throws JsonProcessingException {
        return objectMapper.writeValueAsString(callbackRequest);
    }

}
