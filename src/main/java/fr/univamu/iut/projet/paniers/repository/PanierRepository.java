package fr.univamu.iut.projet.paniers.repository;

import fr.univamu.iut.projet.paniers.entity.Panier;
import fr.univamu.iut.projet.paniers.entity.PanierProduit;
import jakarta.inject.Singleton;
import java.io.Closeable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository pour l'entité Panier.
 * Gère l'accès à la base de données pour les opérations CRUD sur les paniers
 * et leurs produits associés.
 */
@Singleton
public class PanierRepository implements Closeable {

    protected Connection dbConnection;
    private final String dbUrl = "jdbc:mariadb://mysql-architecture-exam.alwaysdata.net/architecture-exam_paniers";
    private final String dbUser = "398207";
    private final String dbPassword = "lechatrouge13";

    public PanierRepository() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to connect to database!", e);
        }
    }

    /**
     * Ferme la connexion à la base de données lorsque le repository est fermé.
     */
    @Override
    public void close() {
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    /**
     * Mappe un ResultSet en un objet Panier.
     * @param rs Le ResultSet courant.
     * @return Un objet Panier créé à partir des données du ResultSet.
     * @throws SQLException Si une erreur SQL se produit.
     */
    private Panier mapResultSetToPanier(ResultSet rs) throws SQLException {
        Integer panierId = rs.getInt("panier_id");
        Timestamp lastUpdateDate = rs.getTimestamp("date_maj");
        return new Panier(panierId, lastUpdateDate);
    }

    /**
     * Mappe un ResultSet en un objet PanierProduit.
     * @param rs Le ResultSet courant.
     * @return Un objet PanierProduit créé à partir des données du ResultSet.
     * @throws SQLException Si une erreur SQL se produit.
     */
    private PanierProduit mapResultSetToPanierProduit(ResultSet rs) throws SQLException {
        return new PanierProduit(
                rs.getInt("panier_produit_id"),
                rs.getInt("panier_id"),
                rs.getString("product_id"),
                rs.getInt("quantity"),
                rs.getString("unit")
        );
    }

    /**
     * Recherche un panier par son ID.
     * @param id L'ID du panier à rechercher.
     * @return Le Panier trouvé ou null si aucun panier n'est trouvé.
     */
    public Panier findById(Integer id) {
        String query = "SELECT * FROM paniers WHERE panier_id=?";
        Panier panier = null;
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                panier = mapResultSetToPanier(rs);
                panier.setPanierProduits(findPanierProduitsByPanierId(id));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding Panier by id: " + id, e);
        }
        return panier;
    }

    /**
     * Recherche tous les paniers.
     * @return Une liste de tous les paniers.
     */
    public List<Panier> findAll() {
        String query = "SELECT * FROM paniers";
        List<Panier> paniers = new ArrayList<>();
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Panier panier = mapResultSetToPanier(rs);
                panier.setPanierProduits(findPanierProduitsByPanierId(panier.getPanierId()));
                paniers.add(panier);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all Paniers", e);
        }
        return paniers;
    }

    /**
     * Recherche les produits d'un panier par l'ID du panier.
     * @param panierId L'ID du panier.
     * @return Une liste des PanierProduit associés au panier.
     */
    private List<PanierProduit> findPanierProduitsByPanierId(Integer panierId) {
        String query = "SELECT * FROM panier_produits WHERE panier_id=?";
        List<PanierProduit> panierProduits = new ArrayList<>();
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, panierId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                panierProduits.add(mapResultSetToPanierProduit(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding PanierProduits for panierId: " + panierId, e);
        }
        return panierProduits;
    }


    /**
     * Sauvegarde ou met à jour un panier dans la base de données.
     * Si l'ID du panier est null, un nouveau panier est inséré.
     * Sinon, le panier existant est mis à jour.
     * @param panier Le panier à sauvegarder.
     * @return Le Panier sauvegardé avec son ID mis à jour (si insertion).
     */
    public Panier save(Panier panier) {
        if (panier.getPanierId() == null) {
            return insert(panier);
        } else {
            return update(panier);
        }
    }

    /**
     * Insère un nouveau panier dans la base de données.
     * @param panier Le panier à insérer.
     * @return Le Panier inséré avec son ID généré.
     */
    private Panier insert(Panier panier) {
        String query = "INSERT INTO paniers (date_maj) VALUES (NOW())";
        try (PreparedStatement ps = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating panier failed, no rows affected.");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Integer generatedId = generatedKeys.getInt(1);
                    panier.setPanierId(generatedId);
                    updatePanierProducts(panier);
                    return findById(generatedId);
                } else {
                    throw new SQLException("Creating panier failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting Panier", e);
        }
    }

    /**
     * Met à jour un panier existant dans la base de données.
     * @param panier Le panier à mettre à jour.
     * @return Le Panier mis à jour.
     */
    private Panier update(Panier panier) {
        String query = "UPDATE paniers SET date_maj = NOW() WHERE panier_id = ?";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, panier.getPanierId());
            ps.executeUpdate();
            updatePanierProducts(panier);
            return findById(panier.getPanierId());
        } catch (SQLException e) {
            throw new RuntimeException("Error updating Panier", e);
        }
    }

    /**
     * Met à jour les produits associés à un panier.
     * Supprime les produits existants pour ce panier et réinsère les nouveaux produits.
     * @param panier Le panier dont les produits doivent être mis à jour.
     */
    private void updatePanierProducts(Panier panier) {
        if (panier.getPanierProduits() != null) {
            deletePanierProductsForPanierId(panier.getPanierId());
            for (PanierProduit product : panier.getPanierProduits()) {
                insertPanierProduct(panier.getPanierId(), product);
            }
        }
    }

    /**
     * Insère un produit de panier dans la base de données.
     * @param panierId L'ID du panier associé.
     * @param product Le PanierProduit à insérer.
     */
    private void insertPanierProduct(Integer panierId, PanierProduit product) {
        String query = "INSERT INTO panier_produits (panier_id, product_id, quantity, unit) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, panierId);
            ps.setString(2, product.getProductId());
            ps.setInt(3, product.getQuantity());
            ps.setString(4, product.getUnit());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting PanierProduit for panierId: " + panierId, e);
        }
    }

    /**
     * Supprime tous les produits de panier associés à un panier spécifique.
     * @param panierId L'ID du panier.
     */
    private void deletePanierProductsForPanierId(Integer panierId) {
        String query = "DELETE FROM panier_produits WHERE panier_id = ?";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, panierId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting PanierProduits for panierId: " + panierId, e);
        }
    }


    /**
     * Supprime un panier par son ID.
     * @param id L'ID du panier à supprimer.
     */
    public void delete(Integer id) {
        String query = "DELETE FROM paniers WHERE panier_id=?";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Panier by id: " + id, e);
        }
    }
}