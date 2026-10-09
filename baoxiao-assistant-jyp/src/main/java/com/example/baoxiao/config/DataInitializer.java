package com.example.baoxiao.config;

import com.example.baoxiao.domain.DocStatus;
import com.example.baoxiao.domain.ExpenseDoc;
import com.example.baoxiao.domain.SysUser;
import com.example.baoxiao.domain.UserRole;
import com.example.baoxiao.dto.DocumentForm;
import com.example.baoxiao.dto.PolicyForm;
import com.example.baoxiao.repository.ExpenseDocRepository;
import com.example.baoxiao.repository.SysUserRepository;
import com.example.baoxiao.security.SessionUser;
import com.example.baoxiao.service.DocumentService;
import com.example.baoxiao.service.PolicyService;
import com.example.baoxiao.service.RuleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 演示数据初始化：三类角色账号 + 若干制度 + 若干单据。
 *
 * <p>制度默认由行政专员「乔姐」维护，单据的业务事实默认待「周敏」核对。</p>
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final String ADMIN_NAME = "乔姐";
    private static final String FINANCE_NAME = "周敏";

    private final SysUserRepository userRepository;
    private final PolicyService policyService;
    private final DocumentService documentService;
    private final RuleService ruleService;
    private final ExpenseDocRepository documentRepository;

    public DataInitializer(SysUserRepository userRepository, PolicyService policyService,
                           DocumentService documentService, RuleService ruleService,
                           ExpenseDocRepository documentRepository) {
        this.userRepository = userRepository;
        this.policyService = policyService;
        this.documentService = documentService;
        this.ruleService = ruleService;
        this.documentRepository = documentRepository;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedPolicies();
        seedRules();
        seedDocuments();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.save(new SysUser("qiaojie", "123456", ADMIN_NAME, UserRole.ADMIN, "行政部"));
        userRepository.save(new SysUser("zhoumin", "123456", FINANCE_NAME, UserRole.FINANCE, "财务部"));
        userRepository.save(new SysUser("zhangsan", "123456", "张三", UserRole.EMPLOYEE, "研发部"));
        userRepository.save(new SysUser("lisi", "123456", "李四", UserRole.EMPLOYEE, "市场部"));
    }

    private void seedPolicies() {
        if (!policyService.listAll().isEmpty()) {
            return;
        }

        publish("BX-TRAVEL-2026", "差旅费报销制度", "差旅",
                "员工因公出差，须在出差前提交出差申请并获直属主管审批。\n"
                        + "城市间交通费按经济舱/二等座标准据实报销，超标部分自理。\n"
                        + "住宿费按目的地城市标准执行：一线城市每晚不超过600元，其他城市不超过400元。\n"
                        + "出差伙食补贴按每人每天100元包干，出差当日往返的按半天50元计。\n"
                        + "差旅费应在出差结束后15个工作日内提交报销，逾期需书面说明原因。",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        publish("BX-TRAFFIC-2026", "市内交通费报销制度", "交通",
                "市内公务出行优先使用公共交通，凭有效票据据实报销。\n"
                        + "因公务搭乘出租车或网约车，单次金额不超过100元，超出部分需部门负责人书面说明。\n"
                        + "同一员工市内交通费每月累计报销不超过800元。\n"
                        + "自驾车因公出行的，按每公里1.5元的油耗补贴标准报销，不再重复报销油费票据。",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        publish("BX-MEAL-2026", "工作餐费报销制度", "餐饮",
                "因加班或外出公务需要就餐的，方可申请工作餐报销。\n"
                        + "工作餐标准为每人每餐不超过80元，超出标准部分由个人承担。\n"
                        + "报销工作餐须提供正规发票，并在报销单上注明事由、参与人数与日期。\n"
                        + "同一天最多报销一次工作餐，不得与差旅伙食补贴重复报销。",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        publish("BX-OFFICE-2026", "办公用品报销制度", "办公",
                "办公用品原则上由行政部统一采购，员工个人垫付采购的须事前向行政部报备。\n"
                        + "单笔办公用品报销金额不超过500元，超过500元须走采购流程。\n"
                        + "报销时须提供购物明细清单与正规发票，明细不清的不予报销。",
                LocalDate.of(2026, 3, 1), null);

        publish("BX-ENTERTAIN-2026", "业务招待费报销制度", "招待",
                "业务招待须事前填写招待申请单并获部门负责人及以上审批，未经审批的招待费不予报销。\n"
                        + "业务招待费单次不超过1000元，人均不超过200元。\n"
                        + "报销时须注明招待对象单位、人数、事由，并附审批单。\n"
                        + "严禁以业务招待名义报销个人消费。",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        // 一份草稿制度：用于验证员工「查不到未发布制度」
        PolicyForm draft = new PolicyForm();
        draft.setCode("BX-REMOTE-2026");
        draft.setTitle("远程办公费用报销制度（拟定）");
        draft.setCategory("办公");
        draft.setContent("远程办公期间购置办公家具的报销标准正在拟定中，尚未发布，不作为报销依据。");
        draft.setEffectiveFrom(LocalDate.of(2026, 6, 1));
        draft.setPublish(false);
        policyService.createOrUpdate(draft, ADMIN_NAME);
    }

    private void seedRules() {
        if (!ruleService.listRules().isEmpty()) {
            return;
        }
        ruleService.saveRule("交通", new BigDecimal("100"));
        ruleService.saveRule("餐饮", new BigDecimal("80"));
        ruleService.saveRule("差旅", new BigDecimal("2000"));
        ruleService.saveRule("办公", new BigDecimal("500"));
        ruleService.saveRule("招待", new BigDecimal("1000"));
    }

    private void seedDocuments() {
        if (documentRepository.count() > 0) {
            return;
        }
        SessionUser zhangsan = new SessionUser(3L, "zhangsan", "张三", UserRole.EMPLOYEE, "研发部");
        SessionUser lisi = new SessionUser(4L, "lisi", "李四", UserRole.EMPLOYEE, "市场部");

        createDoc(zhangsan, "交通", new BigDecimal("86.00"), LocalDate.now().minusDays(3),
                "客户现场往返网约车，含高速费");
        createDoc(zhangsan, "餐饮", new BigDecimal("128.00"), LocalDate.now().minusDays(2),
                "项目上线加班工作餐，4人");
        createDoc(zhangsan, "办公", new BigDecimal("239.00"), LocalDate.now().minusDays(1),
                "团队购买白板笔与打印纸");

        createDoc(lisi, "招待", new BigDecimal("880.00"), LocalDate.now().minusDays(5),
                "接待合作方一行3人商务用餐");
        createDoc(lisi, "差旅", new BigDecimal("1560.00"), LocalDate.now().minusDays(4),
                "上海出差2天，含高铁与住宿");

        // 演示：财务专员已核对通过一条
        documentRepository.findAll().stream().findFirst().ifPresent(doc -> {
            doc.setFactVerified(true);
            doc.setVerifiedBy(FINANCE_NAME);
            doc.setVerifyNote("已核验发票与行程单，业务事实属实");
            doc.setStatus(DocStatus.VERIFIED);
            doc.setVerifiedAt(java.time.LocalDateTime.now());
            documentRepository.save(doc);
        });
    }

    private void publish(String code, String title, String category, String content,
                         LocalDate from, LocalDate to) {
        PolicyForm form = new PolicyForm();
        form.setCode(code);
        form.setTitle(title);
        form.setCategory(category);
        form.setContent(content);
        form.setEffectiveFrom(from);
        form.setEffectiveTo(to);
        form.setPublish(true);
        policyService.createOrUpdate(form, ADMIN_NAME);
    }

    private void createDoc(SessionUser owner, String category, BigDecimal amount,
                           LocalDate date, String summary) {
        DocumentForm form = new DocumentForm();
        form.setCategory(category);
        form.setAmount(amount);
        form.setExpenseDate(date);
        form.setSummary(summary);
        ExpenseDoc doc = documentService.create(owner, form);
        System.out.println("[seed] 单据 " + doc.getDocNo() + " 归属 " + owner.getDisplayName());
    }
}
