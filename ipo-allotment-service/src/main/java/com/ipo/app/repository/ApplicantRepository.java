package com.ipo.app.repository;

import com.ipo.app.entity.EligibleApplicant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicantRepository extends JpaRepository<EligibleApplicant, String> {

    Optional<EligibleApplicant> findByApplicationId(String applicationId);
}