package fr.univamu.iut.projet;

import java.io.Closeable;
import java.sql.*;
import java.util.ArrayList;

/**
 * Classe permettant d'accèder aux produits stockés dans une base de données Mariadb
 */
public class ProductRepositoryMariadb  implements ProductRepositoryInterface, Closeable {

    /**
     * Accès à la base de données (session)
     */
    protected Connection dbConnection ;

    /**
     * Constructeur de la classe
     * @param infoConnection chaîne de caractères avec les informations de connexion
     *                       (p.ex. jdbc:mariadb://mysql-[compte].alwaysdata.net/[compte]_library_db
     * @param user chaîne de caractères contenant l'identifiant de connexion à la base de données
     * @param pwd chaîne de caractères contenant le mot de passe à utiliser
     */
    public ProductRepositoryMariadb(String infoConnection, String user, String pwd ) throws java.sql.SQLException, java.lang.ClassNotFoundException {
        Class.forName("org.mariadb.jdbc.Driver");
        dbConnection = DriverManager.getConnection( infoConnection, user, pwd ) ;
    }

    @Override
    public void close() {
        try{
            dbConnection.close();
        }
        catch(SQLException e){
            System.err.println(e.getMessage());
        }
    }

    @Override
    public Product getProduct(String id) {
        Product selectedProduct = null;

        String query = "SELECT * FROM Product WHERE id=?";

        // construction et exécution d'une requête préparée
        try ( PreparedStatement ps = dbConnection.prepareStatement(query) ){
            ps.setString(1, id);

            // exécution de la requête
            ResultSet result = ps.executeQuery();

            // récupération du premier (et seul) tuple résultat
            // (si la référence du livre est valide)
            if( result.next() )
            {
                String nom = result.getString("nom");
                String description = result.getString("description");
                double prix = result.getDouble("prix");
                String unite = result.getString("unite");
                double stock = result.getDouble("stock");
                String categorie = result.getString("categorie");

                // création et initialisation de l'objet Product
                selectedProduct = new Product(id, nom, description, prix, unite, stock, categorie);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return selectedProduct;
    }

    @Override
    public ArrayList<Product> getAllProducts() {
        ArrayList<Product> listProducts ;

        String query = "SELECT * FROM Product";

        // construction et exécution d'une requête préparée
        try ( PreparedStatement ps = dbConnection.prepareStatement(query) ){
            // exécution de la requête
            ResultSet result = ps.executeQuery();

            listProducts = new ArrayList<>();

            // récupération du premier (et seul) tuple résultat
            while ( result.next() )
            {
                String id = result.getString("id");
                String nom = result.getString("nom");
                String description = result.getString("description");
                double prix = result.getDouble("prix");
                String unite = result.getString("unite");
                double stock = result.getDouble("stock");
                String categorie = result.getString("categorie");


                // création du produit courant
                Product currentProduct = new Product(id, nom, description, prix, unite, stock, categorie);

                listProducts.add(currentProduct);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return listProducts;
    }

    @Override
    public boolean updateProduct(String id, String nom, String description, double prix, String unite, double stock, String categorie) {
        String query = "UPDATE Product SET nom=?, description=?, prix=?, unite=?, stock=?, categorie=?  where id=?";
        int nbRowModified = 0;

        // construction et exécution d'une requête préparée
        try ( PreparedStatement ps = dbConnection.prepareStatement(query) ){
            ps.setString(1, nom);
            ps.setString(2, description);
            ps.setDouble(3, prix);
            ps.setString(4, unite);
            ps.setDouble(5, stock);
            ps.setString(6, categorie);
            ps.setString(7, id);


            // exécution de la requête
            nbRowModified = ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return ( nbRowModified != 0 );
    }

    @Override
    public boolean deleteProduct(String id) {
        String query = "DELETE FROM Product WHERE id=?";
        int nbRowModified = 0;

        // construction et exécution d'une requête préparée
        try ( PreparedStatement ps = dbConnection.prepareStatement(query) ){
            ps.setString(1, id);

            // exécution de la requête
            nbRowModified = ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return ( nbRowModified != 0 );
    }

    @Override
    public boolean createProduct(Product product) {
        String query = "INSERT INTO Product (id, nom, description, prix, unite, stock, categorie) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int nbRowModified = 0;

        // construction et exécution d'une requête préparée
        try ( PreparedStatement ps = dbConnection.prepareStatement(query) ){
            ps.setString(1, product.getId());
            ps.setString(2, product.getNom());
            ps.setString(3, product.getDescription());
            ps.setDouble(4, product.getPrix());
            ps.setString(5, product.getUnite());
            ps.setDouble(6, product.getStock());
            ps.setString(7, product.getCategorie());


            // exécution de la requête
            nbRowModified = ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return ( nbRowModified != 0 );
    }
}