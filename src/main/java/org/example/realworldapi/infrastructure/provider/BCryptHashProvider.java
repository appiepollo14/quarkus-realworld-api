package org.example.realworldapi.infrastructure.provider;

import org.example.realworldapi.domain.model.provider.HashProvider;
import io.quarkus.elytron.security.common.BcryptUtil;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BCryptHashProvider implements HashProvider {

  @Override
  public String hashPassword(String password) {
    return BcryptUtil.bcryptHash(password);
  }

  @Override
  public boolean checkPassword(String plaintext, String hashed) {
    return BcryptUtil.matches(plaintext, hashed);
  }
}
