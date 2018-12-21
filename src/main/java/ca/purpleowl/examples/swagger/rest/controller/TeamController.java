package ca.purpleowl.examples.swagger.rest.controller;

import ca.purpleowl.examples.swagger.jpa.entity.Programmer;
import ca.purpleowl.examples.swagger.jpa.entity.Team;
import ca.purpleowl.examples.swagger.jpa.repository.ProgrammerRepository;
import ca.purpleowl.examples.swagger.jpa.repository.TeamRepository;
import ca.purpleowl.examples.swagger.rest.asset.TeamAsset;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.EmbeddedWrappers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


@Api(value = "team",
     description = "Endpoint for managing Teams")
@RestController
@RequestMapping("/team")
public class TeamController {
    private final TeamRepository teamRepository;
    private final ProgrammerRepository programmerRepository;

    @Autowired
    public TeamController(TeamRepository teamRepository, ProgrammerRepository programmerRepository) {
        this.teamRepository = teamRepository;
        this.programmerRepository = programmerRepository;
    }

    @ApiOperation(value = "Retrieves all teams from the persistence mechanism", response = Team[].class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of all teams", response = Team[].class),
            @ApiResponse(code = 500, message = "Internal error")
    })
    @RequestMapping(method = RequestMethod.GET, produces = "application/hal+json")
    public ResponseEntity<Resources> retrieveAllTeams() {
        EmbeddedWrappers wrappers = new EmbeddedWrappers(true);

        List<Resource> teams =
                teamRepository.findAll()
                              .stream()
                              .map(this::entityToResource)
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

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "Retrieves a team from the persistence mechanism", response = Team.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of a Team", response = Team.class),
            @ApiResponse(code = 404, message = "Team was not found"),
            @ApiResponse(code = 500, message = "Internal error")
    })
    @RequestMapping(path = "/{teamId}", method = RequestMethod.GET, produces = "application/hal+json")
    public ResponseEntity<Resource> retrieveTeam(@PathVariable("teamId") Long teamId) {
        Optional<Team> team = teamRepository.findById(teamId);

        if(team.isPresent()) {
            return ResponseEntity.ok(entityToResource(team.get()));
        }

        return ResponseEntity.notFound().build();
    }

    @ApiOperation(value = "Saves a team to the persistence mechanism", response = Team.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully saved a Team", response = Team.class),
            @ApiResponse(code = 500, message = "Internal error")
    })
    @RequestMapping(method = RequestMethod.POST, produces = "application/hal+json")
    public ResponseEntity<Resource> saveTeam(@RequestBody TeamAsset asset) {
        Team savedTeam = teamRepository.save(assetToEntity(asset));

        Resource returnMe = entityToResource(savedTeam);

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
        Optional<Team> maybeTeam = teamRepository.findById(teamId);
        Optional<Programmer> maybeProgrammer = programmerRepository.findById(programmerId);

        if(maybeTeam.isPresent() && maybeProgrammer.isPresent()) {
            Team team = maybeTeam.get();
            Programmer programmer = maybeProgrammer.get();
            team.addProgrammer(programmer);
            teamRepository.save(team);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.notFound().build();
    }

    private Resource entityToResource(Team team) {
        TeamAsset asset = entityToAsset(team);
        List<Link> links = new ArrayList<>();
        if(team.getTeamId() != null) {
            links.add(
                    linkTo(methodOn(ProgrammerController.class).retrieveAllProgrammers(team.getTeamId()))
                            .withRel("programmers")
                            .expand()
            );
        }
        return new Resource<>(asset, links);
    }

    private TeamAsset entityToAsset(Team team) {
        TeamAsset asset = new TeamAsset();
        asset.setTeamId(team.getTeamId());
        asset.setName(team.getName());
        asset.setTeamFocus(team.getTeamFocus());
        asset.setLastStandUp(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(team.getLastStandUp()));

        return asset;
    }

    private Team assetToEntity(TeamAsset asset) {
        Team entity = new Team();
        entity.setTeamId(asset.getTeamId());
        entity.setName(asset.getName());
        entity.setTeamFocus(asset.getTeamFocus());
        entity.setLastStandUp(LocalDateTime.parse(asset.getLastStandUp()));

        return entity;
    }
}
