package com.symphony.bdk.workflow.swadl.v1;

import com.symphony.bdk.workflow.swadl.v1.event.ActivityCompletedEvent;
import com.symphony.bdk.workflow.swadl.v1.event.ActivityExpiredEvent;
import com.symphony.bdk.workflow.swadl.v1.event.ActivityFailedEvent;
import com.symphony.bdk.workflow.swadl.v1.event.ConnectionAcceptedEvent;
import com.symphony.bdk.workflow.swadl.v1.event.ConnectionRequestedEvent;
import com.symphony.bdk.workflow.swadl.v1.event.FormRepliedEvent;
import com.symphony.bdk.workflow.swadl.v1.event.ImCreatedEvent;
import com.symphony.bdk.workflow.swadl.v1.event.MessageReceivedEvent;
import com.symphony.bdk.workflow.swadl.v1.event.MessageSuppressedEvent;
import com.symphony.bdk.workflow.swadl.v1.event.PostSharedEvent;
import com.symphony.bdk.workflow.swadl.v1.event.RequestReceivedEvent;
import com.symphony.bdk.workflow.swadl.v1.event.RoomCreatedEvent;
import com.symphony.bdk.workflow.swadl.v1.event.RoomDeactivatedEvent;
import com.symphony.bdk.workflow.swadl.v1.event.RoomMemberDemotedFromOwnerEvent;
import com.symphony.bdk.workflow.swadl.v1.event.RoomMemberPromotedToOwnerEvent;
import com.symphony.bdk.workflow.swadl.v1.event.RoomReactivatedEvent;
import com.symphony.bdk.workflow.swadl.v1.event.RoomUpdatedEvent;
import com.symphony.bdk.workflow.swadl.v1.event.TimerFiredEvent;
import com.symphony.bdk.workflow.swadl.v1.event.UserJoinedRoomEvent;
import com.symphony.bdk.workflow.swadl.v1.event.UserLeftRoomEvent;
import com.symphony.bdk.workflow.swadl.v1.event.UserRequestedToJoinRoomEvent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
  @JsonProperty
  private List<Event> oneOf;

  @JsonProperty
  private List<Event> allOf;

  @JsonProperty
  private FormRepliedEvent formReplied;

  @JsonProperty
  private ActivityExpiredEvent activityExpired;

  @JsonProperty
  private ActivityCompletedEvent activityCompleted;

  @JsonProperty
  private ActivityFailedEvent activityFailed;

  @JsonProperty
  private MessageReceivedEvent messageReceived;

  @JsonProperty
  private MessageSuppressedEvent messageSuppressed;

  @JsonProperty
  private PostSharedEvent postShared;

  @JsonProperty
  private ImCreatedEvent imCreated;

  @JsonProperty
  private RoomCreatedEvent roomCreated;

  @JsonProperty
  private RoomUpdatedEvent roomUpdated;

  @JsonProperty
  private RoomDeactivatedEvent roomDeactivated;

  @JsonProperty
  private RoomReactivatedEvent roomReactivated;

  @JsonProperty
  private RoomMemberPromotedToOwnerEvent roomMemberPromotedToOwner;

  @JsonProperty
  private RoomMemberPromotedToOwnerEvent roomMemberDemotedFromOwner;

  @JsonProperty
  private UserJoinedRoomEvent userJoinedRoom;

  @JsonProperty
  private UserLeftRoomEvent userLeftRoom;

  @JsonProperty
  private UserRequestedToJoinRoomEvent userRequestedJoinRoom;

  @JsonProperty
  private ConnectionRequestedEvent connectionRequested;

  @JsonProperty
  private ConnectionAcceptedEvent connectionAccepted;

  @JsonProperty
  private TimerFiredEvent timerFired;

  @JsonProperty
  private RequestReceivedEvent requestReceived;

  public String getEventType() {

    if (this.formReplied != null) {
      return FormRepliedEvent.class.getSimpleName();
    }

    if (this.activityExpired != null) {
      return ActivityExpiredEvent.class.getSimpleName();
    }

    if (this.activityCompleted != null) {
      return ActivityCompletedEvent.class.getSimpleName();
    }

    if (this.activityFailed != null) {
      return ActivityFailedEvent.class.getSimpleName();
    }

    if (this.messageReceived != null) {
      return MessageReceivedEvent.class.getSimpleName();
    }

    if (this.messageSuppressed != null) {
      return MessageSuppressedEvent.class.getSimpleName();
    }

    if (this.postShared != null) {
      return PostSharedEvent.class.getSimpleName();
    }

    if (this.imCreated != null) {
      return ImCreatedEvent.class.getSimpleName();
    }

    if (this.roomCreated != null) {
      return RoomCreatedEvent.class.getSimpleName();
    }

    if (this.roomUpdated != null) {
      return RoomUpdatedEvent.class.getSimpleName();
    }

    if (this.roomDeactivated != null) {
      return RoomDeactivatedEvent.class.getSimpleName();
    }

    if (this.roomReactivated != null) {
      return RoomReactivatedEvent.class.getSimpleName();
    }

    if (this.roomMemberPromotedToOwner != null) {
      return RoomMemberPromotedToOwnerEvent.class.getSimpleName();
    }

    if (this.roomMemberDemotedFromOwner != null) {
      return RoomMemberDemotedFromOwnerEvent.class.getSimpleName();
    }

    if (this.userJoinedRoom != null) {
      return UserJoinedRoomEvent.class.getSimpleName();
    }

    if (this.userLeftRoom != null) {
      return UserLeftRoomEvent.class.getSimpleName();
    }

    if (this.userRequestedJoinRoom != null) {
      return UserRequestedToJoinRoomEvent.class.getSimpleName();
    }

    if (this.connectionRequested != null) {
      return ConnectionRequestedEvent.class.getSimpleName();
    }

    if (this.connectionAccepted != null) {
      return ConnectionAcceptedEvent.class.getSimpleName();
    }

    if (this.timerFired != null) {
      return TimerFiredEvent.class.getSimpleName();
    }

    if (this.requestReceived != null) {
      return RequestReceivedEvent.class.getSimpleName();
    }

    return "";
  }
}
