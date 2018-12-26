package ca.purpleowl.examples.swagger.jpa.repository;

import ca.purpleowl.examples.swagger.jpa.entity.Programmer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProgrammerRepository extends JpaRepository<Programmer, Long> {
    /**
     * We want to be able to select all programmers by their Team ID.  We can't really do this directly using the
     * naming magic of JpaRepository extensions, so we use the Query annotation to provide the JPQL for the specific
     * query that we want to create.  Parameters, such as "teamId" need to use the Param annotation to indicate which
     * parameters from the JPQL they are associated with.
     *
     * @param teamId - The ID of the Team from which all Programmer profiles should be returned.
     * @return A List of Programmer JPA Entities representing the Programmer profiles from the specified Team.
     */
    @Query("SELECT p FROM Programmer p WHERE p.team.id = :teamId ORDER BY p.name")
    List<Programmer> findAllByTeamId(@Param("teamId") long teamId);
}
