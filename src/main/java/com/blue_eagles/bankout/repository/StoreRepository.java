package com.blue_eagles.bankout.repository;

import com.blue_eagles.bankout.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    // Finds active stores only (useful if you want to hide closed businesses)
    List<Store> findByIsActiveTrue();

    // Optional: Find by name if needed for search later
    Store findByName(String name);
}
