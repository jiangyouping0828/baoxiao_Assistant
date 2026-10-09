package com.example.baoxiao.repository;

import com.example.baoxiao.domain.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RuleRepository extends JpaRepository<Rule, Long> {

    Optional<Rule> findByCategory(String category);
}
