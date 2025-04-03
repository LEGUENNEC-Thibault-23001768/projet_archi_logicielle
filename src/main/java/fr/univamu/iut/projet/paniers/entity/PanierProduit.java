package fr.univamu.iut.projet.paniers.entity;

/**
 * Représente un produit dans un panier.
 * Contient des informations sur l'identifiant unique de la relation panier-produit,
 * l'identifiant du panier associé, l'identifiant du produit, la quantité et l'unité.
 */
public class PanierProduit {

    private Integer panierId;
    private String productId;
    private Double quantity;
    private String unit;

    /**
     * Constructeur par défaut de PanierProduit.
     */
    public PanierProduit() {
    }

    /**
     * Constructeur de PanierProduit avec tous les attributs.
     * @param panierId L'identifiant du panier associé.
     * @param productId L'identifiant du produit.
     * @param quantity La quantité du produit dans le panier.
     * @param unit L'unité de mesure de la quantité.
     */
    public PanierProduit(Integer panierId, String productId, Double quantity, String unit) {
        this.panierId = panierId;
        this.productId = productId;
        this.quantity = quantity;
        this.unit = unit;
    }

    /**
     * Obtient l'identifiant du panier associé.
     * @return L'identifiant du panier.
     */
    public Integer getPanierId() {
        return panierId;
    }

    /**
     * Définit l'identifiant du panier associé.
     * @param panierId L'identifiant du panier.
     */
    public void setPanierId(Integer panierId) {
        this.panierId = panierId;
    }

    /**
     * Obtient l'identifiant du produit.
     * @return L'identifiant du produit.
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Définit l'identifiant du produit.
     * @param productId L'identifiant du produit.
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Obtient la quantité du produit dans le panier.
     * @return La quantité du produit.
     */
    public Double getQuantity() {
        return quantity;
    }

    /**
     * Définit la quantité du produit dans le panier.
     * @param quantity La quantité du produit.
     */
    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    /**
     * Obtient l'unité de mesure de la quantité.
     * @return L'unité de mesure.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Définit l'unité de mesure de la quantité.
     * @param unit L'unité de mesure.
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "PanierProduit{" +
                ", panierId=" + panierId +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                '}';
    }
}