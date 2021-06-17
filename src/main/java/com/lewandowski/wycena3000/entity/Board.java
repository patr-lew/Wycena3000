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
@Table(name = "board")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    private String name;

    private String code;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(name = "price_per_m2", scale = 2, precision = 11)
    private BigDecimal pricePerM2;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private BoardType boardType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return Objects.equals(name, board.name) && Objects.equals(user, board.user) && Objects.equals(pricePerM2, board.pricePerM2) && Objects.equals(boardType, board.boardType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, user, pricePerM2, boardType);
    }
}
