package ca.purpleowl.examples.swagger.rest.controller;

import ca.purpleowl.examples.swagger.jpa.entity.Team;
import ca.purpleowl.examples.swagger.rest.asset.TeamAsset;
import ca.purpleowl.examples.swagger.service.TeamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.EmbeddedWrappers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Log
@Api(value = "team",
     description = "Endpoint for managing Teams")
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

    @ApiOperation(value = "Retrieves all teams from the persistence mechanism", response = Team[].class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of all teams", response = Team[].class),
            @ApiResponse(code = 500, message = "Internal error")
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

    @ApiOperation(value = "Retrieves a team from the persistence mechanism", response = Team.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of a Team", response = Team.class),
            @ApiResponse(code = 404, message = "Team was not found"),
            @ApiResponse(code = 500, message = "Internal error")
    })
    @RequestMapping(path = "/{teamId}", method = RequestMethod.GET, produces = "application/hal+json")
    public ResponseEntity<Resource> retrieveTeam(@PathVariable("teamId") long teamId) {
        log.entering(TeamController.class.getName(), RETRIEVE_TEAM, teamId);
        TeamAsset team = teamService.findTeam(teamId);

        if(team != null) {
            Resource returnMe = wrapAsset(team);

            log.exiting(TeamController.class.getName(), RETRIEVE_TEAM, returnMe);
            return ResponseEntity.ok(returnMe);
        }

        log.exiting(TeamController.class.getName(), RETRIEVE_TEAM, 404);
        return ResponseEntity.notFound().build();
    }

    @ApiOperation(value = "Saves a team to the persistence mechanism", response = Team.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully saved a Team", response = Team.class),
            @ApiResponse(code = 500, message = "Internal error")
    })
    @RequestMapping(method = RequestMethod.POST, produces = "application/hal+json")
    public ResponseEntity<Resource> createTeam(@RequestBody TeamAsset teamAsset) {
        log.entering(TeamController.class.getName(), CREATE_TEAM, teamAsset);
        TeamAsset savedTeam = teamService.saveTeam(teamAsset);

        Resource returnMe = wrapAsset(savedTeam);

        log.exiting(TeamController.class.getName(), CREATE_TEAM, returnMe);
        return ResponseEntity.ok(returnMe);
    }

    @ApiOperation(value = "Adds a programmer to the specified team using the provided team ID and programmer ID")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully associated programmer with team."),
            @ApiResponse(code = 404, message = "The provided team or programmer (or both!) couldn't be found"),
            @ApiResponse(code = 500, message = "Internal error")
    })
    @RequestMapping(path = "/{teamId}/add-programmer/{programmerId}", method = RequestMethod.POST, produces = "application/hal+json")
    public ResponseEntity addProgrammerToTeam(@PathVariable("teamId") long teamId,
                                              @PathVariable("programmerId") long programmerId) {
                                                                    //TODO Well, that's hideous.  Is there not a better way?
        log.entering(TeamController.class.getName(), ADD_PROGRAMMER_TO_TEAM, new Object[]{teamId, programmerId});

        if(teamService.addProgrammerToTeam(programmerId, teamId)) {

            log.exiting(TeamController.class.getName(), ADD_PROGRAMMER_TO_TEAM, 200);
            return ResponseEntity.ok().build();
        } else {

            log.exiting(TeamController.class.getName(), ADD_PROGRAMMER_TO_TEAM, 404);
            return ResponseEntity.notFound().build();
        }
    }

    private Resource wrapAsset(TeamAsset asset) {
        log.entering(TeamController.class.getName(), WRAP_ASSET, asset);
        List<Link> links = new ArrayList<>();

        if(asset.getTeamId() != null) {
            links.add(
                    linkTo(methodOn(TeamController.class).retrieveTeam(asset.getTeamId()))
                            .withSelfRel()
            );

            links.add(
                    linkTo(methodOn(ProgrammerController.class).retrieveAllProgrammers(asset.getTeamId()))
                            .withRel("programmers")
                            .expand()
            );
        }

        Resource returnMe = new Resource<>(asset, links);

        log.exiting(TeamController.class.getName(), WRAP_ASSET, returnMe);
        return returnMe;
    }
}
