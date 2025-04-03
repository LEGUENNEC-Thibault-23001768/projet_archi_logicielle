package fr.univamu.iut.projet;

import com.nimbusds.jose.JOSEException;
import fr.univamu.iut.projet.LoginCredentials;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/auth")
@ApplicationScoped
public class AuthResource {

    @Inject
    UserService userService; // Inject the UserService

    @Inject
    JwtUtil jwtUtil;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginCredentials credentials) {
        if (credentials == null || credentials.getEmail() == null || credentials.getPassword() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Email and password are required.").build();
        }

        User authenticatedUser = userService.authenticateUser(credentials.getEmail(), credentials.getPassword());

        if (authenticatedUser != null) {
            try {
                List<String> roles = userService.getUserRoles(authenticatedUser.getId());
                if (roles == null) {
                    roles = Collections.singletonList("user");
                }


                String token = jwtUtil.generateToken(authenticatedUser, roles);

                Map<String, Object> responseData = new HashMap<>();
                responseData.put("token", token);
                authenticatedUser.setPassword(null);
                responseData.put("user", authenticatedUser);

                return Response.ok(responseData).build();

            } catch (JOSEException e) {
                System.err.println("Erreur JWT generation: " + e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erreur interne lors de la connexion.").build();
            } catch (Exception e) {
                System.err.println("Erreur login: " + e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erreur interne.").build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Email ou mot de passe invalide.").build();
        }

    }
}
