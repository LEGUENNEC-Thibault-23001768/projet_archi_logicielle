package fr.univamu.iut.projet.paniers.controller;

import fr.univamu.iut.projet.paniers.entity.Panier;
import fr.univamu.iut.projet.paniers.service.PanierService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des paniers.
 * Expose des endpoints pour créer, lire, mettre à jour et supprimer des paniers.
 */
@Path("/paniers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PanierController {

    @Inject
    private PanierService panierService;


    /**
     * Récupère tous les paniers.
     * @return Response contenant une liste de tous les paniers.
     */
    @GET
    public Response getAllPaniers() {
        List<Panier> paniers = panierService.getAllPaniers();
        return Response.ok(paniers).build();
    }

    /**
     * Récupère un panier par son ID.
     * @param id L'ID du panier à récupérer.
     * @return Response contenant le panier trouvé ou une erreur 404 si non trouvé.
     */
    @GET
    @Path("/{id}")
    public Response getPanierById(@PathParam("id") Integer id) {
        Panier panier = panierService.getPanierById(id);
        if (panier == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(panier).build();
    }

    /**
     * Crée un nouveau panier.
     * @param panier Le panier à créer.
     * @return Response contenant le panier créé et un statut 201 CREATED.
     */
    @POST
    public Response createPanier(Panier panier) {
        Panier createdPanier = panierService.createPanier(panier);
        return Response.status(Response.Status.CREATED).entity(createdPanier).build();
    }

    /**
     * Met à jour un panier existant.
     * @param id L'ID du panier à mettre à jour.
     * @param panierDetails Les détails du panier mis à jour.
     * @return Response contenant le panier mis à jour ou une erreur 404 si non trouvé.
     */
    @PUT
    @Path("/{id}")
    public Response updatePanier(@PathParam("id") Integer id, Panier panierDetails) {
        Panier updatedPanier = panierService.updatePanier(id, panierDetails);
        if (updatedPanier == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(updatedPanier).build();
    }

    /**
     * Supprime un panier par son ID.
     * @param id L'ID du panier à supprimer.
     * @return Response avec un statut 204 NO_CONTENT en cas de succès.
     */
    @DELETE
    @Path("/{id}")
    public Response deletePanier(@PathParam("id") Integer id) {
        panierService.deletePanier(id);
        return Response.noContent().build();
    }
}