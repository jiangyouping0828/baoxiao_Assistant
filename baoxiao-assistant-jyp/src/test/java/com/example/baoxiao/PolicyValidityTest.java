package com.example.baoxiao;

import com.example.baoxiao.domain.Policy;
import com.example.baoxiao.domain.PolicyStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 「有效制度」判定规则测试：只有 已发布 + 在生效期内 才对员工可见。
 */
class PolicyValidityTest {

    @Test
    void publishedWithinRangeIsValid() {
        Policy policy = new Policy("BX-T", "标题", "交通", "正文", "乔姐");
        policy.setStatus(PolicyStatus.PUBLISHED);
        policy.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        policy.setEffectiveTo(LocalDate.of(2026, 12, 31));

        assertTrue(policy.isValidOn(LocalDate.of(2026, 5, 1)));
        assertTrue(policy.isValidOn(LocalDate.of(2026, 1, 1)));
        assertTrue(policy.isValidOn(LocalDate.of(2026, 12, 31)));
    }

    @Test
    void draftIsNeverValid() {
        Policy policy = new Policy("BX-D", "标题", "交通", "正文", "乔姐");
        policy.setStatus(PolicyStatus.DRAFT);
        policy.setEffectiveFrom(LocalDate.of(2026, 1, 1));

        assertFalse(policy.isValidOn(LocalDate.of(2026, 5, 1)));
    }

    @Test
    void archivedIsNeverValid() {
        Policy policy = new Policy("BX-A", "标题", "交通", "正文", "乔姐");
        policy.setStatus(PolicyStatus.ARCHIVED);
        assertFalse(policy.isValidOn(LocalDate.of(2026, 5, 1)));
    }

    @Test
    void outOfRangeIsInvalid() {
        Policy policy = new Policy("BX-R", "标题", "交通", "正文", "乔姐");
        policy.setStatus(PolicyStatus.PUBLISHED);
        policy.setEffectiveFrom(LocalDate.of(2026, 6, 1));

        assertFalse(policy.isValidOn(LocalDate.of(2026, 5, 31)));
    }

    @Test
    void openEndedIsValid() {
        Policy policy = new Policy("BX-O", "标题", "办公", "正文", "乔姐");
        policy.setStatus(PolicyStatus.PUBLISHED);
        policy.setEffectiveFrom(LocalDate.of(2026, 3, 1));
        policy.setEffectiveTo(null);

        assertTrue(policy.isValidOn(LocalDate.of(2030, 1, 1)));
    }
}
