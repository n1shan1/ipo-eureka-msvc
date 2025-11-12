package com.ipo.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ipo.app.entity.Mandate;

@Repository
public interface MandateRepository extends JpaRepository<Mandate, String> {

    Optional<Mandate> findByApplicationId(String applicationId);
}