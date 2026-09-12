package com.travel.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {
  private final AccountService accounts;

  public AdminController(AccountService accounts) {
    this.accounts = accounts;
  }

  public record StatusChange(@NotNull @Pattern(regexp = "active|disabled") String status) {}

  @GetMapping
  public List<UserView> list() {
    return accounts.listUsers();
  }

  @PutMapping("/{id}/status")
  public UserView status(@PathVariable Long id, @Valid @RequestBody StatusChange input) {
    return accounts.setStatus(id, input.status());
  }
}
