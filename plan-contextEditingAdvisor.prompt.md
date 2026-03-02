## Plan: Create ContextEditingAdvisor for Token Management

Create a Spring AI advisor that mirrors Anthropic's context editing capability by clearing older tool results when conversation token count exceeds a configurable threshold. This advisor intercepts requests, estimates token usage, and replaces older tool-related messages with placeholder text to stay within limits.

### Steps

1. **Create `TokenCounter` interface in [`/infrastructure/advisor/`](e:\Github\ragdoc\src\main\java\com\ldfd\ragdoc\infrastructure\advisor\)**: Define a functional interface for token counting with a default approximate implementation based on character/word estimation (similar to Spring AI Alibaba's `TokenCounter.approximateMsgCounter()`).

2. **Create `ContextEditingAdvisor.java` implementing `CallAdvisor` and `StreamAdvisor`**: Adapt the Alibaba `ContextEditingInterceptor` logic to Spring AI's advisor pattern, using `ChatClientRequest.builder()` to rebuild requests with edited messages instead of the `ModelRequest` wrapper.

3. **Implement `adviseCall()` method**: Before chaining, count tokens via `TokenCounter`, find clearable `ToolResponseMessage` and `AssistantMessage` (with tool calls), replace older responses with `placeholder` text while respecting `keep`, `excludeTools`, and `clearAtLeast` config.

4. **Implement `adviseStream()` method**: Apply the same context editing logic to streaming requests before delegating to `StreamAdvisorChain.nextStream()`.

5. **Add Builder pattern for configuration**: Include fields: `trigger` (token threshold), `clearAtLeast`, `keep` (recent tool messages to preserve), `excludeTools`, `placeholder`, and `TokenCounter` instance.

### Further Considerations

1. **Token estimation strategy?** Approximate (char-based, ~4 chars = 1 token) is simplest; alternatively integrate a tiktoken-style library for accuracy.
2. **Register as Spring bean?** Use `@Component` with builder injection, or keep as POJO built via `ContextEditingAdvisor.builder()` for manual configuration. / Option A: POJO builder / Option B: `@Component` with `@ConfigurationProperties`
3. **Order placement?** Should run early (like `ChatHistoryAdvisor` at `HIGHEST_PRECEDENCE`) to edit context before other advisors process it.

