package org.example.realworldapi.infrastructure.provider;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.auth.principal.JWTParser;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.realworldapi.infrastructure.web.provider.TokenProvider;
import org.example.realworldapi.infrastructure.web.security.profile.Role;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class JwtTokenProvider implements TokenProvider {

  public final String COMPLEMENTARY_SUBSCRIPTION = "complementary-subscription";
  public final String CLAIM_ROLES = "ROLES";

  @ConfigProperty(name = "jwt.issuer")
  String issuer;

  @ConfigProperty(name = "jwt.secret")
  String secret;

  @ConfigProperty(name = "jwt.expiration.time.minutes")
  Integer expirationTimeInMinutes;

  @Inject
  JWTParser jwtParser;

  @Inject
  JsonWebToken jwt;

  @Override
  public String createUserToken(String subject) {
    JwtClaimsBuilder builder = Jwt.claims()
        .issuer(issuer)
        .subject(subject)
        .issuedAt(Instant.now())
        .claim(COMPLEMENTARY_SUBSCRIPTION, UUID.randomUUID().toString())
        .claim(CLAIM_ROLES, toArrayNames(Role.USER));

    if (expirationTimeInMinutes != null) {
      builder.expiresAt(Instant.now().plusSeconds(expirationTimeInMinutes * 60));
    }

    return builder.sign();
  }

  @Override
  public JsonWebToken verify(String token) {
    try {
      return jwtParser.parse(token);
    } catch (Exception e) {
      throw new RuntimeException("Invalid JWT token", e);
    }
  }

  @Override
  public Role[] extractRoles(JsonWebToken decodedJWT) {
    Set<String> roles = decodedJWT.getClaim(CLAIM_ROLES);
    return roles.stream().map(Role::valueOf).toArray(Role[]::new);
  }

  private static Set<String> toArrayNames(Role... allowedRoles) {
    Set<String> roleNames = new HashSet<>();
    for (Role role : allowedRoles) {
      roleNames.add(role.name());
    }
    return roleNames;
  }
}
