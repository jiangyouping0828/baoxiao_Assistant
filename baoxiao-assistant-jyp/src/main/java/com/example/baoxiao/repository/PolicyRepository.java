package com.example.baoxiao.repository;

import com.example.baoxiao.domain.Policy;
import com.example.baoxiao.domain.PolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    Optional<Policy> findByCode(String code);

    List<Policy> findByStatus(PolicyStatus status);

    List<Policy> findAllByOrderByUpdatedAtDesc();
}
