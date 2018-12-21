package ca.purpleowl.examples.swagger.rest.controller;

import ca.purpleowl.examples.swagger.jpa.entity.Programmer;
import ca.purpleowl.examples.swagger.jpa.repository.ProgrammerRepository;
import ca.purpleowl.examples.swagger.rest.asset.ProgrammerAsset;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Log
@Api(value = "programmer",
     description = "Endpoint for managing Programmers")
@RestController
@RequestMapping(value = "/programmer")
public class ProgrammerController {
    private final ProgrammerRepository programmerRepository;

    @Autowired
    public ProgrammerController(ProgrammerRepository programmerRepository) {
        this.programmerRepository = programmerRepository;
    }

    @ApiOperation(value = "Retrieves a programmer's profile from the persistence mechanism", response = ProgrammerAsset.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of Programmer Profile", response = ProgrammerAsset.class),
            @ApiResponse(code = 400, message = "The Programmer ID was invalid or not supplied"),
            @ApiResponse(code = 404, message = "Programmer profile was not found"),
            @ApiResponse(code = 500, message = "Internal error")
    })
    @RequestMapping(value = "/{programmerId}", method = RequestMethod.GET, produces = "application/hal+json")
    public ResponseEntity<Resource> retrieveProgrammer(@PathVariable("programmerId") Long programmerId) {
        Optional<Programmer> programmer = programmerRepository.findById(programmerId);

        if(programmer.isPresent()) {
            ProgrammerAsset asset = entityToAsset(programmer.get());
            return ResponseEntity.ok(new Resource<>(asset));
        }

        return ResponseEntity.notFound().build();
    }

    @ApiOperation(value = "Retrieves all programmers from the persistence mechanism", response = ProgrammerAsset[].class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of all programmer profiles", response = ProgrammerAsset[].class),
            @ApiResponse(code = 500, message = "Internal error")
    })
    @RequestMapping(method = RequestMethod.GET, produces = "application/hal+json")
    public ResponseEntity<Resources> retrieveAllProgrammers(@RequestParam(name = "teamId", required = false) Long teamId) {
        log.info("about to try to list programmers");
        log.info("teamId = " + teamId);
        EmbeddedWrappers wrappers = new EmbeddedWrappers(true);

        List<Resource> programmers;
        if(teamId == null) {
            programmers = programmerRepository.findAll()
                                              .stream()
                                              .map(this::entityToResource)
                                              .collect(Collectors.toList());
        } else {
            programmers = programmerRepository.findAllByTeamId(teamId)
                                              .stream()
                                              .map(this::entityToResource)
                                              .collect(Collectors.toList());
        }

        List<Link> selfLink = Collections.singletonList(
                linkTo(methodOn(ProgrammerController.class).retrieveAllProgrammers(teamId))
                        .withSelfRel()
                        //We call this to remove reference to template variables.
                        .expand()
        );

        Resources resources;

        if(programmers.isEmpty()) {
            log.info("returning empty list");
            //We have to do that pain in the ass thing so that we still return the list even though it's empty.
            resources =
                    new Resources<>(Collections.singletonList(wrappers.emptyCollectionOf(ProgrammerAsset.class)),
                                    selfLink);
        } else {
            log.info("returning list with contents!");
            resources = new Resources<>(programmers, selfLink);
        }

        return ResponseEntity.ok(resources);
    }

    @ApiOperation(value = "Saves a programmer's profile to the persistence mechanism.", response = ProgrammerAsset.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully saved the Programmer's profile", response = ProgrammerAsset.class),
            @ApiResponse(code = 400, message = "The provided profile was invalid or incomplete."),
            @ApiResponse(code = 500, message = "Internal Error")
    })
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/hal+json")
    public ResponseEntity<Resource> createProgrammer(@RequestBody ProgrammerAsset programmerAsset) {

        Programmer savedProgrammer = programmerRepository.save(assetToEntity(programmerAsset));

        Resource returnMe = entityToResource(savedProgrammer);

        return ResponseEntity.ok(returnMe);
    }

    private Resource entityToResource(Programmer programmer) {
        List<Link> links = new ArrayList<>();

        ProgrammerAsset asset = entityToAsset(programmer);

        if(programmer.getProgrammerId() != null) {
            links.add(
                    linkTo(methodOn(ProgrammerController.class).retrieveProgrammer(programmer.getProgrammerId()))
                            .withSelfRel()
                            .expand()
            );
        }

        if(programmer.getTeam() != null) {
            links.add(
                    linkTo(methodOn(TeamController.class).retrieveTeam(programmer.getTeam().getTeamId()))
                            .withRel("team")
                            .expand()
            );
        }

        return new Resource<>(asset, links);
    }

    private ProgrammerAsset entityToAsset(Programmer programmer) {
        ProgrammerAsset asset = new ProgrammerAsset();
        asset.setName(programmer.getName());
        asset.setProgrammerId(programmer.getProgrammerId());
        asset.setDateHired(DateTimeFormatter.ISO_LOCAL_DATE.format(programmer.getDateHired()));

        if(programmer.getTeam() != null) {
            asset.setTeamId(programmer.getTeam().getTeamId());
            asset.setTeamName(programmer.getTeam().getName());
        }

        return asset;
    }

    private Programmer assetToEntity(ProgrammerAsset asset) {
        Programmer entity = new Programmer();
        entity.setName(asset.getName());
        entity.setDateHired(LocalDate.parse(asset.getDateHired()));

        return entity;
    }
}
