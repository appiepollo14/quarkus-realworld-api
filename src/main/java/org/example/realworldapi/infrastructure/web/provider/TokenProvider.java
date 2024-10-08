package org.example.realworldapi.infrastructure.web.provider;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.example.realworldapi.infrastructure.web.security.profile.Role;

public interface TokenProvider {

  String createUserToken(String subject);

  JsonWebToken verify(String token);

  Role[] extractRoles(JsonWebToken decodedJWT);
}
