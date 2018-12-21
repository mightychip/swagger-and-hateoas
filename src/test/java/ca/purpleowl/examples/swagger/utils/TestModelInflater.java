package ca.purpleowl.examples.swagger.utils;

import ca.purpleowl.examples.swagger.jpa.entity.Programmer;
import ca.purpleowl.examples.swagger.jpa.entity.Team;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class TestModelInflater {
    public static Team buildMockTeam(Long teamId, String name, String teamFocus, Long startingId, String... programmerNames) {
        Team team = new Team();
        team.setName(name);
        team.setTeamId(teamId);
        team.setTeamFocus(teamFocus);
        team.setLastStandUp(LocalDateTime.now());

        AtomicLong idCounter = new AtomicLong(startingId==null?1L:startingId);

        Arrays.stream(programmerNames)
              .forEach(programmerName -> {
                  Programmer programmer = buildMockProgrammer(programmerName, teamId==null?null:idCounter.getAndIncrement());
                  team.addProgrammer(programmer);
              });

        return team;
    }

    public static Programmer buildMockProgrammer(String name, Long programmerId) {
        Programmer programmer = new Programmer();
        programmer.setName(name);
        programmer.setProgrammerId(programmerId);
        programmer.setDateHired(LocalDate.now());

        return programmer;
    }
}
