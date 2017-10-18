package org.iotp.gateway.extensions.opc.conf.identity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;

/**
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AnonymousIdentityProviderConfiguration.class, name = "anonymous"),
        @JsonSubTypes.Type(value = UsernameIdentityProviderConfiguration.class, name = "username")})
public interface IdentityProviderConfiguration {

    IdentityProvider toProvider();

}
