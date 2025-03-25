package fr.univamu.iut.projet.service;

import fr.univamu.iut.projet.entity.Panier;
import fr.univamu.iut.projet.repository.PanierRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class PanierService {

    @Inject
    private PanierRepository panierRepository;

    public Panier getPanierById(Integer id) {
        return panierRepository.findById(id);
    }

    public List<Panier> getAllPaniers() {
        return panierRepository.findAll();
    }

    public Panier createPanier(Panier panier) {
        return panierRepository.save(panier);
    }

    public Panier updatePanier(Integer id, Panier panierDetails) {
        Panier existingPanier = panierRepository.findById(id);
        if (existingPanier == null) {
            return null;
        }
        panierDetails.setPanierId(id); // Ensure ID is set for update
        return panierRepository.save(panierDetails);
    }

    public void deletePanier(Integer id) {
        panierRepository.delete(id);
    }
}