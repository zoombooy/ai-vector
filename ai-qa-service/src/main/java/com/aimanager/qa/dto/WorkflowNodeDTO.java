package com.aimanager.qa.dto;

import lombok.Data;
import java.util.Map;

@Data
public class WorkflowNodeDTO {
    private String nodeId;
    private String nodeName;
    private String nodeType;
    private Integer positionX;
    private Integer positionY;
    private Map<String, Object> config;
}

