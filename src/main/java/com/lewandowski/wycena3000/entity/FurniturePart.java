package com.lewandowski.wycena3000.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "part")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FurniturePart {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @NotBlank
    private String name;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private FurniturePartType furniturePartType;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(scale = 2, precision = 11)
    private BigDecimal price;

    @Transient
    private int amount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FurniturePart that = (FurniturePart) o;
        return id == that.id && Objects.equals(name, that.name) && furniturePartType.equals(that.furniturePartType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, furniturePartType);
    }
}
