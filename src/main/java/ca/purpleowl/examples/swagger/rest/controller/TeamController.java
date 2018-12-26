package ca.purpleowl.examples.swagger.rest.controller;

import ca.purpleowl.examples.swagger.jpa.entity.Team;
import ca.purpleowl.examples.swagger.rest.asset.TeamAsset;
import ca.purpleowl.examples.swagger.service.TeamService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * This controller serves up and persists Team profiles.  Profiles are retrieved from and stored to the Persistence
 * Mechanism using JPA.  JPA Entities get converted into JSON Asset classes for transmittal via the REST endpoints in
 * this class.
 */
@Log
@Api(tags = {"Team"},
     description = "Endpoint for managing Teams",
     produces = "application/hal+json",
     consumes = "application/json",
     protocols = "http")
@RestController
@RequestMapping("/team")
public class TeamController {
    private static final String RETRIEVE_ALL_TEAMS = "retrieveAllTeams";
    private static final String RETRIEVE_TEAM = "retrieveTeam";
    private static final String CREATE_TEAM = "createTeam";
    private static final String ADD_PROGRAMMER_TO_TEAM = "addProgrammerToTeam";
    private static final String WRAP_ASSET = "wrapAsset";

    private final TeamService teamService;

    @Autowired
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * Accepts a numeric ID as a path parameter shoudl should represent the ID of a Team stored in the persistence
     * mechanism for which information is desired.  If the record exists, a JSON representation of that Team is
     * returned, along with Hypermedia links to this endpoint (labelled "self") and to the list of programmers
     * assigned to the team (labelled "programmers").
     *
     * @param teamId - A numeric representation of the ID of the desired Team
     * @return A ResponseEntity containing a relevant status code and a body containing a JSON representation of the Team.
     */
    @ApiOperation(value = "Retrieves a team from the persistence mechanism",
                  notes = "Accepts a numeric ID as a path parameter shoudl should represent the ID of a Team " +
                          "stored in the persistence mechanism for which information is desired.  If the record " +
                          "exists, a JSON representation of that Team is returned, along with Hypermedia links to " +
                          "this endpoint (labelled \"self\") and to the list of programmers assigned to the team " +
                          "(labelled \"programmers\").",
                  response = Team.class,
                  httpMethod = "GET",
                  produces = "application/hal+json")
    @ApiResponses(value = {
            @ApiResponse(code = 200,
                         message = "Successful retrieval of a Team",
                         response = Team.class),
            @ApiResponse(code = 404,
                         message = "Team was not found"),
            @ApiResponse(code = 500,
                         message = "Internal error")
    })
    @RequestMapping(path = "/{teamId}", method = RequestMethod.GET, produces = "application/hal+json")
    public ResponseEntity<Resource> retrieveTeam(
            @PathVariable("teamId")
            @ApiParam(value = "ID of the desired Team",
                      allowableValues = "range[1, infinity]",
                      allowEmptyValue = true,
                      required = true)
            Long teamId) {
        log.entering(TeamController.class.getName(), RETRIEVE_TEAM, teamId);
        TeamAsset team = teamService.findTeam(teamId);

        if(team != null) {
            Resource returnMe = wrapAsset(team);

            log.exiting(TeamController.class.getName(), RETRIEVE_TEAM, returnMe);
            return ResponseEntity.ok(returnMe);
        }

        log.exiting(TeamController.class.getName(), RETRIEVE_TEAM, HttpStatus.NOT_FOUND);
        return ResponseEntity.notFound().build();
    }

    /**
     * Returns a collection of all Team profiles within the persistence mechanism.  Each Team profile is wrapped in a
     * Resource class (to allow for insertion of Hypermedia links for each Team), which are collected under a single
     * Resources instance (to allow for insertion of Hypermedia links at the root of the collection).
     *
     * The ResponseEntity which wraps the response is also used to describe the success or failure of the requested
     * operation.
     *
     * @return A list of TeamAssets wrapped in Resource wrappers, themselves contained by a Resources collection wrapper.
     */
    @ApiOperation(value = "Retrieves all teams from the persistence mechanism",
            notes = "Returns a collection of all Team profiles within the persistence mechanism.  Each Team " +
                    "profile is wrapped in a Resource class (to allow for insertion of Hypermedia links for " +
                    "each Team), which are collected under a single Resources instance (to allow for insertion " +
                    "of Hypermedia links at the root of the collection).",
            response = Team[].class)
    @ApiResponses(value = {
            @ApiResponse(code = 200,
                    message = "Successful retrieval of all teams",
                    response = Team[].class),
            @ApiResponse(code = 500,
                    message = "Internal error")
    })
    @RequestMapping(method = RequestMethod.GET, produces = "application/hal+json")
    public ResponseEntity<Resources> retrieveAllTeams() {
        log.entering(TeamController.class.getName(), RETRIEVE_ALL_TEAMS);
        EmbeddedWrappers wrappers = new EmbeddedWrappers(true);

        List<Resource> teams = teamService.findAllTeams()
                .stream()
                .map(this::wrapAsset)
                .collect(Collectors.toList());

        List<Link> selfLink = Collections.singletonList(
                linkTo(methodOn(TeamController.class).retrieveAllTeams()).withSelfRel()
        );

        Resources resources;

        if(teams.isEmpty()) {
            resources = new Resources<>(
                    Collections.singletonList(wrappers.emptyCollectionOf(TeamAsset.class)),
                    selfLink
            );
        } else {
            resources = new Resources<>(teams, selfLink);
        }

        log.exiting(TeamController.class.getName(), RETRIEVE_ALL_TEAMS, resources);
        return ResponseEntity.ok(resources);
    }

    /**
     * Accepts a TeamAsset as a parameter, deserialized from the body of a Post request, and returns a TeamAsset in
     * JSON representing that Team after having been saved to the persistence mechanism.
     *
     * Additionally, the following return codes will be supplied if the associated condition occurs:
     *  - 200: Team was properly saved to the persistence mechanism
     *  - 400: The provided Team was invalid or incomplete
     *  - 500: There was an unexpected internal server error
     *
     * @param teamAsset - A TeamAsset deserialized from the body of the request
     * @return A TeamAsset wrapped in a Resource, loaded into the body of a ResponseEntity.
     */
    @ApiOperation(value = "Saves a team to the persistence mechanism",
                  notes = "Accepts a JSON representation of a Team, saves it to the persistence mechanism, and then " +
                          "returns the updated JSON representation of that Team, including the ID of the profile.",
                  response = Team.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully saved a Team", response = Team.class),
            @ApiResponse(code = 500, message = "Internal error")
    })
    @RequestMapping(method = RequestMethod.POST, produces = "application/hal+json")
    public ResponseEntity<Resource> createTeam(
            @RequestBody
            @ApiParam(value = "A JSON representation of the Team profile to be saved to the persistence mechanism")
            TeamAsset teamAsset) {
        log.entering(TeamController.class.getName(), CREATE_TEAM, teamAsset);
        TeamAsset savedTeam = teamService.saveTeam(teamAsset);

        Resource returnMe = wrapAsset(savedTeam);

        log.exiting(TeamController.class.getName(), CREATE_TEAM, returnMe);
        return ResponseEntity.ok(returnMe);
    }

    /**
     * Accepts a numeric Team ID and Programmer ID as path parameters and - if both the Team and Programmer profile are
     * found - adds the Programmer to the Team and returns a 200 HTTP Status.  If either the Team or Programmer profile
     * are NOT found, then a 404 HTTP Status is returned instead.
     * @param teamId - A numeric representation of the ID for the desired Team, supplied as a path parameter.
     * @param programmerId - A numeric representation of the ID for the desired Programmer Profile, supplied as a path parameter.
     * @return An HTTP Status representative of success (200) or failure (404/500) of the operation.
     */
    @ApiOperation(value = "Adds a programmer to the specified Team",
                  notes = "Accepts a numeric Team ID and Programmer ID as path parameters and returns a Status " +
                          "Code representative of the success (200) or failure of the operation due to either the " +
                          "Programmer or Team not being found (404) or an unexpected internal server error (500).")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully associated programmer with team."),
            @ApiResponse(code = 404, message = "The provided team or programmer (or both!) couldn't be found"),
            @ApiResponse(code = 500, message = "Internal error")
    })
    @RequestMapping(path = "/{teamId}/add-programmer/{programmerId}", method = RequestMethod.POST, produces = "application/hal+json")
    public ResponseEntity addProgrammerToTeam(
            @PathVariable("teamId")
            @ApiParam(value = "ID of the desired Team",
                      allowableValues = "range[1, infinity]",
                      allowEmptyValue = true,
                      required = true)
            Long teamId,
            @PathVariable("programmerId")
            @ApiParam(value = "ID of the desired Programmer profile",
                      allowableValues = "range[1, infinity]",
                      allowEmptyValue = true,
                      required = true)
            Long programmerId) {
                                                                    //TODO Well, that's hideous.  Is there not a better way?
        log.entering(TeamController.class.getName(), ADD_PROGRAMMER_TO_TEAM, new Object[]{teamId, programmerId});

        if(teamService.addProgrammerToTeam(programmerId, teamId)) {

            log.exiting(TeamController.class.getName(), ADD_PROGRAMMER_TO_TEAM, HttpStatus.OK);
            return ResponseEntity.ok().build();
        } else {

            log.exiting(TeamController.class.getName(), ADD_PROGRAMMER_TO_TEAM, HttpStatus.NOT_FOUND);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Wraps a TeamAsset in a Resource wrapper and also adds the appropriate Hypermedia links to the Team and the
     * endpoint to read the List of Profiles of all Programmers within the team.
     *
     * @param asset - A TeamAsset class describing a Team.
     * @return A Resources wrapping the TeamAsset and populated with appropriate Hypermedia links.
     */
    private Resource wrapAsset(TeamAsset asset) {
        log.entering(TeamController.class.getName(), WRAP_ASSET, asset);
        List<Link> links = new ArrayList<>();

        if(asset.getTeamId() != null) {
            links.add(linkTo(methodOn(TeamController.class).retrieveTeam(asset.getTeamId()))
                            .withSelfRel());

            links.add(linkTo(methodOn(ProgrammerController.class).retrieveAllProgrammers(asset.getTeamId()))
                            .withRel("programmers")
                            .expand());
        }

        Resource returnMe = new Resource<>(asset, links);

        log.exiting(TeamController.class.getName(), WRAP_ASSET, returnMe);
        return returnMe;
    }
}
