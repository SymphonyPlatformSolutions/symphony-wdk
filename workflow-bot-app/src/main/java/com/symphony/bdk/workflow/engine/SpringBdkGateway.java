package com.symphony.bdk.workflow.engine;

import com.symphony.bdk.core.service.connection.ConnectionService;
import com.symphony.bdk.core.service.message.MessageService;
import com.symphony.bdk.core.service.stream.StreamService;
import com.symphony.bdk.core.service.user.UserService;
import com.symphony.bdk.workflow.engine.executor.BdkGateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpringBdkGateway implements BdkGateway {

  private final MessageService messageService;
  private final StreamService streamService;
  private final UserService userService;
  private final ConnectionService connectionService;

  @Autowired
  public SpringBdkGateway(MessageService messageService,
      StreamService streamService, UserService userService,
      ConnectionService connectionService) {
    this.messageService = messageService;
    this.streamService = streamService;
    this.userService = userService;
    this.connectionService = connectionService;
  }

  @Override
  public MessageService messages() {
    return messageService;
  }

  @Override
  public StreamService streams() {
    return streamService;
  }

  @Override
  public UserService users() {
    return userService;
  }

  @Override
  public ConnectionService connections() {
    return connectionService;
  }
}
