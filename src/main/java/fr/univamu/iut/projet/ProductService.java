package fr.univamu.iut.projet;


import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.util.ArrayList;


/**
 * Classe utilisée pour récupérer les informations nécessaires à la ressource
 * (permet de dissocier ressource et mode d'éccès aux données)
 */
public class ProductService {

    /**
     * Objet permettant d'accéder au dépôt où sont stockées les informations sur les produits
     */
    protected ProductRepositoryInterface productRepo ;

    /**
     * Constructeur permettant d'injecter l'accès aux données
     * @param productRepo objet implémentant l'interface d'accès aux données
     */
    public ProductService(ProductRepositoryInterface productRepo) {
        this.productRepo = productRepo;
    }

    /**
     * Méthode retournant les informations sur les produits au format JSON
     * @return une chaîne de caractère contenant les informations au format JSON
     */
    public String getAllProductsJSON(){

        ArrayList<Product> allProducts = productRepo.getAllProducts();

        // création du json et conversion de la liste de produits
        String result = null;
        try( Jsonb jsonb = JsonbBuilder.create()){
            result = jsonb.toJson(allProducts);
        }
        catch (Exception e){
            System.err.println( e.getMessage() );
        }

        return result;
    }

    /**
     * Méthode retournant au format JSON les informations sur un produit recherché
     * @param id la référence du produit recherché
     * @return une chaîne de caractère contenant les informations au format JSON
     */
    public String getProductJSON( String id ){
        String result = null;
        Product myProduct = productRepo.getProduct(id);

        // si le produit a été trouvé
        if( myProduct != null ) {

            // création du json et conversion du produit
            try (Jsonb jsonb = JsonbBuilder.create()) {
                result = jsonb.toJson(myProduct);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        return result;
    }

    /**
     * Méthode permettant de mettre à jours les informations d'un produit
     * @param id référence du produit à mettre à jours
     * @param product les nouvelles informations a été utiliser
     * @return true si le produit a pu être mis à jours
     */
    public boolean updateProduct(String id, Product product) {
        return productRepo.updateProduct(id, product.nom, product.description, product.prix, product.unite, product.stock, product.categorie);
    }

    /**
     * Méthode permettant de supprimer un produit
     * @param id référence du produit à supprimer
     * @return true si le produit a pu être supprimé
     */
    public boolean deleteProduct(String id) {
        return productRepo.deleteProduct(id);
    }

    /**
     * Méthode permettant de creer un produit
     * @param product produit à creer
     * @return true si le produit a pu être creer
     */
    public boolean createProduct(Product product) {
        return productRepo.createProduct(product);
    }
}