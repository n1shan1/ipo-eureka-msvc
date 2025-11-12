package com.ipo.app.repository;

import com.ipo.app.entity.IPOApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<IPOApplication, Long> {

    Optional<IPOApplication> findByApplicationId(String applicationId);

    boolean existsByIpoIdAndInvestorId(String ipoId, String investorId);

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<IPOApplication> findByIdempotencyKey(String idempotencyKey);
}