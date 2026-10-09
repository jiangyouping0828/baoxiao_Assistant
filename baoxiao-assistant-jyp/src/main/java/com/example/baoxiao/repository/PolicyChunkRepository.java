package com.example.baoxiao.repository;

import com.example.baoxiao.domain.PolicyChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PolicyChunkRepository extends JpaRepository<PolicyChunk, Long> {

    List<PolicyChunk> findByPolicyId(Long policyId);

    void deleteByPolicyId(Long policyId);
}
