package com.example.resourcebooking.repository;

import com.example.resourcebooking.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    boolean existsByName(String name);
}
