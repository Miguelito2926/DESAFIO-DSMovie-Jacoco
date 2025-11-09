package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.UserRepository;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import com.devsuperior.dsmovie.utils.CustomUserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class UserServiceTests {

	@InjectMocks
	private UserService service;

	@Mock
	private UserRepository repository;

	@Mock
	private CustomUserUtil userUtil;

	private UserEntity user;

	@BeforeEach
	void setUp() {
		user = UserFactory.createUserEntity();
	}

	@Test
	@DisplayName("authenticatedShouldReturnUserEntityWhenUserExists")
	public void authenticatedShouldReturnUserEntityWhenUserExists() {

		when(userUtil.getLoggedUsername()).thenReturn(user.getUsername());
		when(repository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

		UserEntity result = service.authenticated();

		assertNotNull(result);
		assertEquals(user.getUsername(), result.getUsername());
		verify(userUtil).getLoggedUsername();
		verify(repository).findByUsername(user.getUsername());
	}

	@Test
	@DisplayName("authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists")
	public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {

		when(userUtil.getLoggedUsername()).thenReturn("notfound@gmail.com");
		when(repository.findByUsername("notfound@gmail.com")).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class, () -> service.authenticated());
		verify(userUtil).getLoggedUsername();
		verify(repository).findByUsername("notfound@gmail.com");
	}

	@Test
	@DisplayName("loadUserByUsernameShouldReturnUserDetailsWhenUserExists")
	public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {

		String username = "maria@gmail.com";
		List<UserDetailsProjection> projections =
				UserDetailsFactory.createCustomAdminClientUser(username);

		when(repository.searchUserAndRolesByUsername(username)).thenReturn(projections);

		UserDetails result = service.loadUserByUsername(username);

		assertNotNull(result);
		assertEquals(username, result.getUsername());
		assertEquals("123", result.getPassword());
		assertTrue(result.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT")));
		assertTrue(result.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

		verify(repository).searchUserAndRolesByUsername(username);
	}

	@Test
	@DisplayName("loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists")
	public void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {

		String username = "notfound@gmail.com";
		when(repository.searchUserAndRolesByUsername(username)).thenReturn(List.of());

		assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(username));
		verify(repository).searchUserAndRolesByUsername(username);
	}
}
