package it.pagopa.swclient.mil.papos.resource;

import io.quarkus.logging.Log;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.dao.TerminalEntity;
import it.pagopa.swclient.mil.papos.dao.TransactionEntity;
import it.pagopa.swclient.mil.papos.model.*;
import it.pagopa.swclient.mil.papos.service.SolutionService;
import it.pagopa.swclient.mil.papos.service.TerminalService;
import it.pagopa.swclient.mil.papos.service.TransactionService;
import it.pagopa.swclient.mil.papos.util.ErrorCodes;
import it.pagopa.swclient.mil.papos.util.Errors;
import it.pagopa.swclient.mil.papos.util.RegexPatterns;
import it.pagopa.swclient.mil.papos.util.Utility;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Date;
import java.util.List;

@Path("/transactions")
public class TransactionResource {

    private final TransactionService transactionService;

    private final TerminalService terminalService;

    private final SolutionService solutionService;

    private final JsonWebToken jwt;

    private static final String CREATION_TIMESTAMP = "creationTimestamp";

    public TransactionResource(TransactionService transactionService, TerminalService terminalService, SolutionService solutionService, JsonWebToken jwt) {
        this.transactionService = transactionService;
        this.terminalService = terminalService;
        this.solutionService = solutionService;
        this.jwt = jwt;
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"public_administration"})
    public Uni<Response> createTransaction(
            @HeaderParam("RequestId")
            @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG)
            @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @Valid @NotNull(message = ErrorCodes.ERROR_DTO_MUST_NOT_BE_NULL_MSG) TransactionDto transaction) {

        Log.debugf("TransactionResource -> createTransaction - Input requestId, createTransaction: %s, %s", requestId, transaction);

        checkToken(transaction.payeeCode());

        return terminalService.findTerminal(transaction.terminalUuid())
                .onFailure()
                .transform(err -> {
                    Log.errorf(err, "TransactionResource -> createTransaction: unexpected error during persist for transaction [%s]", transaction);

                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                            .build());
                })
                .onItem()
                .transformToUni(terminalFound -> {
                    if (terminalFound == null) {
                        Log.errorf("TransactionResource -> createTransaction: no terminal found for terminalUuid [%s]", transaction.terminalUuid());

                        return Uni.createFrom().failure(new NotFoundException(Response
                                .status(Response.Status.NOT_FOUND)
                                .entity(new Errors(ErrorCodes.ERROR_TERMINAL_NOT_FOUND, ErrorCodes.ERROR_TERMINAL_NOT_FOUND_MSG))
                                .build()));
                    }

                    return transactionService.createTransaction(transaction)
                            .onFailure()
                            .transform(err -> {
                                Log.errorf(err, "TransactionResource -> createTransaction: unexpected error during persist for transaction [%s]", transaction);

                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                                        .build());
                            })
                            .onItem()
                            .transform(transactionSaved -> {
                                Log.debugf("TransactionResource -> createTransaction: transaction saved correctly on DB [%s]", transactionSaved);

                                return Response.status(Response.Status.NO_CONTENT).build();
                            });
                });
    }

    @GET
    @Path("/findByPayeeCode")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"public_administration"})
    public Uni<Response> findByPayeeCode(
            @HeaderParam("RequestId")
            @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG)
            @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @QueryParam("payeeCode") String payeeCode,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @Pattern(regexp = RegexPatterns.SORT_STRATEGY_PATTERN)
            @QueryParam("sortStrategy") String sortStrategy,
            @QueryParam("page") int pageNumber,
            @QueryParam("size") int pageSize) {

        Log.debugf("TransactionResource -> findByPayeeCode - Input requestId, payeeCode, startDate, endDate, sortStrategy, page, size: %s, %s, %s, %s, %s, %s, %s", requestId, payeeCode, startDate, endDate, sortStrategy, pageNumber, pageSize);

        checkToken(payeeCode);

        Date convertedStartDate = Utility.convertStringToDate(startDate, true);
        Date convertedEndDate = Utility.convertStringToDate(endDate, false);
        Sort sort = Sort.by(CREATION_TIMESTAMP, "asc".equalsIgnoreCase(sortStrategy) ? Sort.Direction.Ascending : Sort.Direction.Descending);

        return transactionService.getTransactionCountByAttribute("payeeCode", payeeCode)
                .onFailure()
                .transform(err -> {
                    Log.errorf(err, "TransactionResource -> findBy: error while counting transactions for payeeCode", payeeCode);

                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(ErrorCodes.ERROR_COUNTING_TRANSACTIONS, ErrorCodes.ERROR_COUNTING_TRANSACTIONS_MSG))
                            .build());
                })
                .onItem()
                .transformToUni(numberOfTransactions ->
                        transactionService.getTransactionListPagedByAttribute("payeeCode", payeeCode, convertedStartDate, convertedEndDate, sort, pageNumber, pageSize)
                                .onFailure()
                                .transform(err -> {
                                    Log.errorf(err, "TransactionResource -> findBy: Error while retrieving list of transactions for payeeCode [%s], index and size [%s, %s]", payeeCode, pageNumber, pageSize);

                                    return new InternalServerErrorException(Response
                                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                                            .entity(new Errors(ErrorCodes.ERROR_LIST_TRANSACTIONS, ErrorCodes.ERROR_LIST_TRANSACTIONS_MSG))
                                            .build());
                                })
                                .onItem()
                                .transform(transactionsPaged -> {
                                    Log.debugf("TransactionResource -> findBy: size of list of transactions paginated found: [%s]", transactionsPaged.size());

                                    int totalPages = (int) Math.ceil((double) numberOfTransactions / pageSize);
                                    PageMetadata pageMetadata = new PageMetadata(pageSize, numberOfTransactions, totalPages);

                                    return Response
                                            .status(Response.Status.OK)
                                            .entity(new TransactionPageResponse(transactionsPaged, pageMetadata))
                                            .build();
                                }));
    }

    @GET
    @Path("/findByPspId")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"pos_service_provider"})
    public Uni<Response> findByPspId(
            @HeaderParam("RequestId")
            @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG)
            @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @QueryParam("pspId") String pspId,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @Pattern(regexp = RegexPatterns.SORT_STRATEGY_PATTERN)
            @QueryParam("sortStrategy") String sortStrategy,
            @QueryParam("page") int pageNumber,
            @QueryParam("size") int pageSize) {

        Log.debugf("TransactionResource -> findByPspId - Input requestId, pspId, startDate, endDate, sortStrategy, page, size: %s, %s, %s, %s, %s, %s, %s", requestId, pspId, startDate, endDate, sortStrategy, pageNumber, pageSize);

        return solutionService.findAllByLocationOrPsp("pspId", pspId)
                .onFailure()
                .transform(err -> {
                    Log.errorf(err, "TransactionResource -> findByPspId: unexpected error during finding solution with pspId [%s]", pspId);

                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                            .build());
                })
                .onItem()
                .transformToUni(solutionEntities -> {
                    Log.debugf("TransactionResource -> findByPspId: solution found by pspId [%s]: %s", pspId, solutionEntities);

                    List<String> solutionIds = solutionEntities.stream()
                            .map(solution -> solution.id.toString())
                            .toList();

                    return terminalService.findAllBySolutionIds(solutionIds)
                            .onFailure()
                            .transform(err -> {
                                Log.errorf(err, "TransactionResource -> findByPspId: unexpected error during find all terminal by solutionIds [%s]", solutionIds);

                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                                        .build());
                            })
                            .onItem()
                            .transformToUni(terminalEntities -> {
                                Log.debugf("TransactionResource -> findByPspId: terminals found by solutionIds [%s]: %s", solutionIds, terminalEntities);

                                List<String> terminalUuids = terminalEntities.stream()
                                        .map(TerminalEntity::getTerminalUuid)
                                        .toList();

                                return transactionService.getTransactionCountByTerminals(terminalUuids)
                                        .onFailure()
                                        .transform(err -> {
                                            Log.errorf(err, "TransactionResource -> findByPspId: unexpected error during count transaction by terminalUuid [%s]", terminalUuids);

                                            return new InternalServerErrorException(Response
                                                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                                                    .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                                                    .build());
                                        })
                                        .onItem()
                                        .transformToUni(numberOfTransactions -> {
                                            if (numberOfTransactions == 0) {
                                                Log.errorf("TransactionResource -> findByPspId: no transaction found on db by terminalUuids [%s]", terminalUuids);

                                                return Uni.createFrom().failure(new NotFoundException(Response
                                                        .status(Response.Status.NOT_FOUND)
                                                        .entity(new Errors(ErrorCodes.ERROR_TRANSACTION_NOT_FOUND, terminalUuids.toString()))
                                                        .build()));
                                            }

                                            Date convertedStartDate = Utility.convertStringToDate(startDate, true);
                                            Date convertedEndDate = Utility.convertStringToDate(endDate, false);
                                            Sort sort = Sort.by(CREATION_TIMESTAMP, "asc".equalsIgnoreCase(sortStrategy) ? Sort.Direction.Ascending : Sort.Direction.Descending);

                                            return transactionService.getTransactionListPagedByTerminals(terminalUuids, convertedStartDate, convertedEndDate, sort, pageNumber, pageSize)
                                                    .onFailure()
                                                    .transform(err -> {
                                                        Log.errorf(err, "TransactionResource -> findByPspId: unexpected error during find all terminal by solutionIds [%s]", solutionIds);

                                                        return new InternalServerErrorException(Response
                                                                .status(Response.Status.INTERNAL_SERVER_ERROR)
                                                                .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                                                                .build());
                                                    })
                                                    .onItem()
                                                    .transformToUni(transactionsPaged -> {
                                                        Log.debugf("TransactionResource -> findBy: size of list of transactions paginated found: [%s]", transactionsPaged.size());

                                                        int totalPages = (int) Math.ceil((double) numberOfTransactions / pageSize);
                                                        PageMetadata pageMetadata = new PageMetadata(pageSize, numberOfTransactions, totalPages);

                                                        return Uni.createFrom().item(Response
                                                                .status(Response.Status.OK)
                                                                .entity(new TransactionPageResponse(transactionsPaged, pageMetadata))
                                                                .build());
                                                    });
                                        });
                            });
                });
    }

    @DELETE
    @Path("/{transactionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"public_administration"})
    public Uni<Response> deleteTransaction(
            @HeaderParam("RequestId")
            @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG)
            @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @PathParam(value = "transactionId") String transactionId) {

        Log.debugf("TransactionResource -> deleteTransaction - Input requestId, transactionId: %s, %s", requestId,
                transactionId);

        return findTransactionGeneric(transactionId, "deleteTransaction")
                .onItem()
                .transformToUni((transactionEntity ->
                                transactionService.deleteTransaction(transactionEntity)
                                        .onFailure()
                                        .transform(err -> {
                                            Log.errorf(err,
                                                    "TransactionResource -> deleteTransaction: error during deleting transaction [%s]",
                                                    transactionEntity);

                                            return new InternalServerErrorException(Response
                                                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                                                    .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                                                    .build());
                                        })
                                        .onItem()
                                        .transform(transactionUpdated -> {
                                            Log.debugf("TransactionResource -> deleteTransaction: transaction deleted correctly on DB [%s]",
                                                    transactionUpdated);

                                            return Response
                                                    .status(Response.Status.NO_CONTENT)
                                                    .build();
                                        })
                        )
                );
    }

    @PATCH
    @Path("/{transactionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"public_administration"})
    public Uni<Response> updateTransaction(
            @HeaderParam("RequestId")
            @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG)
            @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @Valid @NotNull(message = ErrorCodes.ERROR_DTO_MUST_NOT_BE_NULL_MSG) UpdateTransactionDto transaction,
            @PathParam(value = "transactionId") String transactionId) {

        Log.debugf("TransactionResource -> updateTransaction - Input requestId, updateTransaction: %s, %s", requestId, transaction);

        return findTransactionGeneric(transactionId, "updateTransaction")
                .onItem()
                .transformToUni((transactionEntity ->
                                transactionService.updateTransaction(transactionId, transaction, transactionEntity)
                                        .onFailure()
                                        .transform(err -> {
                                            Log.errorf(err, "TransactionResource -> updateTransaction: error during update transaction [%s]",
                                                    transaction);

                                            return new InternalServerErrorException(Response
                                                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                                                    .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                                                    .build());
                                        })
                                        .onItem()
                                        .transform(transactionUpdated -> {
                                            Log.debugf("TransactionResource -> updateTransaction: transaction updated correctly on DB [%s]",
                                                    transactionUpdated);

                                            return Response
                                                    .status(Response.Status.NO_CONTENT)
                                                    .build();
                                        })
                        )
                );
    }

    @GET
    @Path("/{transactionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"pos_service_provider", "public_administration"})
    public Uni<Response> getTransaction(
            @HeaderParam("RequestId")
            @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG)
            @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @PathParam(value = "transactionId") String transactionId) {

        Log.debugf("TransactionResource -> getTransaction - Input requestId, transactionId: %s, %s", requestId, transactionId);

        return findTransactionGeneric(transactionId, "getTransaction")
                .onItem()
                .transformToUni((transactionEntity ->
                                Uni.createFrom().item(Response
                                        .status(Response.Status.OK)
                                        .entity(transactionEntity)
                                        .build())
                        )
                );
    }

    @GET
    @Path("/latest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"pos_service_provider"})
    public Uni<Response> getLatestTransaction(
            @HeaderParam("RequestId")
            @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG)
            @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @QueryParam("pspId") String pspId,
            @QueryParam("terminalId") String terminalId,
            @QueryParam("status") String status) {

        Log.debugf("TransactionResource -> getLatestTransaction - Input requestId, pspId, terminalId, status: %s, %s, %s, %s", requestId, pspId, terminalId, status);
        Sort sort = Sort.by(CREATION_TIMESTAMP, Sort.Direction.Descending);
        checkToken(pspId);

        return transactionService.latestTransaction(pspId, terminalId, status, sort)
                .onFailure()
                .transform(err -> {
                    Log.errorf(err, "TransactionResource -> getLatestTransaction: unexpected error get latest transaction [%s, %s, %s]", pspId, terminalId, status);

                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                            .build());
                })
                .onItem()
                .transformToUni(transactionEntity -> {
                    if (transactionEntity == null) {
                        Log.errorf("TransactionResource -> getLatestTransaction: error 404 during searching latest transaction [%s, %s, %s]", pspId, terminalId, status);

                        return Uni.createFrom().failure(new NotFoundException(Response
                                .status(Response.Status.NOT_FOUND)
                                .entity(new Errors(ErrorCodes.ERROR_TRANSACTION_NOT_FOUND, ErrorCodes.ERROR_TRANSACTION_NOT_FOUND_MSG))
                                .build()));
                    }
                    Log.debugf("TransactionResource -> getLatestTransaction: transaction found correctly [%s]", transactionEntity);

                    return Uni.createFrom().item(Response
                            .status(Response.Status.OK)
                            .entity(transactionEntity)
                            .build());
                });
    }

    private Uni<TransactionEntity> findTransactionGeneric(String transactionId, String calledBy) {
        return transactionService.findTransaction(transactionId)
                .onFailure()
                .transform(err -> {
                    Log.errorf(err, "TransactionResource ->  %s : error during search transaction with transactionId: [%s]", calledBy, transactionId);

                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                            .build());
                })
                .onItem()
                .transformToUni(transactionEntity -> {
                    if (transactionEntity == null) {
                        Log.errorf("TransactionResource -> %s : error 404 during searching transaction with transactionId: [%s]", calledBy, transactionId);

                        return Uni.createFrom().failure(new NotFoundException(Response
                                .status(Response.Status.NOT_FOUND)
                                .entity(new Errors(ErrorCodes.ERROR_TRANSACTION_NOT_FOUND, ErrorCodes.ERROR_TRANSACTION_NOT_FOUND_MSG))
                                .build()));
                    }

                    return Uni.createFrom().item(transactionEntity);
                });
    }

    private void checkToken(String toCheck) {
        Log.debugf("TransactionResource -> checkToken: sub [%s], pspId/payeeCode: [%s]", jwt.getSubject(), toCheck);

        if (!jwt.getSubject().equals(toCheck)) {
            Log.errorf("TransactionResource -> checkToken: Error while checking token, subject not equals to pspId/payeeCode [%s, %s]", jwt.getSubject(), toCheck);

            throw new WebApplicationException(Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(new Errors(ErrorCodes.ERROR_CHECK_TOKEN, ErrorCodes.ERROR_CHECK_TOKEN_MSG))
                    .build());
        }
    }
}