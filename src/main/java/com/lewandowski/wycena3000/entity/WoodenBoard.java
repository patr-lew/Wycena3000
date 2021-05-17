package com.lewandowski.wycena3000.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "board")
@Getter
@Setter
@AllArgsConstructor
public class WoodenBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private WoodenBoardType woodenBoardType;

    private int height;

    private int width;




    public WoodenBoard() {
    }
}
