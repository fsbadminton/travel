package com.travel.auth;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class UserAccount {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(nullable = false, unique = true, length = 20)
  String username;

  @Column(name = "password_hash", nullable = false, length = 100)
  String passwordHash;

  @Column(nullable = false, length = 20)
  String role = "user";

  @Column(nullable = false, length = 20)
  String status = "active";

  @Column(name = "session_version", nullable = false)
  long sessionVersion;

  @Column(name = "created_at", nullable = false)
  Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  Instant updatedAt;

  protected UserAccount() {}

  UserAccount(String username, String passwordHash, String role) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.role = role;
  }

  @PrePersist
  void onCreate() {
    createdAt = updatedAt = Instant.now();
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }
}
