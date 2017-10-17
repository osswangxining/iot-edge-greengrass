package org.iotp.gateway.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 */
@Slf4j
public class ConfigurationTools {

  private static final ObjectMapper mapper = new ObjectMapper();

  public static <T> T readConfiguration(String configurationFile, Class<T> clazz) throws IOException {
    try {
      return mapper.readValue(getResourceAsStream(configurationFile), clazz);
    } catch (IOException e) {
      log.error("Failed to load {} configuration from {}", clazz, configurationFile);
      throw e;
    }
  }

  public static CertificateInfo loadCertificate(KeystoreConfiguration configuration)
      throws GeneralSecurityException, IOException {
    try {
      KeyStore keyStore = KeyStore.getInstance(configuration.getType());
      keyStore.load(getResourceAsStream(configuration.getLocation()), configuration.getPassword().toCharArray());

      Key key = keyStore.getKey(configuration.getAlias(), configuration.getKeyPassword().toCharArray());
      if (key instanceof PrivateKey) {
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(configuration.getAlias());
        PublicKey publicKey = certificate.getPublicKey();
        KeyPair keyPair = new KeyPair(publicKey, (PrivateKey) key);
        return new CertificateInfo(certificate, keyPair);
      } else {
        throw new GeneralSecurityException(configuration.getAlias() + " is not a private key!");
      }
    } catch (IOException | GeneralSecurityException e) {
      log.error("Keystore configuration: [{}] is invalid!", configuration, e);
      throw e;
    }
  }

  private static InputStream getResourceAsStream(String configurationFile) {
    return ConfigurationTools.class.getClassLoader().getResourceAsStream(configurationFile);
  }
}
