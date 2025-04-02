package fr.univamu.iut.projet;

import java.util.ArrayList;

/**
 * Interface d'accès aux données des produits
 */
public interface ProductRepositoryInterface {

    /**
     *  Méthode fermant le dépôt où sont stockées les informations sur les produits
     */
    public void close();

    /**
     * Méthode retournant le produit dont l'id est passé en paramètre
     * @param id identifiant du produit recherché
     * @return un objet Product représentant le produit recherché
     */
    public Product getProduct( String id );

    /**
     * Méthode retournant la liste des produits
     * @return une liste d'objets produits
     */
    public ArrayList<Product> getAllProducts() ;

    /**
     * Méthode permettant de mettre à jours un produit enregistré
     * @param id identifiant du produit à mettre à jours
     * @param nom nouveau nom du produit
     * @param description nouvelle description du produit
     * @param prix nouveau prix du produit
     * @param unite nouvelle unité du produit
     * @param stock nouveau stock du produit
     * @param categorie nouvelle categorie du produit
     * @return true si le produit existe et la mise à jours a été faite, false sinon
     */
    public boolean updateProduct(String id, String nom, String description, double prix, String unite, double stock, String categorie);

    /**
     * Méthode permettant de supprimer un produit enregistré
     * @param id identifiant du produit à supprimer
     * @return true si le produit existe et la suppression a été faite, false sinon
     */
    public boolean deleteProduct(String id);

    /**
     * Méthode permettant d'enregistrer un nouveau produit
     * @param product produit à enregistrer
     * @return true si le produit a été enregistré, false sinon
     */
    public boolean createProduct(Product product);
}