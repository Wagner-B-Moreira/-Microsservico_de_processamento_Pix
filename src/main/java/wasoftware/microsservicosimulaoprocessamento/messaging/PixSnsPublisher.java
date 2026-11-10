package wasoftware.microsservicosimulaoprocessamento.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import wasoftware.microsservicosimulaoprocessamento.entity.PixTransaction;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Component
@Slf4j
public class PixSnsPublisher {
    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sns.pix-topic-arn}")
    private String topicArn;

    public PixSnsPublisher(SnsClient snsClient, ObjectMapper objectMapper) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
    }

    public void publishPixTransaction(PixTransaction transaction) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(transaction);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar a transacao Pix para o SNS", e);
        }

        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .message(payload)
                .subject("PixTransactionCreated")
                .build();

        snsClient.publish(request);
        log.info("Transacao Pix publicada no SNS. TxId: {}", transaction.getTxid());
    }
}
