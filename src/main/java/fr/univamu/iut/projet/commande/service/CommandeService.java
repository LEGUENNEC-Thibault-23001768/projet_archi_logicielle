package fr.univamu.iut.projet.commande.service;

import fr.univamu.iut.projet.commande.entity.Commande;
import fr.univamu.iut.projet.commande.repository.CommandeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.List;

/**
 * Service gérant la logique métier pour les commandes
 * Interagit avec le CommandeRepository pour l'accès aux données
 */

@ApplicationScoped
public class CommandeService {

    @Inject
    private CommandeRepository commandeRepository;

    /**
     * Récupère une commande par son id
     * @param id L'id de la commande.
     * @return la commande correspondante ou null si non trouvée
     */
    public Commande getCommandeById(Integer id) {
        return commandeRepository.findById(id);
    }

    /**
     * Récupère toutes les commandes.
     * @return La liste de toutes les commandes.
     */
    public List<Commande> getAllCommandes() {
        return commandeRepository.findAll();
    }

    /**
     * Crée une nouvelle commande.
     * Le statut de base sera EN_ATTENTE si non spécifié
     * @param commande La commande à créer
     * @return La commande créée avec son id
     */
    public Commande createCommande(Commande commande) {
        commande.setId(null);
        return commandeRepository.save(commande);
    }

    /**
     * Met à jour une commande existante.
     * @param id L'id de la commande à mettre à jour
     * @param commandeDetails Les nouvelles infos de la commande
     * @return La commande mise à jour ou null si l'id n'existe pas
     */
    public Commande updateCommande(Integer id, Commande commandeDetails) {
        Commande existingCommande = commandeRepository.findById(id);
        if (existingCommande == null) {
            return null;
        }
        commandeDetails.setId(id);
        return commandeRepository.save(commandeDetails);
    }

    /**
     * Supprime une commande par son id
     * @param id is de la commande à supprimer
     */
    public void deleteCommande(Integer id) {
        commandeRepository.delete(id);
    }

    /**
     * Méthode spécifique pour valider une commande
     * @param id id de la commande à valider
     * @return La commande validée ou null si non trouvée
     */
    public Commande validateCommande(Integer id) {
        Commande commande = commandeRepository.findById(id);
        if (commande != null && commande.getStatut() == Commande.Statut.EN_ATTENTE) {
            commande.setStatut(Commande.Statut.VALIDEE);
            return commandeRepository.save(commande);
        }
        return null;
    }

    /**
     * Méthode spécifique pour annuler une commande
     * @param id L'id de la commande à annuler
     * @return La commande annulée ou null (si non trouvée / non annulable)
     */
    public Commande cancelCommande(Integer id) {
        Commande commande = commandeRepository.findById(id);
        if (commande != null && commande.getStatut() == Commande.Statut.EN_ATTENTE) {
            commande.setStatut(Commande.Statut.ANNULEE);
            return commandeRepository.save(commande);
        }
        return null;
    }

    /**
     * Récupère toutes les commandes pour un client spécifique.
     * @param clientId L'ID du client.
     * @return La liste des commandes du client (peut être vide).
     */
    public List<Commande> getCommandesByClientId(Integer clientId) {
        if (clientId == null) {
            return Collections.emptyList();
        }
        return commandeRepository.findByClientId(clientId);
    }

}