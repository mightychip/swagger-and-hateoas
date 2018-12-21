package ca.purpleowl.examples.swagger.jpa.repository;

import ca.purpleowl.examples.swagger.jpa.entity.Programmer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProgrammerRepository extends JpaRepository<Programmer, Long> {
    @Query("SELECT p FROM Programmer p WHERE p.team.teamId = :teamId ORDER BY p.name")
    List<Programmer> findAllByTeamId(@Param("teamId") long teamId);
}
