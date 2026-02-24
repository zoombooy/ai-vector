package com.aimanager.qa.dto;

import lombok.Data;

@Data
public class WorkflowEdgeDTO {
    private String edgeId;
    private String sourceNodeId;
    private String targetNodeId;
    private String condition;
}

