package com.blue_eagles.bankout.controller;

import com.blue_eagles.bankout.entity.Store;
import com.blue_eagles.bankout.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    @Autowired private StoreRepository storeRepository;

    @GetMapping
    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    // Note: You need a StoreRepository interface created similar to AccountRepository
}
