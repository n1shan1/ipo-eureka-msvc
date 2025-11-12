package com.ipo.app.repository;

import com.ipo.app.entity.Allotment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AllotmentRepository extends JpaRepository<Allotment, Long> {
}