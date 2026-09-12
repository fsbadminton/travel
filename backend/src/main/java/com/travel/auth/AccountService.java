package com.travel.auth;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccountService {
  private final UserRepository users;
  private final PasswordEncoder passwords;
  private final String dummyHash;

  public AccountService(UserRepository users, PasswordEncoder passwords) {
    this.users = users;
    this.passwords = passwords;
    this.dummyHash = passwords.encode("unused-password-for-timing");
  }

  @Transactional
  public UserView register(String username, String password) {
    validateCredentials(username, password);
    String normalized = username.toLowerCase(Locale.ROOT);
    if (users.findByUsername(normalized).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "用户名已存在");
    }
    return UserView.from(
        users.saveAndFlush(new UserAccount(normalized, passwords.encode(password), "user")));
  }

  public AccountPrincipal login(String username, String password) {
    UserAccount user = users.findByUsername(username.toLowerCase(Locale.ROOT)).orElse(null);
    boolean matches = passwords.matches(password, user == null ? dummyHash : user.passwordHash);
    if (!matches || user == null || !"active".equals(user.status)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误，或账号已停用");
    }
    return new AccountPrincipal(user.id, user.sessionVersion);
  }

  public UserView current(AccountPrincipal principal) {
    return UserView.from(
        users
            .findById(principal.id())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请重新登录")));
  }

  @Transactional
  public void changePassword(AccountPrincipal principal, String oldPassword, String newPassword) {
    validatePassword(newPassword);
    UserAccount user = users.findForUpdateById(principal.id()).orElseThrow();
    if (user.sessionVersion != principal.sessionVersion()
        || !passwords.matches(oldPassword, user.passwordHash)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前密码不正确");
    }
    user.passwordHash = passwords.encode(newPassword);
    user.sessionVersion++;
  }

  public List<UserView> listUsers() {
    return users.findAll(org.springframework.data.domain.Sort.by("id")).stream()
        .map(UserView::from)
        .toList();
  }

  @Transactional
  public UserView setStatus(Long id, String status) {
    if (!List.of("active", "disabled").contains(status)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "账号状态无效");
    }
    UserAccount user =
        users
            .findForUpdateById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
    if ("admin".equals(user.role)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "不能停用管理员账号");
    }
    if (!user.status.equals(status)) {
      user.status = status;
      user.sessionVersion++;
    }
    return UserView.from(user);
  }

  @Transactional
  public void initializeAdmin(String username, String password) {
    if (username.isBlank() && password.isBlank()) return;
    validateCredentials(username, password);
    String normalized = username.toLowerCase(Locale.ROOT);
    UserAccount existing = users.findByUsername(normalized).orElse(null);
    if (existing != null) {
      if (!"admin".equals(existing.role)) throw new IllegalStateException("管理员用户名已被普通用户占用");
      return;
    }
    users.saveAndFlush(new UserAccount(normalized, passwords.encode(password), "admin"));
  }

  static void validateCredentials(String username, String password) {
    if (username == null || !username.matches("[A-Za-z0-9_]{4,20}")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名须为 4-20 位字母、数字或下划线");
    }
    validatePassword(password);
  }

  static void validatePassword(String password) {
    if (password == null
        || password.length() < 8
        || password.getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "密码至少 8 位，UTF-8 编码不能超过 72 字节");
    }
  }
}
