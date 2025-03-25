package fr.univamu.iut.projet.controller;

import fr.univamu.iut.projet.entity.Panier;
import fr.univamu.iut.projet.service.PanierService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/paniers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PanierController {

    @Inject
    private PanierService panierService;

    @GET
    public Response getAllPaniers() {
        List<Panier> paniers = panierService.getAllPaniers();
        return Response.ok(paniers).build();
    }

    @GET
    @Path("/{id}")
    public Response getPanierById(@PathParam("id") Integer id) {
        Panier panier = panierService.getPanierById(id);
        if (panier == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(panier).build();
    }

    @POST
    public Response createPanier(Panier panier) {
        Panier createdPanier = panierService.createPanier(panier);
        return Response.status(Response.Status.CREATED).entity(createdPanier).build();
    }

    @PUT
    @Path("/{id}")
    public Response updatePanier(@PathParam("id") Integer id, Panier panierDetails) {
        Panier updatedPanier = panierService.updatePanier(id, panierDetails);
        if (updatedPanier == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(updatedPanier).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deletePanier(@PathParam("id") Integer id) {
        panierService.deletePanier(id);
        return Response.noContent().build();
    }
}