package com.lewandowski.wycena3000.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "board_measurement")
@Getter
@Setter
@AllArgsConstructor
public class BoardMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    private int height;

    private int width;




    public BoardMeasurement() {
    }
}
