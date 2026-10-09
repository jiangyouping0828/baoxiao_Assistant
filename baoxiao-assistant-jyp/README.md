# 报销助手（JYP）

面向企业报销场景的**多角色智能助手**。员工自助查询**现行有效制度**，并可**经授权查询本人单据**；
制度由**行政专员（乔姐）**维护，单据的业务事实由**财务专员（周敏）**核对。

> 本项目为二次开发产物，基座来自开源项目 [lxf888864/expense-ai-agent](https://github.com/lxf888864/expense-ai-agent)（Java + Spring Boot + H2 + RAG 规则引擎），
> 在其基础上补齐了「角色权限 + 制度有效性/版本 + 单据归属授权 + 事实核对」四块核心能力。

---

## 一、需求与本项目的对应关系

| 需求 | 落地实现 |
|---|---|
| 先让员工自助查**有效制度** | `Policy` 实体含 状态 / 生效期 / 版本；员工只能查到「已发布且在当前生效期内」的制度；`POST /api/consult` 只在有效制度范围内做 RAG 检索并作答，答复标注制度编号与版本 |
| 再拓展为**经授权查询本人单据** | `ExpenseDoc` 实体带 `ownerUsername`；员工查询走 `/api/documents/my`，按 ID 访问他人单据直接返回 **403** |
| 制度由**行政专员乔姐**维护 | `ADMIN` 角色：新增/修订/发布/归档/删除制度；修订正文自动升版本；可维护额度规则 |
| 业务事实由**财务专员周敏**核对 | `FINANCE` 角色：查看全部单据，标记「事实属实 / 事实有误」并留痕（核对人、时间、意见） |

---

## 二、角色与权限矩阵

| 接口 | 员工 EMPLOYEE | 行政专员 ADMIN（乔姐） | 财务专员 FINANCE（周敏） | 未登录 |
|---|---|---|---|---|
| `GET /api/policies/valid` 有效制度 | ✅ | ✅ | ✅ | 401 |
| `POST /api/consult` 制度问答 | ✅ | ✅ | ✅ | 401 |
| `GET /api/policies` 全量制度 | 403 | ✅ | 403 | 401 |
| `POST /api/policies` 维护制度 | 403 | ✅ | 403 | 401 |
| `GET /api/documents/my` 本人单据 | ✅（仅本人） | ✅（仅本人） | ✅（仅本人） | 401 |
| `GET /api/documents/my/{id}` 按 ID 查 | ✅ 本人 / **403 他人** | 同左 | 同左 | 401 |
| `GET /api/documents` 全部单据 | 403 | 403 | ✅ | 401 |
| `POST /api/documents/{id}/verify` 核对事实 | 403 | 403 | ✅ | 401 |

鉴权由 `security/AuthInterceptor` + `@RoleRequired` 注解统一拦截；越权返回 403 JSON 或跳转首页提示。

---

## 三、技术栈

- Java 11 目标版本（本机 JDK 17 可直接编译运行）
- Spring Boot 2.7.18（Web / Thymeleaf / Validation / Data JPA）
- H2 内存数据库（启动即建表，可访问 `/h2-console`）
- 轻量 RAG：制度正文按句拆分为 `policy_chunk`，中文 bigram + 关键词打分检索，**只检索有效制度**
- LLM 抽象：`LlmClient`（Mock / Ollama / OpenAI 兼容 HTTP 三种实现），默认 **Mock 可完全离线跑通**

---

## 四、快速开始

```bash
cd baoxiao-assistant-jyp
mvn -DskipTests package
java -jar target/baoxiao-assistant-jyp-0.1.0-SNAPSHOT.jar --server.port=18080
```

浏览器打开 <http://127.0.0.1:18080>，使用下方演示账号登录。

> 注意：若环境变量中存在 `SERVER__PORT`，会覆盖 `application.yml` 的 `server.port`，
> 此时请用 `--server.port=18080` 显式指定端口。

也可开发模式运行：`mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=18080"`

### 演示账号（密码均为 `123456`）

| 用户名 | 姓名 | 角色 | 部门 |
|---|---|---|---|
| `qiaojie` | 乔姐 | 行政专员 | 行政部 |
| `zhoumin` | 周敏 | 财务专员 | 财务部 |
| `zhangsan` | 张三 | 员工 | 研发部 |
| `lisi` | 李四 | 员工 | 市场部 |

---

## 五、验证路径（可复现的验收脚本）

1. 以 `zhangsan` 登录 → 进入「制度查询」，可见 **5 条**有效制度；拟定的 `BX-REMOTE-2026`（草稿）**不出现**。
2. 以 `zhangsan` 进入「制度问答」，提问「出差住宿费一晚能报多少？」→ 答复命中 `BX-TRAVEL-2026`，并列出具体条款。
3. 以 `zhangsan` 访问 `/api/documents/my` → 仅 3 条本人单据；访问 `/api/documents/my/4`（李四的单据）→ **403 无权查看他人单据**。
4. 以 `zhoumin` 登录 → 「事实核对」可见全部 5 条单据，对单据执行「事实属实」→ 状态变为 `事实已核对`，留痕核对人=周敏。
5. 以 `qiaojie` 登录 → 「制度维护」可新增并发布 `BX-MOBILE-2026`；随后员工侧有效制度立即变为 6 条，且制度问答可命中新条款。
6. 以 `qiaojie` 修订 `BX-MEAL-2026` 正文（80→100 元）→ 版本自动 **v1 → v2**，员工问答随即命中新版。

---

## 六、目录结构

```
baoxiao-assistant-jyp
├─ pom.xml
├─ src/main/java/com/example/baoxiao
│  ├─ BaoxiaoApplication.java          启动类
│  ├─ config/                          属性配置、LLM 装配、拦截器注册、演示数据初始化
│  ├─ domain/                          SysUser / Policy / PolicyChunk / ExpenseDoc / Rule + 枚举
│  ├─ repository/                      Spring Data JPA 仓储
│  ├─ dto/                             请求/响应模型
│  ├─ llm/                             LlmClient 抽象（Mock / Ollama / HTTP）
│  ├─ security/                        SessionUser / @RoleRequired / AuthInterceptor
│  ├─ service/                         AuthService / PolicyService / DocumentService / ConsultationService / RuleService
│  └─ controller/                      Auth / Policy / Document / Consult / Page + 全局异常处理
├─ src/main/resources
│  ├─ application.yml
│  ├─ static/                          app.css、app.js
│  └─ templates/                       login / index / policies / documents / consult / fragments
└─ src/test/java/.../PolicyValidityTest.java
```

---

## 七、切换到真实大模型

编辑 `src/main/resources/application.yml`，把 `ai.llm.mock` 改为 `false` 并选择 provider：

```yaml
ai:
  llm:
    provider: zhipu                 # 或 openai 兼容 / ollama
    model: glm-4-flash
    apiKey: ${ZHIPU_API_KEY:}
    baseUrl: https://open.bigmodel.cn/api/paas/v4/chat/completions
    mock: false
```

本地 Ollama：`provider: ollama`、`model: qwen2.5:7b`、`baseUrl: http://localhost:11434/api/chat`。
未配置真实模型时 Mock 会基于检索到的制度片段组织答复，全流程仍可离线演示。

---

## 八、已识别的后续演进方向

- 认证：当前为明文口令 + Session 演示实现，生产应替换为 BCrypt + SSO/OAuth2。
- 检索：关键词 RAG 可替换为向量检索（pgvector / Milvus / Qdrant / ES），`PolicyService.retrieveValid` 为唯一替换点。
- 制度：可增加审批流（拟稿 → 复核 → 发布）与生效日定时切换。
- 单据：可接入 OCR 自动识别发票、对接 ERP 单据源，增加报销额度自动试算。
- 审计：增加操作审计表，记录每次制度变更与事实核对的全量轨迹。
