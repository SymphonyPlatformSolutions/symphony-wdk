package com.symphony.bdk.workflow.swadl.v1.activity.stream;

import com.symphony.bdk.workflow.swadl.v1.activity.BaseActivity;
import com.symphony.bdk.workflow.swadl.v1.activity.Obo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @see <a href="https://developers.symphony.com/restapi/reference#stream-info-v2">Get stream API</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetStream extends BaseActivity {
  private String streamId;
  private Obo obo;
}
