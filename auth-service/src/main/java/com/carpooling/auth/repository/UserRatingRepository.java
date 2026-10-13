package com.carpooling.auth.repository;

import com.carpooling.auth.entity.UserRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRatingRepository extends JpaRepository<UserRating, Long> {
    List<UserRating> findByRatedUserIdOrderByCreatedAtDesc(Long ratedUserId);
    List<UserRating> findByRaterId(Long raterId);
}
