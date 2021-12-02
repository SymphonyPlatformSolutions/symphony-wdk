package com.symphony.bdk.workflow;

import static com.symphony.bdk.workflow.custom.assertion.WorkflowAssert.contains;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.symphony.bdk.core.service.message.model.Message;
import com.symphony.bdk.workflow.swadl.SwadlParser;
import com.symphony.bdk.workflow.swadl.exception.ActivityNotFoundException;
import com.symphony.bdk.workflow.swadl.v1.Workflow;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

class FormReplyIntegrationTest extends IntegrationTest {

  @Test
  void sendFormSendMessageOnReply() throws Exception {
    Workflow workflow = SwadlParser.fromYaml(getClass().getResourceAsStream("/form/send-form-reply.swadl.yaml"));

    when(messageService.send(anyString(), any(Message.class))).thenReturn(message("msgId"));

    engine.deploy(workflow);

    // trigger workflow execution
    engine.onEvent(messageReceived("/message"));
    verify(messageService, timeout(5000)).send(eq("123"), contains("form"));

    // reply to form
    await().atMost(5, TimeUnit.SECONDS).ignoreExceptions().until(() -> {
      engine.onEvent(form("msgId", "sendForm", Collections.singletonMap("aField", "My message")));
      // bot should send my reply back
      verify(messageService, atLeast(1)).send(eq("123"), contains("My message"));
      return true;
    });
  }

  @Test
  void sendFormSendMessageOnReply_multipleUsers() throws Exception {
    Workflow workflow = SwadlParser.fromYaml(getClass().getResourceAsStream("/form/send-form-reply.swadl.yaml"));
    when(messageService.send(anyString(), any(Message.class))).thenReturn(message("msgId"));

    engine.deploy(workflow);

    // trigger workflow execution
    engine.onEvent(messageReceived("/message"));
    verify(messageService, timeout(5000)).send(eq("123"), contains("form"));

    await().atMost(5, TimeUnit.SECONDS).ignoreExceptions().until(() -> {
      // user 1 replies to form
      engine.onEvent(form("msgId", "sendForm", Collections.singletonMap("aField", "My message")));
      // user 2 replies to form
      engine.onEvent(form("msgId", "sendForm", Collections.singletonMap("aField", "My message")));

      // bot should send my reply back
      verify(messageService, atLeast(2)).send(eq("123"), contains("My message"));
      return true;
    });
  }

  @Test
  void sendFormSendMessageOnReply_followUpActivity() throws Exception {
    Workflow workflow = SwadlParser.fromYaml(getClass()
        .getResourceAsStream("/form/send-form-reply-followup-activity.swadl.yaml"));

    when(messageService.send(anyString(), any(Message.class))).thenReturn(message("msgId"));

    engine.deploy(workflow);

    // trigger workflow execution
    engine.onEvent(messageReceived("/message"));
    verify(messageService, timeout(5000)).send(eq("123"), contains("form"));

    await().atMost(5, TimeUnit.SECONDS).ignoreExceptions().until(() -> {
      // user 1 replies to form
      engine.onEvent(form("msgId", "sendForm", Collections.singletonMap("aField", "My message")));

      // bot should send back my reply
      verify(messageService, timeout(5000)).send(eq("123"), contains("First reply: My message"));
      verify(messageService, timeout(5000)).send(eq("123"), contains("Second reply: My message"));

      return true;
    });
  }

  @Test
  void sendFormSendMessageOnReply_expiration() throws Exception {
    Workflow workflow = SwadlParser.fromYaml(
        getClass().getResourceAsStream("/form/send-form-reply-expiration.swadl.yaml"));

    when(messageService.send(anyString(), any(Message.class))).thenReturn(message("msgId"));
    engine.deploy(workflow);

    // trigger workflow execution
    engine.onEvent(messageReceived("/message"));
    verify(messageService, timeout(5000)).send(eq("123"), contains("form"));

    // user never replies

    // bot should run the on/activity-expired activity after 1s
    verify(messageService, timeout(5000)).send(eq("123"), contains("Form expired"));
  }

  @Test
  void sendFormNested() throws Exception {
    Workflow workflow = SwadlParser.fromYaml(getClass().getResourceAsStream(
        "/form/send-form-reply-nested.swadl.yaml"));
    when(messageService.send(anyString(), any(Message.class))).thenReturn(message("msgId"));

    engine.deploy(workflow);

    // trigger workflow execution
    engine.onEvent(messageReceived("/message"));
    verify(messageService, timeout(5000)).send(eq("abc"), contains("form"));

    // reply to first form
    await().atMost(5, TimeUnit.SECONDS).ignoreExceptions().until(() -> {
      engine.onEvent(form("msgId", "sendForm", Collections.singletonMap("question", "My message")));
      return true;
    });

    // bot should not run the on/activity-expired activity after 1s because it is attached to the second form that
    // has not been replied to
    verify(messageService, timeout(5000)).send(eq("abc"), contains("expiration-outer"));
  }

  @SuppressWarnings("checkstyle:LineLength")
  @Test
  void sendFormInvalidFormId() throws Exception {
    final Workflow workflow = SwadlParser.fromYaml(
        getClass().getResourceAsStream("/form/invalid/swadl/send-form-reply-unknown-activity-id.swadl.yaml"));

    assertThatExceptionOfType(ActivityNotFoundException.class)
        .isThrownBy(() -> engine.deploy(workflow))
        .satisfies(e -> assertThat(e.getMessage()).isEqualTo(
            "Invalid activity in the workflow send-form-reply-invalid-activity-id: No activity found with id unknownActivityId referenced in pongReply"));
  }

  @Test
  void sendFormLoop() throws Exception {
    Workflow workflow = SwadlParser.fromYaml(getClass().getResourceAsStream("/form/send-form-loop.swadl.yaml"));

    when(messageService.send(anyString(), any(Message.class))).thenReturn(message("msgId"));

    engine.deploy(workflow);

    // trigger workflow execution
    engine.onEvent(messageReceived("/message"));
    verify(messageService, timeout(5000)).send(eq("ABC"), contains("form"));

    // reply to form
    await().atMost(5, TimeUnit.SECONDS).ignoreExceptions().until(() -> {
      engine.onEvent(form("msgId", "sendForm", Collections.singletonMap("action", "reject")));
      // bot should send my form back because reject was selected
      verify(messageService, timeout(5000).times(2)).send(eq("ABC"), contains("form"));
      return true;
    });
  }

  @Test
  void sendFormContinuation() throws Exception {
    Workflow workflow = SwadlParser.fromYaml(getClass().getResourceAsStream("/form/send-form-continuation.swadl.yaml"));

    when(messageService.send(anyString(), any(Message.class))).thenReturn(message("msgId"));

    engine.deploy(workflow);

    // trigger workflow execution
    engine.onEvent(messageReceived("/message"));
    verify(messageService, timeout(5000)).send(eq("ABC"), contains("form"));

    // reply to form
    await().atMost(5, TimeUnit.SECONDS).ignoreExceptions().until(() -> {
      engine.onEvent(form("msgId", "sendForm", Collections.singletonMap("action", "reject")));
      // bot should send my form back because reject was selected
      verify(messageService, timeout(5000)).send(eq("ABC"), contains("afterReply1"));
      return true;
    });
  }

}
