package org.iotp.gateway.service;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.internal.security.SSLSocketFactoryFactory;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import lombok.Data;

/**
 */
@Data
public class MqttGatewaySecurityConfiguration {

  private String accessToken;
  private String keystore;
  private String keystorePassword;
  private String keystoreKeyAlias;
  private String truststore;
  private String truststorePassword;

  public boolean isTokenBased() {
    return !StringUtils.isEmpty(accessToken);
  }

  public boolean isSsl() {
    return !StringUtils.isEmpty(truststore);
  }

  public void setupSecurityOptions(MqttConnectOptions options) {
    if (this.isTokenBased()) {
      options.setUserName(this.getAccessToken());
      if (!StringUtils.isEmpty(this.getTruststore())) {
        Properties sslProperties = new Properties();
        sslProperties.put(SSLSocketFactoryFactory.TRUSTSTORE, this.getTruststore());
        sslProperties.put(SSLSocketFactoryFactory.TRUSTSTOREPWD, this.getTruststorePassword());
        sslProperties.put(SSLSocketFactoryFactory.TRUSTSTORETYPE, "JKS");
        sslProperties.put(SSLSocketFactoryFactory.CLIENTAUTH, false);
        options.setSSLProperties(sslProperties);
      }
    } else {
      // TODO: check and document this
      Properties sslProperties = new Properties();
      sslProperties.put(SSLSocketFactoryFactory.KEYSTORE, this.getKeystore());
      sslProperties.put(SSLSocketFactoryFactory.KEYSTOREPWD, this.getKeystorePassword());
      sslProperties.put(SSLSocketFactoryFactory.KEYSTORETYPE, "JKS");
      sslProperties.put(SSLSocketFactoryFactory.TRUSTSTORE, this.getTruststore());
      sslProperties.put(SSLSocketFactoryFactory.TRUSTSTOREPWD, this.getTruststorePassword());
      sslProperties.put(SSLSocketFactoryFactory.TRUSTSTORETYPE, "JKS");
      sslProperties.put(SSLSocketFactoryFactory.CLIENTAUTH, true);
      options.setSSLProperties(sslProperties);
    }
  }

  public String getClientId() {
    if (this.isTokenBased()) {
      return sha256(this.getAccessToken().getBytes(StandardCharsets.UTF_8));
    } else {
      try {
        FileInputStream is = new FileInputStream(this.getKeystore());

        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(is, this.getKeystorePassword().toCharArray());

        Key key = keystore.getKey(this.getKeystoreKeyAlias(), this.getKeystorePassword().toCharArray());
        if (key instanceof PrivateKey) {
          // Get certificate of public key
          java.security.cert.Certificate cert = keystore.getCertificate(this.getKeystoreKeyAlias());

          // Get public key
          PublicKey publicKey = cert.getPublicKey();

          return sha256(publicKey.getEncoded());
        } else {
          throw new RuntimeException("No public key!");
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private String sha256(byte[] data) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(data);
      return Base64Utils.encodeToString(md.digest());
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
