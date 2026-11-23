package com.example.pizzabolchat.repository;

import com.example.pizzabolchat.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    boolean existsByChatIdAndUserIdAndRatedUserId(Long chatId, Long userId, Long ratedUserId);
}
