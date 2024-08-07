package it.pagopa.swclient.mil.papos.service;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.dao.TransactionEntity;
import it.pagopa.swclient.mil.papos.dao.TransactionRepository;
import it.pagopa.swclient.mil.papos.model.TransactionDto;
import it.pagopa.swclient.mil.papos.model.UpdateTransactionDto;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Date;
import java.util.List;

import static it.pagopa.swclient.mil.papos.util.Utility.roundCeilObjectIdhex;

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
     * @param payeeCode CF of the subject that receives the payment
     * @return a number
     */
    public Uni<Long> getTransactionCountByPayee(String payeeCode) {
        Log.debugf("TransactionService -> getTransactionCountByPayee - Input parameters: %s, %s", payeeCode);

        return transactionRepository.count("payeeCode", payeeCode);
    }

    /**
     * Returns a list of transactions paginated. The query filters on attributeName.
     *
     * @param payeeCode CF of the subject that receives the payment
     * @param terminalUuids list of uuid of terminal
     * @param pageIndex     0-based page index
     * @param pageSize      page size
     * @return a list of transactions
     */
    public Uni<List<TransactionEntity>> getTransactionListPagedByPayeeAndTerminals(String payeeCode, List<String> terminalUuids, Date startDate, Date endDate, Sort sortStrategy, int pageIndex, int pageSize) {
        Log.debugf("TransactionService -> getTransactionListPagedByAttribute - Input parameters: %s, %s, %s, %s, %s, %s, %s", payeeCode, terminalUuids, startDate, endDate, sortStrategy, pageIndex, pageSize);

        String query = "{ 'payeeCode': ?1, '_id': { '$gte': ?2, '$lte': ?3 }, 'terminalUuid': { '$in': [?4] } }";

        return transactionRepository
                .find(query, sortStrategy, payeeCode, roundCeilObjectIdhex(startDate), roundCeilObjectIdhex(endDate), terminalUuids)
                .page(pageIndex, pageSize)
                .list();
    }

    /**
     * Returns a number corresponding to the total number of transaction found.
     *
     * @param terminalUuids list of uuid of terminal associated to pspId of the solution
     * @return a number
     */
    public Uni<Long> getTransactionCountByTerminals(List<String> terminalUuids) {
        Log.debugf("TransactionService -> getTransactionCountByAttribute - Input parameters: %s", terminalUuids);

        return transactionRepository.count("terminalUuid in ?1", terminalUuids);
    }

    /**
     * Returns a list of transactions paginated.
     *
     * @param terminalUuids list of uuid of terminal associated to pspId of the solution
     * @param pageIndex     0-based page index
     * @param pageSize      page size
     * @return a list of transactions
     */
    public Uni<List<TransactionEntity>> getTransactionListPagedByTerminals(List<String> terminalUuids, Date startDate, Date endDate, Sort sortStrategy, int pageIndex, int pageSize) {
        Log.debugf("TransactionService -> getTransactionListPagedByTerminals - Input parameters: %s, %s, %s, %s, %s, %s", terminalUuids, startDate, endDate, sortStrategy, pageIndex, pageSize);

        String query = "{ '_id': { '$gte': ?2, '$lte': ?3 }, 'terminalUuid': { '$in': [?1] } }";

        return transactionRepository
                .find(query, sortStrategy, terminalUuids, roundCeilObjectIdhex(startDate), roundCeilObjectIdhex(endDate))
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
                .find("{_id: ObjectId(?1)}", transactionId)
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
     * Returns a list of transactions corresponding to given terminalUuids, status.
     *
     * @param terminalUuids list of uuid of terminal associated to pspId of the solution
     * @param status        of the transaction
     * @return list of transactions found
     */
    public Uni<TransactionEntity> findLatestByTerminalUuidAndStatus(List<String> terminalUuids, String status, Sort sort) {
        Log.debugf("TransactionService -> getTransactionCountByTerminals - Input parameters: %s, %s, %s", terminalUuids, status, sort);

        return transactionRepository.find("terminalUuid in ?1 and status = ?2", sort, terminalUuids, status)
                .firstResult();
    }

    private TransactionEntity createTransactionEntity(TransactionDto transactionDto) {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setTerminalUuid(transactionDto.terminalUuid());
        transactionEntity.setNoticeNumber(transactionDto.noticeNumber());
        transactionEntity.setPayeeCode(transactionDto.payeeCode());
        transactionEntity.setCreationTimestamp(new Date());
        transactionEntity.setLastUpdateTimestamp(new Date());
        transactionEntity.setStatus("CREATED");

        return transactionEntity;
    }
}
