package com.lewandowski.wycena3000.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "project")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    private String name;

    @Column(name = "created_at")
    private LocalDateTime created;

    @Column(name = "modified_at")
    private LocalDateTime modified;

    @Column(name = "total_cost", scale = 2, precision = 11)
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(scale = 2, precision = 11)
    private BigDecimal price = BigDecimal.ZERO;

    @OneToOne(mappedBy = "project")
    private ProjectDetails projectDetails;

    @Column(scale = 5, precision = 10)
    private BigDecimal margin;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "project_board",
            joinColumns = @JoinColumn(name = "project_id"))
    @MapKeyJoinColumn(name = "board_id")
    @Column(name = "amount")
    private Map<BoardMeasurement, Integer> boardMeasurements;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "project_part",
            joinColumns = @JoinColumn(name = "project_id"))
    @MapKeyJoinColumn(name = "part_id")
    @Column(name = "amount")
    private Map<Part, Integer> parts;

    private String comment;


    @PrePersist
    public void created() {
        this.created = LocalDateTime.now();
    }

    @PreUpdate
    public void updated() {
        this.modified = LocalDateTime.now();
    }
}
