package fr.univamu.iut.projet.paniers.service;

import fr.univamu.iut.projet.paniers.entity.Panier;
import fr.univamu.iut.projet.paniers.repository.PanierRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

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
     * Récupère un panier par son ID.
     * @param id L'ID du panier à récupérer.
     * @return Le Panier trouvé ou null si aucun panier n'est trouvé.
     */
    public Panier getPanierById(Integer id) {
        return panierRepository.findById(id);
    }

    /**
     * Récupère tous les paniers.
     * @return Une liste de tous les paniers.
     */
    public List<Panier> getAllPaniers() {
        return panierRepository.findAll();
    }

    /**
     * Crée un nouveau panier.
     * @param panier Le panier à créer.
     * @return Le Panier créé.
     */
    public Panier createPanier(Panier panier) {
        return panierRepository.save(panier);
    }

    /**
     * Met à jour un panier existant.
     * @param id L'ID du panier à mettre à jour.
     * @param panierDetails Les détails du panier mis à jour.
     * @return Le Panier mis à jour ou null si le panier n'existe pas.
     */
    public Panier updatePanier(Integer id, Panier panierDetails) {
        Panier existingPanier = panierRepository.findById(id);
        if (existingPanier == null) {
            return null;
        }
        panierDetails.setPanierId(id); // S'assure que l'ID est défini pour la mise à jour
        return panierRepository.save(panierDetails);
    }

    /**
     * Supprime un panier par son ID.
     * @param id L'ID du panier à supprimer.
     */
    public void deletePanier(Integer id) {
        panierRepository.delete(id);
    }
}