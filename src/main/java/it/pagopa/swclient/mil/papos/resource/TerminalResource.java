package it.pagopa.swclient.mil.papos.resource;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.model.TerminalDto;
import it.pagopa.swclient.mil.papos.service.TerminalService;
import it.pagopa.swclient.mil.papos.util.ErrorCodes;
import it.pagopa.swclient.mil.papos.util.Errors;
import it.pagopa.swclient.mil.papos.util.RegexPatterns;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/terminals")
public class TerminalResource {
    private final TerminalService terminalService;

    public TerminalResource(TerminalService terminalService) {
        this.terminalService = terminalService;
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // @RolesAllowed({ "pos_service_provider" })
    public Uni<Response> createTerminal(
            @HeaderParam("RequestId")
            @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG)
            @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @Valid @NotNull(message = ErrorCodes.ERROR_TERMINALDTO_MUST_NOT_BE_NULL_MSG) TerminalDto terminal) {

        Log.debugf("TerminalResource -> createTerminal - Input requestId, createTerminal: %s, %s", requestId, terminal);

        return terminalService.createTerminal(terminal)
                .onFailure()
                .transform(err -> {
                    Log.errorf(err,
                            "TerminalResource -> createTerminal: unexpected error during persist for terminal [%s]",
                            terminal);

                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB,
                                    ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                            .build());
                })
                .onItem()
                .transform(terminalSaved -> {
                    Log.debugf("TerminalResource -> createTerminal: terminal saved correctly on DB [%s]",
                            terminalSaved);

                    return Response.status(Response.Status.CREATED).build();
                });
    }

    @PATCH
    @Path("/{terminalUuid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // @RolesAllowed({ "pos_service_provider" })
    public Uni<Response> updateTerminal(
            @HeaderParam("RequestId")
            @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG)
            @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @Valid @NotNull(message = ErrorCodes.ERROR_TERMINALDTO_MUST_NOT_BE_NULL_MSG) TerminalDto terminal,
            @PathParam(value = "terminalUuid") String terminalUuid) {

        Log.debugf("TerminalResource -> updateTerminal - Input requestId, updateTerminal: %s, %s", requestId, terminal);

        return terminalService.findTerminal(terminalUuid)
                .onFailure()
                .transform(err -> {
                    Log.errorf(err,
                            "TerminalResource -> updateTerminal: error during search terminal with terminalUuid: [%s]",
                            terminalUuid);

                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                            .build());
                })
                .onItem()
                .transformToUni(terminalEntity -> {
                    if (terminalEntity == null) {
                        Log.errorf(
                                "TerminalResource -> updateTerminal: error 404 during searching terminal with terminalUuid: [%s]",
                                terminalUuid);

                        return Uni.createFrom().failure(new NotFoundException(Response
                                .status(Response.Status.NOT_FOUND)
                                .entity(new Errors(ErrorCodes.ERROR_TERMINAL_NOT_FOUND,
                                        ErrorCodes.ERROR_TERMINAL_NOT_FOUND_MSG))
                                .build()));
                    }

                    return terminalService.updateTerminal(terminalUuid, terminal, terminalEntity)
                            .onFailure()
                            .transform(err -> {
                                Log.errorf(err, "TerminalResource -> updateTerminal: error during update terminal [%s]",
                                        terminal);

                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB,
                                                ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                                        .build());
                            })
                            .onItem()
                            .transform(terminalUpdated -> {
                                Log.debugf("TerminalResource -> updateTerminal: terminal updated correctly on DB [%s]",
                                        terminalUpdated);

                                return Response
                                        .status(Response.Status.NO_CONTENT)
                                        .build();
                            });
                });
    }

    @DELETE
    @Path("/{terminalUuid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // @RolesAllowed({ "pos_service_provider" })
    public Uni<Response> deleteTerminal(
            @HeaderParam("RequestId")
            @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG)
            @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @PathParam(value = "terminalUuid") String terminalUuid) {

        Log.debugf("TerminalResource -> deleteTerminal - Input requestId, terminalUuid: %s, %s", requestId,
                terminalUuid);

        return terminalService.findTerminal(terminalUuid)
                .onFailure()
                .transform(err -> {
                    Log.errorf(err,
                            "TerminalResource -> deleteTerminal: error during search terminal with terminalUuid: [%s]",
                            terminalUuid);

                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                            .build());
                })
                .onItem()
                .transformToUni(terminalEntity -> {
                    if (terminalEntity == null) {
                        Log.errorf(
                                "TerminalResource -> deleteTerminal: error 404 during searching terminal with terminalUuid: [%s, %s]",
                                terminalUuid);

                        return Uni.createFrom().failure(new NotFoundException(Response
                                .status(Response.Status.NOT_FOUND)
                                .entity(new Errors(ErrorCodes.ERROR_TERMINAL_NOT_FOUND,
                                        ErrorCodes.ERROR_TERMINAL_NOT_FOUND_MSG))
                                .build()));
                    }

                    return terminalService.deleteTerminal(terminalEntity)
                            .onFailure()
                            .transform(err -> {
                                Log.errorf(err,
                                        "TerminalResource -> deleteTerminal: error during deleting terminal [%s]",
                                        terminalEntity);

                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB,
                                                ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                                        .build());
                            })
                            .onItem()
                            .transform(terminalUpdated -> {
                                Log.debugf("TerminalResource -> deleteTerminal: terminal deleted correctly on DB [%s]",
                                        terminalUpdated);

                                return Response
                                        .status(Response.Status.NO_CONTENT)
                                        .build();
                            });
                });
    }
}
