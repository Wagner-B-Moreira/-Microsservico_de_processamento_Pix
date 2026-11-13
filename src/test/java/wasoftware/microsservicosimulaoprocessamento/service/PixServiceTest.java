package wasoftware.microsservicosimulaoprocessamento.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wasoftware.microsservicosimulaoprocessamento.dto.PixRequestDTO;
import wasoftware.microsservicosimulaoprocessamento.entity.PixTransaction;
import wasoftware.microsservicosimulaoprocessamento.enums.PixStatus;
import wasoftware.microsservicosimulaoprocessamento.enums.messaging.PixSnsPublisher;
import wasoftware.microsservicosimulaoprocessamento.repository.PixRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PixServiceTest {

    @Mock
    private PixRepository pixRepository;

    @Mock
    private PixSnsPublisher snsPublisher;

    @InjectMocks
    private PixService pixService;

    @Test
    void createPixCharge_shouldSaveTransactionAndPublishToSns_whenRequestIsValid() {
        // arrange
        PixRequestDTO request = new PixRequestDTO("12345678900", new BigDecimal("150.00"));

        when(pixRepository.findByIdempotencyKey("request-123")).thenReturn(java.util.Optional.empty());
        when(pixRepository.save(any(PixTransaction.class))).thenAnswer(invocation -> {
            PixTransaction transaction = invocation.getArgument(0);
            transaction.setId(1L);
            transaction.setTxid("generated-txid");
            return transaction;
        });

        // act
        PixTransaction result = pixService.createPixCharge(request, "request-123");

        // assert
        assertNotNull(result);
        assertEquals("12345678900", result.getChavePix());
        assertEquals(new BigDecimal("150.00"), result.getValor());
        assertEquals(PixStatus.PENDING, result.getStatus());
        assertNotNull(result.getTxid());
        assertEquals("request-123", result.getIdempotencyKey());

        verify(pixRepository, times(1)).findByIdempotencyKey("request-123");
        verify(pixRepository, times(1)).save(any(PixTransaction.class));
        verify(snsPublisher, times(1)).publishPixTransaction(result);
    }

    @Test
    void createPixCharge_shouldReturnExistingTransactionWithoutPublishing_whenKeyIsRepeated() {
        PixRequestDTO request = new PixRequestDTO("12345678900", new BigDecimal("150.00"));
        PixTransaction existing = PixTransaction.builder()
                .id(1L)
                .txid("existing-txid")
                .idempotencyKey("request-123")
                .chavePix("12345678900")
                .valor(new BigDecimal("150.00"))
                .status(PixStatus.PENDING)
                .build();
        when(pixRepository.findByIdempotencyKey("request-123"))
                .thenReturn(java.util.Optional.of(existing));

        PixTransaction result = pixService.createPixCharge(request, "request-123");

        assertSame(existing, result);
        verify(pixRepository, never()).save(any(PixTransaction.class));
        verifyNoInteractions(snsPublisher);
    }

    @Test
    void createPixCharge_shouldRejectDifferentRequest_whenKeyIsRepeated() {
        PixRequestDTO request = new PixRequestDTO("different-key", new BigDecimal("150.00"));
        PixTransaction existing = PixTransaction.builder()
                .idempotencyKey("request-123")
                .chavePix("12345678900")
                .valor(new BigDecimal("150.00"))
                .build();
        when(pixRepository.findByIdempotencyKey("request-123"))
                .thenReturn(java.util.Optional.of(existing));

        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> pixService.createPixCharge(request, "request-123")
        );
        verify(pixRepository, never()).save(any(PixTransaction.class));
        verifyNoInteractions(snsPublisher);
    }

    @Test
    void createPixCharge_shouldThrowException_whenValueIsZero() {
        // arrange
        PixRequestDTO request = new PixRequestDTO("12345678900", BigDecimal.ZERO);

        // act + assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pixService.createPixCharge(request, "request-123")
        );

        assertEquals("O valor do Pix deve ser maior que zero.", exception.getMessage());
        verifyNoInteractions(pixRepository);
        verifyNoInteractions(snsPublisher);
    }

    @Test
    void createPixCharge_shouldThrowException_whenValueIsNegative() {
        // arrange
        PixRequestDTO request = new PixRequestDTO("12345678900", new BigDecimal("-10.00"));

        // act + assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pixService.createPixCharge(request, "request-123")
        );

        assertEquals("O valor do Pix deve ser maior que zero.", exception.getMessage());
        verifyNoInteractions(pixRepository);
        verifyNoInteractions(snsPublisher);
    }

    @Test
    void createPixCharge_shouldThrowException_whenPixKeyIsBlank() {
        // arrange
        PixRequestDTO request = new PixRequestDTO("   ", new BigDecimal("10.00"));

        // act + assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pixService.createPixCharge(request, "request-123")
        );

        assertEquals("A chave Pix de destino/origem não pode ser vazia.", exception.getMessage());
        verifyNoInteractions(pixRepository);
        verifyNoInteractions(snsPublisher);
    }
}
