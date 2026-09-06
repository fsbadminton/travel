package com.travel.auth;

import java.io.Serializable;

public record AccountPrincipal(Long id, long sessionVersion) implements Serializable {}
