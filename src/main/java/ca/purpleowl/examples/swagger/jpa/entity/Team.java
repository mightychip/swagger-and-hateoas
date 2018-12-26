package ca.purpleowl.examples.swagger.jpa.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Table
@Entity
@EqualsAndHashCode(callSuper = true)
public class Team extends AbstractEntity {
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
