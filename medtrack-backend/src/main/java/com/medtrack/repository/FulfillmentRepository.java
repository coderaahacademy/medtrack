package com.medtrack.repository;

import com.medtrack.entity.Fulfillment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FulfillmentRepository extends JpaRepository<Fulfillment, Long> {
}