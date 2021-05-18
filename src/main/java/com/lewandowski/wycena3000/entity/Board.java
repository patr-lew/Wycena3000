package com.lewandowski.wycena3000.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "board")
@Getter
@Setter
@NoArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String code;

    @Column(name = "price_per_m2", scale = 2, precision = 11)
    private BigDecimal pricePerM2;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private BoardType boardType;

}
