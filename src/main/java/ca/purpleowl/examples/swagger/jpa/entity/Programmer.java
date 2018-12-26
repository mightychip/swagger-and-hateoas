package ca.purpleowl.examples.swagger.jpa.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@Table
@Entity
@EqualsAndHashCode(callSuper = true) //Some additional instructions are needed here.
public class Programmer extends AbstractEntity {
    @Column
    private String name;

    @Column
    private LocalDate dateHired;

    @ManyToOne
    @JoinColumn(name = "teamId")
    private Team team;

    public Programmer() {}
}
