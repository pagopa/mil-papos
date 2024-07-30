package it.pagopa.swclient.mil.papos.service;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.dao.TransactionEntity;
import it.pagopa.swclient.mil.papos.dao.TransactionRepository;
import it.pagopa.swclient.mil.papos.model.TransactionDto;
import it.pagopa.swclient.mil.papos.model.UpdateTransactionDto;
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
        TransactionEntity entity = createTransactionEntity(transactionDto);

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
        Log.debugf("TransactionService -> getTransactionCountByAttribute - Input parameters: %s, %s", attributeName, attributeValue);

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
        Log.debugf("TransactionService -> getTransactionListPagedByAttribute - Input parameters: %s, %s, %s, %s, %s, %s, %s", attributeName, attributeValue, startDate, endDate, sortStrategy, pageIndex, pageSize);

        String query = String.format("{ %s: ?1, creationTimestamp: { $gte: ?2, $lte: ?3 } }", attributeName);

        return transactionRepository
                .find(query, sortStrategy, attributeValue, startDate, endDate)
                .page(pageIndex, pageSize)
                .list();
    }

    /**
     * Find first transaction equals to transactionId given in input.
     *
     * @param transactionId id of transaction
     * @return transaction found
     */
    public Uni<TransactionEntity> findTransaction(String transactionId) {
        Log.debugf("TransactionService -> findTransaction - Input parameters: %s", transactionId);

        return transactionRepository
                .find("transactionId = ?1", transactionId)
                .firstResult();
    }

    /**
     * Delete transaction starting from a transactionEntity.
     *
     * @param transaction transaction to be deleted
     * @return void
     */
    public Uni<Void> deleteTransaction(TransactionEntity transaction) {
        Log.debugf("TransactionService -> deleteTransaction - Input parameters: %s", transaction);

        return transactionRepository.delete(transaction)
                .onFailure()
                .transform(error -> error)
                .onItem()
                .transform(transactionDeleted -> transactionDeleted);
    }

    /**
     * Update transaction starting from a terminalDto.
     *
     * @param transactionDto dto of modified terminal
     * @param transactionId  transactionId of old transaction to be modified
     * @return transaction updated
     */
    public Uni<TransactionEntity> updateTransaction(String transactionId, UpdateTransactionDto transactionDto, TransactionEntity oldTransaction) {
        Log.debugf("TransactionService -> updateTransaction - Input parameters: %s, %s, %s", transactionId, transactionDto, oldTransaction);

        oldTransaction.setStatus(transactionDto.status());
        oldTransaction.setAmount(transactionDto.amount());

        return transactionRepository.update(oldTransaction)
                .onFailure()
                .transform(error -> error)
                .onItem()
                .transform(transactionUpdated -> transactionUpdated);
    }

    /**
     * Retrieves the latest transaction by POS and status.
     *
     * @param pspId      ID of the POS PSP
     * @param terminalId ID of the terminal for the PSP
     * @param status     status of the transactions to search for
     * @return transaction found
     */
    public Uni<TransactionEntity> latestTransaction(String pspId, String terminalId, String status, Sort sort) {
        Log.debugf("TransactionService -> latestTransaction - Input parameters: %s, %s, %s", pspId, terminalId, status);

        return transactionRepository
                .find("pspId = ?1 AND terminalId = ?2 AND status = ?3", sort, pspId, terminalId, status)
                .firstResult();
    }

    private TransactionEntity createTransactionEntity(TransactionDto transactionDto) {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setNoticeNumber(transactionDto.noticeNumber());
        transactionEntity.setPayeeCode(transactionDto.payeeCode());
        transactionEntity.setCreationTimestamp(new Date());
        transactionEntity.setLastUpdateTimestamp(new Date());
        transactionEntity.setStatus("CREATED");

        return transactionEntity;
    }
}
