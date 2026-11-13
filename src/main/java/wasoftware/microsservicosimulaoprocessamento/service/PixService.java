package wasoftware.microsservicosimulaoprocessamento.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import wasoftware.microsservicosimulaoprocessamento.dto.PixRequestDTO;
import wasoftware.microsservicosimulaoprocessamento.entity.PixTransaction;
import wasoftware.microsservicosimulaoprocessamento.enums.PixStatus;
import wasoftware.microsservicosimulaoprocessamento.enums.messaging.PixSnsPublisher;
import wasoftware.microsservicosimulaoprocessamento.repository.PixRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PixService {
    private final PixRepository pixRepository;
    private final PixSnsPublisher snsPublisher;

    @Transactional // Garante que a operação de banco de dados ocorra dentro de uma transação atômica
    public PixTransaction createPixCharge(PixRequestDTO requestDTO, String idempotencyKey) {
        // Validação de negócio: o valor da transação deve ser estritamente maior que zero
        if (requestDTO.valor() == null || requestDTO.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do Pix deve ser maior que zero.");
        }

        if (requestDTO.chavePix() == null || requestDTO.chavePix().isBlank()) {
            throw new IllegalArgumentException("A chave Pix de destino/origem não pode ser vazia.");
        }

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("A chave de idempotência é obrigatória.");
        }

        var existingTransaction = pixRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTransaction.isPresent()) {
            PixTransaction transaction = existingTransaction.get();
            if (!transaction.getChavePix().equals(requestDTO.chavePix())
                    || transaction.getValor().compareTo(requestDTO.valor()) != 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A chave de idempotência já foi usada com dados diferentes."
                );
            }
            return transaction;
        }

        // Constrói a entidade de domínio com status inicial PENDING
        PixTransaction transaction = PixTransaction.builder()
                .idempotencyKey(idempotencyKey)
                .chavePix(requestDTO.chavePix())
                .valor(requestDTO.valor())
                .status(PixStatus.PENDING)
                .build();

        // Salva a transação no banco de dados H2 em memória
        PixTransaction savedTransaction = pixRepository.save(transaction);
        log.info("Cobrança Pix salva no H2 com status PENDING. ID: {}, TxId: {}",
                savedTransaction.getId(), savedTransaction.getTxid());

        // Dispara a mensageria assíncrona publicando o evento no Tópico AWS SNS
        snsPublisher.publishPixTransaction(savedTransaction);

        return savedTransaction;
    }
}
