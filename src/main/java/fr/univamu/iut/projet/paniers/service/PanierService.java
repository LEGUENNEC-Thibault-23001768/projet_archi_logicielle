package fr.univamu.iut.projet.paniers.service;

import fr.univamu.iut.projet.paniers.entity.Panier;
import fr.univamu.iut.projet.paniers.repository.PanierRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.List;

/**
 * Service pour la gestion des paniers.
 * Orchestre la logique métier pour les opérations sur les paniers,
 * en utilisant le PanierRepository pour l'accès aux données.
 */
@Singleton
public class PanierService {

    @Inject
    private PanierRepository panierRepository;

    /**
     * Récupère le panier d'un utilisateur par son ID utilisateur.
     * Si aucun panier n'existe, en crée un nouveau.
     * @param userId L'ID de l'utilisateur.
     * @return Le panier existant ou un nouveau panier pour l'utilisateur.
     */
    public Panier getOrCreatePanierByUserId(String userId) {
        Panier panier = panierRepository.findByUserId(userId);
        if (panier == null) {
            panier = panierRepository.insert(userId);
            if (panier == null) {
                panier = panierRepository.findByUserId(userId);
                if (panier == null) {
                    throw new RuntimeException("Impossible de trouver ou créer un panier pour l'utilisateur: " + userId);
                }
            }
        }
        return panier;
    }


    /**
     * Met à jour le panier d'un utilisateur.
     * Remplace les produits existants par ceux fournis dans panierDetails.
     * @param userId L'ID de l'utilisateur dont le panier doit être mis à jour.
     * @param panierDetails Contient les nouveaux produits du panier.
     * @return Le panier mis à jour.
     */
    public Panier updatePanierForUser(String userId, Panier panierDetails) {
        Panier existingPanier = getOrCreatePanierByUserId(userId);

        Panier panierToUpdate = new Panier();
        panierToUpdate.setPanierId(existingPanier.getPanierId());
        panierToUpdate.setUserId(userId);
        panierToUpdate.setPanierProduits(panierDetails.getPanierProduits());

        return panierRepository.update(panierToUpdate);
    }

    /**
     * Récupère un panier par son ID, mais seulement s'il appartient à l'utilisateur spécifié.
     * @param id L'ID du panier.
     * @param userId L'ID de l'utilisateur propriétaire attendu.
     * @return Le panier trouvé, ou null s'il n'existe pas ou n'appartient pas à l'utilisateur.
     */
    public Panier getPanierByIdAndUserId(Integer id, String userId) {
        if (id == null || userId == null || userId.isEmpty()) {
            return null;
        }
        return panierRepository.findByIdAndUserId(id, userId);
    }

    /**
     * Vide les produits du panier d'un utilisateur.
     * @param userId L'ID de l'utilisateur.
     * @return Le panier vidé (mais toujours existant).
     */
    public Panier clearPanierItemsForUser(String userId) {
        Panier panier = getOrCreatePanierByUserId(userId);
        if (panier != null) {
            Panier panierToUpdate = new Panier();
            panierToUpdate.setPanierId(panier.getPanierId());
            panierToUpdate.setUserId(userId);
            panierToUpdate.setPanierProduits(Collections.emptyList());
            return panierRepository.update(panierToUpdate);
        }
        return null;
    }





    /**
     * Récupère tous les paniers.
     * @return Une liste de tous les paniers.
     */
    public List<Panier> getAllPaniers() {
        return panierRepository.findAll();
    }
}