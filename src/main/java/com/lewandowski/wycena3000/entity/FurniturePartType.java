package com.lewandowski.wycena3000.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "part_type")
@Getter
@Setter
@AllArgsConstructor
public class FurniturePartType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    public FurniturePartType() {
    }
}
