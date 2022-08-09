package com.symphony.bdk.workflow.engine.executor.message;

import static com.symphony.bdk.workflow.engine.executor.message.PinMessageExecutor.IM;
import static com.symphony.bdk.workflow.engine.executor.message.PinMessageExecutor.ROOM;

import com.symphony.bdk.core.auth.AuthSession;
import com.symphony.bdk.gen.api.model.V1IMAttributes;
import com.symphony.bdk.gen.api.model.V2StreamAttributes;
import com.symphony.bdk.gen.api.model.V3RoomAttributes;
import com.symphony.bdk.workflow.engine.executor.ActivityExecutor;
import com.symphony.bdk.workflow.engine.executor.ActivityExecutorContext;
import com.symphony.bdk.workflow.swadl.v1.activity.message.UnpinMessage;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class UnpinMessageExecutor implements ActivityExecutor<UnpinMessage> {

  @Override
  public void execute(ActivityExecutorContext<UnpinMessage> execution) throws IOException {
    String streamId = execution.getActivity().getStreamId();

    V2StreamAttributes stream = execution.bdk().streams().getStream(streamId);
    String streamType = stream.getStreamType().getType();

    if (IM.equals(streamType)) {
      this.unpinInstantMessage(execution, streamId);
    } else if (ROOM.equals(streamType)) {
      this.unpinRoomMessage(execution, streamId);
    } else {
      throw new IllegalArgumentException(
          String.format("Unable to unpin message in stream type %s in activity %s", streamType,
              execution.getActivity().getId()));
    }
  }

  private boolean isObo(UnpinMessage activity) {
    return activity.getObo() != null && (activity.getObo().getUsername() != null
        || activity.getObo().getUserId() != null);
  }

  private void unpinInstantMessage(ActivityExecutorContext<UnpinMessage> execution, String streamId) {
    UnpinMessage activity = execution.getActivity();

    V1IMAttributes imAttributes = new V1IMAttributes();
    imAttributes.setPinnedMessageId("");

    log.debug("Unpin message in IM {}", streamId);

    if (this.isObo(activity)) {
      throw new IllegalArgumentException(
          String.format("Unpin instant message, in activity %s, is not OBO enabled", activity.getId()));
    } else {
      execution.bdk().streams().updateInstantMessage(streamId, imAttributes);
    }
  }

  private void unpinRoomMessage(ActivityExecutorContext<UnpinMessage> execution, String streamId) {
    UnpinMessage activity = execution.getActivity();
    V3RoomAttributes roomAttributes = new V3RoomAttributes();
    roomAttributes.setPinnedMessageId("");

    log.debug("Unpin message in Room {}", streamId);

    if (this.isObo(activity)) {
      AuthSession authSession;
      if (activity.getObo().getUsername() != null) {
        authSession = execution.bdk().obo(activity.getObo().getUsername());
      } else {
        authSession = execution.bdk().obo(activity.getObo().getUserId());
      }

      execution.bdk().obo(authSession).streams().updateRoom(streamId, roomAttributes);
    } else {
      execution.bdk().streams().updateRoom(streamId, roomAttributes);
    }
  }
}
