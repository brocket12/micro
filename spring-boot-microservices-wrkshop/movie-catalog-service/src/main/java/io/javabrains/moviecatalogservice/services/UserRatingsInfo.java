package io.javabrains.moviecatalogservice.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;


import io.javabrains.moviecatalogservice.models.CatalogItem;
import io.javabrains.moviecatalogservice.models.Movie;
import io.javabrains.moviecatalogservice.models.Rating;
import io.javabrains.moviecatalogservice.models.UserRating;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class UserRatingsInfo {
	@Autowired
	RestTemplate restTemplate;

	@HystrixCommand(fallbackMethod = "getFallbackUserRating",
			commandProperties = {
					//timeout before breaking circuit
					@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value="2000"),
					//look at last n request
					@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value="5"),
					//50 percent of last n request fail then break
					@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value="50"),
					//how long of a break this service gets before it is requested again.
					@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value="5000"),
	})
	public UserRating getUserRating(String userId) {
		return restTemplate.getForObject("http://ratings-data-service/ratingsdata/user/" + userId, UserRating.class);
	}

	public UserRating getFallbackUserRating(String userId) {
		UserRating userRating = new UserRating();
		userRating.setUserId(userId);
		userRating.setRatings(Arrays.asList(new Rating("0", 0)));
		return userRating;
	}

}
