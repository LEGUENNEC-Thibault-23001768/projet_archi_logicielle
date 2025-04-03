package fr.univamu.iut.projet.commande.controller;

import fr.univamu.iut.projet.commande.entity.Commande;
import fr.univamu.iut.projet.commande.service.CommandeService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des commandes.
 */

@Path("/commandes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommandeController {

    @Inject
    private CommandeService commandeService;

    /**
     * Récupère les commandes pour un client spécifique OU toutes les commandes (si aucun clientId n'est fourni)
     * GET /api/commandes?clientId={clientId}
     * @param clientId (Optionnel) ID du client dont on veut les commandes.
     * @return Réponse HTTP avec la liste des commandes filtrées ou toutes les commandes.
     */
    @GET
    public Response getClientCommandes(@QueryParam("clientId") Integer clientId) {
        List<Commande> commandes;
        if (clientId != null ) {
            System.out.println("Récupération des commandes pour clientId: " + clientId);
            commandes = commandeService.getCommandesByClientId(clientId);
        } else {
            System.out.println("Récupération de TOUTES les commandes (aucun clientId fourni).");
            commandes = commandeService.getAllCommandes();
        }
        return Response.ok(commandes).build();
    }

    /**
     * Endpoint pour récupérer une commande par son ID.
     * GET /api/commandes/{id}
     * @param id id de la commande à récupérer
     * @return Réponse HTTP 200 avec la commande si trouvée, err 404 sinon
     */
    @GET
    @Path("/{id}")
    public Response getCommandeById(@PathParam("id") Integer id) {
        Commande commande = commandeService.getCommandeById(id);
        if (commande == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Commande non trouvée pour l'ID: " + id)
                    .build();
        }
        return Response.ok(commande).build();
    }

    /**
     * Endpoint pour créer une nouvelle commande.
     * POST /api/commandes
     * @param commande La commande à créer
     * @return Réponse HTTP 201 (Créée)
     *         Réponse HTTP 400 (Bad Request)
     */
    @POST
    public Response createCommande(Commande commande) {
        if (commande == null || commande.getClientId() == null || commande.getPanierId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Données de commande incomplètes ou invalides.")
                    .build();
        }

        Commande createdCommande = commandeService.createCommande(commande);
        URI location = UriBuilder.fromResource(CommandeController.class)
                .path(String.valueOf(createdCommande.getId()))
                .build();

        return Response.created(location).entity(createdCommande).build();
    }

    /**
     * Endpoint pour mettre à jour une commande existante.
     * PUT /api/commandes/{id}
     * @param id id de la commande à mettre à jour
     * @param commandeDetails  nouvelles données de la commande
     * @return Réponse HTTP 200 avec la commande mise à jour si succès
     *         Réponse HTTP 404 si l'ID n'existe pas
     *         Réponse HTTP 400 si les données sont invalides
     */
    @PUT
    @Path("/{id}")
    public Response updateCommande(@PathParam("id") Integer id, Commande commandeDetails) {
        if (commandeDetails == null || commandeDetails.getClientId() == null ) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Données de commande incomplètes ou invalides pour la mise à jour.")
                    .build();
        }

        Commande updatedCommande = commandeService.updateCommande(id, commandeDetails);
        if (updatedCommande == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Impossible de mettre à jour, commande non trouvée pour l'ID: " + id)
                    .build();
        }
        return Response.ok(updatedCommande).build();
    }

    /**
     * Endpoint pour supprimer une commande.
     * DELETE /api/commandes/{id}
     * @param id id de la commande à supprimer.
     * @return Réponse HTTP 204 si succès.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteCommande(@PathParam("id") Integer id) {
        commandeService.deleteCommande(id);
        return Response.noContent().build();
    }

    /**
     * Endpoint pour valider une commande
     * PATCH ou PUT /api/commandes/{id}/validation
     * @param id id de la commande à valider
     * @return Réponse HTTP 200 avec la commande validée, 404 ou 409 si non possible.
     */
    @POST
    @Path("/{id}/validation")
    public Response validateCommande(@PathParam("id") Integer id) {
        Commande commandeValidee = commandeService.validateCommande(id);
        if (commandeValidee == null) {
            Commande existing = commandeService.getCommandeById(id);
            if (existing == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Commande non trouvée pour validation: " + id).build();
            } else {
                // la commande n'est pas dans l'état EN ATTENTE
                return Response.status(Response.Status.CONFLICT).entity("La commande " + id + " ne peut pas être validée (état actuel: " + existing.getStatut() + ")").build();
            }
        }
        return Response.ok(commandeValidee).build();
    }

    /**
     * Endpoint pour annuler une commande
     * POST ou DELETE /api/commandes/{id}/annulation
     * @param id id de la commande à annuler.
     * @return Réponse HTTP 200 avec la commande annulée, 404 ou 409 si non possible.
     */
    @POST
    @Path("/{id}/annulation")
    public Response cancelCommande(@PathParam("id") Integer id) {
        Commande commandeAnnulee = commandeService.cancelCommande(id);
        if (commandeAnnulee == null) {
            Commande existing = commandeService.getCommandeById(id);
            if (existing == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Commande non trouvée pour annulation: " + id).build();
            } else {
                return Response.status(Response.Status.CONFLICT).entity("La commande " + id + " ne peut pas être annulée (état actuel: " + existing.getStatut() + ")").build();
            }
        }
        return Response.ok(commandeAnnulee).build();
    }

}