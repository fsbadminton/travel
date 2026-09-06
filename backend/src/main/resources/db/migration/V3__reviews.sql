CREATE TABLE reviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  place_id BIGINT NOT NULL UNIQUE,
  stars INT NULL,
  content TEXT,
  pros TEXT,
  cons TEXT,
  recommend_score INT NULL,
  enabled_fields_json VARCHAR(1000),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_reviews_place FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE,
  CONSTRAINT chk_reviews_stars CHECK (stars IS NULL OR (stars BETWEEN 1 AND 5)),
  CONSTRAINT chk_reviews_recommend CHECK (recommend_score IS NULL OR (recommend_score BETWEEN 1 AND 5))
);
CREATE TABLE review_versions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  review_id BIGINT NOT NULL,
  snapshot_json TEXT NOT NULL,
  version_no INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_review_versions_review FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE,
  UNIQUE KEY uk_review_version (review_id, version_no)
);

CREATE TABLE media (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  place_id BIGINT NULL,
  food_id BIGINT NULL,
  original_path VARCHAR(500) NOT NULL,
  thumbnail_path VARCHAR(500) NOT NULL,
  original_filename VARCHAR(255) NOT NULL,
  mime_type VARCHAR(100) NOT NULL,
  size_bytes BIGINT NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_media_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_media_place FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE,
  INDEX idx_media_place (place_id), INDEX idx_media_food (food_id)
);
