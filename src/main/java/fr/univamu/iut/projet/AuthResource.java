package fr.univamu.iut.projet;

import fr.univamu.iut.projet.LoginCredentials;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@ApplicationScoped
public class AuthResource {

    @Inject
    UserService userService; // Inject the UserService

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginCredentials credentials) {
        if (credentials == null || credentials.getEmail() == null || credentials.getPassword() == null) {
            // Consider if an empty password should be allowed for login based on your requirements
            return Response.status(Response.Status.BAD_REQUEST).entity("Email and password are required.").build();
        }

        // Call the modified authenticateUser which uses plain text comparison
        User authenticatedUser = userService.authenticateUser(credentials.getEmail(), credentials.getPassword());

        if (authenticatedUser != null) {
            // Authentication successful
            // The password field is already cleared in the service method before returning
            return Response.ok(authenticatedUser).build();
        } else {
            // Authentication failed
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid email or password.").build();
        }
    }
}