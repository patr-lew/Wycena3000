package com.lewandowski.wycena3000.entity;

import com.lewandowski.wycena3000.exception.NegativeAmountException;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Builder
@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    public Project addMeasurement(Measurement addedMeasurement) {
        Optional<Measurement> doubledMeasurement = checkForDuplicatedMeasurements(addedMeasurement);

        int newAmount = updateMeasurementsAmount(doubledMeasurement, addedMeasurement);

        deleteDoubledMeasurement(doubledMeasurement);

        if (newAmount == 0) {
            return this;
        }

        measurements.put(addedMeasurement, newAmount);
        return this;
    }

    private void deleteDoubledMeasurement(Optional<Measurement> doubledMeasurement) {
        if (doubledMeasurement.isPresent()) {
            measurements.remove(doubledMeasurement.get());
        }
    }

    private int updateMeasurementsAmount(Optional<Measurement> optionalDoubledMeasurement, Measurement addedMeasurement) {
        int newAmount = addedMeasurement.getAmount();

        if (optionalDoubledMeasurement.isPresent()) {
            Measurement doubleMeasurement = optionalDoubledMeasurement.get();
            newAmount += measurements.get(doubleMeasurement);
        }

        validateAmount(newAmount, addedMeasurement);
        return newAmount;
    }

    private void validateAmount(int newAmount, Measurement addedMeasurement) {
        if (newAmount < 0) {
            throw new NegativeAmountException
                    (String.format("The amount of measurements cannot be negative. Amount of measurement of %s: %d ",
                            addedMeasurement.getBoard().getName(), newAmount));
        }
    }

    private Optional<Measurement> checkForDuplicatedMeasurements(Measurement addedMeasurement) {
        for (Measurement measurement : measurements.keySet()) {
            if (measurement.getBoard().equals(addedMeasurement.getBoard()) &&
                    measurement.getHeight() == addedMeasurement.getHeight() &&
                    measurement.getWidth() == addedMeasurement.getWidth()) {

                return Optional.of(measurement);
            }
        }
        return Optional.empty();
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
