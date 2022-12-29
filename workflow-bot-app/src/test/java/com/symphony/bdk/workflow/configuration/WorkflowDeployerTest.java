package com.symphony.bdk.workflow.configuration;


import com.symphony.bdk.workflow.engine.WorkflowEngine;
import com.symphony.bdk.workflow.exception.NotFoundException;
import com.symphony.bdk.workflow.swadl.v1.Workflow;
import com.symphony.bdk.workflow.versioning.model.VersionedWorkflow;
import com.symphony.bdk.workflow.versioning.service.VersioningService;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
public class WorkflowDeployerTest {

  @Mock
  WorkflowEngine workflowEngine;

  @Mock
  VersioningService versioningService;

  @InjectMocks
  WorkflowDeployer workflowDeployer;

  @Test
  void testWorkflowNotExists() {
    when(versioningService.find("id")).thenReturn(Collections.emptyList());
    boolean exist = workflowDeployer.workflowExist("id");
    assertThat(exist).isFalse();
  }

  @Test
  void testWorkflowExists() {
    when(versioningService.find("id")).thenReturn(Collections.singletonList(new VersionedWorkflow()));
    boolean exist = workflowDeployer.workflowExist("id");
    assertThat(exist).isTrue();
  }

  @Test
  void testWorkflowNotExistsWithIdAndVersion() {
    when(versioningService.find("id", "v1")).thenReturn(Optional.empty());
    boolean exist = workflowDeployer.workflowExist("id", "v1");
    assertThat(exist).isFalse();
  }

  @Test
  void testWorkflowExistsWithIdAndVersion() {
    when(versioningService.find("id", "v1")).thenReturn(Optional.of(new VersionedWorkflow()));
    boolean exist = workflowDeployer.workflowExist("id", "v1");
    assertThat(exist).isTrue();
  }

  @Test
  void testWorkflowSwadlPath() {
    final String path1 = "/path/to/swadl/id/v1";
    final String path2 = "/path/to/swadl/id/v2";
    when(versioningService.find("id")).thenReturn(
            Arrays.asList(new VersionedWorkflow().setVersionedWorkflowId("id", "v0").setPath(path1),
                    new VersionedWorkflow().setVersionedWorkflowId("id", "v1").setPath(path2)));
    List<Path> paths = workflowDeployer.workflowSwadlPath("id");
    assertThat(paths).hasSize(2);
    assertThat(paths).containsExactlyInAnyOrder(Path.of(path1), Path.of(path2));
  }

  @Test
  void testWorkflowSwadlPathEmpty() {
    when(versioningService.find("id", "v1")).thenReturn(Optional.empty());

    assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> workflowDeployer.workflowSwadlPath("id", "v1"))
            .satisfies(e -> assertThat(e.getMessage()).isEqualTo("Version v1 of the workflow id does not exist"));
  }

  @Test
  void testWorkflowSwadlPathWithIdAndVersion() {
    when(versioningService.find("id", "v1")).thenReturn(Optional.of(new VersionedWorkflow().setPath("/path/to/swadl")));
    Path path = workflowDeployer.workflowSwadlPath("id", "v1");
    assertThat(path.toString()).isEqualTo("/path/to/swadl");
  }

  @Test
  void testWorkflowSwadlPathWithIdAndVersionEmpty() {
    when(versioningService.find("id", "v1")).thenReturn(Optional.empty());

    assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> workflowDeployer.workflowSwadlPath("id", "v1"))
            .satisfies(e -> assertThat(e.getMessage()).isEqualTo("Version v1 of the workflow id does not exist"));
  }

  @Test
  void testIsPathAlreadyExist() {
    when(versioningService.find(any(Path.class))).thenReturn(Optional.of(new VersionedWorkflow()));
    boolean pathAlreadyExist = workflowDeployer.isPathAlreadyExist(Path.of(""));
    assertThat(pathAlreadyExist).isTrue();
  }

  @Test
  void testIsPathAlreadyNotExist() {
    when(versioningService.find(any(Path.class))).thenReturn(Optional.empty());
    boolean pathAlreadyExist = workflowDeployer.isPathAlreadyExist(Path.of(""));
    assertThat(pathAlreadyExist).isFalse();
  }

  @Test
  void testAddAllWorkflowsFromFolder() {
    BpmnModelInstance mockInstance = mock(BpmnModelInstance.class);

    when(workflowEngine.parseAndValidate(any(Workflow.class))).thenReturn(mockInstance);
    doNothing().when(workflowEngine).deploy(any(Workflow.class), eq(mockInstance));
    doNothing().when(versioningService).save(any(), any(), any(), any());

    workflowDeployer.addAllWorkflowsFromFolder(Path.of("src/test/resources/basic/publish"));

    verify(versioningService).save(eq("basic-workflow"), eq(""), any(String.class), any(String.class));
    verify(workflowEngine).deploy(any(Workflow.class), eq(mockInstance));
  }

  @Test
  void testAddAllWorkflowsFromFolderException() {
    String file = "src/test/resources/basic/publish/basic-workflow.swadl.yaml";
    assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> workflowDeployer.addAllWorkflowsFromFolder(Path.of(file)))
            .satisfies(
                    e -> assertThat(e.getMessage()).isEqualTo("Could not find workflows folder to monitor with path: " + file));
  }

  @Test
  void testAddWorkflowDraft() throws IOException, ProcessingException {
    BpmnModelInstance mockInstance = mock(BpmnModelInstance.class);
    String workflowFile = "src/test/resources/basic/draft/basic-draft-workflow.swadl.yaml";
    Path path = Path.of(workflowFile);
    VersionedWorkflow versionedWorkflow = new VersionedWorkflow()
            .setVersionedWorkflowId("basic-draft-workflow", "")
            .setPath(workflowFile)
            .setIsToPublish(true);

    when(workflowEngine.parseAndValidate(any(Workflow.class))).thenReturn(mockInstance);
    when(versioningService.find(eq(path))).thenReturn(Optional.of(versionedWorkflow));

    workflowDeployer.addWorkflow(path);

    verify(versioningService).find(eq(path));
    verify(workflowEngine, never()).deploy(any(Workflow.class), any(BpmnModelInstance.class));
    verify(workflowEngine).undeploy(eq("basic-draft-workflow"));
  }

  @ParameterizedTest
  @CsvSource({"ENTRY_CREATE", "ENTRY_MODIFY"})
  void testHandleFileEventCreate(String kind) throws IOException, ProcessingException {
    WatchEvent event;
    if (kind.equals("ENTRY_CREATE")) {
      event = new WatchEvent(StandardWatchEventKinds.ENTRY_CREATE);
    } else {
      event = new WatchEvent(StandardWatchEventKinds.ENTRY_MODIFY);
    }
    String workflowFile = "src/test/resources/basic/publish/basic-workflow.swadl.yaml";
    Path path = Path.of(workflowFile);
    BpmnModelInstance mockInstance = mock(BpmnModelInstance.class);
    VersionedWorkflow versionedWorkflow = new VersionedWorkflow()
            .setVersionedWorkflowId("basic-workflow", "")
            .setPath(workflowFile)
            .setIsToPublish(true);

    when(workflowEngine.parseAndValidate(any(Workflow.class))).thenReturn(mockInstance);
    doNothing().when(workflowEngine).deploy(any(Workflow.class), eq(mockInstance));
    when(versioningService.find(eq(path))).thenReturn(Optional.of(versionedWorkflow));
    doNothing().when(versioningService).save(any(), any(), any(), any());

    workflowDeployer.handleFileEvent(path, event);

    verify(versioningService).save(eq("basic-workflow"), eq(""), any(String.class), any(String.class));
    verify(workflowEngine).deploy(any(Workflow.class), eq(mockInstance));
    verify(workflowEngine, never()).undeploy(any());
  }

  @Test
  void testHandleFileEventModify() throws IOException, ProcessingException {
    String workflowFile = "src/test/resources/basic/publish/basic-workflow.swadl.yaml";
    Path path = Path.of(workflowFile);
    BpmnModelInstance mockInstance = mock(BpmnModelInstance.class);
    VersionedWorkflow versionedWorkflow = new VersionedWorkflow()
            .setVersionedWorkflowId("basic-workflow", "")
            .setPath(workflowFile)
            .setIsToPublish(true);

    when(workflowEngine.parseAndValidate(any(Workflow.class))).thenReturn(mockInstance);
    doNothing().when(workflowEngine).deploy(any(Workflow.class), eq(mockInstance));
    when(versioningService.find(eq(path))).thenReturn(Optional.of(versionedWorkflow));
    doNothing().when(versioningService).save(any(), any(), any(), any());

    workflowDeployer.handleFileEvent(path, new WatchEvent(StandardWatchEventKinds.ENTRY_MODIFY));

    verify(versioningService).save(eq("basic-workflow"), eq(""), any(String.class), any(String.class));
    verify(workflowEngine).deploy(any(Workflow.class), eq(mockInstance));
    verify(workflowEngine, never()).undeploy(any());
  }

  @Test
  void testHandleFileEventDeleteWorkflowFound() throws IOException, ProcessingException {
    String workflowFile = "src/test/resources/basic/publish/basic-workflow.swadl.yaml";
    Path path = Path.of(workflowFile);
    VersionedWorkflow versionedWorkflow = new VersionedWorkflow()
            .setVersionedWorkflowId("basic-workflow", "")
            .setPath(workflowFile)
            .setIsToPublish(true);

    when(versioningService.find(eq(path))).thenReturn(Optional.of(versionedWorkflow));
    doNothing().when(versioningService).delete(eq("basic-workflow"), eq(""));
    doNothing().when(workflowEngine).undeploy(eq("basic-workflow"));

    workflowDeployer.handleFileEvent(path, new WatchEvent(StandardWatchEventKinds.ENTRY_DELETE));

    verify(versioningService).delete(eq("basic-workflow"), eq(""));
    verify(workflowEngine, never()).deploy(any(), any());
    verify(workflowEngine).undeploy(eq("basic-workflow"));
  }

  @Test
  void testHandleFileEventDeleteWorkflowNotFound() throws IOException, ProcessingException {
    String workflowFile = "src/test/resources/basic/publish/basic-workflow.swadl.yaml";
    Path path = Path.of(workflowFile);

    when(versioningService.find(eq(path))).thenReturn(Optional.empty());

    workflowDeployer.handleFileEvent(path, new WatchEvent(StandardWatchEventKinds.ENTRY_DELETE));

    verify(versioningService, never()).delete(any(), any());
    verify(workflowEngine, never()).deploy(any(), any());
    verify(workflowEngine, never()).undeploy(any());
  }

  private static class WatchEvent implements java.nio.file.WatchEvent<Path> {

    private final java.nio.file.WatchEvent.Kind<Path> kind;

    public WatchEvent(WatchEvent.Kind<Path> kind) {
      this.kind = kind;
    }

    @Override
    public Kind<Path> kind() {
      return this.kind;
    }

    @Override
    public int count() {
      return 0;
    }

    @Override
    public Path context() {
      return null;
    }
  }
}