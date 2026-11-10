package wasoftware.microsservicosimulaoprocessamento.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PixRequestDTO(
        @NotBlank(message = "A chave Pix é obrigatória")
        String chavePix,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal valor
) {}