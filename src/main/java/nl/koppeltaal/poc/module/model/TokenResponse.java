package nl.koppeltaal.poc.module.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenResponse {

  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("token_type")
  private String tokenType;

  @JsonProperty("expires_in")
  private Integer expiresIn;

  private String scope;
  private String sub;
  @JsonProperty("id_token")
  private String idToken;
  @JsonProperty("refresh_token")
  private String refreshToken;

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public Integer getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(Integer expiresIn) {
    this.expiresIn = expiresIn;
  }

  public String getIdToken() {
    return idToken;
  }

  public void setIdToken(String idToken) {
    this.idToken = idToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public String getSub() {
    return sub;
  }

  public void setSub(String sub) {
    this.sub = sub;
  }

  @Override
  public String toString() {
    return "TokenResponse{" +
        "accessToken='" + accessToken + '\'' +
        ", tokenType='" + tokenType + '\'' +
        ", expiresIn=" + expiresIn +
        ", scope='" + scope + '\'' +
        ", idToken='" + idToken + '\'' +
        ", refreshToken='" + refreshToken + '\'' +
        '}';
  }
}
