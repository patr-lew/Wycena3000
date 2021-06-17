package com.lewandowski.wycena3000.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Builder
@Entity
@Table(name = "measurement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {

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
        Measurement that = (Measurement) o;
        return id == that.id && height == that.height && width == that.width && Objects.equals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, board, height, width);
    }

}

