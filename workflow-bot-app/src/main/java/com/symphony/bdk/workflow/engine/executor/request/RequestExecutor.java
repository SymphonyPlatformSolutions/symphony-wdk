package com.symphony.bdk.workflow.engine.executor.request;

import com.symphony.bdk.http.api.ApiException;
import com.symphony.bdk.http.api.ApiResponse;
import com.symphony.bdk.http.api.util.TypeReference;
import com.symphony.bdk.workflow.engine.executor.ActivityExecutor;
import com.symphony.bdk.workflow.engine.executor.ActivityExecutorContext;
import com.symphony.bdk.workflow.swadl.v1.activity.request.ExecuteRequest;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Slf4j
public class RequestExecutor implements ActivityExecutor<ExecuteRequest> {

  private static final String OUTPUT_STATUS_KEY = "status";
  private static final String OUTPUT_BODY_KEY = "body";

  @Override
  public void execute(ActivityExecutorContext<ExecuteRequest> execution) {
    ExecuteRequest activity = execution.getActivity();
    Object data = null;
    int statusCode;

    try {
      ApiResponse<Object> apiResponse = execution.bdk()
          .apiClient(activity.getUrl())
          .invokeAPI("", activity.getMethod(), Collections.emptyList(), activity.getBody(), activity.getHeaders(),
              Collections.emptyMap(), Collections.emptyMap(), "application/json", "application/json", null,
              new TypeReference<>() {});

      data = apiResponse.getData();
      statusCode = apiResponse.getStatusCode();
    } catch (ApiException apiException) {
      statusCode = apiException.getCode();
      log.debug("This error happens when the request fails.", apiException);
    }

    execution.setOutputVariable(OUTPUT_STATUS_KEY, statusCode);
    execution.setOutputVariable(OUTPUT_BODY_KEY, data);
  }
}
