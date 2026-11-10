package wasoftware.microsservicosimulaoprocessamento.enums;

public enum PixStatus {

    PENDING,    // Cobrança gerada, aguardando mensageria/processamento assíncrono
    COMPLETED,  // Pagamento processado com sucesso pelo consumidor SQS
    FAILED      // Falha ocorrida durante o fluxo transacional
}
