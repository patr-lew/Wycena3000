package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.entity.Board;
import com.lewandowski.wycena3000.entity.BoardType;
import com.lewandowski.wycena3000.entity.FurniturePart;
import com.lewandowski.wycena3000.entity.FurniturePartType;
import com.lewandowski.wycena3000.repository.FurniturePartRepository;
import com.lewandowski.wycena3000.repository.FurniturePartTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FurniturePartService {

    private final FurniturePartTypeRepository furniturePartTypeRepository;
    private final FurniturePartRepository furniturePartRepository;

    public FurniturePartService(FurniturePartTypeRepository furniturePartTypeRepository, FurniturePartRepository furniturePartRepository) {
        this.furniturePartTypeRepository = furniturePartTypeRepository;
        this.furniturePartRepository = furniturePartRepository;
    }

    public List<FurniturePart> findAll() {
        return furniturePartRepository.findAll();
    }

    public FurniturePart save(FurniturePart furniturePart) {
        return furniturePartRepository.save(furniturePart);
    }

    public List<FurniturePartType> getFurniturePartTypes() {
        return furniturePartTypeRepository.findAll();
    }


    public List<FurniturePart> getFurnitureParts() {
        return furniturePartRepository.findAll();
    }
}
