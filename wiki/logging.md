# 日志增强功能

## @LogKeyword 注解

`@LogKeyword` 注解用于通过 SpEL 表达式从方法参数中自动提取字段信息并放入 SLF4J MDC 中，实现日志的上下文追踪。

### 核心特性

- ✅ **自动 MDC 管理** - 方法执行前自动设置 MDC，执行后自动清理
- ✅ **SpEL 表达式支持** - 支持完整的 Spring Expression Language 功能
- ✅ **多参数支持** - 可以从多个方法参数中提取信息
- ✅ **可重复注解** - 同一方法可以使用多个 `@LogKeyword` 注解
- ✅ **异常安全** - 即使方法抛出异常也能正确清理 MDC
- ✅ **性能优化** - SpEL 表达式自动缓存，避免重复解析

### 快速开始

#### 1. 配置切面

首先需要在 Spring 配置中启用 AOP 并注册 `LogKeywordAspect`：

```java
@Configuration
@EnableAspectJAutoProxy
public class LoggingConfig {
    
    @Bean
    public LogKeywordAspect logKeywordAspect() {
        return new LogKeywordAspect();
    }
}
```

#### 2. 配置日志格式

在 `logback-spring.xml` 中配置 MDC 输出：

```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [userId=%X{userId}] [orderId=%X{orderId}] - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
```

### 使用场景

#### 场景 1：单参数 - 提取对象属性

从单个对象参数中提取字段：

```java
@Service
public class UserService {
    
    @LogKeyword(keyword = "userId", spel = "#{#user.id}")
    public void processUser(User user) {
        log.info("处理用户");
        // 日志输出: ... [userId=123] - 处理用户
    }
}
```

#### 场景 2：单参数 - 提取多个字段

从同一个对象中提取多个字段：

```java
@LogKeyword(keyword = "userId", spel = "#{#user.id}")
@LogKeyword(keyword = "userName", spel = "#{#user.name}")
@LogKeyword(keyword = "userEmail", spel = "#{#user.email}")
public void updateUser(User user) {
    log.info("更新用户信息");
    // 日志输出: ... [userId=123] [userName=张三] [userEmail=zhang@example.com] - 更新用户信息
}
```

#### 场景 3：多参数 - 基本类型

直接引用多个基本类型参数：

```java
@LogKeyword(keyword = "userId", spel = "#{#userId}")
@LogKeyword(keyword = "orderId", spel = "#{#orderId}")
@LogKeyword(keyword = "action", spel = "#{#action}")
public void processOrderAction(Long userId, Long orderId, String action) {
    log.info("处理订单操作");
    // 日志输出: ... [userId=100] [orderId=200] [action=CREATE] - 处理订单操作
}
```

#### 场景 4：多参数 - 从不同对象提取

从多个对象参数中提取字段：

```java
@LogKeyword(keyword = "userId", spel = "#{#user.id}")
@LogKeyword(keyword = "userName", spel = "#{#user.name}")
@LogKeyword(keyword = "orderId", spel = "#{#order.id}")
@LogKeyword(keyword = "orderStatus", spel = "#{#order.status}")
public void updateOrder(User user, Order order) {
    log.info("更新订单");
    // 日志输出: ... [userId=123] [userName=张三] [orderId=456] [orderStatus=PENDING] - 更新订单
}
```

#### 场景 5：嵌套对象属性

访问嵌套对象的属性：

```java
@LogKeyword(keyword = "orderId", spel = "#{#order.id}")
@LogKeyword(keyword = "userId", spel = "#{#order.user.id}")
@LogKeyword(keyword = "userName", spel = "#{#order.user.name}")
public void processOrder(Order order) {
    log.info("处理订单");
    // 日志输出: ... [orderId=789] [userId=123] [userName=张三] - 处理订单
}
```

#### 场景 6：复杂 SpEL 表达式

使用 SpEL 的高级功能：

```java
// 字符串拼接
@LogKeyword(keyword = "userInfo", spel = "#{#user.id + ':' + #user.name}")
public void processUser(User user) {
    log.info("处理用户");
    // 日志输出: ... [userInfo=123:张三] - 处理用户
}

// 条件表达式
@LogKeyword(keyword = "userType", spel = "#{#user.age >= 18 ? 'adult' : 'minor'}")
public void checkUser(User user) {
    log.info("检查用户");
    // 日志输出: ... [userType=adult] - 检查用户
}

// 方法调用
@LogKeyword(keyword = "isAdmin", spel = "#{#user.roles.contains('ADMIN')}")
public void authorize(User user) {
    log.info("授权检查");
    // 日志输出: ... [isAdmin=true] - 授权检查
}

// 空值处理（Elvis 运算符）
@LogKeyword(keyword = "userName", spel = "#{#user.name ?: 'unknown'}")
public void processUser(User user) {
    log.info("处理用户");
    // 日志输出: ... [userName=unknown] - 处理用户（当 name 为 null 时）
}

// 集合操作
@LogKeyword(keyword = "roleCount", spel = "#{#user.roles.size()}")
public void checkPermissions(User user) {
    log.info("检查权限");
    // 日志输出: ... [roleCount=3] - 检查权限
}
```

#### 场景 7：不自动清理 MDC

某些场景下需要 MDC 值在方法执行后保留：

```java
@LogKeyword(keyword = "userId", spel = "#{#user.id}", clearBeforeReturn = false)
public void login(User user) {
    log.info("用户登录");
    // MDC 中的 userId 不会被清理，可以在后续的请求处理中继续使用
}

// 在请求结束时手动清理
public void logout() {
    MDC.remove("userId");
    log.info("用户登出");
}
```

### 注解参数说明

#### keyword (必填)

MDC 中的 key 名称。

```java
@LogKeyword(keyword = "userId", spel = "#{#user.id}")
```

#### spel (必填)

SpEL 表达式，用于从方法参数中提取值。

**语法规则**：
- 使用 `#{#paramName}` 引用方法参数
- 使用 `.` 访问对象属性
- 支持所有 SpEL 表达式功能

```java
// 简单引用
spel = "#{#userId}"

// 对象属性
spel = "#{#user.id}"

// 嵌套属性
spel = "#{#order.user.name}"

// 表达式运算
spel = "#{#user.id + ':' + #user.name}"

// 条件判断
spel = "#{#user.age >= 18 ? 'adult' : 'minor'}"

// 方法调用
spel = "#{#user.roles.contains('ADMIN')}"
```

#### clearBeforeReturn (可选)

是否在方法返回前清理 MDC 中的值，默认为 `true`。

```java
// 自动清理（默认）
@LogKeyword(keyword = "userId", spel = "#{#user.id}")

// 不自动清理
@LogKeyword(keyword = "userId", spel = "#{#user.id}", clearBeforeReturn = false)
```

### SpEL 表达式参考

| 功能 | SpEL 表达式 | 说明 |
|------|------------|------|
| 引用参数 | `#{#userId}` | 引用名为 userId 的参数 |
| 对象属性 | `#{#user.id}` | 访问对象的属性 |
| 嵌套属性 | `#{#order.user.name}` | 访问嵌套对象的属性 |
| 字符串拼接 | `#{#user.id + ':' + #user.name}` | 组合多个值 |
| 方法调用 | `#{#user.getName()}` | 调用对象方法 |
| 集合方法 | `#{#user.roles.contains('ADMIN')}` | 调用集合方法 |
| 集合大小 | `#{#user.roles.size()}` | 获取集合大小 |
| 条件表达式 | `#{#user.age >= 18 ? 'adult' : 'minor'}` | 三元运算符 |
| Elvis 运算符 | `#{#user.name ?: 'unknown'}` | 空值处理 |
| 安全导航 | `#{#user?.name}` | 避免空指针异常 |
| 逻辑运算 | `#{#user.age > 18 and #user.verified}` | 逻辑与 |
| 算术运算 | `#{#order.price * #order.quantity}` | 算术计算 |

### 最佳实践

#### 1. 选择合适的 MDC Key 名称

使用清晰、一致的命名规范：

```java
// ✅ 推荐：使用驼峰命名
@LogKeyword(keyword = "userId", spel = "#{#user.id}")
@LogKeyword(keyword = "orderId", spel = "#{#order.id}")

// ❌ 不推荐：使用下划线或大写
@LogKeyword(keyword = "user_id", spel = "#{#user.id}")
@LogKeyword(keyword = "ORDER_ID", spel = "#{#order.id}")
```

#### 2. 提取关键业务标识

优先提取能够唯一标识业务实体的字段：

```java
// ✅ 推荐：提取关键标识
@LogKeyword(keyword = "userId", spel = "#{#user.id}")
@LogKeyword(keyword = "orderId", spel = "#{#order.id}")
@LogKeyword(keyword = "transactionId", spel = "#{#transaction.id}")

// ❌ 不推荐：提取过多非关键信息
@LogKeyword(keyword = "userAge", spel = "#{#user.age}")
@LogKeyword(keyword = "userGender", spel = "#{#user.gender}")
```

#### 3. 避免提取敏感信息

不要将敏感信息放入 MDC：

```java
// ❌ 不推荐：提取敏感信息
@LogKeyword(keyword = "password", spel = "#{#user.password}")
@LogKeyword(keyword = "creditCard", spel = "#{#payment.creditCardNumber}")

// ✅ 推荐：只提取非敏感标识
@LogKeyword(keyword = "userId", spel = "#{#user.id}")
@LogKeyword(keyword = "paymentId", spel = "#{#payment.id}")
```

#### 4. 处理空值

使用 Elvis 运算符或安全导航避免空指针：

```java
// ✅ 推荐：使用 Elvis 运算符
@LogKeyword(keyword = "userName", spel = "#{#user.name ?: 'anonymous'}")

// ✅ 推荐：使用安全导航
@LogKeyword(keyword = "companyName", spel = "#{#user?.company?.name ?: 'N/A'}")
```

#### 5. 合理使用 clearBeforeReturn

根据业务场景选择是否自动清理：

```java
// ✅ 推荐：方法级别的临时追踪，自动清理
@LogKeyword(keyword = "orderId", spel = "#{#order.id}")
public void processOrder(Order order) {
    // 方法执行完成后自动清理
}

// ✅ 推荐：请求级别的追踪，不自动清理
@LogKeyword(keyword = "userId", spel = "#{#user.id}", clearBeforeReturn = false)
public void login(User user) {
    // 在整个请求生命周期中保留
}
```

#### 6. 性能考虑

SpEL 表达式会被自动缓存，但仍需注意：

```java
// ✅ 推荐：简单的属性访问
@LogKeyword(keyword = "userId", spel = "#{#user.id}")

// ⚠️ 注意：复杂的计算可能影响性能
@LogKeyword(keyword = "total", spel = "#{#order.items.![price * quantity].sum()}")
```

### 与其他日志追踪方案对比

#### vs 手动 MDC 管理

```java
// 传统方式：手动管理 MDC
public void processOrder(Order order) {
    try {
        MDC.put("orderId", String.valueOf(order.getId()));
        MDC.put("userId", String.valueOf(order.getUserId()));
        log.info("处理订单");
        // 业务逻辑
    } finally {
        MDC.remove("orderId");
        MDC.remove("userId");
    }
}

// 使用 @LogKeyword：自动管理
@LogKeyword(keyword = "orderId", spel = "#{#order.id}")
@LogKeyword(keyword = "userId", spel = "#{#order.userId}")
public void processOrder(Order order) {
    log.info("处理订单");
    // 业务逻辑
}
```

**优势**：
- ✅ 代码更简洁
- ✅ 自动异常安全
- ✅ 减少样板代码
- ✅ 统一管理

#### vs Filter/Interceptor

`@LogKeyword` 适合方法级别的细粒度追踪，而 Filter/Interceptor 适合请求级别的全局追踪。两者可以结合使用：

```java
// Filter：请求级别的追踪
public class RequestLoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) {
        MDC.put("traceId", generateTraceId());
        MDC.put("requestUri", request.getRequestURI());
        // ...
    }
}

// @LogKeyword：方法级别的追踪
@LogKeyword(keyword = "userId", spel = "#{#user.id}")
@LogKeyword(keyword = "orderId", spel = "#{#order.id}")
public void processOrder(User user, Order order) {
    // 日志中同时包含 traceId, requestUri, userId, orderId
}
```

### 故障排查

#### 问题 1：MDC 值为 null

**原因**：SpEL 表达式解析失败或参数为 null

**解决方案**：
```java
// 检查参数名是否正确（需要启用编译参数 -parameters）
@LogKeyword(keyword = "userId", spel = "#{#user.id}")  // 确保参数名为 user

// 使用 Elvis 运算符处理 null
@LogKeyword(keyword = "userId", spel = "#{#user?.id ?: 'unknown'}")
```

#### 问题 2：SpEL 表达式解析异常

**原因**：表达式语法错误或访问不存在的属性

**解决方案**：
```java
// 检查表达式语法
@LogKeyword(keyword = "userId", spel = "#{#user.id}")  // ✅ 正确
@LogKeyword(keyword = "userId", spel = "#{user.id}")   // ❌ 错误：缺少 #

// 使用安全导航避免空指针
@LogKeyword(keyword = "companyName", spel = "#{#user?.company?.name}")
```

#### 问题 3：MDC 没有被清理

**原因**：设置了 `clearBeforeReturn = false`

**解决方案**：
```java
// 方法级别追踪：使用默认值（自动清理）
@LogKeyword(keyword = "orderId", spel = "#{#order.id}")

// 请求级别追踪：手动清理
@LogKeyword(keyword = "userId", spel = "#{#user.id}", clearBeforeReturn = false)
public void login(User user) { }

// 在 Filter 或请求结束时清理
MDC.clear();
```

#### 问题 4：性能问题

**原因**：SpEL 表达式过于复杂

**解决方案**：
```java
// ❌ 避免：复杂的集合操作
@LogKeyword(keyword = "total", spel = "#{#order.items.![price * quantity].sum()}")

// ✅ 推荐：简单的属性访问
@LogKeyword(keyword = "orderTotal", spel = "#{#order.totalAmount}")
```

### 注意事项

1. **编译参数**：需要在 Maven/Gradle 中启用 `-parameters` 编译参数，否则无法获取参数名
   ```xml
   <plugin>
       <groupId>org.apache.maven.plugins</groupId>
       <artifactId>maven-compiler-plugin</artifactId>
       <configuration>
           <parameters>true</parameters>
       </configuration>
   </plugin>
   ```

2. **AOP 代理**：只对 Spring 管理的 Bean 生效，不支持 `private` 方法

3. **异常处理**：SpEL 解析异常不会影响业务逻辑执行，只会记录警告日志

4. **线程安全**：MDC 是 ThreadLocal 的，在使用线程池或异步调用时需要注意传递

5. **性能影响**：SpEL 表达式会被缓存，对性能影响很小，但避免在高频方法中使用过于复杂的表达式

### 完整示例

```java
@Service
@Slf4j
public class OrderService {
    
    // 创建订单：记录用户和订单信息
    @LogKeyword(keyword = "userId", spel = "#{#userId}")
    @LogKeyword(keyword = "orderId", spel = "#{#order.id}")
    @Transactional
    public Order createOrder(Long userId, Order order) {
        log.info("开始创建订单");
        // 业务逻辑
        log.info("订单创建成功");
        return order;
    }
    
    // 更新订单：记录订单和操作人信息
    @LogKeyword(keyword = "orderId", spel = "#{#order.id}")
    @LogKeyword(keyword = "operatorId", spel = "#{#operator.id}")
    @LogKeyword(keyword = "operatorName", spel = "#{#operator.name}")
    @Transactional
    public void updateOrder(Order order, User operator) {
        log.info("开始更新订单");
        // 业务逻辑
        log.info("订单更新成功");
    }
    
    // 查询订单：记录查询条件
    @LogKeyword(keyword = "userId", spel = "#{#userId}")
    @LogKeyword(keyword = "status", spel = "#{#status}")
    @LogKeyword(keyword = "startDate", spel = "#{#startDate}")
    public List<Order> queryOrders(Long userId, String status, LocalDate startDate) {
        log.info("查询订单列表");
        // 业务逻辑
        return orders;
    }
    
    // 复杂场景：组合多个信息
    @LogKeyword(keyword = "orderInfo", spel = "#{#order.id + ':' + #order.status}")
    @LogKeyword(keyword = "userInfo", spel = "#{#order.user.id + ':' + #order.user.name}")
    @LogKeyword(keyword = "hasDiscount", spel = "#{#order.discountAmount > 0}")
    public void processOrder(Order order) {
        log.info("处理订单");
        // 业务逻辑
    }
}
```

### 相关资源

- [Spring Expression Language (SpEL) 官方文档](https://docs.spring.io/spring-framework/reference/core/expressions.html)
- [SLF4J MDC 官方文档](http://www.slf4j.org/manual.html#mdc)
- [Logback 配置文档](https://logback.qos.ch/manual/configuration.html)

