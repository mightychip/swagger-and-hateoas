package ca.purpleowl.examples.swagger.rest.controller;

import ca.purpleowl.examples.swagger.jpa.entity.Team;
import ca.purpleowl.examples.swagger.jpa.repository.TeamRepository;
import ca.purpleowl.examples.swagger.rest.asset.TeamAsset;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.EmbeddedWrappers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    public TeamController(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

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
                    //Ehhhh... close enough.
                    Collections.singletonList(wrappers.emptyCollectionOf(TeamAsset.class)),
                    selfLink
            );
        } else {
            resources = new Resources<>(teams, selfLink);
        }

        return ResponseEntity.ok(resources);
    }

    @RequestMapping(path = "/{teamId}", method = RequestMethod.GET, produces = "application/hal+json")
    public ResponseEntity<Resource> retrieveTeam(@PathVariable("teamId") Long teamId) {
        Optional<Team> team = teamRepository.findById(teamId);

        if(team.isPresent()) {
            return ResponseEntity.ok(new Resource<>(entityToAsset(team.get())));
        }

        return ResponseEntity.notFound().build();
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/hal+json")
    public ResponseEntity<Resource> saveTeam(@RequestBody TeamAsset team) {
        return null;
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
}
