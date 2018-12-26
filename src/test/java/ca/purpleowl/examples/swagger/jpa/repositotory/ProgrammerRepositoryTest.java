package ca.purpleowl.examples.swagger.jpa.repositotory;

import ca.purpleowl.examples.swagger.jpa.entity.Programmer;
import ca.purpleowl.examples.swagger.jpa.entity.Team;
import ca.purpleowl.examples.swagger.jpa.repository.ProgrammerRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static ca.purpleowl.examples.swagger.utils.TestModelInflater.buildMockTeam;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ProgrammerRepositoryTest {
    @Autowired
    private ProgrammerRepository fixture;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * This is the only method that we've actually customized on the repositories... there's no sense testing methods
     * which are part of the framework itself.
     *
     * Here, we'll write some of the stuff we're using into the DB using the EntityManager directly... then we'll try
     * to retrieve it using the Repository.
     */
    @Test
    public void testFindAllProgrammersByTeamId() {
        Team team = buildMockTeam(null, "Team", "Java", null, "programmer1", "programmer2", "programmer3");

        entityManager.persist(team);

        Team otherTeam = buildMockTeam(null, "Team", "Java", null, "programmer4");

        entityManager.persist(otherTeam);
        entityManager.flush();

        List<Programmer> result = fixture.findAllByTeamId(team.getId());

        assertEquals(team.getProgrammers().size(), result.size());

        //We can't really directly compare the lists, but we can ensure that the result contains all of the Programmers
        //we're expecting it to contain.
        result.forEach(
                programmer -> assertTrue(team.getProgrammers().contains(programmer))
        );
    }
}
