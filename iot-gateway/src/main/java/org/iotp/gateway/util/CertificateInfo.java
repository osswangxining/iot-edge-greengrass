package org.iotp.gateway.util;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

import lombok.Data;

/**
 */
@Data
public class CertificateInfo {

  private final X509Certificate certificate;
  private final KeyPair keyPair;

}
