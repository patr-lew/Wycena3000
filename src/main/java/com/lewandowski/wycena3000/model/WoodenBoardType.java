package com.lewandowski.wycena3000.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "board_type")
@Getter
@Setter
@NoArgsConstructor
public class WoodenBoardType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Column(name = "price_per_m2", scale = 11, precision = 2)
    private BigDecimal pricePerM2;

}
