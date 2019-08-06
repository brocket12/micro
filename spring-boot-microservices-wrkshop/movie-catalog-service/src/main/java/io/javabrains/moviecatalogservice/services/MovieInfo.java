package io.javabrains.moviecatalogservice.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

import io.javabrains.moviecatalogservice.models.CatalogItem;
import io.javabrains.moviecatalogservice.models.Movie;
import io.javabrains.moviecatalogservice.models.Rating;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class MovieInfo {
	@Autowired
	RestTemplate restTemplate;

	@HystrixCommand(fallbackMethod = "getFallbackCatalogItem",
			//below is for bulkhead Design pattern
			threadPoolKey="movieInfoPool",
			threadPoolProperties= {
					//this many threads allowed for this bulkhead
					@HystrixProperty(name = "coreSize", value="20"),
					//this many threads can be queued up and ready to go next when
					//so they dont take up any threads
					//if beyond it will go to fallback method
					@HystrixProperty(name = "maxQueueSize", value="10"),
			})
	public CatalogItem getCatalogItem(Rating rating) {
		Movie movie = restTemplate.getForObject("http://movie-info-service/movies/" + rating.getMovieId(), Movie.class);
		return new CatalogItem(movie.getName(), movie.getDescription(), rating.getRating());

	}

	public CatalogItem getFallbackCatalogItem(Rating rating) {
		return new CatalogItem("Movie Name Not Found", "Movie not found", rating.getRating());
	}

}
