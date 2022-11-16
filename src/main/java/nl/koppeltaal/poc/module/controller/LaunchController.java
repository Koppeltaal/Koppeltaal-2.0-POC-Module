package nl.koppeltaal.poc.module.controller;

import nl.koppeltaal.poc.module.model.KoppeltaalAuthentication;
import nl.koppeltaal.poc.module.model.TokenResponse;
import nl.koppeltaal.poc.module.util.PkceUtil;
import nl.koppeltaal.spring.boot.starter.smartservice.configuration.SmartServiceConfiguration;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.FhirCapabilitiesService;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.SmartClientCredentialService;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class LaunchController {

  private final FhirCapabilitiesService capabilitiesService;
  private final SmartServiceConfiguration smartServiceConfiguration;

  private final SmartClientCredentialService smartClientCredentialService;
  private final Map<String, String> stateToRedirectUrlMap = new HashMap<>();
  private final Map<String, String> stateToVerifierMap = new HashMap<>();
  private final RestTemplate restTemplate = new RestTemplate();

  public LaunchController(FhirCapabilitiesService capabilitiesService,
                          SmartServiceConfiguration smartServiceConfiguration,
                          SmartClientCredentialService smartClientCredentialService) {
    this.capabilitiesService = capabilitiesService;
    this.smartServiceConfiguration = smartServiceConfiguration;
    this.smartClientCredentialService = smartClientCredentialService;
  }

  @GetMapping("module_launch")
  public View launchSHOF(@RequestParam String iss, @RequestParam String launch, HttpServletRequest request, RedirectAttributes redirectAttributes) {
    final String authorizeUrl = capabilitiesService.getAuthorizeUrl();
    final String state = UUID.randomUUID().toString();
    final String codeUrl = request.getRequestURL().toString().replace("module_launch", "consume_code");
    final String codeVerifier  = PkceUtil.generateCodeVerifier();
    stateToRedirectUrlMap.put(state, codeUrl);
    stateToRedirectUrlMap.put(state, codeVerifier);

    redirectAttributes.addAttribute("aud", iss);
    redirectAttributes.addAttribute("launch", launch);
    redirectAttributes.addAttribute("client_id", smartServiceConfiguration.getClientId());
    redirectAttributes.addAttribute("scope", smartServiceConfiguration.getScope());
    redirectAttributes.addAttribute("state", state);
    redirectAttributes.addAttribute("redirect_uri", codeUrl);
    redirectAttributes.addAttribute("code_challenge", PkceUtil.generateCodeChallenge(codeVerifier));
    redirectAttributes.addAttribute("code_challenge_method", "S256");

    return new RedirectView(authorizeUrl);
  }

  @GetMapping("consume_code")
  public String consumeCode(@RequestParam String code, @RequestParam String state, ModelMap modelMap, HttpServletRequest request) {

    final String tokenUrl = capabilitiesService.getTokenUrl();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    final LinkedMultiValueMap<String, String> attributes = new LinkedMultiValueMap<>();
    attributes.add("grant_type", "authorization_code");
    attributes.add("code", code);
    attributes.add("redirect_uri", stateToRedirectUrlMap.get(state));


    attributes.add("client_assertion_type", SmartClientCredentialService.CLIENT_ASSERTION_TYPE);
    attributes.add("client_assertion", smartClientCredentialService.getSmartServiceClientAssertion(tokenUrl));

    attributes.add("code_verifier", stateToVerifierMap.get(state));

    HttpEntity<LinkedMultiValueMap<String, String>> entity = new HttpEntity<>(attributes, headers);

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
