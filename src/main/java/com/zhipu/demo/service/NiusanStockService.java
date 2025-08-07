package com.zhipu.demo.service;

import com.zhipu.demo.entity.NiusanStock;
import com.zhipu.demo.repository.NiusanStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NiusanStockService {
    @Autowired
    private NiusanStockRepository repository;

    public void saveAll(List<NiusanStock> list) {
        repository.saveAll(list);
    }

    public List<NiusanStock> findAll() {
        return repository.findAll();
    }
} 