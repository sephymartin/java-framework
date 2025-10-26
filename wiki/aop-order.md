# AOP 执行顺序规范

## 概述

在 Spring 应用中，多个 AOP 切面可能同时作用于同一个方法。为了确保切面按照预期的顺序执行，需要使用 `@Order` 注解来明确指定执行顺序。

## 执行顺序规则

### 基本规则

1. **Order 值越小，优先级越高**
2. **默认 Order = Integer.MAX_VALUE**（最低优先级）
3. **相同 Order 值时，执行顺序不确定**

### 执行流程

对于 `@Around` 通知，执行顺序如下：

```
请求进入
  ↓
Order=100 (LogKeywordAspect) - 前置处理
  ↓
Order=200 (RedissonLockByAspect) - 前置处理
  ↓
Order=300 (TransactionAspect) - 前置处理
  ↓
目标方法执行
  ↓
Order=300 (TransactionAspect) - 后置处理
  ↓
Order=200 (RedissonLockByAspect) - 后置处理
  ↓
Order=100 (LogKeywordAspect) - 后置处理
  ↓
响应返回
```

**注意**：Order 值小的切面最先进入，最后退出（类似洋葱模型）

## 推荐的 Order 值分配

### 标准分配表

| Order 值 | 切面类型 | 说明 | 示例 |
|---------|---------|------|------|
| 1-50 | 基础设施层 | 最高优先级，处理全局性的基础功能 | 请求追踪、性能监控 |
| 100 | 日志追踪层 | 设置 MDC 上下文，确保后续切面的日志包含追踪信息 | `@LogKeyword` |
| 200 | 分布式锁层 | 获取分布式锁，控制并发访问 | `@RedisLock` |
| 300 | 事务管理层 | 数据库事务控制 | `@Transactional` |
| 400 | 缓存层 | 缓存处理 | `@Cacheable` |
| 500 | 权限校验层 | 权限和安全检查 | `@PreAuthorize` |
| 1000+ | 业务切面层 | 业务相关的横切关注点 | 业务日志、审计 |

### 项目中的切面顺序

#### 1. LogKeywordAspect (Order = 100)

**作用**：设置 MDC 日志上下文

**原因**：
- ✅ 需要在所有其他切面之前执行
- ✅ 确保后续切面的日志都包含追踪信息
- ✅ 在方法执行完成后清理 MDC

```java
@Aspect
@Order(100)
public class LogKeywordAspect {
    // ...
}
```

#### 2. RedissonLockByAspect (建议 Order = 200)

**作用**：获取分布式锁

**原因**：
- ✅ 在事务之前获取锁，避免死锁
- ✅ 在日志追踪之后，锁的获取过程会被记录
- ✅ 确保同一时刻只有一个线程执行业务逻辑

```java
@Aspect
@Order(200)  // 建议添加
public class RedissonLockByAspect {
    // ...
}
```

#### 3. Spring 事务 (Order = Ordered.LOWEST_PRECEDENCE)

**作用**：数据库事务管理

**原因**：
- ✅ 在锁内部开启事务，避免长时间持有数据库连接
- ✅ 事务应该在业务逻辑的最外层
- ✅ Spring 默认事务的 Order 是最低优先级

```java
@Transactional
public void businessMethod() {
    // 业务逻辑
}
```

## 使用示例

### 示例 1：完整的切面组合

```java
@Service
public class OrderService {
    
    /**
     * 执行顺序：
     * 1. LogKeywordAspect (Order=100) - 设置 MDC
     * 2. RedissonLockByAspect (Order=200) - 获取锁
     * 3. TransactionAspect (Order=最低) - 开启事务
     * 4. 执行业务方法
     * 5. TransactionAspect - 提交/回滚事务
     * 6. RedissonLockByAspect - 释放锁
     * 7. LogKeywordAspect - 清理 MDC
     */
    @LogKeyword(keyword = "orderId", spel = "#{#orderId}")
    @RedisLock(key = "order:#{#orderId}")
    @Transactional
    public void processOrder(Long orderId) {
        // 业务逻辑
        log.info("处理订单"); // 日志中包含 orderId
    }
}
```

### 示例 2：自定义业务切面

```java
/**
 * 业务审计切面
 * Order = 1000，在所有基础设施切面之后执行
 */
@Aspect
@Order(1000)
public class AuditAspect {
    
    @Around("@annotation(audit)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Audit audit) {
        // 此时 MDC 已经设置，日志中会包含追踪信息
        log.info("审计开始");
        
        try {
            Object result = joinPoint.proceed();
            log.info("审计成功");
            return result;
        } catch (Throwable e) {
            log.error("审计失败", e);
            throw e;
        }
    }
}
```

### 示例 3：性能监控切面

```java
/**
 * 性能监控切面
 * Order = 50，在日志追踪之前执行，记录完整的执行时间
 */
@Aspect
@Order(50)
public class PerformanceMonitorAspect {
    
    @Around("execution(* com.example.service..*(..))")
    public Object monitor(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            return joinPoint.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("方法执行时间: {}ms", endTime - startTime);
        }
    }
}
```

## 常见问题

### Q1: 为什么日志追踪要在分布式锁之前？

**A**: 因为获取锁的过程也需要被记录。如果日志追踪在锁之后，那么获取锁的日志就无法包含追踪信息。

```
正确顺序（日志追踪在前）：
1. 设置 MDC (userId=123)
2. 尝试获取锁 -> 日志: [userId=123] 获取锁成功
3. 执行业务逻辑 -> 日志: [userId=123] 处理订单

错误顺序（日志追踪在后）：
1. 尝试获取锁 -> 日志: 获取锁成功 (缺少 userId)
2. 设置 MDC (userId=123)
3. 执行业务逻辑 -> 日志: [userId=123] 处理订单
```

### Q2: 为什么分布式锁要在事务之前？

**A**: 避免死锁和长时间持有数据库连接。

```
正确顺序（锁在事务之前）：
1. 获取分布式锁
2. 开启数据库事务
3. 执行业务逻辑
4. 提交事务（快速释放数据库连接）
5. 释放分布式锁

错误顺序（事务在锁之前）：
1. 开启数据库事务
2. 尝试获取分布式锁（可能等待很久）
3. 执行业务逻辑
4. 提交事务
5. 释放分布式锁
问题：等待锁期间一直持有数据库连接，可能导致连接池耗尽
```

### Q3: 如何验证切面执行顺序？

**A**: 可以在每个切面中添加日志：

```java
@Aspect
@Order(100)
public class LogKeywordAspect {
    
    @Around("logKeywordPointcut()")
    public Object processLogKeyword(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug(">>> LogKeywordAspect 前置处理");
        try {
            return joinPoint.proceed();
        } finally {
            log.debug("<<< LogKeywordAspect 后置处理");
        }
    }
}
```

输出示例：
```
>>> LogKeywordAspect 前置处理
>>> RedissonLockByAspect 前置处理
>>> TransactionAspect 前置处理
执行业务方法
<<< TransactionAspect 后置处理
<<< RedissonLockByAspect 后置处理
<<< LogKeywordAspect 后置处理
```

### Q4: 多个切面有相同的 Order 值会怎样？

**A**: 执行顺序不确定，取决于 Spring 的加载顺序。**强烈建议避免相同的 Order 值。**

### Q5: 如何处理切面之间的依赖？

**A**: 通过合理设置 Order 值，确保被依赖的切面先执行。

```java
// 错误示例：审计切面依赖 MDC 中的 userId，但没有设置 Order
@Aspect
public class AuditAspect {  // 默认 Order = Integer.MAX_VALUE
    @Around("...")
    public Object audit(ProceedingJoinPoint joinPoint) {
        String userId = MDC.get("userId");  // 可能为 null！
        // ...
    }
}

// 正确示例：设置更大的 Order 值，确保在 LogKeywordAspect 之后执行
@Aspect
@Order(1000)  // 大于 LogKeywordAspect 的 100
public class AuditAspect {
    @Around("...")
    public Object audit(ProceedingJoinPoint joinPoint) {
        String userId = MDC.get("userId");  // 一定有值
        // ...
    }
}
```

## 最佳实践

### 1. 明确指定 Order

所有自定义切面都应该明确指定 `@Order` 值，不要依赖默认值。

```java
// ✅ 推荐
@Aspect
@Order(100)
public class MyAspect { }

// ❌ 不推荐
@Aspect
public class MyAspect { }  // 默认 Order = Integer.MAX_VALUE
```

### 2. 使用常量定义 Order 值

```java
public interface AopOrderConstants {
    int PERFORMANCE_MONITOR = 50;
    int LOG_KEYWORD = 100;
    int DISTRIBUTED_LOCK = 200;
    int TRANSACTION = 300;
    int CACHE = 400;
    int SECURITY = 500;
    int BUSINESS_AUDIT = 1000;
}

@Aspect
@Order(AopOrderConstants.LOG_KEYWORD)
public class LogKeywordAspect { }
```

### 3. 文档化切面顺序

在每个切面类的 JavaDoc 中说明其 Order 值和原因。

```java
/**
 * 日志关键字切面
 * <p>
 * Order = 100，在所有业务切面之前执行，确保 MDC 上下文被正确设置
 */
@Aspect
@Order(100)
public class LogKeywordAspect { }
```

### 4. 单元测试验证顺序

```java
@Test
public void testAspectOrder() {
    // 验证切面执行顺序
    List<String> executionOrder = new ArrayList<>();
    
    // 模拟切面执行
    // 验证 executionOrder 的顺序是否正确
}
```

## 参考资料

- [Spring AOP Order Documentation](https://docs.spring.io/spring-framework/reference/core/aop/ataspectj/advice.html#aop-ataspectj-advice-ordering)
- [Spring Core - Ordered Interface](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/Ordered.html)
- [AspectJ Programming Guide](https://www.eclipse.org/aspectj/doc/released/progguide/index.html)

