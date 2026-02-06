# 调试日志清理说明

## 临时调试日志位置

为了定位 400 BAD_REQUEST 错误,在 `ToolAdvisor.java` 中添加了临时调试日志。

### 需要删除的代码块

所有调试代码都标记为 `[DEBUG-START]` 和 `[DEBUG-END]`,方便查找和删除。

#### 文件: `src/main/java/com/ldfd/ragdoc/infrastructure/advisor/ToolAdvisor.java`

**位置 1: doBeforeCall 方法 (约 207-218 行)**
```java
// [DEBUG-START] 临时调试日志 - 定位400错误
System.out.println("========== [DEBUG] Before Call Request ==========");
System.out.println("Prompt Instructions Count: " + chatClientRequest.prompt().getInstructions().size());
chatClientRequest.prompt().getInstructions().forEach(msg -> 
    System.out.println("  - Message Type: " + msg.getMessageType())
);
if (chatClientRequest.prompt().getOptions() instanceof ToolCallingChatOptions options) {
    System.out.println("Options Type: " + options.getClass().getSimpleName());
}
System.out.println("=================================================");
// [DEBUG-END]
```

**位置 2: adviseCall 方法中的异常捕获 (约 131-145 行)**
```java
// [DEBUG-START] 临时调试日志 - 捕获API调用异常
try {
    chatClientResponse = callAdvisorChain.copy(this).nextCall(processedChatClientRequest);
} catch (Exception e) {
    System.err.println("========== [DEBUG] API Call Failed ==========");
    System.err.println("Exception Type: " + e.getClass().getName());
    System.err.println("Error Message: " + e.getMessage());
    e.printStackTrace();
    System.err.println("=============================================");
    throw e;
}
// [DEBUG-END]
```
恢复为:
```java
chatClientResponse = callAdvisorChain.copy(this).nextCall(processedChatClientRequest);
```

**位置 3: doAfterCall 方法 (约 222-233 行)**
```java
// [DEBUG-START] 临时调试日志 - 查看响应状态
System.out.println("========== [DEBUG] After Call Response ==========");
if (chatClientResponse != null && chatClientResponse.chatResponse() != null) {
    System.out.println("Has Tool Calls: " + chatClientResponse.chatResponse().hasToolCalls());
    System.out.println("Response: " + chatClientResponse.chatResponse());
} else {
    System.out.println("Response is null");
}
System.out.println("=================================================");
// [DEBUG-END]
```

**位置 4: doBeforeStream 方法 (约 396-407 行)**
```java
// [DEBUG-START] 临时调试日志 - 定位Stream 400错误
System.out.println("========== [DEBUG] Before Stream Request ==========");
System.out.println("Prompt Instructions Count: " + chatClientRequest.prompt().getInstructions().size());
chatClientRequest.prompt().getInstructions().forEach(msg -> 
    System.out.println("  - Message Type: " + msg.getMessageType())
);
if (chatClientRequest.prompt().getOptions() instanceof ToolCallingChatOptions options) {
    System.out.println("Options Type: " + options.getClass().getSimpleName());
}
System.out.println("===================================================");
// [DEBUG-END]
```

## 快速清理方法

### 方法 1: 使用 IDE 搜索替换
1. 打开 `ToolAdvisor.java`
2. 使用正则表达式搜索: `\s*// \[DEBUG-START\][\s\S]*?// \[DEBUG-END\]\n`
3. 全部替换为空

### 方法 2: 手动删除
搜索文件中的 `[DEBUG-START]` 和 `[DEBUG-END]` 标记,删除这两个标记之间的所有内容(包括标记本身)。

### 方法 3: 使用 grep 查找
```bash
grep -n "DEBUG-START\|DEBUG-END" src/main/java/com/ldfd/ragdoc/infrastructure/advisor/ToolAdvisor.java
```

## 恢复原始方法

删除调试日志后,确保方法恢复为:

```java
protected ChatClientRequest doBeforeCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
    return chatClientRequest;
}

protected ChatClientResponse doAfterCall(ChatClientResponse chatClientResponse, CallAdvisorChain callAdvisorChain) {
    return chatClientResponse;
}

protected ChatClientRequest doBeforeStream(ChatClientRequest chatClientRequest,
                                           StreamAdvisorChain streamAdvisorChain) {
    return chatClientRequest;
}

// adviseCall 方法中的调用恢复为:
chatClientResponse = callAdvisorChain.copy(this).nextCall(processedChatClientRequest);
```

## 验证

删除后运行以下命令确保没有遗留:
```bash
grep -r "DEBUG-START\|DEBUG-END" src/
```

应该没有任何输出。
