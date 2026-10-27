# 多租户客户跟进与工单 SaaS

> 一个项目同时覆盖 **Java 后端** + **测试开发** 双求职方向。
> Spring Cloud Gateway + 多租户隔离 + Redis 缓存三件套 + Kafka 事件驱动 + Pytest 接口自动化。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17、Spring Boot 3.2、Spring Cloud Gateway、MyBatis-Plus 3.5、JJWT、Redisson |
| 中间件 | MySQL 8、Redis 7、Kafka 3.6（bitnami 镜像） |
| 前端 | Vue 3 + Vite + Element Plus |
| 测试 | Pytest + Requests + Allure Pytest |
| 部署 | Docker Compose |

## 本地启动

```bash
# 1. 启动基础设施
cd 05_代码
docker-compose up -d mysql redis kafka

# 2. 启动三个后端（三个终端）
cd backend
mvn -pl gateway spring-boot:run    # :8080
mvn -pl customer spring-boot:run   # :8081
mvn -pl ticket spring-boot:run     # :8082

# 3. 启动前端
cd frontend
npm install
npm run dev                        # :5173

# 4. 跑测试
cd 08_测试/qa-automation
python -m pytest -v
```

测试账号：`qa_user` / `qa_pass_123`（tenant_id=1）

## 核心架构亮点

1. **Gateway 全局 JWT 鉴权**：AuthGlobalFilter 校验 token，白名单路径（/api/auth/*）放行，租户上下文透传
2. **多租户隔离**：MyBatis-Plus TenantLineInnerInterceptor 自动追加 tenant_id 条件，业务代码无感知
3. **Redis 缓存三件套**：布隆过滤器防穿透 + Redisson 分布式锁防击穿 + 随机 TTL 防雪崩 + 延迟双删保一致性
4. **Kafka 事件驱动**：工单状态变更 afterCommit 发事件，本地消息表兜底保证最终一致，Consumer 幂等写客户时间线
5. **工单状态机**：OPEN→PROCESSING→RESOLVED→CLOSED，非法流转返回 40901

## 技术选型 Trade-off

| 决策 | 选择 | 放弃 | 原因 |
|---|---|---|---|
| 服务发现 | 静态路由直连 | Nacos | 16GB 内存跑 rag-* 容器后 Nacos OOM，本地演示用静态路由 |
| 多租户方案 | 字段隔离（tenant_id） | Schema 隔离 | MVP 简化，字段隔离足够面试展示 |
| 消息队列 | Kafka | RabbitMQ | JD2 明确要求 Kafka |
| 缓存序列化 | 手动 ObjectMapper + JavaTimeModule | Spring 自动配置 | CustomerService 手动 new ObjectMapper，需显式注册 JSR310 |

## 面试问答要点

**Q: 布隆过滤器怎么用的？**
A: 客户 ID 建索引时加入布隆过滤器，查询时先 bloom.contains(id)，不存在直接返回 null，防缓存穿透。create() 时把新 ID 加入 bloom。

**Q: 缓存一致性怎么保证？**
A: 延迟双删策略——更新 DB 前删缓存，更新后延迟 500ms 再删一次，覆盖读旧值回写缓存的窗口。

**Q: Kafka 消息丢了怎么办？**
A: afterCommit 发 Kafka，发送失败落本地消息表（local_message），定时任务扫描补发。Consumer 端按 eventId 幂等去重。

**Q: 多租户怎么隔离的？**
A: MyBatis-Plus TenantLineInnerInterceptor 自动在 SQL 里追加 WHERE tenant_id = ?，业务代码完全不感知。gateway 从 JWT 解析 tenantId 透传下游。

## 项目结构

```
multi-tenant-crm-ticket/
├── 00_原始项目描述.md
├── 01_项目基线说明书.md
├── 02_架构设计/
├── 03_评审报告/
├── 04_任务拆解与开发规范.md
├── 05_代码/
│   ├── backend/           # Maven 多模块
│   │   ├── gateway/       # :8080 路由+鉴权
│   │   ├── customer/      # :8081 客户+租户
│   │   ├── ticket/        # :8082 工单状态机
│   │   └── sql/init.sql
│   ├── frontend/          # Vue3 + Element Plus
│   └── docker-compose.yml
├── 06_冒烟/
├── 08_测试/
│   ├── qa-automation/     # Pytest 9 用例
│   └── 测试报告.md
├── 08.5_安全扫描/
├── 09_验收/
└── _项目状态.md
```
