package ca.purpleowl.examples.swagger.rest.controller;

import ca.purpleowl.examples.swagger.jpa.entity.Programmer;
import ca.purpleowl.examples.swagger.jpa.entity.Team;
import ca.purpleowl.examples.swagger.jpa.repository.ProgrammerRepository;
import ca.purpleowl.examples.swagger.jpa.repository.TeamRepository;
import ca.purpleowl.examples.swagger.rest.asset.TeamAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static ca.purpleowl.examples.swagger.rest.controller.ControllerTestUtil.loadFromFile;
import static ca.purpleowl.examples.swagger.utils.TestModelInflater.buildMockProgrammer;
import static ca.purpleowl.examples.swagger.utils.TestModelInflater.buildMockTeam;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TeamControllerTest  {
    private static final String JSON_PATH_TEMPLATE = "expected-json/team-controller/%s";

    @LocalServerPort
    private Integer localServerPort;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private ProgrammerRepository mockProgrammerRepo;

    @MockBean
    private TeamRepository mockTeamRepo;

    @Test
    public void testReadEmptyCollection() {
        when(mockTeamRepo.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<String> response = testRestTemplate.getForEntity("/team", String.class);

        String expectedJson = loadFromFile(String.format(JSON_PATH_TEMPLATE, "empty-table.json"))
                .replaceAll("localServerPort", localServerPort.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }

    @Test
    public void testReadAllTeams() {
        Team team1 = buildMockTeam(1L, "name1", "Java", 1L, "programmer1", "programmer2", "programmer3");
        Team team2 = buildMockTeam(2L, "name2", "JavaScript", 4L, "programmer4", "programmer5");

        List<Team> mockResults = Arrays.asList(team1, team2);

        when(mockTeamRepo.findAll()).thenReturn(mockResults);

        ResponseEntity<String> response = testRestTemplate.getForEntity("/team", String.class);

        String expectedJson = loadFromFile(String.format(JSON_PATH_TEMPLATE, "all-teams.json"))
                .replaceAll("localServerPort", localServerPort.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }

    @Test
    public void testRetrieveTeamById() {
        Optional<Team> mockResult = Optional.of(buildMockTeam(420L, "name1", "Java", 1L, "programmer1"));

        when(mockTeamRepo.findById(eq(420L))).thenReturn(mockResult);

        Map<String, String> params = Collections.singletonMap("teamId", "420");

        ResponseEntity<String> response = testRestTemplate.getForEntity("/team/{teamId}", String.class, params);

        String expectedJson = loadFromFile(String.format(JSON_PATH_TEMPLATE, "team-by-id.json"))
                .replaceAll("localServerPort", localServerPort.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }

    @Test
    public void testTeamNotFound() {
        Optional<Team> mockResult = Optional.empty();

        when(mockTeamRepo.findById(eq(420L))).thenReturn(mockResult);

        Map<String, String> params = Collections.singletonMap("teamId", "420");

        ResponseEntity<String> response = testRestTemplate.getForEntity("/team/{teamId}", String.class, params);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testAddProgrammerToTeam() {
        Optional<Team> mockTeamResult = Optional.of(buildMockTeam(420L, "teamId", "Java", null));
        Optional<Programmer> mockProgrammerResult = Optional.of(buildMockProgrammer("name", 420L));

        when(mockTeamRepo.findById(eq(420L))).thenReturn(mockTeamResult);
        when(mockProgrammerRepo.findById(eq(420L))).thenReturn(mockProgrammerResult);

        Map<String, String> params = new HashMap<>();
        params.put("teamId", "420");
        params.put("programmerId", "420");

        ResponseEntity<String> response = testRestTemplate.postForEntity("/team/{teamId}/add-programmer/{programmerId}", null, String.class, params);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testAddProgrammerToTeamThatDoesNotExist() {
        Optional<Team> mockTeamResult = Optional.empty();
        Optional<Programmer> mockProgrammerResult = Optional.of(buildMockProgrammer("name", 420L));

        when(mockTeamRepo.findById(eq(420L))).thenReturn(mockTeamResult);
        when(mockProgrammerRepo.findById(eq(420L))).thenReturn(mockProgrammerResult);

        Map<String, String> params = new HashMap<>();
        params.put("teamId", "420");
        params.put("programmerId", "420");

        ResponseEntity<String> response = testRestTemplate.postForEntity("/team/{teamId}/add-programmer/{programmerId}", null, String.class, params);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testSaveTeam() {
        TeamAsset teamAsset = new TeamAsset();
        teamAsset.setLastStandUp(LocalDate.of(2001,1,1).atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        teamAsset.setTeamFocus("Java");
        teamAsset.setName("name");

        Team entity = new Team();
        entity.setLastStandUp(LocalDate.of(2001,1,1).atStartOfDay());
        entity.setTeamFocus("Java");
        entity.setName("name");
        entity.setTeamId(1L);

        when(mockTeamRepo.save(any(Team.class))).thenReturn(entity);

        ResponseEntity<String> response = testRestTemplate.postForEntity("/team", teamAsset, String.class);

        String expectedJson = loadFromFile(String.format(JSON_PATH_TEMPLATE, "saved-team.json"))
                .replaceAll("localServerPort", localServerPort.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }
}
