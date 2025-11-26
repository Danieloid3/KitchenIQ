package com.uni.kitcheniq.repository;

import com.uni.kitcheniq.models.ShiftChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftChangeRepository extends JpaRepository<ShiftChange, Long> {

}
