package org.huan.demo.ai.controller;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.agent.AgentTool;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.pii.PIIDetectionHook;
import com.alibaba.cloud.ai.graph.agent.hook.pii.PIIType;
import com.alibaba.cloud.ai.graph.agent.hook.pii.RedactionStrategy;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.summarization.SummarizationHook;
import com.alibaba.cloud.ai.graph.agent.interceptor.contextediting.ContextEditingInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.todolist.TodoListInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.toolemulator.ToolEmulatorInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.toolretry.ToolRetryInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.toolselection.ToolSelectionInterceptor;
import com.alibaba.cloud.ai.graph.agent.tools.ToolContextConstants;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.skills.registry.SkillRegistry;
import com.alibaba.cloud.ai.graph.skills.registry.classpath.ClasspathSkillRegistry;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import com.alibaba.cloud.ai.graph.store.stores.MemoryStore;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.huan.demo.ai.entity.*;
import org.huan.demo.ai.hooks.LoggingHook;
import org.huan.demo.ai.hooks.LoggingModelHook;
import org.huan.demo.ai.hooks.MessageTrimmingHook;
import org.huan.demo.ai.hooks.QueryEnhancementHook;
import org.huan.demo.ai.interceptor.AnswerValidationInterceptor;
import org.huan.demo.ai.interceptor.DynamicPromptInterceptor;
import org.huan.demo.ai.interceptor.PersonalizedPromptInterceptor;
import org.huan.demo.ai.interceptor.ToolErrorInterceptor;
import org.huan.demo.ai.record.Request;
import org.huan.demo.ai.record.Response;
import org.huan.demo.ai.tools.DateTimeTool;
import org.huan.demo.ai.tools.DocumentSearchTool;
import org.huan.demo.ai.tools.SearchTool;
import org.huan.demo.ai.tools.WebSearchTool;
import org.huan.demo.ai.util.UserPreferenceStore;
import org.huan.demo.config.properties.AliProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@RestController
@RequestMapping("/ai")
public class HelloController {
    @Resource
    private AliProperties aliProperties;

    @Resource
    private ChatModel chatModel;

    @Value("${spring.ai.dashscope.chat.options.model}")
    private String modelName;

    @GetMapping("/hello")
    public Flux<String> hello(@RequestParam(name = "msg", required = false, defaultValue = "你是谁") String msg, HttpServletResponse res) {
        res.setCharacterEncoding("UTF-8");
        return chatModel.stream(msg);
    }


    @GetMapping("/agent")
    public void agent() throws GraphRunnerException {
        String instruction = """
                  你是一个经验丰富的软件架构师。
                                
                  在回答问题时，请：
                  1. 首先理解用户的核心需求
                  2. 分析可能的技术方案
                  3. 提供清晰的建议和理由
                  4. 如果需要更多信息，主动询问
                                
                  保持专业、友好的语气。
                """;
        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(aliProperties.getApiKey()).build();
        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions.builder()
                        .model(modelName)
                        .temperature(0.7)
                        .maxToken(2000)
                        .topP(0.9).build())
                .build();
        //工具
        ToolCallback searchTool = FunctionToolCallback.builder("search", new SearchTool())
                .description("搜索工具")
                .inputType(String.class)
                .build();

        ReactAgent agent = ReactAgent.builder()
                .name("my_agent")
                .model(chatModel)
                .tools(searchTool)
                .interceptors(new ToolErrorInterceptor(), new DynamicPromptInterceptor())
//                .systemPrompt("你是一个专业的技术助手。请准确、简洁地回到问题。")
                .instruction(instruction)
                .outputType(PoemOutput.class)
                .hooks(new LoggingHook(), new MessageTrimmingHook(), ModelCallLimitHook.builder().runLimit(5).build())
                .build();
        //基础调用
        AssistantMessage call = agent.call("今天天气怎么样？");
        System.out.println(call.getText());
        //获取完整状态
        Optional<OverAllState> result = agent.invoke("帮我写一首诗");
        if (result.isPresent()) {
            OverAllState state = result.get();
            //访问历史消息
            Optional<Object> messages = state.value("messages");
            List<Message> messageList = (List<Message>) messages.get();
            //访问自定义状态
            Optional<Object> customData = state.value("custom_key");
            System.out.println("完整状态：" + state);
        }
    }

    @GetMapping("/message")
    public void message() throws URISyntaxException {
        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(aliProperties.getApiKey()).build();
        ChatModel model = DashScopeChatModel
                .builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions
                        .builder()
                        .model(DashScopeModel.ChatModel.DEEPSEEK_V3_1.getValue())
                        .build())
                .build();
        //message
        SystemMessage message = new SystemMessage("你是一个又帮助的助手。");
        UserMessage userMessage = new UserMessage("你好，你好吗？");
        List<Message> messages = List.of(message, userMessage);
        Prompt prompt = new Prompt(messages);

        UserMessage x = UserMessage.builder().text("xxx")
                .metadata(Map.of(
                        "userId", "11",
                        "session_id", "sess111"
                )).build();
        UserMessage picMessage = UserMessage.builder()
                .text("描述这张图片的内容")
                .media(Media.builder()
                        .mimeType(MimeTypeUtils.IMAGE_JPEG)
                        .data(new URI("https://gips3.baidu.com/it/u=3886271102,3123389489&fm=3028&app=3028&f=JPEG&fmt=auto?w=1280&h=960")).build())
                .build();

//        ChatResponse chatResponse = model.call(prompt);
//        ChatResponseMetadata metadata = chatResponse.getMetadata();
//        if(metadata!=null && metadata.getUsage() != null){
//            System.out.println("Input tokens:"+metadata.getUsage().getPromptTokens());
//            System.out.println("Output tokens:"+metadata.getUsage().getCompletionTokens());
//            System.out.println("Total tokens:"+metadata.getUsage().getTotalTokens());
//        }
//        AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
//        if(assistantMessage.hasToolCalls()){
//            for (AssistantMessage.ToolCall toolCall : assistantMessage.getToolCalls()) {
//                System.out.println(toolCall.name());
//                System.out.println(toolCall.arguments());
//                System.out.println(toolCall.id());
//            }
//        }
//        System.out.println(assistantMessage.getText());
//        System.out.println(assistantMessage.getMedia());


    }

    private static void printResult(Object call) {
        System.out.println("-------------------------------------------------------------------------------------");
        System.out.println(call);
    }

    @GetMapping("/tool")
    public void tool() {
        String content = ChatClient.create(chatModel)
                .prompt("Can you set an alarm 10 minutes from now?")
                .tools(new DateTimeTool())
                .call()
                .content();
        System.out.println(content);

    }


    @GetMapping("/hook")
    public void hook() throws GraphRunnerException {
        /**
         *   内置hook和interceptor
         */
        //压缩消息
        SummarizationHook summarizationHook = SummarizationHook.builder()
                .model(chatModel)
                .maxTokensBeforeSummary(4000)
                .messagesToKeep(20)
                .build();
        //人机协同
        HumanInTheLoopHook humanInTheLoopHook = HumanInTheLoopHook.builder()
                .approvalOn("sendEmailTool", ToolConfig.builder().description("Please confirm sending the email").build())
                .approvalOn("deleteDataTool", "Please confirm delete the data")
                .build();
        //模型调用限制
        ModelCallLimitHook modelCallLimitHook = ModelCallLimitHook.builder().runLimit(5).build();
        //PII检测 Personally Identifiable Information
        PIIDetectionHook piiDetectionHook = PIIDetectionHook.builder()
                .piiType(PIIType.EMAIL)
                .strategy(RedactionStrategy.REDACT)
                .applyToInput(true)
                .build();
        //工具重试
        ToolRetryInterceptor.builder()
                .maxRetries(2)
                .onFailure(ToolRetryInterceptor.OnFailureBehavior.RETURN_MESSAGE)
                .build();
        //规划 planning
        TodoListInterceptor todoListInterceptor = TodoListInterceptor.builder().build();
        //LLM 工具选择
        ToolSelectionInterceptor toolSelectionInterceptor = ToolSelectionInterceptor.builder().selectionModel(chatModel).build();
        //LLM 工具模拟器
        ToolEmulatorInterceptor toolEmulatorInterceptor = ToolEmulatorInterceptor.builder().model(chatModel).build();
        //上下文编辑
        ContextEditingInterceptor contextEditingInterceptor = ContextEditingInterceptor.builder().trigger(120000).clearAtLeast(6000).build();

        /**
         * 自定义hook和interceptor
         * MessagesModelHook
         * ModelHook
         * AgentHook
         * ModelInterceptor
         * ToolInterceptor
         */
        ReactAgent agent = ReactAgent.builder()
                .name("hook-agent")
                .model(chatModel)
                .hooks(new LoggingHook(), new LoggingModelHook())
                .interceptors(todoListInterceptor)
                .build();

        AssistantMessage call = agent.call("国内最厉害的大模型是哪一个？");
        printResult(call.getText());
    }


    @GetMapping("/skill")
    public void skill() throws GraphRunnerException {

//        SkillRegistry registry = FileSystemSkillRegistry.builder()
//                .projectSkillsDirectory("D:\\workspace\\java\\demo\\src\\main\\resources\\skills")
//                .build();

        SkillRegistry registry = ClasspathSkillRegistry.builder()
                .classpathPath("skills")
                .build();

        SkillsAgentHook skillsAgentHook = SkillsAgentHook.builder()
                .skillRegistry(registry)
                .autoReload(true)
                .build();


        ReactAgent agent = ReactAgent.builder()
                .name("weather-agent")
                .model(chatModel)
                .hooks(List.of(skillsAgentHook))
                .build();

        AssistantMessage call = agent.call("介绍你有哪些技能？");
        printResult(call.getText());
    }

    @GetMapping("/context")
    public void context() throws GraphRunnerException {

        UserPreferenceStore store = new UserPreferenceStore();
        store.savePreferences("123", new UserPreferences("随和", "中文", List.of("打球", "游戏", "躺平")));

        ReactAgent agent = ReactAgent.builder()
                .name("context-agent")
                .model(chatModel)
//                .interceptors(new StateAwarePromptInterceptor())
                .interceptors(new PersonalizedPromptInterceptor(store))
                .saver(new MemorySaver())
                .build();

        RunnableConfig config = RunnableConfig.builder()
                .threadId("1")
                .addMetadata("user-id", "123")
                .build();
        agent.call("你好，我的名字是Moria", config);
        agent.call("写一首关于春天的诗", config);
        AssistantMessage message = agent.call("我的兴趣是什么", config);
        printResult(message.getText());
        AssistantMessage res = agent.call("我叫什么名字？", config);
        printResult(res.getText());
    }

    private static record ToolStringRequest(String topic) {
    }


    @GetMapping("/hitl/1")
    public void hitl() throws GraphRunnerException {
//
//        ToolCallback poetTool = FunctionToolCallback.builder("poem",  (Map<String, Object> args) -> {
//                    String requirement = (String) args.get("content");
//                    System.out.println("诗歌创作需求: " + args);
//                    return "春江潮水连海平，海上明月共潮生...";
//                })
//                .description("写诗工具")
//                .inputType(Map.class)
//                .build();

        ToolCallback poetTool = FunctionToolCallback.builder("poem", (ToolStringRequest args) -> {
                    System.out.println("诗歌创作需求: " + args);
                    return "主题：春天，语言：文言文";
                })
                .description("写诗工具")
                .inputType(ToolStringRequest.class)
                .build();


        HumanInTheLoopHook humanInTheLoopHook = HumanInTheLoopHook.builder()
                .approvalOn("poem", ToolConfig.builder().description("请确认诗歌创作操作").build())
                .build();


        ReactAgent agent = ReactAgent.builder()
                .name("hitl-agent")
                .model(chatModel)
                .tools(poetTool)
                .hooks(List.of(humanInTheLoopHook))
                .saver(new MemorySaver())
                .build();

        RunnableConfig config = RunnableConfig.builder()
                .threadId("1")
                .build();

        Optional<NodeOutput> result = agent.invokeAndGetOutput("帮我写一首100字左右的诗", config);
        if (result.isPresent() && result.get() instanceof InterruptionMetadata interruptionMetadata) {
            List<InterruptionMetadata.ToolFeedback> toolFeedbacks = interruptionMetadata.toolFeedbacks();
            for (InterruptionMetadata.ToolFeedback toolFeedback : toolFeedbacks) {
                System.out.println("工具:" + toolFeedback.getName());
                System.out.println("参数:" + toolFeedback.getArguments());
                System.out.println("描述:" + toolFeedback.getDescription());
            }
            //模拟人工决策
            InterruptionMetadata.Builder feedbackBuilder = InterruptionMetadata.builder()
                    .nodeId(interruptionMetadata.node())
                    .state(interruptionMetadata.state());
            toolFeedbacks.forEach(toolFeedback -> {
                InterruptionMetadata.ToolFeedback approvedFeedback = InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                        .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED)
                        .build();
                feedbackBuilder.addToolFeedback(approvedFeedback);
            });

            InterruptionMetadata approvedMetadata = feedbackBuilder.build();

            //恢复执行
            System.out.println("--------第二次调用，恢复执行------------");
            RunnableConfig resumeConfig = RunnableConfig.builder()
                    .threadId("1")
                    .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, approvedMetadata)
                    .build();
            Optional<NodeOutput> finalResult = agent.invokeAndGetOutput("", resumeConfig);
            if (finalResult.isPresent()) {
                System.out.println("执行完成");
                System.out.println("最终结果：" + finalResult.get());
                System.out.println("\n\n");
                Optional<List<Message>> messages = finalResult.get().state().value("messages");
                if (messages.isPresent()) {
                    List<Message> messageList = (List<Message>) messages.get();
                    messageList.forEach(x -> {
                        if (x instanceof UserMessage userMessage) {
                            System.out.println("-------------");
                            System.out.println("user message: " + userMessage.getText());
                        } else if (x instanceof AssistantMessage assistantMessage) {
                            System.out.println("-------------");
                            System.out.println("assistant message: " + assistantMessage.getText());
                        }
                    });

                }
            }

        }

    }

    static class PreprocessorNode implements NodeAction {
        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            String input = state.value("input", "");
            System.out.println("PreprocessorNode input:" + input);
            String cleaned = input.trim();
            return Map.of("cleaned_input", cleaned);
        }
    }

    static class ValidatorNode implements NodeAction {
        @Override
        public Map<String, Object> apply(OverAllState state) throws Exception {
            Optional<Object> qaResultOpt = state.value("qa_result");
            if (qaResultOpt.isPresent() && qaResultOpt.get() instanceof Message message) {
                System.out.println("ValidatorNode qa_result：" + message.getText());
                boolean isValid = message.getText().length() > 100;
                return Map.of("is_valid", isValid);
            }
            return Map.of("is_valid", false);
        }
    }

    @GetMapping("/hitl/2")
    public void hitl2() throws GraphStateException {
        //创建工具
        FunctionToolCallback<ToolStringRequest, String> searchTool = FunctionToolCallback.builder("search", (ToolStringRequest args) -> {
                    System.out.println("--------调用工具-----：" + args);
                    return "搜索结果：AI Agent 是能够感知环境、自主决策并采取行动的智能系统。";
                }).description("搜索工具，用于查找相关信息")
                .inputType(ToolStringRequest.class)
                .build();
        //保存器
        MemorySaver saver = new MemorySaver();
        //agent
        ReactAgent agent = ReactAgent.builder()
                .name("qa_agent")
                .model(chatModel)
                .instruction("你是一个问答专家，负责回到用户的问题。如果需要搜索信息，请使用search工具。\n\n 用户问题：{cleaned_input}")
                .outputKey("qa_result")
                .saver(saver)
                .hooks(HumanInTheLoopHook.builder()
                        .approvalOn("search", ToolConfig.builder().description("搜索操作需要人工审批，请确认是否执行搜索").build())
                        .build()
                )
                .tools(searchTool)
                .build();
        //定义状态管理策略
        KeyStrategyFactory keyStrategyFactory = () -> {
            Map<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put("input", new ReplaceStrategy());
            strategies.put("cleaned_input", new ReplaceStrategy());
            strategies.put("qa_result", new ReplaceStrategy());
            strategies.put("is_valid", new ReplaceStrategy());
            return strategies;
        };
        //构建工作流
        StateGraph workflow = new StateGraph(keyStrategyFactory);
        //添加普通node
        workflow.addNode("preprocess", AsyncNodeAction.node_async(new PreprocessorNode()));
        workflow.addNode("validate", AsyncNodeAction.node_async(new ValidatorNode()));
        //添加agent node
        workflow.addNode(agent.name(), agent.asNode(true, false));
        //定义流程
        workflow.addEdge(StateGraph.START, "preprocess");
        workflow.addEdge("preprocess", agent.name());
        workflow.addEdge(agent.name(), "validate");

        //条件边 通过则结束 否则重新处理
        workflow.addConditionalEdges("validate", AsyncEdgeAction.edge_async((state -> {
            Boolean isValid = state.value("is_valid", false);
            return isValid ? "end" : agent.name();
        })), Map.of(
                "end", StateGraph.END,
                agent.name(), agent.name()
        ));

        //编译工作流
        CompiledGraph compiledGraph = workflow.compile(
                CompileConfig.builder()
                        .saverConfig(SaverConfig.builder().register(saver).build()).build()
        );
        //执行工作流 处理中断
        String threadId = "hitl2-22211113333";
        Map<String, Object> input = Map.of("input", "请搜索下量子计算的基本原理");

        Optional<NodeOutput> nodeOutputOptional = compiledGraph.invokeAndGetOutput(input, RunnableConfig.builder().threadId(threadId).build());

        if (nodeOutputOptional.isPresent() && nodeOutputOptional.get() instanceof InterruptionMetadata interruptionMetadata) {
            System.out.println("----- 工作流被中断，等待人工审核。-----");
            System.out.println("----- 中断节点：" + interruptionMetadata.node());

            List<InterruptionMetadata.ToolFeedback> feedbacks = interruptionMetadata.toolFeedbacks();
            for (InterruptionMetadata.ToolFeedback feedback : feedbacks) {
                System.out.println("工具名称：" + feedback.getName());
                System.out.println("工具参数：" + feedback.getArguments());
                System.out.println("工具描述：" + feedback.getDescription());
            }
            //构建人工反馈
            InterruptionMetadata.Builder feedBackBuilder = InterruptionMetadata.builder()
                    .nodeId(interruptionMetadata.node())
                    .state(interruptionMetadata.state());
            feedbacks.forEach(toolFeedback -> {
                feedBackBuilder.addToolFeedback(InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                        .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED).build());
            });
            InterruptionMetadata approvalMetadata = feedBackBuilder.build();
            //使用批准策略恢复执行
            RunnableConfig runnableConfig = RunnableConfig.builder()
                    .threadId(threadId)
                    .addHumanFeedback(approvalMetadata)
                    .build();

            nodeOutputOptional = compiledGraph.invokeAndGetOutput(Map.of(), runnableConfig);
            if (nodeOutputOptional.isPresent()) {
                List<Message> messages = nodeOutputOptional.get().state().value("messages", List.of());
                messages.forEach(x -> {
                    if (x instanceof UserMessage userMessage) {
                        System.out.println("-------------");
                        System.out.println("user message: " + userMessage.getText());
                    } else if (x instanceof AssistantMessage assistantMessage) {
                        System.out.println("-------------");
                        System.out.println("assistant message: " + assistantMessage.getText());
                    }
                });
            }
        }
        System.out.println("-------结束调用----------");
    }

    private record GetMemoryRequest(List<String> namespace, String key) {
    }

    private record SaveMemoryRequest(List<String> namespace, String key, Map<String, Object> value) {
    }

    private record MemoryResponse(String message, Map<String, Object> value) {
    }

    @GetMapping("/memory")
    public void memory() throws GraphRunnerException {

        MemoryStore store = new MemoryStore();

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", "张三");
        userData.put("language", "中文");

        StoreItem userItem = StoreItem.of(List.of("users"), "user_123", userData);
        store.putItem(userItem);

        //获取用户信息的工具
        BiFunction<GetMemoryRequest, ToolContext, MemoryResponse> getUserInfoFunction = (request, context) -> {
            RunnableConfig config = (RunnableConfig) context.getContext().get(ToolContextConstants.AGENT_CONFIG_CONTEXT_KEY);
            Store _store = config.store();
            Optional<StoreItem> itemOpt = _store.getItem(request.namespace(), request.key());
            if (itemOpt.isPresent()) {
                Map<String, Object> value = itemOpt.get().getValue();
                return new MemoryResponse("找到用户信息", value);
            }
            return new MemoryResponse("未找到用户", Map.of());
        };

        FunctionToolCallback<GetMemoryRequest, MemoryResponse> getUserInfoTool = FunctionToolCallback.builder("getUserInfo", getUserInfoFunction)
                .description("查询用户信息")
                .inputType(GetMemoryRequest.class)
                .build();


        //保存用户的工具
        BiFunction<SaveMemoryRequest, ToolContext, MemoryResponse> saveUserInfoFunction = (request, context) -> {
            RunnableConfig config = (RunnableConfig) context.getContext().get(ToolContextConstants.AGENT_CONFIG_CONTEXT_KEY);
            Store _store = config.store();
            StoreItem storeItem = StoreItem.of(request.namespace(), request.key(), request.value());
            _store.putItem(storeItem);
            return new MemoryResponse("用户信息保存成功", request.value());
        };

        FunctionToolCallback<SaveMemoryRequest, MemoryResponse> saveUserInfoTool = FunctionToolCallback.builder("saveUserInfo", saveUserInfoFunction)
                .description("保存用户信息")
                .inputType(SaveMemoryRequest.class)
                .build();


        ReactAgent agent = ReactAgent.builder()
                .name("memory-agent")
                .model(chatModel)
                .tools(getUserInfoTool, saveUserInfoTool)
                .saver(new MemorySaver())
                .interceptors(new ToolErrorInterceptor())
                .build();

        RunnableConfig config = RunnableConfig.builder()
                .threadId("1")
                .store(store)
                .addMetadata("user_id", "user_123")
                .build();

        Optional<OverAllState> invokeResult = agent.invoke("查询用户信息，namespace=['users'], key='user_123'", config);
        printInvokeResult(invokeResult);
        System.out.println("-------------------------666666666-------------------");
        Optional<OverAllState> saveResult = agent.invoke("我叫李四，请保存我的信息。使用saveUserInfo工具，namespace=['users']，key='user_666'，value={'name': '李四', 'language': 'english'}，并确保调用工具时参数是json格式", config);
        printInvokeResult(saveResult);
        Optional<StoreItem> savedItem = store.getItem(List.of("users"), "user_666");
        savedItem.ifPresent(storeItem -> System.out.println(storeItem.getValue()));

    }


    private void printInvokeResult(Optional<OverAllState> invokeResult) {
        if (invokeResult.isPresent()) {
            List<Message> messages = invokeResult.get().value("messages", List.of());
            System.out.println("---------------------------------------------------");
            for (Message message : messages) {
                if (message instanceof UserMessage userMessage) {
                    System.out.println("user message: " + userMessage.getText());
                } else if (message instanceof AssistantMessage assistantMessage) {
                    System.out.println("assistant message: " + assistantMessage.getText());
                }
            }
            System.out.println("---------------------------------------------------");
        } else {
            System.out.println("--------------invoke 没有结果-----------");
        }
        System.out.println("~~~~~~~~~~~~调用完成~~~~~~~~~~~~~~~~");
    }

    @GetMapping("/multiAgent")
    public void multiAgent() throws GraphRunnerException {

        ReactAgent webResearchAgent = ReactAgent.builder()
                .name("web_research")
                .model(chatModel)
                .description("从互联网搜索信息")
                .instruction("请搜索并收集关于以下主题的信息：{input}")
                .outputKey("web_data")
                .build();
        ReactAgent dbResearchAgent = ReactAgent.builder()
                .name("db_research")
                .model(chatModel)
                .description("从数据库查询信息")
                .instruction("请从数据库中查询并收集关于以下主题的消息：{input}")
                .outputKey("db_data")
                .build();

        ParallelAgent researchAgent = ParallelAgent.builder()
                .name("parallel_research")
                .description("并行收集多个数据源的信息")
                .subAgents(List.of(webResearchAgent, dbResearchAgent))
                .mergeOutputKey("research_data")
                .build();

        ReactAgent analysisAgent = ReactAgent.builder()
                .name("analysis_agent")
                .model(chatModel)
                .description("分析研究数据")
                .instruction("请分析以下收集到的数据并提供见解：{research_data}")
                .outputKey("analysis_result")
                .build();
        ReactAgent pdfReportAgent = ReactAgent.builder()
                .name("pdf_agent")
                .model(chatModel)
                .description("生成PDF格式报告")
                .instruction("""
                        请根据研究结果和分析结果生成一份PDF格式的报告。
                                      
                        研究结果：{research_data}
                        分析结果：{analysis_result}
                        """)
                .outputKey("pdf_report")
                .build();
        ReactAgent htmlReportAgent = ReactAgent.builder()
                .name("html_report")
                .model(chatModel)
                .description("生成HTML格式报告")
                .instruction("""
                        请根据研究结果和分析结果生成一份HTML格式的报告。
                                      
                        研究结果：{research_data}
                        分析结果：{analysis_result}
                        """)
                .outputKey("html_report")
                .build();
        LlmRoutingAgent reportAgent = LlmRoutingAgent.builder()
                .name("report_router")
                .description("根据需求选择报告格式")
                .model(chatModel)
                .subAgents(List.of(pdfReportAgent, htmlReportAgent))
                .build();
        SequentialAgent hybridWorkflow = SequentialAgent.builder()
                .name("research_workflow")
                .description("安装的研究工作流：并行收集 -> 分析 -> 路由生成报告")
                .subAgents(List.of(researchAgent, analysisAgent, reportAgent))
                .build();
        Optional<OverAllState> result = hybridWorkflow.invoke("研究AI技术趋势并生成HTML报告");
    }

    @GetMapping("/agentTool")
    public void agentTool() throws GraphRunnerException {

        ReactAgent writerAgent = ReactAgent.builder()
                .name("full_typed_writer")
                .model(chatModel)
                .description("完整类型化的写作工具")
                .instruction("根据结构化输入（topic,wordCount,style）创作文章，并结构化输出（title,content,characterCount）")
                .inputType(ArticleRequest.class)
                .outputType(ArticleOutPut.class)
                .build();

        ReactAgent reviewerAgent = ReactAgent.builder()
                .name("typed_reviewer")
                .model(chatModel)
                .description("完整类型化的评审工具")
                .instruction("对文章进行评审，返回评审意见（comment，approved，suggestions）")
                .outputType(ReviewOutPut.class)
                .build();

        ReactAgent orchestratorAgent = ReactAgent.builder()
                .name("orchestrator")
                .model(chatModel)
                .instruction("协调写作和评审流程。先调用写作工具创作文章，然后调用评审工具进行评审。")
                .tools(
                        AgentTool.getFunctionToolCallback(writerAgent),
                        AgentTool.getFunctionToolCallback(reviewerAgent)
                ).build();
        Optional<OverAllState> invoke = orchestratorAgent.invoke("请写一篇关于友谊的散文，约200字，需要评审");
        System.out.println(invoke);
        printInvokeResult(invoke);
    }

    @GetMapping("/rag")
    public void rag() {
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(aliProperties.getApiKey()).build();
        ChatModel model = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions.builder().model(modelName).build())
                .build();
        DocumentSearchTool documentSearchTool = new DocumentSearchTool(SimpleVectorStore.builder(DashScopeEmbeddingModel.builder().dashScopeApi(dashScopeApi).build()).build());
        WebSearchTool webSearchTool = new WebSearchTool();

        ToolCallback documentSearchCallback = FunctionToolCallback
                .builder("document_search", (Function<Request, Response>) documentSearchTool::search)
                .description("从文档库中搜索相关信息")
                .inputType(Request.class).build();

        ToolCallback webSearchCallback = FunctionToolCallback.builder("web_search", (Function<Request, Response>) webSearchTool::search)
                .description("从互联网搜索最新信息")
                .inputType(Request.class)
                .build();

        ReactAgent agent = ReactAgent.builder()
                .name("hybrid_rag_agent")
                .model(model)
                .instruction("""
                        你是一个智能助手，可以访问多个信息源来回答问题。
                              
                        使用工具时：
                        1. 优先使用 document_search 搜索文档库
                        2. 如果需要最新信息，使用 web_search
                        3. 基于检索到的信息生成准确、完整的答案
                        4. 如果信息不足，可以多次调用工具
                        """)
                .tools(documentSearchCallback, webSearchCallback)
                .hooks(new QueryEnhancementHook(chatModel))
                .interceptors(new AnswerValidationInterceptor(chatModel))
                .build();
    }

    @Resource(name = "dataAnalysisAgent")
    private ReactAgent localDataAnalysisAgent;


    @GetMapping("/a2a")
    private String a2a() throws GraphRunnerException {
        System.out.println("------------start a2a");
//        AssistantMessage call = localDataAnalysisAgent.call("用200个字分析总结这个月全国新能源汽车销售数据");
        Optional<OverAllState> result = localDataAnalysisAgent.invoke("用200个字分析总结这个月全国新能源汽车销售数据");
        result.ifPresent(state-> System.out.println("-----------finish a2a"));
        printInvokeResult(result);

        if (result.isPresent()) {
            List<Message> messages = result.get().value("messages", List.of());
            Optional<String> reduce = messages.stream().filter(msg -> msg instanceof AssistantMessage)
                    .map(msg -> ((AssistantMessage) msg).getText())
                    .reduce((first, second) -> second);
            if(reduce.isPresent()){
                return reduce.get();
            }
        }
        return "失败";
    }


}
