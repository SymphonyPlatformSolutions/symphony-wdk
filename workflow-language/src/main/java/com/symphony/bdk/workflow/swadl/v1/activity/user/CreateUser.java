package com.symphony.bdk.workflow.swadl.v1.activity.user;

import com.symphony.bdk.workflow.swadl.v1.Variable;
import com.symphony.bdk.workflow.swadl.v1.activity.BaseActivity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * @see <a href="https://developers.symphony.com/restapi/reference#create-user-v2">Create user API</a>
 * @see <a href="https://developers.symphony.com/restapi/reference#update-user-status">Update user API</a>
 * @see <a href="https://developers.symphony.com/restapi/reference#update-features">Update user features API</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CreateUser extends BaseActivity {

  private String type = "NORMAL";
  private String email;
  private String username;
  private String firstname;
  private String lastname;
  private String displayName;

  @Nullable
  private String recommendedLanguage;
  @Nullable
  private Contact contact;
  @Nullable
  private Business business;

  @Nullable
  private Variable<List<Variable<String>>> roles = Variable.nullValue();

  @Nullable
  private Variable<Map<String, Boolean>> entitlements;

  @Nullable
  private String status;

  @Nullable
  private Password password;

  @Nullable
  private Keys keys;


  @Data
  public static class Contact {
    private String workPhoneNumber;
    private String mobilePhoneNumber;
    private String twoFactorAuthNumber;
    private String smsNumber;
  }


  @Data
  public static class Business {
    private String companyName;
    private String department;
    private String division;
    private String title;
    private String location;
    private String jobFunction;
    private Variable<List<Variable<String>>> assetClasses = Variable.nullValue();
    private Variable<List<Variable<String>>> industries = Variable.nullValue();
    private Variable<List<Variable<String>>> functions = Variable.nullValue();
    private Variable<List<Variable<String>>> marketCoverages = Variable.nullValue();
    private Variable<List<Variable<String>>> responsibilities = Variable.nullValue();
    private Variable<List<Variable<String>>> instruments = Variable.nullValue();
  }


  @Data
  public static class Password {
    private String hashedPassword;
    private String hashedSalt;
    private String hashedKmPassword;
    private String hashedKmSalt;
  }


  @Data
  public static class Keys {
    private Key current;
    private Key previous;
  }


  @Data
  public static class Key {
    private String action;
    private String expiration;
    private String key;
  }
}
