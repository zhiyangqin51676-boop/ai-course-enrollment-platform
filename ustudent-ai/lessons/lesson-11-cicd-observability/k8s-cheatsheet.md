# Kubernetes 面试 Cheatsheet（学生版 · 背下来能 defend 简历）

> 你的作品三部署在 **AWS ECS**。简历上写的 **k8s** 是同一套编排概念、换套名字。
> 面试问 k8s 基本都是**概念题**,不是让你现场运维集群。这份背熟,能诚实、清楚地答。
>
> **诚实原则**:说"我理解 k8s 核心概念,动手做过 ECS 部署,两者概念对等"——
> **别吹"运维过大规模 k8s 集群"**。诚实 + 讲得清 = 加分;吹牛被追问穿帮 = 减分。

## 0. 一句话电梯版

> "Kubernetes 是开源的容器编排平台:你**声明式**地描述'我想要什么'(几个副本、什么镜像、
> 怎么暴露),k8s 负责让集群**收敛到那个状态**并**自愈**(Pod 挂了自动重建)。
> 我在 ECS 上做过等价的部署,核心概念是一样的。"

## 1. ECS ↔ k8s 概念对照（你会 ECS 就会讲 k8s）

| ECS（你做过的） | k8s（简历写的） | 作用 |
|---|---|---|
| Task Definition | Deployment 里的 **Pod 模板** | 容器跑法:镜像/资源/端口/env |
| Task | **Pod** | 一个跑着的实例 |
| Service（保持 N 个） | **Deployment**(`replicas: N`) | 维持副本数 + 滚动更新 + 回滚 |
| ALB + Target Group | **Service + Ingress** | Service=集群内稳定入口+负载均衡;Ingress=域名/路径从外部进 |
| desired count | **replicas** | 要几个副本 |
| rolling update | **rolling update** | 起新→健康→切→停旧,零停机 |

## 2. 五个核心对象（必须能一句话说清）

| 对象 | 一句话 | 常见追问 |
|---|---|---|
| **Pod** | k8s 最小调度单元,含 1+ 共享网络/存储的容器 | "Pod 和容器区别?"→ 容器是运行时,Pod 是 k8s 的调度包装,一个 Pod 可含多个紧耦合容器(如 sidecar) |
| **Deployment** | 声明"要 N 个某 Pod",管副本+滚更+回滚 | "怎么回滚?"→ `kubectl rollout undo`,切回上个 ReplicaSet |
| **Service** | 给会漂移的 Pod 一个稳定 IP/DNS + 负载均衡 | "为什么需要?"→ Pod 会重建、IP 变,Service 的虚拟 IP 不变 |
| **Ingress** | 七层入口,按域名/路径路由到 Service | "和 Service 区别?"→ Service 是 L4,Ingress 是 L7(HTTP 路由) |
| **Namespace** | 集群内的逻辑隔离分区 | 多团队/多环境隔离 |

## 3. 最小 YAML（看得懂就行,不用背）

```yaml
apiVersion: apps/v1
kind: Deployment
metadata: { name: ustudent-ai }
spec:
  replicas: 3                    # 要 3 个副本(= ECS desired count)
  selector: { matchLabels: { app: ustudent-ai } }
  template:                      # ← Pod 模板(= ECS Task Definition)
    metadata: { labels: { app: ustudent-ai } }
    spec:
      containers:
        - name: ai
          image: <ecr>/ustudent-ai:latest
          ports: [{ containerPort: 8000 }]
---
apiVersion: v1
kind: Service                    # 稳定入口 + 负载均衡到 3 个 Pod
spec:
  selector: { app: ustudent-ai }
  ports: [{ port: 80, targetPort: 8000 }]
```
**要点**:声明式(说"想要什么",不是"怎么一步步做")+ label selector(Service 靠 label 找 Pod)。

## 4. 高频面试题 + 参考答

| 问 | 答 |
|---|---|
| **Pod 和 container 区别?** | 容器是运行时单元;Pod 是 k8s 最小调度单元,包 1+ 容器,共享网络和存储卷 |
| **Deployment 和 Service 区别?** | Deployment 管**Pod 的生命周期**(副本/滚更/回滚);Service 管**怎么访问**这些 Pod(稳定入口+负载均衡) |
| **k8s 怎么做滚动更新?** | Deployment 改镜像 → k8s 起新 ReplicaSet 的 Pod、逐个替换旧的,过程中始终有 Pod 服务(零停机) |
| **怎么回滚?** | `kubectl rollout undo deployment/xxx`,切回上一个 ReplicaSet(≈ ECS 切回上个 task def revision) |
| **k8s 和 docker compose 区别?** | compose = **单机**多容器编排(开发/小场景);k8s = **跨多机集群**编排 + 自愈 + 扩缩容 + 滚更 |
| **k8s 怎么自愈?** | 你声明期望状态(3 副本),控制器**持续对比实际 vs 期望**,少了就补、挂了就重建 |
| **什么时候该用 k8s(而不是 ECS)?** | 云中立/多云、需要可移植性、复杂编排、大生态(Helm/Operator);AWS-only 小团队用 ECS 更省心 |
| **怎么给 Pod 传密钥?** | Secret 对象(别写死在镜像/YAML 明文),挂成 env 或 volume |
| **liveness vs readiness probe?** | liveness=挂了没(挂了重启);readiness=能收流量没(没好就不发流量给它) |

## 5. 会踩的坑（也是"你真懂"的信号）

- ❌ 说"k8s 比 ECS 高级" —— 没有高下,**看场景**(会讲权衡才是懂)。
- ❌ 把 **Pod 当成容器** —— Pod 可含多个容器。
- ❌ 以为改了镜像 `:latest` k8s 会自动更新 —— 要改 Deployment(改 tag 或 rollout restart),和 ECS 的 `force-new-deployment` 同理。
- ❌ 吹"我运维过生产 k8s 集群" —— 被追问 etcd/网络插件/节点故障就穿帮。**诚实讲概念 + ECS 实操经历**。

## 6. 你能诚实说的一句话（简历/面试通用）

> "I deployed my AI service to production on **AWS ECS** and understand how the
> same concepts map to **Kubernetes** — Deployments, Services, Ingress, rolling
> updates, and self-healing via declarative desired state."
