package com.travel.place;

import com.travel.auth.UserAccount;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(
    name = "places",
    indexes = {
      @Index(name = "idx_places_geo", columnList = "latitude,longitude"),
      @Index(name = "idx_places_user_date", columnList = "user_id,visit_date")
    })
public class Place {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  UserAccount user;

  @Column(nullable = false, length = 120)
  String name;

  String country;
  String province;
  String city;
  String address;

  @Column(nullable = false)
  BigDecimal longitude;

  @Column(nullable = false)
  BigDecimal latitude;

  @Column(name = "visit_date", nullable = false)
  LocalDate visitDate;

  @Column(nullable = false)
  String status = "draft";

  @Column(name = "guide_content", columnDefinition = "TEXT")
  String guideContent;

  protected Place() {}

  public Long getId() {
    return id;
  }

  public Place(UserAccount user) {
    this.user = user;
  }
}
