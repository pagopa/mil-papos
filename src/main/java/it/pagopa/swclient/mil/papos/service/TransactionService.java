package it.pagopa.swclient.mil.papos.service;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.dao.TransactionEntity;
import it.pagopa.swclient.mil.papos.dao.TransactionRepository;
import it.pagopa.swclient.mil.papos.model.TransactionDto;
import it.pagopa.swclient.mil.papos.util.Utility;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Date;
import java.util.List;

@ApplicationScoped
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Create a new transaction starting from a transactionDto.
     *
     * @param transactionDto dto of transaction to be generated
     * @return transaction created
     */
    public Uni<TransactionEntity> createTransaction(TransactionDto transactionDto) {

        Log.debugf("TransactionService -> createTransaction - Input parameters: %s", transactionDto);

        String transactionUuid = Utility.generateRandomUuid();
        TransactionEntity entity = createTransactionEntity(transactionDto, transactionUuid);

        return transactionRepository.persist(entity)
                .onFailure()
                .transform(error -> error)
                .onItem()
                .transform(transactionSaved -> transactionSaved);
    }

    /**
     * Returns a number corresponding to the total number of transaction found.
     *
     * @param attributeName  name of the attribute
     * @param attributeValue value of the attribute
     * @return a number
     */
    public Uni<Long> getTransactionCountByAttribute(String attributeName, String attributeValue) {
        return transactionRepository.count(attributeName, attributeValue);
    }

    /**
     * Returns a list of transactions paginated. The query filters on attributeName.
     *
     * @param attributeName  string representing the name of attribute to be filtered
     * @param attributeValue value of attribute
     * @param pageIndex      0-based page index
     * @param pageSize       page size
     * @return a list of transactions
     */
    public Uni<List<TransactionEntity>> getTransactionListPagedByAttribute(String attributeName, String attributeValue, Date startDate, Date endDate, Sort sortStrategy, int pageIndex, int pageSize) {
        String query = String.format("{ %s: ?1, creationTimestamp: { $gte: ?2, $lte: ?3 } }", attributeName);

        return transactionRepository
                .find(query, sortStrategy, attributeValue, startDate, endDate)
                .page(pageIndex, pageSize)
                .list();
    }

    private TransactionEntity createTransactionEntity(TransactionDto transactionDto, String uuid) {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setTransactionId(uuid);
        transactionEntity.setPspId(transactionDto.pspId());
        transactionEntity.setTerminalId(transactionDto.terminalId());
        transactionEntity.setNoticeNumber(transactionDto.noticeNumber());
        transactionEntity.setPayeeCode(transactionDto.payeeCode());
        transactionEntity.setCreationTimestamp(new Date());
        transactionEntity.setLastUpdateTimestamp(new Date());
        transactionEntity.setStatus("CREATED");

        return transactionEntity;
    }
}
