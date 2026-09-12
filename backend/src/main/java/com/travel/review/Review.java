package com.travel.review;

import com.travel.place.Place;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reviews")
public class Review {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "place_id", unique = true)
  Place place;

  Integer stars;

  @Column(columnDefinition = "TEXT")
  String content;

  @Column(columnDefinition = "TEXT")
  String pros;

  @Column(columnDefinition = "TEXT")
  String cons;

  Integer recommendScore;

  @Column(length = 1000)
  String enabledFieldsJson;

  @Column(nullable = false)
  Instant createdAt;

  @Column(nullable = false)
  Instant updatedAt;

  protected Review() {}

  Review(Place p) {
    place = p;
  }

  @PrePersist
  void created() {
    createdAt = updatedAt = Instant.now();
  }

  @PreUpdate
  void updated() {
    updatedAt = Instant.now();
  }
}
