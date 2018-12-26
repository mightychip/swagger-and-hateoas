package ca.purpleowl.examples.swagger.service;

import ca.purpleowl.examples.swagger.jpa.entity.Programmer;
import ca.purpleowl.examples.swagger.jpa.entity.Team;
import ca.purpleowl.examples.swagger.jpa.repository.ProgrammerRepository;
import ca.purpleowl.examples.swagger.jpa.repository.TeamRepository;
import ca.purpleowl.examples.swagger.rest.asset.TeamAsset;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class exists purely to decouple the JPA model from the REST Controllers.  They should be unaware of the
 * implementation for the persistence layer.  Swagger2 also introduces some significant bloat from Annotations, so it's
 * good to move logic from that controller back to a Service class for clarity if nothing else.
 */
@Log
@Service
public class TeamService {
    private static final String FIND_TEAM = "findTeam";
    private static final String FIND_ALL_TEAMS = "findAllTeams";
    private static final String SAVE_TEAM = "createTeam";
    private static final String ADD_PROGRAMMER_TO_TEAM = "addProgrammerToTeam";
    private static final String ENTITY_TO_ASSET = "entityToAsset";
    private static final String ASSET_TO_ENTITY = "assetToEntity";

    private final TeamRepository teamRepository;
    private final ProgrammerRepository programmerRepository;

    @Autowired
    public TeamService(TeamRepository teamRepository, ProgrammerRepository programmerRepository) {
        this.teamRepository = teamRepository;
        this.programmerRepository = programmerRepository;
    }

    public TeamAsset findTeam(long teamId) {
        log.entering(TeamService.class.getName(), FIND_TEAM, teamId);
        TeamAsset returnMe = null;

        Optional<Team> team = teamRepository.findById(teamId);

        if(team.isPresent()) {
            returnMe = entityToAsset(team.get());
        }

        log.exiting(TeamService.class.getName(), FIND_TEAM, returnMe);
        return returnMe;
    }

    public List<TeamAsset> findAllTeams() {
        log.entering(TeamService.class.getName(), FIND_ALL_TEAMS);

        List<TeamAsset> returnMe = teamRepository.findAll()
                                                 .stream()
                                                 .map(TeamService::entityToAsset)
                                                 .collect(Collectors.toList());

        log.exiting(TeamService.class.getName(), FIND_ALL_TEAMS, returnMe);
        return returnMe;
    }

    public TeamAsset saveTeam(TeamAsset asset) {
        log.entering(TeamService.class.getName(), SAVE_TEAM, asset);
        Team team = assetToEntity(asset);

        team = teamRepository.save(team);

        TeamAsset returnMe = entityToAsset(team);

        log.exiting(TeamService.class.getName(), SAVE_TEAM, returnMe);
        return returnMe;
    }

    public boolean addProgrammerToTeam(long programmerId, long teamId) {
        log.entering(TeamService.class.getName(), ADD_PROGRAMMER_TO_TEAM, new Object[]{programmerId, teamId});
        Optional<Team> maybeTeam = teamRepository.findById(teamId);
        Optional<Programmer> maybeProgrammer = programmerRepository.findById(programmerId);

        boolean returnMe = false;

        if(maybeTeam.isPresent() && maybeProgrammer.isPresent()) {
            Team team = maybeTeam.get();
            Programmer programmer = maybeProgrammer.get();
            team.addProgrammer(programmer);
            teamRepository.save(team);
            returnMe = true;
        }

        log.exiting(TeamService.class.getName(), ADD_PROGRAMMER_TO_TEAM, returnMe);
        //I'm just going to assume that this is always an indicator of whether or not we were successful.
        return returnMe;
    }


    private static TeamAsset entityToAsset(Team team) {
        log.entering(TeamService.class.getName(), ENTITY_TO_ASSET, team);
        TeamAsset asset = new TeamAsset();
        asset.setTeamId(team.getId());
        asset.setName(team.getName());
        asset.setTeamFocus(team.getTeamFocus());
        asset.setLastStandUp(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(team.getLastStandUp()));

        log.exiting(TeamService.class.getName(), ENTITY_TO_ASSET, asset);
        return asset;
    }

    private static Team assetToEntity(TeamAsset asset) {
        log.entering(TeamService.class.getName(), ASSET_TO_ENTITY, asset);
        Team entity = new Team();
        entity.setId(asset.getTeamId());
        entity.setName(asset.getName());
        entity.setTeamFocus(asset.getTeamFocus());
        entity.setLastStandUp(LocalDateTime.parse(asset.getLastStandUp()));

        log.exiting(TeamService.class.getName(), ASSET_TO_ENTITY, entity);
        return entity;
    }
}
