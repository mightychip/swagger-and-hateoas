package ca.purpleowl.examples.swagger.jpa.repository;

import ca.purpleowl.examples.swagger.jpa.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
