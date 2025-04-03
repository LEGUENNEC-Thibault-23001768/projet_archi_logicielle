package fr.univamu.iut.projet.paniers.controller;

import fr.univamu.iut.projet.paniers.entity.Panier;
import fr.univamu.iut.projet.paniers.service.PanierService;
import fr.univamu.iut.projet.paniers.util.JWTUtil;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;

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
    @Inject private JWTUtil jwtUtil;


    private Optional<String> validateAuthHeaderAndGetUserId(HttpHeaders httpHeaders) {
        List<String> authHeaders = httpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null || authHeaders.isEmpty()) {
            System.err.println("Auth Error: Missing Authorization header");
            return Optional.empty();
        }

        String bearerToken = authHeaders.get(0);
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            System.err.println("Auth Error: Invalid Authorization header format");
            return Optional.empty();
        }

        String token = bearerToken.substring(7); // Enlever "Bearer "
        return jwtUtil.getUserIdFromToken(token);
    }

    private boolean checkUserRole(HttpHeaders httpHeaders, String requiredRole) {
        List<String> authHeaders = httpHeaders.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null || authHeaders.isEmpty() || !authHeaders.get(0).startsWith("Bearer ")) {
            return false;
        }
        String token = authHeaders.get(0).substring(7);
        Optional<List<String>> rolesOpt = jwtUtil.getRolesFromToken(token);
        return rolesOpt.map(roles -> roles.contains(requiredRole)).orElse(false);
    }


    /**
     * Récupère tous les paniers.
     * @return Response contenant une liste de tous les paniers.
     */
    @GET
    @RolesAllowed({"admin"})
    public Response getAllPaniers() {
        List<Panier> paniers = panierService.getAllPaniers();
        return Response.ok(paniers).build();
    }

    /**
     * Récupère le panier de l'utilisateur actuellement connecté.
     * Crée un panier s'il n'en existe pas.
     * Nécessite que l'utilisateur soit authentifié.
     * @return Response contenant le panier de l'utilisateur.
     */
    @GET
    @Path("/mine")
    public Response getMyPanier(@Context HttpHeaders httpHeaders) {
        Optional<String> userIdOpt = validateAuthHeaderAndGetUserId(httpHeaders);
        if (userIdOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Authentification requise ou token invalide.").build();
        }
        String userId = userIdOpt.get();

        Panier panier = panierService.getOrCreatePanierByUserId(userId);
        return Response.ok(panier).build();
    }

    /**
     * Vide le panier de l'utilisateur connecté (supprime tous les produits).
     * Ne supprime pas l'entité Panier elle-même.
     * Nécessite que l'utilisateur soit authentifié.
     * @return Response avec le panier vidé.
     */
    @DELETE
    @Path("/mine/items")
    public Response clearMyPanierItems(@Context HttpHeaders httpHeaders) {
        Optional<String> userIdOpt = validateAuthHeaderAndGetUserId(httpHeaders);
        if (userIdOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Authentification requise ou token invalide.").build();
        }
        String userId = userIdOpt.get();
        Panier panier = panierService.clearPanierItemsForUser(userId);
        if (panier == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Panier non trouvé.").build();
        }
        return Response.ok(panier).build();
    }

    /**
     * Met à jour le panier de l'utilisateur connecté.
     * Remplace complètement les produits du panier par ceux fournis.
     * Nécessite que l'utilisateur soit authentifié.
     * @param panierDetails Les détails du panier mis à jour.
     * @return Response contenant le panier mis à jour.
     */
    @PUT
    @Path("/mine")
    public Response updateMyPanier(@Context HttpHeaders httpHeaders, Panier panierDetails) {
        Optional<String> userIdOpt = validateAuthHeaderAndGetUserId(httpHeaders);
        if (userIdOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Authentification requise ou token invalide.").build();
        }
        String userId = userIdOpt.get();

        Panier panier = panierService.updatePanierForUser(userId, panierDetails);
        if (panier == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Erreur lors du vidage du panier.").build();
        }
        return Response.ok(panier).build();
    }
}