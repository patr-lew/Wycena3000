package com.lewandowski.wycena3000.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "part_type")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PartType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartType partType = (PartType) o;
        return Objects.equals(name, partType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
