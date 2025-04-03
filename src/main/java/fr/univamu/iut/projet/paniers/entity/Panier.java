package fr.univamu.iut.projet.paniers.entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un panier dans le système.
 * Un panier contient un identifiant unique, une date de dernière mise à jour,
 * et une liste de produits contenus dans le panier.
 */
public class Panier {

    private Integer panierId;
    private String userId;
    private Timestamp lastUpdateDate;
    private List<PanierProduit> panierProduits;

    public Panier() {
    }

    /**
     * Constructeur de Panier avec ID et date de mise à jour.
     * @param panierId L'identifiant unique du panier.
     * @param lastUpdateDate La date de dernière mise à jour du panier.
     */
    public Panier(Integer panierId, String userId, Timestamp lastUpdateDate) {
        this.panierId = panierId;
        this.userId = userId;
        this.lastUpdateDate = lastUpdateDate;
        this.panierProduits = new ArrayList<>();
    }

    /**
     * Obtient l'identifiant du panier.
     * @return L'identifiant du panier.
     */
    public Integer getPanierId() {
        return panierId;
    }

    /**
     * Obtient l'identifiant au quel appartient le panier
     * @return L'identifiant de l'utilisateur
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Définit l'identifiant au quel appartient ce panier
     * @param userId L'identifier de l'utilisateur
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Définit l'identifiant du panier.
     * @param panierId L'identifiant du panier.
     */
    public void setPanierId(Integer panierId) {
        this.panierId = panierId;
    }

    /**
     * Obtient la date de dernière mise à jour du panier.
     * @return La date de dernière mise à jour.
     */
    public Timestamp getLastUpdateDate() {
        return lastUpdateDate;
    }

    /**
     * Définit la date de dernière mise à jour du panier.
     * @param lastUpdateDate La date de dernière mise à jour.
     */
    public void setLastUpdateDate(Timestamp lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    /**
     * Obtient la liste des produits dans le panier.
     * @return La liste des PanierProduit.
     */
    public List<PanierProduit> getPanierProduits() {
        return panierProduits;
    }

    /**
     * Définit la liste des produits dans le panier.
     * @param panierProduits La liste des PanierProduit.
     */
    public void setPanierProduits(List<PanierProduit> panierProduits) {
        this.panierProduits = panierProduits;
    }

    @Override
    public String toString() {
        return "Panier{" +
                "panierId=" + panierId +
                ", userId='" + userId + '\'' +
                ", lastUpdateDate=" + lastUpdateDate +
                ", panierProduits=" + panierProduits +
                '}';
    }
}