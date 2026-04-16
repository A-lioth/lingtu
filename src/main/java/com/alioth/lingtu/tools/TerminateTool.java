package com.alioth.lingtu.tools;

import org.springframework.ai.tool.annotation.Tool;

/**
 * 终止工具
 *
 * @author alioth
 */
public class TerminateTool {
    @Tool(description = "End the agent turn when the user's request is fully satisfied (including after a single tool already returned the answer), " +
            "or when you cannot proceed. Prefer answering in plain text without tools when possible; use this tool to stop the step loop after you are done.")
    public String doTerminate(String conversationId) {
        return "terminate the task!!";
    }
}
