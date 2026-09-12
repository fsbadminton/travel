package com.travel.config;

import com.travel.auth.AccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner {
  private final AccountService accounts;
  private final String username;
  private final String password;

  public AdminInitializer(
      AccountService accounts,
      @Value("${app.admin.username:}") String username,
      @Value("${app.admin.password:}") String password) {
    this.accounts = accounts;
    this.username = username;
    this.password = password;
  }

  @Override
  public void run(ApplicationArguments args) {
    accounts.initializeAdmin(username, password);
  }
}
