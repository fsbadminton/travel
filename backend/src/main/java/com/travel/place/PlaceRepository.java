package com.travel.place;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PlaceRepository extends JpaRepository<Place,Long> {
  List<Place> findByUserIdAndStatusOrderByVisitDateDesc(Long userId,String status);
  Optional<Place> findByIdAndUserId(Long id,Long userId);
}
