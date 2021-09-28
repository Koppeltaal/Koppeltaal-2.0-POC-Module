package nl.koppeltaal.poc.module.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import nl.koppeltaal.poc.module.model.KoppeltaalAuthentication;
import nl.koppeltaal.poc.module.model.TokenResponse;
import nl.koppeltaal.spring.boot.starter.smartservice.configuration.SmartServiceConfiguration;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.FhirCapabilitiesService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
public class LaunchController {

  private final FhirCapabilitiesService capabilitiesService;
  private final SmartServiceConfiguration smartServiceConfiguration;
  private final Map<String, String> stateToRedirectUrlMap = new HashMap<>();
  private final RestTemplate restTemplate = new RestTemplate();

  public LaunchController(FhirCapabilitiesService capabilitiesService,
      SmartServiceConfiguration smartServiceConfiguration) {
    this.capabilitiesService = capabilitiesService;
    this.smartServiceConfiguration = smartServiceConfiguration;
  }

  @GetMapping("module_launch")
  public String launchSHOF(@RequestParam String iss, @RequestParam String launch, HttpServletRequest request) {
    final String authorizeUrl = capabilitiesService.getAuthorizeUrl();
    final String state = UUID.randomUUID().toString();
    final String codeUrl = URLEncoder.encode(request.getRequestURL().toString().replace("module_launch", "consume_code"),
        StandardCharsets.UTF_8);

    final String redirectUrl = String.format(
        "%s?"
            + "aud=%s&"
            + "launch=%s&"
            + "response_type=code&"
            + "client_id=%s&"
            + "scope=%s&"
            + "state=%s&"
            + "redirect_url=%s"
        , authorizeUrl, iss, launch, smartServiceConfiguration.getClientId(),
        smartServiceConfiguration.getScope(), state, codeUrl);

    stateToRedirectUrlMap.put(state, redirectUrl);

    return "redirect:" + redirectUrl;
  }

  @GetMapping("consume_code")
  public String consumeCode(@RequestParam String code, @RequestParam String state, ModelMap modelMap, HttpServletRequest request) {

    final String tokenUrl = capabilitiesService.getTokenUrl();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    final Map<String, String> attributes = new HashMap<>();
    attributes.put("grant_type", "authorization_code");
    attributes.put("code", code);
    attributes.put("redirect_uri", stateToRedirectUrlMap.get(state));

    HttpEntity<Map<String, String>> entity = new HttpEntity<>(attributes, headers);

    final ResponseEntity<TokenResponse> tokenResponseEntity = restTemplate.exchange(
        tokenUrl,
        HttpMethod.POST,
        entity,
        TokenResponse.class
    );

    final TokenResponse tokenResponse = tokenResponseEntity.getBody();
    final KoppeltaalAuthentication authentication = new KoppeltaalAuthentication(
        Collections.emptyList(), tokenResponse);

    SecurityContextHolder.getContext().setAuthentication(authentication);

    modelMap.addAttribute("tokenResponse", tokenResponse);

    stateToRedirectUrlMap.remove(state);

    return "index";
  }

  @GetMapping("consume_token")
  public String login() {
    return "index";
  }

}
