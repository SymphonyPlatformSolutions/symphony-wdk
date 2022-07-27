package com.symphony.bdk.workflow.engine;

import static org.assertj.core.api.BDDAssertions.then;

import com.symphony.bdk.core.service.session.SessionService;
import com.symphony.bdk.workflow.engine.camunda.WorkflowEventToCamundaEvent;
import com.symphony.bdk.workflow.swadl.SwadlParser;
import com.symphony.bdk.workflow.swadl.v1.Workflow;

import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowDirectGraphBuilderTest {
  @Mock
  SessionService sessionService;
  @Mock
  RuntimeService runtimeService;
  @InjectMocks
  WorkflowEventToCamundaEvent eventMapper;

  WorkflowDirectGraphBuilder workflowDirectGraphBuilder;

  @Test
  @DisplayName("Build approval workflow into a direct graph")
  void buildWorkflowDirectGraph_approvalFlow() throws Exception {
    Workflow workflow = SwadlParser.fromYaml(getClass().getResourceAsStream("/complex/approval.swadl.yaml"));
    workflowDirectGraphBuilder = new WorkflowDirectGraphBuilder(workflow, eventMapper);
    WorkflowDirectGraph directGraph = workflowDirectGraphBuilder.build();
    then(directGraph.getDictionary()).hasSize(8);
    then(directGraph.getStartEvents()).hasSize(1);
  }

  @Test
  @DisplayName("Build group workflow into a direct graph")
  void buildWorkflowDirectGraph_groupFlow() throws Exception {
    Workflow workflow = SwadlParser.fromYaml(getClass().getResourceAsStream("/complex/groups.swadl.yaml"));
    workflowDirectGraphBuilder = new WorkflowDirectGraphBuilder(workflow, eventMapper);
    WorkflowDirectGraph directGraph = workflowDirectGraphBuilder.build();
    then(directGraph.getDictionary()).hasSize(7);
    then(directGraph.getStartEvents()).hasSize(1);
  }

  @Test
  @DisplayName("Build connection workflow into a direct graph")
  void buildWorkflowDirectGraph_connectionFlow() throws Exception {
    Workflow workflow =
        SwadlParser.fromYaml(getClass().getResourceAsStream("/complex/connection-admin-approval.swadl.yaml"));
    workflowDirectGraphBuilder = new WorkflowDirectGraphBuilder(workflow, eventMapper);
    WorkflowDirectGraph directGraph = workflowDirectGraphBuilder.build();
    then(directGraph.getDictionary()).hasSize(8);
    then(directGraph.getStartEvents()).hasSize(1);
  }
}