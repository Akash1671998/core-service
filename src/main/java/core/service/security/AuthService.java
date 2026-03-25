
package core.service.security;
import core.service.auth.dto.AuthResponse;
import core.service.auth.dto.LoginRequest;
import core.service.auth.dto.RegisterRequest;
import core.service.auth.dto.UserResponse;
import core.service.auth.entity.User;
import core.service.auth.repository.UserRepository;
import core.service.response.RequestResponse;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public RequestResponse<?> register(RegisterRequest request) {
        try {
            Optional<User> existUser = repo.findByUsername(request.getUsername());
            if (existUser.isPresent()) {
                return new RequestResponse<>(400, "User already exist kindly create another user", null);
            }
            User user = new User();
            user.setName(request.getName());
            user.setUsername(request.getUsername());
            user.setPassword(encoder.encode(request.getPassword()));
            user.setRole(request.getRole());

            repo.save(user);
            return new RequestResponse<>(200, "User registered successfully", null);
        } catch (Exception e) {

            return new RequestResponse<>(500, "Something went wrong: " + e.getMessage(), null);
        }

    }

    public RequestResponse<?> login(LoginRequest request) {

        try {
            User user = repo.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!encoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid password");
            }

            String token = jwtService.generateToken(user.getUsername());

            AuthResponse authResponse = new AuthResponse(
                    token,
                    user.getUsername(),
                    user.getRole(),
                    user.getId());
            return new RequestResponse<>(200, "Login successful", authResponse);

        } catch (RuntimeException e) {
            return new RequestResponse<>(400, e.getMessage(), null);

        } catch (Exception e) {
            return new RequestResponse<>(500, "Something went Wrong", null);
        }

    }

    public RequestResponse<?> getAllUsers() {
        try {
            List<User> users = repo.findAll();
    
            if (users == null || users.isEmpty()) {
                return new RequestResponse<>(200, "User Not Found", null);
            }
            List<UserResponse> allUser = new ArrayList<>();
            for (User user : users) {
                UserResponse response = new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getRole(),
                        user.getUsername()
                );
                allUser.add(response);
            }
            return new RequestResponse<>(200, "Users fetched successfully", allUser);
        } catch (Exception e) {
            return new RequestResponse<>(500, "Something went wrong: " + e.getMessage(), null);
        }
    }
}