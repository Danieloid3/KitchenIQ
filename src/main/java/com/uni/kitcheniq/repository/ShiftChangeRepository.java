package com.uni.kitcheniq.repository;

import com.uni.kitcheniq.models.ShiftChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftChangeRepository extends JpaRepository<ShiftChange, Long> {

    @Query("SELECT sc FROM ShiftChange sc ORDER BY sc.changeDateTime DESC")
    List<ShiftChange> findAllOrderByChangeDateTimeDesc();
}
