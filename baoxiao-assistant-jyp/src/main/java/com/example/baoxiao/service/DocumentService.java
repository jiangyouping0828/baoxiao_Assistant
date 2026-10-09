package com.example.baoxiao.service;

import com.example.baoxiao.domain.DocStatus;
import com.example.baoxiao.domain.ExpenseDoc;
import com.example.baoxiao.dto.DocumentForm;
import com.example.baoxiao.security.SessionUser;
import com.example.baoxiao.repository.ExpenseDocRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 单据服务。
 *
 * <p>权限边界：
 * <ul>
 *   <li>员工：仅能查询 / 提交「本人」单据（经授权查询本人单据）；</li>
 *   <li>财务专员（周敏）：可查看全部单据并核对业务事实；</li>
 *   <li>跨人访问一律拒绝（{@link #getOwnedDoc}）。</li>
 * </ul>
 * </p>
 */
@Service
public class DocumentService {

    private static final DateTimeFormatter DOC_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final AtomicInteger SEQ = new AtomicInteger(0);

    private final ExpenseDocRepository documentRepository;

    public DocumentService(ExpenseDocRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /** 员工查询本人单据（唯一入口）。 */
    public List<ExpenseDoc> listOwnedBy(String username) {
        return documentRepository.findByOwnerUsernameOrderBySubmittedAtDesc(username);
    }

    /** 财务专员查看全部待核对单据。 */
    public List<ExpenseDoc> listAll() {
        return documentRepository.findAllByOrderBySubmittedAtDesc();
    }

    /**
     * 按 ID 获取单据，并强制校验归属——「经授权查询本人单据」的落地实现。
     */
    public ExpenseDoc getOwnedDoc(Long id, String username) {
        ExpenseDoc doc = documentRepository.findById(id)
                .orElseThrow(() -> BizException.notFound("单据不存在：" + id));
        if (!doc.getOwnerUsername().equals(username)) {
            throw BizException.forbidden("无权查看他人单据，仅可查询本人单据");
        }
        return doc;
    }

    @Transactional
    public ExpenseDoc create(SessionUser owner, DocumentForm form) {
        String docNo = "BX" + LocalDateTime.now().format(DOC_NO_FORMAT)
                + String.format("%03d", SEQ.incrementAndGet() % 1000);
        ExpenseDoc doc = new ExpenseDoc(docNo, owner.getUsername(), owner.getDisplayName(),
                form.getCategory(), form.getAmount(), form.getExpenseDate(), form.getSummary());
        return documentRepository.save(doc);
    }

    /** 财务专员核对业务事实（乔姐维护制度，周敏核对事实）。 */
    @Transactional
    public ExpenseDoc verify(Long id, String financeOperator, boolean verified, String note) {
        ExpenseDoc doc = documentRepository.findById(id)
                .orElseThrow(() -> BizException.notFound("单据不存在：" + id));
        doc.setFactVerified(verified);
        doc.setVerifiedBy(financeOperator);
        doc.setVerifyNote(note);
        doc.setVerifiedAt(LocalDateTime.now());
        doc.setStatus(verified ? DocStatus.VERIFIED : DocStatus.REJECTED);
        return documentRepository.save(doc);
    }

    /** 财务专员受理（标记核对中）。 */
    @Transactional
    public ExpenseDoc startChecking(Long id, String financeOperator) {
        ExpenseDoc doc = documentRepository.findById(id)
                .orElseThrow(() -> BizException.notFound("单据不存在：" + id));
        if (doc.getStatus() == DocStatus.SUBMITTED) {
            doc.setStatus(DocStatus.CHECKING);
            doc.setVerifiedBy(financeOperator);
            doc = documentRepository.save(doc);
        }
        return doc;
    }
}
