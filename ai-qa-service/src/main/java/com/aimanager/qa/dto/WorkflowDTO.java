package com.aimanager.qa.dto;

import lombok.Data;
import java.util.List;

@Data
public class WorkflowDTO {
    private Long id;
    private String workflowName;
    private String workflowCode;
    private String description;
    private String category;
    private String tags;
    private Integer status;
    private String version;
    private List<WorkflowNodeDTO> nodes;
    private List<WorkflowEdgeDTO> edges;
}

