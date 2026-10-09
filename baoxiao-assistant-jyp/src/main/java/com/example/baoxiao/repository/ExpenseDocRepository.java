package com.example.baoxiao.repository;

import com.example.baoxiao.domain.ExpenseDoc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseDocRepository extends JpaRepository<ExpenseDoc, Long> {

    List<ExpenseDoc> findByOwnerUsernameOrderBySubmittedAtDesc(String ownerUsername);

    List<ExpenseDoc> findAllByOrderBySubmittedAtDesc();
}
