// projet_archi_logicielle/produits-utilisateurs/src/main/java/fr/univamu/iut/projet/UserResource.java

package fr.univamu.iut.projet;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException; // Importer l'exception

@Path("/users")
public class UserResource {

    @Inject
    UserService userService;


    /**
     * Endpoint permettant de creer un utilisateur (modifié pour accepter TEXT_PLAIN)
     * @param userDataString Le corps de la requête reçu comme texte brut (attendu comme JSON stringifié)
     * @return une réponse "created" si la création a été effectuée, une erreur sinon
     */
    @POST
    // @Consumes(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
    public Response createUser(String userDataString ){

        User user = null;
        try (Jsonb jsonb = JsonbBuilder.create()) {
            user = jsonb.fromJson(userDataString, User.class);
        } catch (JsonbException | NullPointerException e) {
            System.err.println("Erreur de parsing JSON pour la création d'utilisateur: " + e.getMessage());
            System.err.println("Chaîne reçue: " + userDataString);
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Format JSON invalide reçu dans le corps de la requête.")
                           .build();
        } catch (Exception e) {
             System.err.println("Erreur inattendue lors du parsing JSON: " + e.getMessage());
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Erreur interne lors du traitement de la requête.")
                            .build();
        }

        if (user == null) {
             return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Impossible de créer l'objet utilisateur à partir des données fournies.")
                            .build();
        }

        System.out.println("Tentative de création de l'utilisateur (parsé depuis TEXT_PLAIN): " + user);

        if( ! userService.createUser(user) ) {
             System.err.println("Conflit détecté par userService.createUser pour: " + user.getEmail());
             return Response.status(Response.Status.CONFLICT)
                            .entity("Utilisateur déjà existant (email ou id).")
                            .build();
        }
        else {
             System.out.println("Utilisateur créé avec succès: " + user.getEmail());
             return Response.ok("created").type(MediaType.TEXT_PLAIN).build(); 
        }
    }
}