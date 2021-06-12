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
import java.util.Objects;

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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_cost", scale = 2, precision = 11)
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(scale = 2, precision = 11)
    private BigDecimal price = BigDecimal.ZERO;

    @OneToOne(mappedBy = "project")
    private ProjectDetails projectDetails;

    @Column(scale = 5, precision = 10)
    private BigDecimal margin;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "project_measurement",
            joinColumns = @JoinColumn(name = "project_id"))
    @MapKeyJoinColumn(name = "measurement_id")
    @Column(name = "amount")
    private Map<Measurement, Integer> measurements;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(name, project.name) && Objects.equals(user, project.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, user);
    }
}
