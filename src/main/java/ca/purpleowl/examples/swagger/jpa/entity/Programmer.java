package ca.purpleowl.examples.swagger.jpa.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table
@Data
public class Programmer {
    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private Long programmerId;

    @Column
    private String name;

    @Column
    private LocalDate dateHired;

    @ManyToOne
    @JoinColumn(name = "teamId")
    private Team team;

    public Programmer() {}
}
