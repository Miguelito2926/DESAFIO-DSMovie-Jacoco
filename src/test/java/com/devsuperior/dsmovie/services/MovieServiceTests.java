package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {
	
	@InjectMocks
	private MovieService service;

	@Mock
	private MovieRepository repository;

	private Long existingId;
	private Long nonExistingId;
	private Long dependentId;

	private MovieEntity movie;
	private MovieDTO movieDTO;
	private PageRequest pageRequest;

	@BeforeEach
	void setUp() {

		existingId = 1L;
		nonExistingId = 1000L;
		dependentId = 3L;

		movie = MovieFactory.createMovieEntity();
		movieDTO = MovieFactory.createMovieDTO();
		pageRequest = PageRequest.of(0, 10);

		// Mock: findAll
		when(repository.searchByTitle(anyString(), eq(pageRequest)))
				.thenReturn(new PageImpl<>(List.of(movie)));

		// Mock: findById
		when(repository.findById(existingId)).thenReturn(Optional.of(movie));
		when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

		// Mock: save
		when(repository.save(any(MovieEntity.class))).thenReturn(movie);

		// Mock: getReferenceById
		when(repository.getReferenceById(existingId)).thenReturn(movie);
		when(repository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

		// Mock: existsById
		when(repository.existsById(existingId)).thenReturn(true);
		when(repository.existsById(nonExistingId)).thenReturn(false);
		when(repository.existsById(dependentId)).thenReturn(true);

		// Mock: deleteById
		doNothing().when(repository).deleteById(existingId);
		doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
	}

	@Test
	public void findAllShouldReturnPagedMovieDTO() {
		Page<MovieDTO> result = service.findAll("", pageRequest);
		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		verify(repository, times(1)).searchByTitle(any(), eq(pageRequest));
	}
	
	@Test
	public void findByIdShouldReturnMovieDTOWhenIdExists() {
		MovieDTO result = service.findById(existingId);

		assertNotNull(result);
		assertEquals(movie.getTitle(), result.getTitle());
		verify(repository).findById(existingId);
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExistingId);
		});
	}
	
	@Test
	public void insertShouldReturnMovieDTO() {
		MovieDTO result = service.insert(movieDTO);

		assertNotNull(result);
		assertEquals(movie.getTitle(), result.getTitle());
		verify(repository).save(any(MovieEntity.class));
	}
	
	@Test
	public void updateShouldReturnMovieDTOWhenIdExists() {
		MovieDTO result = service.update(existingId, movieDTO);
		assertNotNull(result);
		verify(repository).getReferenceById(existingId);
		verify(repository).save(movie);
	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		assertThrows(ResourceNotFoundException.class, () -> {
			service.update(nonExistingId, movieDTO);
		});
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		assertDoesNotThrow(() -> {
			service.delete(existingId);
		});
		verify(repository).deleteById(existingId);
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});
		verify(repository, never()).deleteById(nonExistingId);
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
		assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
		});
		verify(repository).deleteById(dependentId);
	}
}
