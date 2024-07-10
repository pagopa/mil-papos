package it.pagopa.swclient.mil.papos.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.dao.TransactionEntity;
import it.pagopa.swclient.mil.papos.dao.TransactionRepository;
import it.pagopa.swclient.mil.papos.model.TransactionDto;
import it.pagopa.swclient.mil.papos.util.Utility;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransactionService {
    private final TransactionRepository transactionRespository;

    public TransactionService(TransactionRepository transactionRespository) {
        this.transactionRespository = transactionRespository;
    }

    /**
     * Create a new transaction starting from a transactionDto.
     *
     * @param transactionDto dto of transaction to be generated
     * @return transaction created
     */
    public Uni<TransactionEntity> createTransaction(TransactionDto transactionDto) {

        Log.debugf("TerminalService -> createTransaction - Input parameters: %s", transactionDto);

        String transactionUuid = Utility.generateRandomUuid();
        TransactionEntity entity = createTransactionEntity(transactionDto, transactionUuid);

        return transactionRespository.persist(entity)
                .onFailure()
                .transform(error -> error)
                .onItem()
                .transform(terminalSaved -> terminalSaved);
    }

    private TransactionEntity createTransactionEntity(TransactionDto transactionDto, String uuid) {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setPspId(transactionDto.pspId());
        transactionEntity.setTerminalId(transactionDto.terminalId());
        transactionEntity.setNoticeNumber(transactionDto.noticeNumber());
        transactionEntity.setPayeeCode(transactionDto.payeeCode());
        transactionEntity.setTransactionId(uuid);

        return transactionEntity;
    }
}
