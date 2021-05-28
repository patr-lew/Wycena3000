package com.lewandowski.wycena3000.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "board_measurement")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @NotNull
    @Min(value = 1)
    private int height;

    @NotNull
    @Min(value = 1)
    private int width;

    @Transient
    private int amount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardMeasurement that = (BoardMeasurement) o;
        return id == that.id && height == that.height && width == that.width && Objects.equals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, board, height, width);
    }

}
