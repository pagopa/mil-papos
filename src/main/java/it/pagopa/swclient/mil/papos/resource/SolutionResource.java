package it.pagopa.swclient.mil.papos.resource;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.papos.model.PageMetadata;
import it.pagopa.swclient.mil.papos.model.SolutionDto;
import it.pagopa.swclient.mil.papos.model.SolutionPageResponse;
import it.pagopa.swclient.mil.papos.service.SolutionService;
import it.pagopa.swclient.mil.papos.util.ErrorCodes;
import it.pagopa.swclient.mil.papos.util.Errors;
import it.pagopa.swclient.mil.papos.util.RegexPatterns;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/solutions")
public class SolutionResource {
    private final SolutionService solutionService;

    public SolutionResource(SolutionService solutionService) {
        this.solutionService = solutionService;
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "mil_papos_admin" })
    public Uni<Response> createSolution(
            @HeaderParam("RequestId") @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG) @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @Valid @NotNull(message = ErrorCodes.ERROR_DTO_MUST_NOT_BE_NULL_MSG) SolutionDto solution) {

        Log.debugf("SolutionResource -> createSolution - Input requestId, solutionDto: %s, %s", requestId, solution);

        return solutionService.createSolution(solution)
                .onFailure()
                .transform(err -> {
                    Log.errorf(err,
                            "SolutionResource -> createSolution: unexpected error during persist for solution [%s]",
                            solution);

                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                            .build());
                })
                .onItem()
                .transform(solutionSaved -> {
                    Log.debugf("SolutionResource -> createSolution: solution saved correctly on DB [%s]",
                            solutionSaved);

                    return Response
                            .status(Response.Status.CREATED)
                            .entity(solutionSaved)
                            .build();
                });
    }

    @GET
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "mil_papos_admin" })
    public Uni<Response> getSolutions(
            @HeaderParam("RequestId") @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG) @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @QueryParam("page") int pageNumber,
            @QueryParam("size") int pageSize) {

        return solutionService
                .getSolutionsCount()
                .onFailure()
                .transform(err -> {
                    Log.errorf(err, "SolutionResource -> findAll: error while counting all solutions");

                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(ErrorCodes.ERROR_COUNTING_SOLUTIONS,
                                    ErrorCodes.ERROR_COUNTING_SOLUTIONS_MSG))
                            .build());
                })
                .onItem()
                .transformToUni(numberOfSolutions -> {
                    Log.debugf("SolutionResource -> findAll: found a total count of [%s] solutions", numberOfSolutions);

                    return solutionService.findSolutions(requestId, pageNumber, pageSize)
                            .onFailure()
                            .transform(err -> {
                                Log.errorf(err,
                                        "SolutionResources -> findAll: Error while retrieving list of solutions, index and size [%s, %s]",
                                        pageNumber, pageSize);

                                return new InternalServerErrorException(Response
                                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                                        .entity(new Errors(ErrorCodes.ERROR_LIST_SOLUTIONS,
                                                ErrorCodes.ERROR_LIST_SOLUTIONS_MSG))
                                        .build());
                            })
                            .onItem()
                            .transform(solutionsPaged -> {
                                Log.debugf(
                                        "SolutionResource -> findAll: size of list of solutions paginated found: [%s]",
                                        solutionsPaged.size());

                                int totalPages = (int) Math.ceil((double) numberOfSolutions / pageSize);
                                PageMetadata pageMetadata = new PageMetadata(pageSize, numberOfSolutions, totalPages);

                                return Response
                                        .status(Response.Status.OK)
                                        .entity(new SolutionPageResponse(solutionsPaged, pageMetadata))
                                        .build();
                            });
                });
    }

    @GET
    @Path("/{solutionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ "mil_papos_admin" })
    public Uni<Response> findSolution(
            @HeaderParam("RequestId") @NotNull(message = ErrorCodes.ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG) @Pattern(regexp = RegexPatterns.REQUEST_ID_PATTERN) String requestId,
            @PathParam(value = "solutionId") String solutionId) {

        Log.debugf("SolutionResource -> findSolution: Input requestId, solutionId: %s, %s", requestId, solutionId);

        return solutionService.findById(solutionId)
                .onFailure()
                .transform(err -> {
                    Log.errorf(err,
                            "SolutionResource -> findSolution: error during search solution with solutionId: [%s]",
                            solutionId);

                    return new InternalServerErrorException(Response
                            .status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(ErrorCodes.ERROR_GENERIC_FROM_DB, ErrorCodes.ERROR_GENERIC_FROM_DB_MSG))
                            .build());
                })
                .onItem()
                .transformToUni(solution -> {
                    if (solution == null) {
                        Log.errorf(
                                "SolutionResource -> findSolution: error 404 during searching solution with solutionId: [%s, %s]",
                                solutionId);

                        return Uni.createFrom().failure(new NotFoundException(Response
                                .status(Response.Status.NOT_FOUND)
                                .entity(new Errors(ErrorCodes.ERROR_SOLUTION_NOT_FOUND,
                                        ErrorCodes.ERROR_SOLUTION_NOT_FOUND_MSG))
                                .build()));
                    }

                    return Uni.createFrom().item(Response
                            .status(Response.Status.OK)
                            .entity(solution)
                            .build());
                });
    }
}
