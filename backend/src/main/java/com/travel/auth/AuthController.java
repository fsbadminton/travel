package com.travel.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AccountService accounts;
  private final SecurityContextRepository contexts;
  private final HttpSessionCsrfTokenRepository tokens;

  public AuthController(
      AccountService accounts,
      SecurityContextRepository contexts,
      HttpSessionCsrfTokenRepository tokens) {
    this.accounts = accounts;
    this.contexts = contexts;
    this.tokens = tokens;
  }

  public record Credentials(
      @NotBlank @Pattern(regexp = "[A-Za-z0-9_]{4,20}") String username,
      @NotBlank @Size(min = 8, max = 72) String password) {}

  public record PasswordChange(
      @NotBlank @Size(max = 72) String currentPassword,
      @NotBlank @Size(min = 8, max = 72) String newPassword) {}

  public record TokenView(String headerName, String token) {}

  @GetMapping("/csrf")
  public TokenView csrf(CsrfToken token) {
    return new TokenView(token.getHeaderName(), token.getToken());
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserView register(@Valid @RequestBody Credentials input) {
    return accounts.register(input.username(), input.password());
  }

  @PostMapping("/login")
  public UserView login(
      @Valid @RequestBody Credentials input,
      HttpServletRequest request,
      HttpServletResponse response) {
    AccountPrincipal principal = accounts.login(input.username(), input.password());
    UserView user = accounts.current(principal);
    // Discard the anonymous or previous user's session to prevent session fixation.
    if (request.getSession(false) != null) request.getSession(false).invalidate();
    request.getSession(true);
    var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(
        UsernamePasswordAuthenticationToken.authenticated(
            principal,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_" + user.role().toUpperCase(Locale.ROOT)))));
    SecurityContextHolder.setContext(context);
    contexts.saveContext(context, request, response);
    tokens.saveToken(null, request, response);
    return user;
  }

  @GetMapping("/me")
  public UserView me(@AuthenticationPrincipal AccountPrincipal principal) {
    if (principal == null) {
      throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
    }
    return accounts.current(principal);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(HttpServletRequest request) {
    if (request.getSession(false) != null) request.getSession(false).invalidate();
    SecurityContextHolder.clearContext();
  }

  @PutMapping("/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void password(
      @AuthenticationPrincipal AccountPrincipal principal,
      @Valid @RequestBody PasswordChange input,
      HttpServletRequest request) {
    accounts.changePassword(principal, input.currentPassword(), input.newPassword());
    logout(request);
  }
}
