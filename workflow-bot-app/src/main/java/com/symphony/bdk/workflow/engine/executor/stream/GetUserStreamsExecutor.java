package com.symphony.bdk.workflow.engine.executor.stream;

import com.symphony.bdk.core.service.pagination.model.PaginationAttribute;
import com.symphony.bdk.gen.api.model.StreamAttributes;
import com.symphony.bdk.gen.api.model.StreamFilter;
import com.symphony.bdk.gen.api.model.StreamType;
import com.symphony.bdk.workflow.engine.executor.ActivityExecutor;
import com.symphony.bdk.workflow.engine.executor.ActivityExecutorContext;
import com.symphony.bdk.workflow.swadl.v1.activity.stream.GetUserStreams;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class GetUserStreamsExecutor implements ActivityExecutor<GetUserStreams> {

  private static final String OUTPUTS_STREAMS_KEY = "streams";

  @Override
  public void execute(ActivityExecutorContext<GetUserStreams> execution) {
    log.debug("Getting user streams");

    GetUserStreams getUserStreams = execution.getActivity();
    List<StreamAttributes> userStreams;
    if (getUserStreams.getLimit() != null && getUserStreams.getSkip() != null) {
      userStreams = execution.bdk().streams().listStreams(toFilter(getUserStreams),
          new PaginationAttribute(getUserStreams.getSkip().get().intValue(),
              getUserStreams.getLimit().get().intValue()));
    } else {
      userStreams = execution.bdk().streams().listStreams(toFilter(getUserStreams));
    }

    execution.setOutputVariable(OUTPUTS_STREAMS_KEY, userStreams);
  }

  private StreamFilter toFilter(GetUserStreams getUserStreams) {
    StreamFilter filter = new StreamFilter()
        .includeInactiveStreams(getUserStreams.getIncludeInactiveStreams().get());

    if (getUserStreams.getTypes() != null) {
      filter.setStreamTypes(getUserStreams.getTypes().get().stream()
          .map(t -> new StreamType().type(StreamType.TypeEnum.fromValue(t.get())))
          .collect(Collectors.toList()));
    }
    return filter;
  }
}
