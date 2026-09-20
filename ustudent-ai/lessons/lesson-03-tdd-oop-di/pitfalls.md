# 第 3 课翻车点 cheatsheet

按"症状 → 原因 → 一句话解法"组织,便于现场快速判断。

---

## TDD 段

### 学生看到 IDE 红波浪线就慌
- **症状**:一学生举手"我这没跑就报错"。
- **原因**:没意识到"编译不过 = 第一次 RED"。
- **解法**:黑板上写 "**红波浪线 = RED 的一种**"。让他跑 `./gradlew test`,看错误信息才敢往下。

### `import` 到不存在的 class,pytest 直接崩
- **症状**:`pytest` 输出 `ImportError`,学生不知这算红还是环境坏了。
- **解法**:告诉学生 "**ImportError 就是 pytest 的 RED**。写实现文件让它 import 得到就是 GREEN 的第一步。"

### 学生跳过 RED 直接写代码
- **症状**:老师说"先写测试",几个人已经把 `graduation.py` 写完了。
- **解法**:**当场让他删掉 `graduation.py`**,重来一遍。**"你今天不体验 RED,回家你还是不会用 TDD。"**

### `assertThat` vs `assertEquals` 混用
- **症状**:JUnit 5 老代码里用 `assertEquals`,课上教的是 AssertJ 的 `assertThat`。
- **解法**:统一用 AssertJ。`import org.assertj.core.api.Assertions.assertThat`。**保持一致比"哪个好"更重要**。

### Gradle 跑测试慢/卡
- **症状**:`./gradlew test` 超 30 秒,学生失去耐心。
- **解法**:提前打开 `--tests GraduationServiceTest` 单独跑;Kotlin 项目 warm-up 后再教学。**不要现场等 45 秒的 gradle 首跑**。

---

## OOP 段

### 学生问"class 和 struct 有什么区别?"(C 背景)
- **解法**:"**在 Python/Java/Kotlin 里,class 就是有方法的 struct**。别纠结名词,今天只讲两点:**数据 + 方法 打包**、**private 隐藏细节**。"

### 学生想学继承,你没讲
- **症状**:"老师,那 extends 呢?abstract class 呢?"
- **解法**:"**今天只讲 encapsulation**。继承、多态是**需要用到时才教**。你今天先把 class 用起来,遇到重复模式再学继承。**避免过度设计**。"

### Python 学生忘 `self`
- **症状**:`can_graduate(credits, gpa)` 忘了写 `self`,报 `takes 2 positional arguments but 3 were given`。
- **解法**:白板上大字写 "**方法第一个参数一定是 `self`**"。多提几次。

### 学生把行为放全局函数、数据放 dict
- **症状**:作业交上来,`GraduationRequest` 是 Pydantic,但 `is_eligible()` 写成了 `can_graduate(credits, gpa)` 独立函数。
- **解法**:review 时明确指出"**这违反今天的 OOP 目标**"。让学生改成挂在 request 上。

### Pydantic v2 vs v1 语法混
- **症状**:学生 `class Config: schema_extra = ...` 报 warning。
- **解法**:课上不展开,提一句 "**我们用 Pydantic v2**。v1 教程别看。如果你 Google 到旧写法先看官方文档 pydantic.dev/2 目录。"

---

## Spring IOC/DI 段

### 学生问 "`@Service` 和 `@Component` 到底啥区别?"
- **解法**:"**技术上一样,语义上不同**。`@Component` 通用,`@Service` 表示业务逻辑层。**读代码的人一眼知道分层**。"

### 学生找不到 Spring 是怎么"知道"要 scan
- **症状**:"我建了 class 加了 `@Service`,Spring 怎么知道?"
- **解法**:"**你的 main class 上有 `@SpringBootApplication`,它包含了 `@ComponentScan`。默认扫描 main class 所在 package 及其子 package。**"

### field injection 老代码搜出来
- **症状**:学生看别人的 blog,写了 `@Autowired private val repo: StudentRepository`。
- **解法**:"**别用 field injection**。写 constructor 参数。原因:不可变、构造时依赖凑齐、测试友好。IntelliJ 已经会 lint 这个。"

### `@Autowired` 在 constructor 上加不加?
- **解法**:"**Kotlin / Spring 4.3+ 里 constructor 就一个,`@Autowired` 可以省**。写了也不错,但**新代码别写**,保持干净。"

### 多个 bean 匹配一个类型,启动报错
- **症状**:`NoUniqueBeanDefinitionException`。
- **解法**:课上不深讲。一句话:"**如果有两个 `StudentRepository` 实现,Spring 不知道注哪个,用 `@Primary` 或 `@Qualifier` 指定**。"

### 循环依赖
- **症状**:A 注入 B,B 注入 A,启动报错。
- **解法**:"**循环依赖是设计问题,不是配置问题**。往往说明某个 class 职责太重,拆一个出来。今天不展开。"

### 学生把 `new` 又用回去了
- **症状**:homework review 发现 `EnrollmentService()` 硬 `new` 出来传给测试。
- **解法**:"**测试里可以直接 new,那是被测对象。但**依赖**(比如 repo)要 mock/fake,不能真 new 连数据库**。"

---

## FastAPI 作业段

### `ImportError: no module named app`
- **原因**:pytest 从错误目录跑,或 `pytest.ini` 缺 `pythonpath`。
- **解法**:确认 `pytest.ini` 有 `pythonpath = .`,并且从项目根目录跑 `pytest`。

### `422 Unprocessable Entity`
- **原因**:请求 JSON 字段名/类型和 Pydantic model 对不上。
- **解法**:打开 `/docs`,看 request schema,逐字段对照。

### `AttributeError: 'GraduationRequest' object has no attribute 'is_eligible'`
- **原因**:方法定义缩进错了,变成了 `__init__` 内部的 nested function。
- **解法**:方法要和 `__init__` **同层缩进**。IntelliSense 里 `req.` 看不到 `is_eligible` 就是错了。

### FastAPI `TestClient` import 报错
- **症状**:`ImportError: cannot import name 'TestClient' from 'fastapi.testclient'`。
- **原因**:starlette / httpx 版本冲突。
- **解法**:`pip install -r requirements-dev.txt`,确保 httpx 装了。

### 学生 `pytest.ini` 里没 `testpaths`
- **症状**:`pytest` 跑不到新加的测试。
- **解法**:确认 `pytest.ini` 有 `testpaths = tests` 且新测试文件叫 `test_*.py`。

---

## 教学纪律

### 学生"提前跑作业"跑到一半问 LLM 报错
- **原因**:好奇心,想直接把 lesson 4 的 `/ask` 加上。
- **解法**:"**今天作业没 LLM,别提前**。你先把 TDD 姿势练明白,LLM 下节课接。"

### 学生想现在申请 Groq key
- **解法**:"**下节课再申**。现在把注意力放 TDD/OOP/DI 上。分心就学不透。"

### 学生想改后端 Kotlin
- **解法**:"**本营只写 Python AI 层**,后端演示只是**看**,不改。"

---

## 时间管理

### 如果 TDD Round 1 超时,Python 段没时间
- **解法**:Python 段砍到 5 分钟,只做 Step 1–3(RED → GREEN),负面 case 留作业。**关键是"节奏跨语言一致"这个 message 传到就行**。

### 如果 IOC/DI 段超时
- **解法**:砍手动 DI 段(4.2),直接从"糟糕版"跳到"Spring 自动版"。**核心 message**:constructor 参数是依赖入口。

### 如果作业带走段没时间
- **解法**:让学生**下课自己看 `starter/README.md`**。**下节课开场花 5 分钟 recap 作业进度**,不放弃这个节点。
