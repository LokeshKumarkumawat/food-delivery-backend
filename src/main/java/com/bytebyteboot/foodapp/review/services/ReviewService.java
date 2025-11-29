package com.bytebyteboot.foodapp.review.services;

import com.bytebyteboot.foodapp.response.Response;
import com.bytebyteboot.foodapp.review.dtos.ReviewDTO;

import java.util.List;

public interface ReviewService {
    Response<ReviewDTO> createReview(ReviewDTO reviewDTO);
    Response<List<ReviewDTO>> getReviewsForMenu(Long menuId);
    Response<Double> getAverageRating(Long menuId);
}
