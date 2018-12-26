package ca.purpleowl.examples.swagger.rest.controller;

import ca.purpleowl.examples.swagger.rest.asset.ProgrammerAsset;
import ca.purpleowl.examples.swagger.service.ProgrammerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.EmbeddedWrappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * This controller serves up and persists Programmer profiles.  Profiles are retrieved from and stored to the
 * persistence mechanism using JPA, which is translated to JSON Asset classes for transmittal via the REST Endpoints
 * contained here.
 */
@Log
@Api(tags = {"Programmer"},
     description = "Endpoint for managing Programmers",
     produces = "application/hal+json",
     consumes = "application/json",
     protocols = "http")
@RestController
@RequestMapping(value = "/programmer")
public class ProgrammerController {
    private final ProgrammerService programmerService;

    private static final String RETRIEVE_PROGRAMMER = "retrieveProgrammer";
    private static final String RETRIEVE_ALL_PROGRAMMERS = "retrieveAllProgrammers";
    private static final String CREATE_PROGRAMMER = "createProgrammer";
    private static final String WRAP_ASSET = "wrapAsset";

    /**
     * Autowired constructor which accepts a ProgrammerService as a parameter.
     *
     * @param programmerService - An instance of ProgrammerService, which loosens coupling between the Controller and the JPA model.
     */
    @Autowired
    public ProgrammerController(ProgrammerService programmerService) {
        this.programmerService = programmerService;
    }

    /**
     * Accepts a numeric ID as a path parameter which should represent the ID of a programmer profile stored in the
     * persistence mechanism.  If the record exists, a JSON representation of that profile will be returned, along with
     * the appropriate Hypermedia links: a link labelled "self" for this endpoint, as well as a link labelled "team" for
     * the Team endpoint if the Programmer profile is associated with a Team.
     *
     * @param programmerId - A numeric representation of the ID of the desired Programmer profile
     * @return A ResponseEntity containing a relevant Status Code and a body containing a JSON representation of the Programmer profile
     */
    @ApiOperation(value = "Retrieves a programmer's profile from the persistence mechanism",
                  notes = "Accepts a numeric ID as a path parameter which should represent the ID of a programmer " +
                          "profile stored in the mechanism.  If the record exists, a JSON representation of that " +
                          "profile will be returned, along with the appropriate Links: a link labelled \"self\" for " +
                          "this endpoint, as well as a link labelled \"team\" for the Team endpoint if the " +
                          "Programmer profile is associated with a Team.",
                  response = ProgrammerAsset.class,
                  httpMethod = "GET",
                  produces = "application/hal+json")
    @ApiResponses(value = {
            @ApiResponse(code = 200,
                         message = "Successful retrieval of Programmer Profile",
                         response = ProgrammerAsset.class),
            @ApiResponse(code = 400,
                         message = "The Programmer ID was invalid or not supplied"),
            @ApiResponse(code = 404,
                         message = "Programmer profile was not found"),
            @ApiResponse(code = 500,
                         message = "Internal error")
    })
    @RequestMapping(value = "/{programmerId}", method = RequestMethod.GET, produces = "application/hal+json")
    public ResponseEntity<Resource> retrieveProgrammer(
            @PathVariable("programmerId")
            @ApiParam(value = "ID of the desired Programmer profile",
                      allowableValues = "range[1, infinity]",
                      //Actually doesn't refer to the value being empty, but allowing the "default value" to be empty.
                      allowEmptyValue = true,
                      required = true)
            Long programmerId) {
        log.entering(ProgrammerController.class.getName(), RETRIEVE_PROGRAMMER, programmerId);
        ProgrammerAsset asset = programmerService.findProgrammer(programmerId);

        if(asset != null) {
            if(log.isLoggable(Level.FINER)){
                log.finer(String.format("Found Programmer with ID %d: %s", programmerId, asset));
            }

            Resource returnMe = wrapAsset(asset);

            log.exiting(ProgrammerController.class.getName(), RETRIEVE_PROGRAMMER, returnMe);
            return ResponseEntity.ok(returnMe);
        }

        log.exiting(ProgrammerController.class.getName(), RETRIEVE_PROGRAMMER, HttpStatus.NOT_FOUND);
        return ResponseEntity.notFound().build();
    }

    /**
     * Accepts an optional numeric ID as a query parameter.  If provided, this represents the ID of the team for which
     * all Programmer profiles should be listed.  If no such parameter is provided, then all Programmer profiles within
     * the system are returned.  These are wrapped within Resource wrappers and returned within a ResponseEntity which
     * provides the Status Code and any other relevant information regarding the success or failure of the request.
     *
     * @param teamId - An optional parameter representing the numeric ID of the Team for which all programmers should be listed.  If not used, null should be provided.
     * @return A ResponseEntity object containing a relevant Status Code and a JSON representation of the desired Programmer profiles.
     */
    @ApiOperation(value = "Retrieves all programmers from the persistence mechanism",
                  notes = "Accepts an optional numeric ID as a query parameter.  If provided, this represents the " +
                          "ID of the team for which all Programmer profiles should be listed.  If no such parameter " +
                          "is provided, then all Programmer profiles within the system are returned.  These are " +
                          "wrapped within Resource wrappers and returned within a ResponseEntity which provides the " +
                          "Status Code and any other relevant information regarding the success or failure of the " +
                          "request.",
                  response = ProgrammerAsset[].class,
                  httpMethod = "GET",
                  produces = "application/hal+json")
    @ApiResponses(value = {
            @ApiResponse(code = 200,
                         message = "Successful retrieval of all programmer profiles",
                         response = ProgrammerAsset[].class),
            @ApiResponse(code = 500,
                         message = "Internal error")
    })
    @RequestMapping(method = RequestMethod.GET, produces = "application/hal+json")
    public ResponseEntity<Resources> retrieveAllProgrammers(
            @RequestParam(name = "teamId",
                          required = false)
            @ApiParam(value = "Optional ID of the Team for which all Programmer profiles should be listed",
                      allowableValues = "range[1, infinity]",
                      allowEmptyValue = true)
            Long teamId) {
        log.entering(ProgrammerController.class.getName(), RETRIEVE_ALL_PROGRAMMERS, teamId);

        List<Resource> programmers;
        if(teamId == null) {
            log.finer("querying for all programmers");
            programmers = programmerService.findAllProgrammers()
                                           .stream()
                                           .map(this::wrapAsset)
                                           .collect(Collectors.toList());
        } else {
            log.finer(String.format("querying for all programmers on team %d", teamId));
            programmers = programmerService.findAllProgrammersOnTeam(teamId)
                                           .stream()
                                           .map(this::wrapAsset)
                                           .collect(Collectors.toList());
        }

        //We will build a link back to this endpoint and title it as "self."
        List<Link> selfLink = Collections.singletonList(
                linkTo(methodOn(ProgrammerController.class).retrieveAllProgrammers(teamId))
                        .withSelfRel()
                        //We call this to remove reference to template variables.
                        .expand()
        );

        Resources resources;

        if(programmers.isEmpty()) {
            EmbeddedWrappers wrappers = new EmbeddedWrappers(true);
            log.info("returning empty list");
            //We have to do that pain in the ass thing so that we still return the list even though it's empty.
            resources =
                    new Resources<>(Collections.singletonList(wrappers.emptyCollectionOf(ProgrammerAsset.class)),
                                    selfLink);
        } else {
            log.info(String.format("returning list with %d programmers", programmers.size()));
            resources = new Resources<>(programmers, selfLink);
        }

        log.exiting(ProgrammerController.class.getName(), RETRIEVE_ALL_PROGRAMMERS, resources);
        return ResponseEntity.ok(resources);
    }

    /**
     * Accepts a ProgrammerAsset as a parameter, deserialized from the body of a Post Request, and returns a
     * ProgrammerAsset in JSON representing that Programmer Profile after having been saved to the persistence
     * mechanism.
     *
     * Additionally, a return code of either 200, 400, or 500 will be attached to the ResponseEntity which effectively
     * wraps the wrapper placed around the ProgrammerAsset object.  There's a lot of wrapping here, mainly to satisfy
     * some weirdness in my implementation of the Swagger2 framework.
     *
     * @param programmerAsset - A ProgrammerAsset deserialized from the body of the request.
     * @return A ProgrammerAsset wrapped in a Resource, loaded into the body of a ResponseEntity.
     */
    @ApiOperation(value = "Saves a programmer's profile to the persistence mechanism.",
                  notes = "Accepts a JSON representation of a Programmer's profile, saves it to the Persistence " +
                          "Mechanism, and then returns an updated JSON representation of that Programmer Profile, " +
                          "including the ID of the newly saved profile.",
                  response = ProgrammerAsset.class,
                  httpMethod = "POST",
                  produces = "application/hal+json",
                  consumes = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200,
                         message = "Successfully saved the Programmer's profile",
                         response = ProgrammerAsset.class),
            @ApiResponse(code = 400,
                         message = "The provided profile was invalid or incomplete."),
            @ApiResponse(code = 500,
                         message = "Internal Error")
    })
    @RequestMapping(method = RequestMethod.POST,
                    consumes = "application/json",
                    produces = "application/hal+json")
    public ResponseEntity<Resource> createProgrammer(
            @RequestBody
            @ApiParam(value = "A JSON representation of the Programmer profile to be saved to the persistence mechanism")
            ProgrammerAsset programmerAsset) {
        log.entering(ProgrammerController.class.getName(), CREATE_PROGRAMMER, programmerAsset);
        ProgrammerAsset savedProgrammer = programmerService.saveProgrammer(programmerAsset);

        Resource returnMe = wrapAsset(savedProgrammer);

        log.exiting(ProgrammerController.class.getName(), CREATE_PROGRAMMER, returnMe);
        return ResponseEntity.ok(returnMe);
    }

    /**
     * Warps a ProgrammerAsset in a Resource wrapper and also adds the appropriate Links to the endpoint to read the
     * Programmer's profile and the endpoint to read the Programmer's Team's profile.
     *
     * @param asset - A ProgrammerAsset describing a Programmer profile.
     * @return A Resource wrapping the ProgrammerAsset and populated with appropriate Hypermedia links.
     */
    private Resource wrapAsset(ProgrammerAsset asset) {
        log.entering(ProgrammerController.class.getName(), WRAP_ASSET, asset);

        List<Link> links = new ArrayList<>();

        if(asset.getProgrammerId() != null) {
            links.add(linkTo(methodOn(ProgrammerController.class).retrieveProgrammer(asset.getProgrammerId()))
                            //Actually, I don't know if this is necessarily always right (ie. being a selfRel)...
                            .withSelfRel()
                            .expand());
        }

        if(asset.getTeamId() != null) {
            links.add(linkTo(methodOn(TeamController.class).retrieveTeam(asset.getTeamId()))
                            .withRel("team")
                            .expand());
        }

        Resource returnMe = new Resource<>(asset, links);

        log.exiting(ProgrammerController.class.getName(), WRAP_ASSET, returnMe);
        return returnMe;
    }
}
