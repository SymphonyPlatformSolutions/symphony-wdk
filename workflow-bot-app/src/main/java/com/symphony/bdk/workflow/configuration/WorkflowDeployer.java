package com.symphony.bdk.workflow.configuration;

import com.symphony.bdk.workflow.engine.WorkflowEngine;
import com.symphony.bdk.workflow.jpamodel.VersionedWorkflow;
import com.symphony.bdk.workflow.jparepo.CustomerRepository;
import com.symphony.bdk.workflow.jparepo.VersionedWorkflowRepository;
import com.symphony.bdk.workflow.swadl.SwadlParser;
import com.symphony.bdk.workflow.swadl.v1.Workflow;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
@Transactional
public class WorkflowDeployer {

  private final WorkflowEngine<BpmnModelInstance> workflowEngine;
  private final VersionedWorkflowRepository versionedWorkflowRepository;
  private final CustomerRepository customerRepository;
  private final Map<Path, Pair<String, Boolean>> deployedWorkflows = new HashMap<>();

  private final Map<String, Path> workflowIdPathMap = new HashMap<>();

  public WorkflowDeployer(@Autowired WorkflowEngine<BpmnModelInstance> workflowEngine,
      VersionedWorkflowRepository versionedWorkflowRepository, CustomerRepository customerRepository) {
    this.workflowEngine = workflowEngine;
    this.versionedWorkflowRepository = versionedWorkflowRepository;
    this.customerRepository = customerRepository;
  }

  public void addAllWorkflowsFromFolder(Path path) {
    if (!Files.isDirectory(path)) {
      throw new IllegalArgumentException("Could not find workflows folder to monitor with path: " + path);
    }

    log.info("Watching workflows from {}", path);
    File[] existingFiles = path.toFile().listFiles();
    if (existingFiles != null) {
      for (File file : existingFiles) {
        if (isYaml(file.toPath())) {
          try {
            this.addWorkflow(file.toPath());
          } catch (Exception e) {
            log.error("Failed to add workflow for file {}", file, e);
          }
        }
      }
    }
  }

  public void addWorkflow(Path workflowFile) throws IOException, ProcessingException {
    if (workflowFile.toFile().length() == 0) {
      return;
    }
    log.debug("Adding a new workflow");
    Workflow workflow = SwadlParser.fromYaml(workflowFile.toFile());
    BpmnModelInstance instance = workflowEngine.parseAndValidate(workflow);
    Pair<String, Boolean> deployedWorkflow = deployedWorkflows.get(workflowFile);
    if (workflow.isToPublish()) {
      log.debug("Deploying this new workflow");
      workflowEngine.deploy(workflow, instance);

      // persist swadl
      if (!workflow.getVersion().isBlank()) {
        String swadl = Files.readString(workflowFile.toFile().toPath(), StandardCharsets.UTF_8);
        this.persistSwadl(workflow.getId(), workflow.getVersion(), swadl);
      }

    } else if (deployedWorkflow != null && deployedWorkflow.getRight()) {
      log.debug("Workflow is a draft version, undeploy the old version");
      workflowEngine.undeploy(deployedWorkflow.getLeft());
    }
    deployedWorkflows.put(workflowFile, Pair.of(workflow.getId(), workflow.isToPublish()));
    workflowIdPathMap.put(workflow.getId(), workflowFile);
  }

  private void persistSwadl(String workflowId, String version, String swadl) {
    VersionedWorkflow versionedWorkflow = new VersionedWorkflow();
    //versionedWorkflow.setWorkflowId(workflowId);
    versionedWorkflow.setSwadl(swadl);

    this.versionedWorkflowRepository.save(versionedWorkflow);
    Iterable<VersionedWorkflow> byId = this.versionedWorkflowRepository.findAll();

    System.out.println("debug");
  }


  public void handleFileEvent(Path changedFile, WatchEvent<Path> event) throws IOException, ProcessingException {
    if (isYaml(changedFile)) {
      if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
        this.addWorkflow(changedFile);

      } else if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
        String workflowId = deployedWorkflows.get(changedFile).getLeft();
        this.workflowEngine.undeploy(workflowId);
        this.deployedWorkflows.remove(changedFile);
        this.workflowIdPathMap.remove(workflowId);

      } else if (event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
        this.addWorkflow(changedFile);

      } else {
        log.debug("Unknown event: {}", event);
      }
    }
  }

  private boolean isYaml(Path changedFile) {
    return changedFile.toString().endsWith(".yaml") || changedFile.toString().endsWith(".yml");
  }

  public boolean workflowExist(String id) {
    return workflowIdPathMap.containsKey(id);
  }

  public Path workflowSwadlPath(String id) {
    return workflowIdPathMap.get(id);
  }

  public Set<Path> workflowSwadlPaths() {
    return deployedWorkflows.keySet();
  }
}
