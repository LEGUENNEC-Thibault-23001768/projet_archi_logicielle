package fr.univamu.iut.projet;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users")
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        String usersJson = userService.getAllUsersJSON();
        return Response.ok(usersJson).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("id") String id) {
        String userJson = userService.getUserJSON(id);
        if (userJson == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(userJson).build();
    }

    /**
     * Endpoint permettant de publier les informations d'un utilisateur dont l'email est passé paramètre dans le chemin
     * @param email email de l'utilisateur recherché
     * @return les informations de l'utilisateur recherché au format JSON
     */
    @GET
    @Path("/email/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserByEmail(@PathParam("email") String email){
        String result = userService.getUserByEmailJSON(email);

        // si l'utilisateur n'a pas été trouvé
        if( result == null )
            throw new NotFoundException();

        return Response.ok(result).build();
    }


    /**
     * Endpoint permettant de mettre à jours un utilisateur
     * @param id la référence de l'utilisateur dont il faut changer les informations
     * @param user l'utilisateur transmis en HTTP au format JSON et convertit en objet User
     * @return une réponse "updated" si la mise à jour a été effectuée, une erreur NotFound sinon
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("id") String id, User user ){

        // si l'utilisateur n'a pas été trouvé
        if( ! userService.updateUser(id, user) )
            throw new NotFoundException();
        else
            return Response.ok("updated").build();
    }

    /**
     * Endpoint permettant de supprimer un utilisateur
     * @param id la référence de l'utilisateur à supprimer
     * @return une réponse "deleted" si la suppression a été effectuée, une erreur NotFound sinon
     */
    @DELETE
    @Path("{id}")
    public Response deleteUser(@PathParam("id") String id ){

        // si l'utilisateur n'a pas été trouvé
        if( ! userService.deleteUser(id) )
            throw new NotFoundException();
        else
            return Response.ok("deleted").build();
    }


    /**
     * Endpoint permettant de creer un utilisateur
     * @param user l'utilisateur transmis en HTTP au format JSON et convertit en objet User
     * @return une réponse "created" si la création a été effectuée, une erreur Conflict sinon
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(User user ){

        // si l'utilisateur n'a pas pu être créé (id déjà utilisé)
        if( ! userService.createUser(user) )
            return Response.status(Response.Status.CONFLICT).build();
        else
            return Response.ok("created").build();
    }
}