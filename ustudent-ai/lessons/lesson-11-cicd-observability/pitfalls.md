# 第 11 课翻车点 cheatsheet（讲授课）

> 本节是讲授+演示。翻车点分:CI/CD 演示的坑、k8s 讲解的坑、可观测演示的坑。

## CI/CD

### Bitbucket Pipeline 用了错的 AWS region
- **症状**：deploy step 报 `ResourceNotFoundException: Service not found`。
- **解法**：Repository Variables 里 `AWS_REGION` 必须和 terraform 部署到的 region 一致。

### Secrets 进了 yml
- **症状**：AWS key / DB 密码出现在 `bitbucket-pipelines.yml` 或 git 历史。
- **解法**：只走 **Repository Variables**(勾 secured)。进了 git 立刻 revoke + 轮换。

### 把 deploy 改成自动(去掉 manual)
- **症状**：一推 main 就自动上生产,坏代码直接到用户。
- **解法**：保留 `trigger: manual` —— deployment gate 是特性不是麻烦。回扣作品二 human approval。

### 学生纠结 "pipeline 没跑起来" 卡住
- **提醒**：本节 pipeline 是**选做**。没配好不影响作品三(作品三=第 10 课手动部署)。别让学生卡在 CI 配置上。

## k8s(讲解时容易被带偏)

### 学生问"那我们为什么不用 k8s"
- **答**:ECS 对 AWS-only 小项目更省心;k8s 概念今天讲清就够 defend 简历。**不是谁更高级,是场景不同。**

### 学生想现场装 k8s 集群
- **拦一下**:本节**不上集群**。想动手课后 `kind`/`minikube` 本地玩(exercise 选做 C)。课上装集群会吃掉整节时间。

### 把 Pod 讲成"就是容器"
- **纠正**:Pod 是最小**调度**单元,可含多个共享网络/存储的容器(sidecar 模式)。这是高频追问。

### 学生吹"运维过 k8s 生产集群"
- **反复叮嘱**:诚实讲"理解概念 + ECS 实操"。被追问 etcd/CNI/节点故障会穿帮,**诚实反而加分**。

## 可观测性

### Splunk / SignalFX 看不到数据
- **症状**：CloudWatch 有日志,Splunk 里没有。
- **解法**：检查 CloudWatch → Splunk 转发(AWS Add-on / Lambda forwarder)。没接就 `aws logs tail /ecs/ustudent-production --follow` 兜底演示,一样讲清 log 支柱。

### 把 log 和 metric 混为一谈
- **纠正**:log=离散事件查"具体一次";metric=聚合数值看"趋势/告警"。面试常考区别。

### 演示时只给 dashboard、不讲告警
- **提醒**:dashboard 没人一直盯。重点讲**告警 + 告警疲劳 + symptom-based**,这才是 SRE 味道,也接第 12 课。

### 高基数标签把 metric 系统打爆
- **甜点**:metric 打上 `user_id` 这种高基数标签 → 时间序列爆炸、账单爆炸。面试进阶点,提一句。
