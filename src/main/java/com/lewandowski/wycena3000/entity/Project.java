package com.lewandowski.wycena3000.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "project")
@Getter
@Setter
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Column(name = "created_at")
    private LocalDateTime created;

    @Column(name = "modified_at")
    private LocalDateTime modified;

    @Column(name = "total_cost", scale = 2, precision = 11)
    private BigDecimal totalCost;

    @Column(scale = 2, precision = 11)
    private BigDecimal price;

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private ProjectDetails projectDetails;

    @ElementCollection
    @CollectionTable(name = "project_board",
            joinColumns = @JoinColumn(name = "project_id"))
    @MapKeyJoinColumn(name = "board_id")
    @Column(name = "amount")
    private Map<WoodenBoard, Integer> boards;

    @ElementCollection
    @CollectionTable(name = "project_part",
            joinColumns = @JoinColumn(name = "project_id"))
    @MapKeyJoinColumn(name = "part_id")
    @Column(name = "amount")
    private Map<FurniturePart, Integer> furnitureParts;

    public Project() {
    }

}
