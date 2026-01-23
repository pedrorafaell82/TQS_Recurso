package pedro.tqs.user;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import pedro.tqs.auth.dto.RegisterRequest;
import pedro.tqs.user.exception.EmailAlreadyExistsException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Test
    void register_success_normalizesEmail_assignsVolunteerRole_andHashesPassword() {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserService service = new UserService(repo, encoder);

        when(repo.existsByEmail("user@test.com")).thenReturn(false);
        when(encoder.encode("strongPass1")).thenReturn("HASH");
        when(repo.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = service.register(new RegisterRequest("  Name  ", "USER@Test.com ", "strongPass1"));

        assertEquals("user@test.com", resp.email());
        assertTrue(resp.roles().contains("VOLUNTEER"));

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(repo).save(captor.capture());
        AppUser saved = captor.getValue();

        assertEquals("Name", saved.getName());
        assertEquals("user@test.com", saved.getEmail());
        assertEquals("HASH", saved.getPasswordHash());
        assertEquals(Set.of(Role.VOLUNTEER), saved.getRoles());

        verify(encoder).encode("strongPass1");
    }

    @Test
    void register_duplicateEmail_throws() {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserService service = new UserService(repo, encoder);

        when(repo.existsByEmail("user@test.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> service.register(new RegisterRequest("Name", "user@test.com", "strongPass1")));

        verify(repo, never()).save(any());
        verifyNoInteractions(encoder);
    }

    @Test
    void loadByEmail_existing_returnsUser() {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserService service = new UserService(repo, encoder);

        AppUser u = mock(AppUser.class);
        when(repo.findByEmail("a@b.com")).thenReturn(Optional.of(u));

        assertSame(u, service.loadByEmail("A@B.com"));
    }

    @Test
    void loadByEmail_missing_throws() {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserService service = new UserService(repo, encoder);

        when(repo.findByEmail("a@b.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.loadByEmail("A@B.com"));
    }
}
