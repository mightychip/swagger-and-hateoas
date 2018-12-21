package ca.purpleowl.examples.swagger.jpa.entity;

import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@Data
public class Team {
    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private Long teamId;

    @Column
    private String name;

    @Column
    private String teamFocus;

    @Column
    private LocalDateTime lastStandUp;

    //We exclude this from the generated toString because it will just cause us trouble.
    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL)
    private List<Programmer> programmers = new ArrayList<>();

    public Team() {}

    public void addProgrammer(Programmer programmer) {
        programmer.setTeam(this);
        this.programmers.add(programmer);
    }
}
