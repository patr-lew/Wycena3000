package com.lewandowski.wycena3000.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

@Builder
@Entity
@Table(name = "part")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    private String name;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private PartType partType;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(scale = 2, precision = 11)
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Transient
    private int amount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Part part = (Part) o;
        return Objects.equals(name, part.name) && Objects.equals(partType, part.partType) && Objects.equals(price, part.price) && Objects.equals(user, part.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, partType, price, user);
    }
}
