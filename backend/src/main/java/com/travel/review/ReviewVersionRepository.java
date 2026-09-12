package com.travel.review;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewVersionRepository extends JpaRepository<ReviewVersion, Long> {
  List<ReviewVersion> findByReviewIdOrderByVersionNoDesc(Long reviewId);

  int countByReviewId(Long reviewId);
}
