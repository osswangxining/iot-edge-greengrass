package org.iotp.gateway.util;

import lombok.Data;

/**
 */
@Data
public class KeystoreConfiguration {

  private String type;
  private String location;
  private String password;
  private String alias;
  private String keyPassword;

}
