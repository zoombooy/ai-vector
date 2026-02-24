package com.aimanager.qa.dto;

import lombok.Data;
import java.util.Map;

@Data
public class WorkflowExecuteRequest {
    private String workflowCode;
    private Map<String, Object> input;
    private String sessionId;
}

