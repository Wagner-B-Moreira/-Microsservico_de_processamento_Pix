package wasoftware.microsservicosimulaoprocessamento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wasoftware.microsservicosimulaoprocessamento.entity.PixTransaction;

import java.util.Optional;

@Repository // Indica que esta interface é um componente de repositório Spring Data JPA
public interface PixRepository extends JpaRepository<PixTransaction, Long> {

    // Método de consulta derivado para buscar transações pelo identificador único txid
    Optional<PixTransaction> findByTxid(String txid);

    Optional<PixTransaction> findByIdempotencyKey(String idempotencyKey);

}