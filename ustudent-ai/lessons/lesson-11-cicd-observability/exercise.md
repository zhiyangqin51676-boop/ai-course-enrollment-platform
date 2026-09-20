# 第 11 课练习 · CI/CD + 编排 + 可观测性

> 讲授课,**无强制作品**(作品三=第 10 课的部署)。以下鼓励但不强制。
> 本节的"硬产出"是**你能在面试里 defend 简历上的 k8s / CI/CD / 监控**。

## 必做（0 成本,5 分钟自测）

打开 `k8s-cheatsheet.md`,**合上答案**,自己能不能一句话答出:
1. Pod 和 container 区别?
2. Deployment 和 Service 区别?
3. k8s 怎么滚动更新 / 怎么回滚?
4. k8s 和 docker compose 区别?
5. 什么时候用 k8s、什么时候用 ECS?

答不顺的,回去看对照表(ECS 你已经会了,k8s 就是换名字)。

## 选做 A · 接上 CI/CD（做过第 10 课部署的同学）

1. 打开你 repo 的 `bitbucket-pipelines.yml`,对着讲三个 step:`test` / `build-and-push` / `deploy(manual)`。
2. Bitbucket → Repository settings → **Repository variables** 配好:
   `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` / `AWS_REGION` / `AWS_ACCOUNT_ID` / `ECR_REPOSITORY` / `ECS_CLUSTER` / `ECS_SERVICE`(**secrets 勾 secured**)。
3. 推一个测试 commit,看 pipeline 里 `test` 跑绿;推 main 看 `build-and-push` 绿、`deploy` 停在**待你手动点**。
4. 截图 pipeline 跑通(test 绿 + deploy 待 manual)。

> ⚠️ 做完记得 `terraform destroy`(同第 10 课成本警告)。

## 选做 B · 看一眼可观测性

- Splunk:搜 `source="ecs/ustudent-production"` 看日志;或 `aws logs tail /ecs/ustudent-production --follow` 兜底。
- SignalFX(或 CloudWatch):看服务的错误率 / 延迟 / task 数。
- 想一个**好告警**:你会给这个服务设什么阈值?(提示:symptom-based,如"5xx 率 > 5% 持续 2 分钟",而不是"CPU > 80%")

## 选做 C · 本地摸 k8s（想深入的)

不进 AWS、免费:
```bash
brew install kind kubectl        # 或 minikube
kind create cluster
kubectl apply -f deploy.yaml      # 用 cheatsheet 里那份 Deployment+Service
kubectl get pods                  # 看 3 个 Pod 起来
kubectl rollout undo deploy/ustudent-ai   # 试一把回滚
kind delete cluster
```
> 只为"摸过"的手感,不要求。

## 产出（写进作品集）

**一段英文简历句**(拼上第 10 课那句 = 完整 ship + operate 经历):
> Automated delivery with a Bitbucket CI/CD pipeline (pytest on every push,
> manual-gate production deploy to ECS on main). Understand container
> orchestration across ECS and Kubernetes (Deployments, Services, rolling
> updates, self-healing). Instrumented logs (Splunk) and metrics (SignalFX)
> with symptom-based alerting.
