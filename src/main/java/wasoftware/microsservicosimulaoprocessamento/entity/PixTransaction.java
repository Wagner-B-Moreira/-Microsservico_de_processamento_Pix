package wasoftware.microsservicosimulaoprocessamento.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import wasoftware.microsservicosimulaoprocessamento.enums.PixStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity // Marca a classe como uma entidade gerenciada pelo JPA (tabela no banco de dados)
@Table(name = "pix_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PixTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String txid;


    @Column(nullable = false)
     private String chavePix;


    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor; // Valor monetário da transação com precisão financeira estrita


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PixStatus status;


    @Column(nullable = false)
    private LocalDateTime createdAt;


    @PrePersist
    public void prePersist() {

        // Callback executado automaticamente antes de salvar a entidade pela primeira vez
        if (this.txid == null || this.txid.isBlank()) {
            this.txid = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = PixStatus.PENDING; // Toda transação nasce pendente até ser processada via mensageria AWS
        }
    }


}
