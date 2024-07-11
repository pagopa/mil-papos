package it.pagopa.swclient.mil.papos.resource;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.model.TransactionDto;
import it.pagopa.swclient.mil.papos.service.TransactionService;
import it.pagopa.swclient.mil.papos.util.ErrorCodes;
import it.pagopa.swclient.mil.papos.util.Errors;
import it.pagopa.swclient.mil.papos.util.RegexPatterns;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/transactions")
public class TransactionResource {

    private final TransactionService transactionService;

    public TransactionResource(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // @RolesAllowed({ "pos_service_provider" })
    public Uni<Response> createTransaction(
            @HeaderParam("RequestId")
            @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG)
            @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @Valid @NotNull(message = ErrorCodes.ERROR_DTO_MUST_NOT_BE_NULL_MSG) TransactionDto transaction) {

        Log.debugf("TransactionResource -> createTransaction - Input requestId, createTransaction: %s, %s", requestId, transaction);

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
    }
}
