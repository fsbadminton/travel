package com.travel.food;

import com.travel.auth.UserAccount;
import com.travel.place.Place;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "foods")
public class Food {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  UserAccount user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "place_id")
  Place place;

  @Column(nullable = false)
  String name;

  @Column(nullable = false)
  String address;

  @JdbcTypeCode(SqlTypes.DECIMAL)
  double longitude;
  @JdbcTypeCode(SqlTypes.DECIMAL)
  double latitude;
  @JdbcTypeCode(SqlTypes.DECIMAL)
  Double price;
  @JdbcTypeCode(SqlTypes.DECIMAL)
  Double rating;
  String openingHours;
  String link;
  String notes;
  String status = "draft";

  protected Food() {}

  public Food(UserAccount u) {
    user = u;
  }
}
