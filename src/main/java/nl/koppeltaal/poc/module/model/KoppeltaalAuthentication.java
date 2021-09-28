package nl.koppeltaal.poc.module.model;

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class KoppeltaalAuthentication extends AbstractAuthenticationToken {
  private final TokenResponse tokenResponse;

  public KoppeltaalAuthentication(Collection<? extends GrantedAuthority> authorities, TokenResponse tokenResponse) {
    super(authorities);
    this.tokenResponse = tokenResponse;
  }

  @Override
  public Object getCredentials() {
    return "";
  }

  @Override
  public Object getPrincipal() {
    return this.tokenResponse.getIdToken();
  }
}
