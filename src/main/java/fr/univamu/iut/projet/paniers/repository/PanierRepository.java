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
        Integer panierId = rs.getInt("id");
        String userId = rs.getString("user_id");
        Timestamp lastUpdateDate = rs.getTimestamp("date_maj");
        return new Panier(panierId, userId, lastUpdateDate);
    }

    /**
     * Mappe un ResultSet en un objet PanierProduit.
     * @param rs Le ResultSet courant.
     * @return Un objet PanierProduit créé à partir des données du ResultSet.
     * @throws SQLException Si une erreur SQL se produit.
     */
    private PanierProduit mapResultSetToPanierProduit(ResultSet rs) throws SQLException {
        return new PanierProduit(
                rs.getInt("panier_id"),
                rs.getString("produit_id"),
                rs.getDouble("quantite"),
                rs.getString("unite")
        );
    }

    /**
     * Recherche un panier par l'ID de l'utilisateur.
     * @param userId L'ID de l'utilisateur.
     * @return Le Panier trouvé ou null si aucun panier n'est trouvé pour cet utilisateur.
     */
    public Panier findByUserId(String userId) {
        String query = "SELECT * FROM paniers WHERE user_id=?";
        Panier panier = null;
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                panier = mapResultSetToPanier(rs);
                panier.setPanierProduits(findPanierProduitsByPanierId(panier.getPanierId()));
            }
            dbConnection.commit(); // Commit after read (optional but good practice with setAutoCommit(false))
        } catch (SQLException e) {
            try { dbConnection.rollback(); } catch (SQLException ex) { System.err.println("Rollback failed: " + ex.getMessage()); }
            throw new RuntimeException("Error finding Panier by userId: " + userId, e);
        }
        return panier;
    }

    /**
     * Recherche un panier par son ID et l'ID utilisateur (pour la sécurité).
     * @param id L'ID du panier.
     * @param userId L'ID de l'utilisateur propriétaire attendu.
     * @return Le Panier trouvé ou null.
     */
    public Panier findByIdAndUserId(Integer id, String userId) {
        String query = "SELECT * FROM paniers WHERE id=? AND user_id=?";
        Panier panier = null;
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.setString(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                panier = mapResultSetToPanier(rs);
                panier.setPanierProduits(findPanierProduitsByPanierId(panier.getPanierId()));
            }
            dbConnection.commit();
        } catch (SQLException e) {
            try { dbConnection.rollback(); } catch (SQLException ex) { System.err.println("Rollback failed: " + ex.getMessage()); }
            throw new RuntimeException("Error finding Panier by id=" + id + " and userId=" + userId, e);
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
    private List<PanierProduit> findPanierProduitsByPanierId(Integer panierId) throws SQLException { // Made package-private or private, ensure called within transaction
        String query = "SELECT * FROM panier_produits WHERE panier_id=?";
        List<PanierProduit> panierProduits = new ArrayList<>();
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, panierId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                panierProduits.add(mapResultSetToPanierProduit(rs));
            }
        } // Ne pas commit/rollback ici, fait par l'appelant
        return panierProduits;
    }
    /**
     * Insère un nouveau panier dans la base de données.
     * @param userId l'ID de l'utilisateur pour qui créer le panier.
     * @return Le Panier inséré avec son ID généré.
     */
    public Panier insert(String userId) {
        String query = "INSERT INTO paniers (user_id, date_maj) VALUES (?, NOW())";
        Panier newPanier = null;
        try (PreparedStatement ps = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, userId);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating panier failed, no rows affected.");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Integer generatedId = generatedKeys.getInt(1);
                    newPanier = this.findByIdAndUserId(generatedId, userId); // Use secure find
                } else {
                    throw new SQLException("Creating panier failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("uk_paniers_user_id")) {
                System.err.println("Attempted to create a duplicate panier for user: " + userId);
                return findByUserId(userId);
            }
            throw new RuntimeException("Error inserting Panier for user " + userId, e);
        }
        return newPanier; // Peut être null si findByIdAndUserId échoue après création
    }



    /**
     * Met à jour un panier existant dans la base de données.
     * @param panier Le panier à mettre à jour.
     * @return Le Panier mis à jour.
     */
    public Panier update(Panier panier) {
        String checkQuery = "UPDATE paniers SET date_maj = NOW() WHERE id = ? AND user_id = ?";
        try (PreparedStatement psCheck = dbConnection.prepareStatement(checkQuery)) {
            psCheck.setInt(1, panier.getPanierId());
            psCheck.setString(2, panier.getUserId());
            int affectedRows = psCheck.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Panier non trouvé (id=" + panier.getPanierId() + ") ou n'appartient pas à l'utilisateur (userId=" + panier.getUserId() + ")");
            }

            updatePanierProducts(panier.getPanierId(), panier.getPanierProduits());

            return findByIdAndUserId(panier.getPanierId(), panier.getUserId());

        } catch (SQLException e) {
            throw new RuntimeException("Error updating Panier id=" + panier.getPanierId(), e);
        }
    }

    /**
     * Met à jour les produits associés à un panier.
     * Supprime les produits existants pour ce panier et réinsère les nouveaux produits.
     * @param panierId L'id du panier dont les produits doivent être mis à jour.
     */
    private void updatePanierProducts(Integer panierId, List<PanierProduit> products) throws SQLException {
        deletePanierProductsForPanierId(panierId);

        // Insérer les nouveaux produits
        if (products != null && !products.isEmpty()) {
            String insertQuery = "INSERT INTO panier_produits (panier_id, produit_id, quantite, unite) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psInsert = dbConnection.prepareStatement(insertQuery)) {
                for (PanierProduit product : products) {
                    psInsert.setInt(1, panierId);
                    psInsert.setString(2, product.getProductId());
                    psInsert.setDouble(3, product.getQuantity()); // Utiliser setDouble pour correspondre au type SQL
                    psInsert.setString(4, product.getUnit());
                    psInsert.addBatch();
                }
                psInsert.executeBatch();
            }
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
     * Supprime un panier par son ID, uniquement s'il appartient à l'utilisateur spécifié.
     * @param id L'ID du panier à supprimer.
     * @param userId L'ID de l'utilisateur propriétaire.
     * @return true si la suppression a réussi, false sinon.
     */
    public boolean deleteByIdAndUserId(Integer id, String userId) {
        String query = "DELETE FROM paniers WHERE id=? AND user_id=?";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.setString(2, userId);
            int affectedRows = ps.executeUpdate();
            dbConnection.commit();
            return affectedRows > 0;
        } catch (SQLException e) {
            try { dbConnection.rollback(); } catch (SQLException ex) { System.err.println("Rollback failed: " + ex.getMessage()); }
            throw new RuntimeException("Error deleting Panier by id=" + id + " and userId=" + userId, e);
        }
    }
}