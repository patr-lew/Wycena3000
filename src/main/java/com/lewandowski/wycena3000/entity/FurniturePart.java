package com.lewandowski.wycena3000.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "part")
@Getter
@Setter
@AllArgsConstructor
public class FurniturePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private FurniturePartType furniturePartType;

    @Column(scale = 2, precision = 11)
    private BigDecimal price;

    public FurniturePart() {
    }
}
