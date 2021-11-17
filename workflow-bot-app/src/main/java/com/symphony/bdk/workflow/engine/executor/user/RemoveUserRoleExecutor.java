package com.symphony.bdk.workflow.engine.executor.user;

import com.symphony.bdk.core.service.user.constant.RoleId;
import com.symphony.bdk.workflow.engine.executor.ActivityExecutor;
import com.symphony.bdk.workflow.engine.executor.ActivityExecutorContext;
import com.symphony.bdk.workflow.swadl.v1.Variable;
import com.symphony.bdk.workflow.swadl.v1.activity.user.RemoveUserRole;

import lombok.extern.slf4j.Slf4j;

/**
 * Leads to multiple API calls, so the execution could be incomplete or under performant if a lot of users are passed.
 */
@Slf4j
public class RemoveUserRoleExecutor implements ActivityExecutor<RemoveUserRole> {

  @Override
  public void execute(ActivityExecutorContext<RemoveUserRole> context) {
    RemoveUserRole userRole = context.getActivity();

    for (Variable<Number> userId : userRole.getUserIds().get()) {
      for (Variable<String> role : userRole.getRoles().get()) {
        log.debug("Removing role {} from user {}", role, userId);
        context.bdk().users().removeRole(userId.get().longValue(), RoleId.valueOf(role.get()));
      }
    }
  }

}
