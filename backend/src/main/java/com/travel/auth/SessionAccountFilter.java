package com.travel.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// Recheck persisted account state so password changes and disabling revoke existing sessions.
public class SessionAccountFilter extends OncePerRequestFilter {
  private final UserRepository users;
  public SessionAccountFilter(UserRepository users) { this.users = users; }

  @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof AccountPrincipal principal) {
      var account = users.findById(principal.id());
      if (account.isEmpty() || !"active".equals(account.get().status)
          || account.get().sessionVersion != principal.sessionVersion()) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
      }
    }
    chain.doFilter(request, response);
  }
}
