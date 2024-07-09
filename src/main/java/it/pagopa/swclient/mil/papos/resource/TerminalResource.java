package it.pagopa.swclient.mil.papos.resource;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import it.pagopa.swclient.mil.papos.model.PageMetadata;
import it.pagopa.swclient.mil.papos.model.TerminalDto;
import it.pagopa.swclient.mil.papos.model.TerminalPageResponse;
import it.pagopa.swclient.mil.papos.model.WorkstationsDto;
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
import org.jboss.resteasy.reactive.RestForm;

import java.io.InputStream;


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

    @POST
    @Path("/bulkload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    // @RolesAllowed({ "pos_service_provider" })
    public Uni<Response> bulkLoadTerminals(
            @HeaderParam("RequestId")
            @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG)
            @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @RestForm("file") InputStream fileInputStream) {

        Log.debugf("TerminalResource -> bulkLoadTerminals: Input requestId, fileInputStream: %s, %s", requestId, fileInputStream);

        if (fileInputStream == null) {
            Log.error("TerminalResource -> bulkLoadTerminals: error fileInputStream is null");

            return Uni.createFrom().item(() ->
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(new Errors(ErrorCodes.ERROR_BULKLOAD_FILE_MUST_NOT_BE_NULL, ErrorCodes.ERROR_BULKLOAD_FILE_MUST_NOT_BE_NULL_MSG))
                            .build()
            );
        }

        return Uni.createFrom().item(Unchecked.supplier(fileInputStream::readAllBytes)).onItem()
                .transformToUni(file -> {
                    if (file.length == 0) {
                        Log.error("TerminalResource -> bulkLoadTerminals: error uploaded file is empty");

                        return Uni.createFrom().item(() ->
                                Response.status(Response.Status.BAD_REQUEST)
                                        .entity(new Errors(ErrorCodes.ERROR_BULKLOAD_FILE_MUST_NOT_BE_NULL, ErrorCodes.ERROR_BULKLOAD_FILE_MUST_NOT_BE_NULL_MSG))
                                        .build()
                        );
                    }

                    return terminalService.processBulkLoad(file)
                            .onFailure()
                            .transform(err -> {
                                Log.errorf(err, "TerminalResource -> bulkLoadTerminals: error during bulkLoad process with file: length [%d] bytes", file.length);

                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                                        .build());
                            })
                            .onItem()
                            .transform(bulkLoadStatus -> {
                                Log.debugf("TerminalResource -> bulkLoadTerminals: bulkLoad terminals completed [%s]", bulkLoadStatus);

                                return Response
                                        .status(Response.Status.ACCEPTED)
                                        .entity(bulkLoadStatus)
                                        .build();
                            });
                });
    }

    @GET
    @Path("/bulkload/{bulkLoadingId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // @RolesAllowed({ "pos_service_provider" })
    public Uni<Response> getBulkLoadingStatusFile(
            @HeaderParam("RequestId")
            @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG)
            @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @PathParam(value = "bulkLoadingId") String bulkLoadingId) {

        Log.debugf("TerminalResource -> getBulkLoadingStatusFile: Input requestId, bulkLoadingId: %s, %s", requestId, bulkLoadingId);

        return terminalService.findBulkLoadStatus(bulkLoadingId)
                .onFailure()
                .transform(err -> {
                    Log.errorf(err,
                            "TerminalResource -> getBulkLoadingStatusFile: error during search bulkLoadStatus with bulkLoadingId: [%s]",
                            bulkLoadingId);

                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                            .build());
                })
                .onItem()
                .transformToUni(bulkLoadStatus -> {
                    if (bulkLoadStatus == null) {
                        Log.errorf(
                                "TerminalResource -> getBulkLoadingStatusFile: error 404 during searching bulkLoadStatus with bulkLoadingId: [%s, %s]",
                                bulkLoadingId);

                        return Uni.createFrom().failure(new NotFoundException(Response
                                .status(Response.Status.NOT_FOUND)
                                .entity(new Errors(ErrorCodes.ERROR_BULKLOADSTATUS_NOT_FOUND, ErrorCodes.ERROR_BULKLOADSTATUS_NOT_FOUND_MSG))
                                .build()));
                    }

                    return Uni.createFrom().item(Response
                            .status(Response.Status.OK)
                            .entity(bulkLoadStatus)
                            .build());
                });
    }

    @GET
    @Path("/findByPayeeCode")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // @RolesAllowed({ "pos_service_provider" })
    public Uni<Response> findByPayeeCode(
            @HeaderParam("RequestId")
            @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG)
            @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @QueryParam("payeeCode") String payeeCode,
            @QueryParam("page") int pageNumber,
            @QueryParam("size") int pageSize) {

        return findByAttribute(requestId, "payeeCode", payeeCode, pageNumber, pageSize);
    }

    @GET
    @Path("/findByPspId")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // @RolesAllowed({ "pos_service_provider" })
    public Uni<Response> findByPspId(
            @HeaderParam("RequestId")
            @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG)
            @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @QueryParam("pspId") String pspId,
            @QueryParam("page") int pageNumber,
            @QueryParam("size") int pageSize) {

        return findByAttribute(requestId, "pspId", pspId, pageNumber, pageSize);
    }

    @GET
    @Path("/findByWorkstation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // @RolesAllowed({ "pos_service_provider" })
    public Uni<Response> findByWorkstation(
            @HeaderParam("RequestId")
            @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG)
            @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @QueryParam("workstation") String workstation,
            @QueryParam("page") int pageNumber,
            @QueryParam("size") int pageSize) {

        return findByAttribute(requestId, "workstation", workstation, pageNumber, pageSize);
    }

    @PATCH
    @Path("/{terminalUuid}/updateWorkstations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // @RolesAllowed({ "pos_service_provider" })
    public Uni<Response> updateWorkstations(
            @HeaderParam("RequestId")
            @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG)
            @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @Valid @NotNull(message = ErrorCodes.ERROR_TERMINALDTO_MUST_NOT_BE_NULL_MSG) WorkstationsDto workstations,
            @PathParam(value = "terminalUuid") String terminalUuid) {

        Log.debugf("TerminalResource -> updateWorkstations - Input requestId, workstations: %s, %s", requestId, workstations);

        return terminalService.findTerminal(terminalUuid)
                .onFailure()
                .transform(err -> {
                    Log.errorf(err,
                            "TerminalResource -> updateWorkstations: error during search terminal with terminalUuid: [%s]",
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
                                "TerminalResource -> updateWorkstations: error 404 during searching terminal with terminalUuid: [%s]",
                                terminalUuid);

                        return Uni.createFrom().failure(new NotFoundException(Response
                                .status(Response.Status.NOT_FOUND)
                                .entity(new Errors(ErrorCodes.ERROR_TERMINAL_NOT_FOUND, ErrorCodes.ERROR_TERMINAL_NOT_FOUND_MSG))
                                .build()));
                    }

                    return terminalService.updateWorkstations(workstations, terminalEntity)
                            .onFailure()
                            .transform(err -> {
                                Log.errorf(err, "TerminalResource -> updateWorkstations: error during update workstations of terminalUuid: [%s]",
                                        terminalUuid);

                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                                        .build());
                            })
                            .onItem()
                            .transform(terminalUpdated -> {
                                Log.debugf("TerminalResource -> updateWorkstations: workstations updated correctly on DB [%s]",
                                        terminalUpdated);

                                return Response
                                        .status(Response.Status.NO_CONTENT)
                                        .build();
                            });
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
                                .entity(new Errors(ErrorCodes.ERROR_TERMINAL_NOT_FOUND, ErrorCodes.ERROR_TERMINAL_NOT_FOUND_MSG))
                                .build()));
                    }

                    return terminalService.updateTerminal(terminalUuid, terminal, terminalEntity)
                            .onFailure()
                            .transform(err -> {
                                Log.errorf(err, "TerminalResource -> updateTerminal: error during update terminal [%s]",
                                        terminal);

                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
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
                                .entity(new Errors(ErrorCodes.ERROR_TERMINAL_NOT_FOUND, ErrorCodes.ERROR_TERMINAL_NOT_FOUND_MSG))
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
                                        .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
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

    private Uni<Response> findByAttribute(String requestId, String attributeName, String attributeValue, int pageNumber, int pageSize) {
        Log.debugf("TerminalResource -> findBy - Input requestId: %s, attributeName: %s, attributeValue: %s, pageNumber: %s, size: %s", requestId, attributeName, attributeValue, pageNumber, pageSize);

        return terminalService.getTerminalCountByAttribute(attributeName, attributeValue)
                .onFailure()
                .transform(err -> {
                    Log.errorf(err, "TerminalResource -> findBy: error while counting terminals for [%s, %s]", attributeName, attributeValue);

                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(ErrorCodes.ERROR_COUNTING_TERMINALS, ErrorCodes.ERROR_COUNTING_TERMINALS_MSG))
                            .build());
                })
                .onItem()
                .transformToUni(numberOfTerminals -> {
                    Log.debugf("TerminalResource -> findBy: found a total count of [%s] terminals", numberOfTerminals);

                    return terminalService.getTerminalListPagedByAttribute(attributeName, attributeValue, pageNumber, pageSize)
                            .onFailure()
                            .transform(err -> {
                                Log.errorf(err, "TerminalResource -> findBy: Error while retrieving list of terminals for [%s, %s], index and size [%s, %s]", attributeName, attributeValue, pageNumber, pageSize);

                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(ErrorCodes.ERROR_LIST_TERMINALS, ErrorCodes.ERROR_LIST_TERMINALS_MSG))
                                        .build());
                            })
                            .onItem()
                            .transform(terminalsPaged -> {
                                Log.debugf("TerminalResource -> findBy: size of list of terminals paginated found: [%s]", terminalsPaged.size());

                                int totalPages = (int) Math.ceil((double) numberOfTerminals / pageSize);
                                PageMetadata pageMetadata = new PageMetadata(pageSize, numberOfTerminals, totalPages);

                                return Response
                                        .status(Response.Status.OK)
                                        .entity(new TerminalPageResponse(terminalsPaged, pageMetadata))
                                        .build();
                            });
                });
    }
}
