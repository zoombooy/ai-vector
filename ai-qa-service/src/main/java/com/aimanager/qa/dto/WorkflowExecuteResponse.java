package com.aimanager.qa.dto;

import lombok.Data;
import java.util.Map;

@Data
public class WorkflowExecuteResponse {
    private String status;
    private Map<String, Object> output;
    private String errorMessage;
    private Integer duration;
    private String workflowId;
    private String workflowName;
    private String workflowVersion;
    private String sessionId;

}

