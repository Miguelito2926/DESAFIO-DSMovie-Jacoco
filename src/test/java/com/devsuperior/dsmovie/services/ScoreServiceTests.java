package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {

	@InjectMocks
	private ScoreService scoreService;

	@Mock
	private UserService userService;

	@Mock
	private MovieRepository movieRepository;

	@Mock
	private ScoreRepository scoreRepository;

	private MovieEntity movie;
	private UserEntity user;
	private ScoreDTO scoreDTO;
	private ScoreEntity score;
	private Long existingMovieId;
	private Long nonExistingMovieId;

	@BeforeEach
	void setUp() throws Exception {
		existingMovieId = 1L;
		nonExistingMovieId = 2L;

		movie = MovieFactory.createMovieEntity();
		user = UserFactory.createUserEntity();
		score = ScoreFactory.createScoreEntity();
		scoreDTO = new ScoreDTO(existingMovieId, 5.0);

		when(userService.authenticated()).thenReturn(user);

		when(movieRepository.findById(existingMovieId)).thenReturn(Optional.of(movie));
		when(movieRepository.findById(nonExistingMovieId)).thenReturn(Optional.empty());

		Mockito.when(scoreRepository.saveAndFlush(Mockito.<ScoreEntity>any()))
				.thenAnswer(invocation -> {
					ScoreEntity newScore = invocation.getArgument(0, ScoreEntity.class);
					movie.getScores().add(newScore);
					return newScore;
				});

		Mockito.when(movieRepository.save(Mockito.<MovieEntity>any())).thenReturn(movie);

	}


	@Test
	public void saveScoreShouldReturnMovieDTO() {

		MovieDTO result = scoreService.saveScore(scoreDTO);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(existingMovieId, result.getId());
		Assertions.assertEquals(movie.getTitle(), result.getTitle());
		Assertions.assertTrue(result.getScore() > 0.0);
		Assertions.assertTrue(result.getCount() >= 1);

		verify(userService, times(1)).authenticated();
		verify(movieRepository, times(1)).findById(existingMovieId);
		verify(scoreRepository, times(1)).saveAndFlush(Mockito.<ScoreEntity>any());
		verify(movieRepository, times(1)).save(Mockito.<MovieEntity>any());

	}
	
	@Test
	public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> scoreService.saveScore(new ScoreDTO(nonExistingMovieId, 4.0)));
	}
}
