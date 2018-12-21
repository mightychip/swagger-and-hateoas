package ca.purpleowl.examples.swagger.rest.controller;

import ca.purpleowl.examples.swagger.jpa.entity.Programmer;
import ca.purpleowl.examples.swagger.jpa.entity.Team;
import ca.purpleowl.examples.swagger.jpa.repository.ProgrammerRepository;
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ca.purpleowl.examples.swagger.rest.controller.ControllerTestUtil.loadFromFile;
import static ca.purpleowl.examples.swagger.utils.TestModelInflater.buildMockTeam;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProgrammerControllerTest {
    private static final String JSON_PATH_TEMPLATE = "expected-json/programmer-controller/%s";

    @LocalServerPort
    private Integer localServerPort;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private ProgrammerRepository mockRepository;

    @Test
    public void testReadEmptyCollection() {
        when(mockRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<String> response = testRestTemplate.getForEntity("/programmer", String.class);

        String expectedJson = loadFromFile(String.format(JSON_PATH_TEMPLATE, "empty-table.json"))
                .replaceAll("localServerPort", localServerPort.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }

    @Test
    public void testReadMultipleProgrammers() {
        Team team1 = buildMockTeam(1L, "name1", "Java", 1L, "programmer1", "programmer2", "programer3");
        Team team2 = buildMockTeam(2L, "name2", "JavaScript", 4L, "programmer4", "programmer5");

        List<Programmer> mockResult = Stream.concat(team1.getProgrammers().stream(), team2.getProgrammers().stream())
                                            .collect(Collectors.toList());

        when(mockRepository.findAll()).thenReturn(mockResult);

        ResponseEntity<String> response = testRestTemplate.getForEntity("/programmer", String.class);

        String expectedJson = loadFromFile(String.format(JSON_PATH_TEMPLATE, "all-programmers.json"))
                .replaceAll("localServerPort", localServerPort.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }

    @Test
    public void testReadProgrammersByTeam() {
        Team team = buildMockTeam(1L, "name1", "Java", 1L, "programmer1", "programmer2", "programer3");

        when(mockRepository.findAllByTeamId(eq(420L))).thenReturn(team.getProgrammers());

        Map<String, String> params = Collections.singletonMap("teamId", "420");

        ResponseEntity<String> response = testRestTemplate.getForEntity("/programmer?teamId={teamId}", String.class, params);

        String expectedJson = loadFromFile(String.format(JSON_PATH_TEMPLATE, "programmers-by-team-id.json"))
                .replaceAll("localServerPort", localServerPort.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }

    @Test
    public void testReadProgrammerById() {
        Team team = buildMockTeam(1L, "name1", "Java", 420L, "programmer1");

        when(mockRepository.findById(eq(420L))).thenReturn(Optional.of(team.getProgrammers().get(0)));

        Map<String, String> params = Collections.singletonMap("programmerId", "420");

        ResponseEntity<String> response = testRestTemplate.getForEntity("/programmer/{programmerId}", String.class, params);

        String expectedJson = loadFromFile(String.format(JSON_PATH_TEMPLATE, "programmer-by-id.json"))
                .replaceAll("localServerPort", localServerPort.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }

    @Test
    public void testProgrammerNotFoundById() {
        when(mockRepository.findById(eq(420L))).thenReturn(Optional.empty());

        Map<String, String> params = Collections.singletonMap("programmerId", "420");

        ResponseEntity<String> response = testRestTemplate.getForEntity("/programmer/{programmerId}", String.class, params);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testSaveProgrammer() {

    }
}
