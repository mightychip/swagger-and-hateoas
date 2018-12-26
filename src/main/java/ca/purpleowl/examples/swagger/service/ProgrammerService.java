package ca.purpleowl.examples.swagger.service;

import ca.purpleowl.examples.swagger.jpa.entity.Programmer;
import ca.purpleowl.examples.swagger.jpa.repository.ProgrammerRepository;
import ca.purpleowl.examples.swagger.rest.asset.ProgrammerAsset;
import ca.purpleowl.examples.swagger.rest.controller.ProgrammerController;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The purpose of this is just to loosen the coupling between the JPA Model and the REST Endpoints.  I always like to
 * keep the two separate.  This also helps us pull a bunch of logic away from the REST Controller.  It should only
 * really be making decisions on what the Status Code is, and adding hypermedia links to the returned data, obviously.
 */
@Log
@Service
public class ProgrammerService {
    private static final String FIND_PROGRAMMER = "findProgrammer";
    private static final String FIND_ALL_PROGRAMMERS = "findAllProgrammers";
    private static final String FIND_ALL_PROGRAMMERS_ON_TEAM = "findAllProgrammersOnTeam";
    private static final String SAVE_PROGRAMMER = "saveProgrammer";

    private static final String ENTITY_TO_ASSET = "entityToAsset";
    private static final String ASSET_TO_ENTITY = "assetToEntity";

    private final ProgrammerRepository programmerRepository;

    @Autowired
    public ProgrammerService(ProgrammerRepository programmerRepository) {
        this.programmerRepository = programmerRepository;
    }

    public ProgrammerAsset findProgrammer(long programmerId) {
        log.entering(ProgrammerService.class.getName(), FIND_PROGRAMMER, programmerId);
        ProgrammerAsset returnMe = null;

        Optional<Programmer> programmer = programmerRepository.findById(programmerId);

        if(programmer.isPresent()) {
            returnMe = entityToAsset(programmer.get());
        }

        log.exiting(ProgrammerService.class.getName(), FIND_PROGRAMMER, returnMe);
        return returnMe;
    }

    public List<ProgrammerAsset> findAllProgrammers() {
        log.entering(ProgrammerService.class.getName(), FIND_ALL_PROGRAMMERS);

        List<ProgrammerAsset> returnMe =
                programmerRepository.findAll()
                                    .stream()
                                    .map(ProgrammerService::entityToAsset)
                                    .collect(Collectors.toList());

        log.exiting(ProgrammerService.class.getName(), FIND_ALL_PROGRAMMERS, returnMe);
        return returnMe;
    }

    public List<ProgrammerAsset> findAllProgrammersOnTeam(long teamId) {
        log.entering(ProgrammerService.class.getName(), FIND_ALL_PROGRAMMERS_ON_TEAM, teamId);

        List<ProgrammerAsset> returnMe =
                programmerRepository.findAllByTeamId(teamId)
                                    .stream()
                                    .map(ProgrammerService::entityToAsset)
                                    .collect(Collectors.toList());

        log.exiting(ProgrammerService.class.getName(), FIND_ALL_PROGRAMMERS_ON_TEAM, returnMe);
        return returnMe;
    }

    public ProgrammerAsset saveProgrammer(ProgrammerAsset saveMe) {
        log.entering(ProgrammerService.class.getName(), SAVE_PROGRAMMER, saveMe);
        Programmer programmer = assetToEntity(saveMe);

        programmer = programmerRepository.save(programmer);

        ProgrammerAsset returnMe = entityToAsset(programmer);

        log.exiting(ProgrammerService.class.getName(), SAVE_PROGRAMMER, returnMe);
        return returnMe;
    }

    /**
     * Converts a JPA Entity to an Asset class.
     *
     * @param programmer - A JPA Entity describing a Programmer Profile.
     * @return A JSON Asset class representing the Programmer JPA Entity.
     */
    private static ProgrammerAsset entityToAsset(Programmer programmer) {
        log.entering(ProgrammerController.class.getName(), ENTITY_TO_ASSET, programmer);

        ProgrammerAsset asset = new ProgrammerAsset();
        asset.setName(programmer.getName());
        asset.setProgrammerId(programmer.getId());
        asset.setDateHired(DateTimeFormatter.ISO_LOCAL_DATE.format(programmer.getDateHired()));

        if(programmer.getTeam() != null) {
            asset.setTeamId(programmer.getTeam().getId());
            asset.setTeamName(programmer.getTeam().getName());
        }

        log.exiting(ProgrammerController.class.getName(), ENTITY_TO_ASSET, asset);
        return asset;
    }

    /**
     * Converts a JSON Asset class to its associated JPA Entity.
     *
     * @param asset - A ProgrammerAsset representing the desired JPA Entity.
     * @return A JPA Entity containing all relevant data from the Asset.
     */
    private static Programmer assetToEntity(ProgrammerAsset asset) {
        log.entering(ProgrammerController.class.getName(), ASSET_TO_ENTITY, asset);

        Programmer entity = new Programmer();
        entity.setName(asset.getName());
        entity.setDateHired(LocalDate.parse(asset.getDateHired()));

        log.exiting(ProgrammerController.class.getName(), ASSET_TO_ENTITY, entity);
        return entity;
    }
}
