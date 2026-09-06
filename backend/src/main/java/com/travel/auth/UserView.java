package com.travel.auth;

import java.time.Instant;

public record UserView(Long id, String username, String role, String status, Instant createdAt) {
  static UserView from(UserAccount user) {
    return new UserView(user.id, user.username, user.role, user.status, user.createdAt);
  }
}
