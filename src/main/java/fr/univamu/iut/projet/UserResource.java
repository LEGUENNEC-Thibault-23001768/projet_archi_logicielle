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

    // ... (les méthodes GET, PUT, DELETE restent inchangées) ...

    /**
     * Endpoint permettant de creer un utilisateur (modifié pour accepter TEXT_PLAIN)
     * @param userDataString Le corps de la requête reçu comme texte brut (attendu comme JSON stringifié)
     * @return une réponse "created" si la création a été effectuée, une erreur sinon
     */
    @POST
    // @Consumes(MediaType.APPLICATION_JSON) // <-- Ancienne ligne
    @Consumes(MediaType.TEXT_PLAIN)       // <-- Nouvelle ligne: Accepter du texte brut
    public Response createUser(String userDataString ){ // <-- Changer le paramètre en String

        User user = null;
        try (Jsonb jsonb = JsonbBuilder.create()) {
            // Essayer de parser la chaîne reçue en objet User
            user = jsonb.fromJson(userDataString, User.class);
        } catch (JsonbException | NullPointerException e) {
             // Gérer les erreurs de parsing JSON ou si la chaîne est nulle
            System.err.println("Erreur de parsing JSON pour la création d'utilisateur: " + e.getMessage());
            System.err.println("Chaîne reçue: " + userDataString);
            // Retourner une erreur Bad Request si le JSON est invalide
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Format JSON invalide reçu dans le corps de la requête.")
                           .build();
        } catch (Exception e) {
             // Gérer d'autres erreurs potentielles liées à Jsonb
             System.err.println("Erreur inattendue lors du parsing JSON: " + e.getMessage());
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Erreur interne lors du traitement de la requête.")
                            .build();
        }

        // Vérifier si l'objet User a pu être créé à partir du JSON
        if (user == null) {
             return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Impossible de créer l'objet utilisateur à partir des données fournies.")
                            .build();
        }

        // --- Logique de création existante ---
        // Ajouter des logs pour vérifier l'objet User parsé
        System.out.println("Tentative de création de l'utilisateur (parsé depuis TEXT_PLAIN): " + user);

        // si l'utilisateur n'a pas pu être créé (ex: conflit géré par le service)
        if( ! userService.createUser(user) ) {
             // L'erreur 409 (Conflict) est gérée par le service/repo si l'email/id existe déjà
             // On assume que createUser retourne false en cas de conflit connu
             System.err.println("Conflit détecté par userService.createUser pour: " + user.getEmail());
             return Response.status(Response.Status.CONFLICT)
                            .entity("Utilisateur déjà existant (email ou id).")
                            .build();
        }
        else {
             System.out.println("Utilisateur créé avec succès: " + user.getEmail());
             // ATTENTION: Ne pas renvoyer l'objet User ici car le navigateur
             // avec no-cors ne pourra probablement pas lire la réponse JSON.
             // Renvoyer une réponse simple textuelle.
             // return Response.ok(user).build(); // <-- Ne marchera pas avec no-cors côté client
             return Response.ok("created").type(MediaType.TEXT_PLAIN).build(); // <-- Réponse simple
        }
    }
}