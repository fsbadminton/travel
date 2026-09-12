package com.travel.review;

import java.time.Instant;

public record ReviewVersionView(Long id, int versionNo, String snapshotJson, Instant createdAt) {
  static ReviewVersionView from(ReviewVersion v) {
    return new ReviewVersionView(v.id, v.versionNo, v.snapshotJson, v.createdAt);
  }
}
