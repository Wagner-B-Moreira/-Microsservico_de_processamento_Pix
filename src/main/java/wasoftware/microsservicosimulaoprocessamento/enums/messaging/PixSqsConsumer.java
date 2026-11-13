package wasoftware.microsservicosimulaoprocessamento.enums.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import wasoftware.microsservicosimulaoprocessamento.enums.PixStatus;
import wasoftware.microsservicosimulaoprocessamento.entity.PixTransaction;
import wasoftware.microsservicosimulaoprocessamento.repository.PixRepository;

@Component
@Slf4j
public class PixSqsConsumer {
    private final PixRepository pixRepository;
    private final ObjectMapper objectMapper;

    public PixSqsConsumer(PixRepository pixRepository, ObjectMapper objectMapper) {
        this.pixRepository = pixRepository;
        this.objectMapper = objectMapper;
    }


    // Anotação do Spring Cloud AWS SQS para escutar continuamente a fila configurada
    @SqsListener("${aws.sqs.pix-queue-name}")
    @Transactional

    public void receivePixMessage(String messagePayload) throws JsonProcessingException {
        log.info("Mensagem recebida do SQS: {}", messagePayload);

        String jsonContent = messagePayload;
        if (messagePayload.contains("\"Message\"")) {
            var jsonNode = objectMapper.readTree(messagePayload);
            if (jsonNode.has("Message")) {
                jsonContent = jsonNode.get("Message").asText();
            }
        }

        PixTransaction eventData = objectMapper.readValue(jsonContent, PixTransaction.class);
        var optionalTransaction = pixRepository.findByTxid(eventData.getTxid());

        if (optionalTransaction.isPresent()) {
            PixTransaction transaction = optionalTransaction.get();
            log.info("Processando pagamento Pix para o TxId: {}", transaction.getTxid());
            transaction.setStatus(PixStatus.COMPLETED);
            pixRepository.save(transaction);
            log.info("Transacao Pix concluida. TxId: {}", transaction.getTxid());
        } else {
            log.warn("Transacao Pix nao encontrada para o TxId: {}", eventData.getTxid());
        }
    }
}
