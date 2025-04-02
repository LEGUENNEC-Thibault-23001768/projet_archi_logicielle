package fr.univamu.iut.projet.commande.repository;

import fr.univamu.iut.projet.commande.entity.Commande;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;

import java.io.Closeable;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository pour l'accès aux données des commandes dans la db
 */

@Singleton
public class CommandeRepository implements Closeable {

    protected Connection dbConnection;
    private final String dbUrl = "jdbc:mariadb://mysql-architecture-exam.alwaysdata.net/architecture-exam_commandes";
    private final String dbUser = "398207";
    private final String dbPassword = "lechatrouge13";

    public CommandeRepository() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            this.dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            System.out.println("Connexion à la base de données Commandes réussie.");
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Échec de la connexion à la base de données Commandes: " + e.getMessage());
            throw new RuntimeException("Impossible d'initialiser CommandeRepository: Échec de connexion DB", e);
        }
    }


    @PreDestroy
    public void closeDbConnection() {
        close();
    }

    /**
     * Ferme la connexion à la db si elle est ouverte
     */
    @Override
    public void close() {
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
                System.out.println("Connexion à la base de données Commandes fermée.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion DB Commandes: " + e.getMessage());
        }
    }


    private Commande mapResultSetToCommande(ResultSet rs) throws SQLException {
        return new Commande(
                rs.getInt("id"),
                rs.getInt("client_id"),
                rs.getInt("panier_id"),
                rs.getDate("date_retrait"),
                rs.getInt("relai_id"),
                rs.getBigDecimal("prix_total"),
                Commande.Statut.valueOf(rs.getString("statut"))
        );
    }

    public Commande findById(Integer id) {
        String query = "SELECT * FROM commande WHERE id=?";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToCommande(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la commande par ID: " + id, e);
        }
        return null;
    }

    /**
     * Récupère toutes les commandes de la base de données
     * @return Une liste de toutes les commandes
     * @throws RuntimeException en cas d'erreur SQL
     */
    public List<Commande> findAll() {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT * FROM commande";
        try (Statement st = dbConnection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                commandes.add(mapResultSetToCommande(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de toutes les commandes", e);
        }
        return commandes;
    }

    /**
     * Sauvegarde une commande (insère si nouvelle, maj si existante)
     * @param commande commande à sauvegarder
     * @return commande sauvegardée
     * @throws RuntimeException en cas d'erreur SQL
     */
    public Commande save(Commande commande) {
        if (commande.getId() == null) {
            return insert(commande);
        } else {
            return update(commande);
        }
    }

    /**
     * Insère une nouvelle commande dans la base de données.
     * @param commande la commande à insérer.
     * @return la commande insérée avec son nouvel ID
     * @throws RuntimeException en cas d'erreur SQL ou si l'ID n'est pas généré
     */
    private Commande insert(Commande commande) {
        String query = "INSERT INTO commande (client_id, panier_id, date_retrait, relai_id, prix_total, statut) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, commande.getClientId());
            ps.setInt(2, commande.getPanierId());
            ps.setDate(3, commande.getDateRetrait());
            ps.setInt(4, commande.getRelaiId());
            ps.setBigDecimal(5, commande.getPrixTotal());
            ps.setString(6, (commande.getStatut() != null ? commande.getStatut() : Commande.Statut.EN_ATTENTE).name());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Échec de l'insertion de la commande, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    commande.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Échec de l'insertion de la commande, aucun ID obtenu.");
                }
            }
            if (commande.getStatut() == null) {
                commande.setStatut(Commande.Statut.EN_ATTENTE);
            }
            return commande;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion de la commande", e);
        }
    }

    /**
     * Met à jour une commande existante dans la base de données
     * @param commande La commande à mettre à jour
     * @return La commande mise à jour
     * @throws RuntimeException en cas d'erreur SQL
     */
    private Commande update(Commande commande) {
        String query = "UPDATE commande SET client_id=?, panier_id=?, date_retrait=?, relai_id=?, prix_total=?, statut=? WHERE id=?";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, commande.getClientId());
            ps.setInt(2, commande.getPanierId());
            ps.setDate(3, commande.getDateRetrait());
            ps.setInt(4, commande.getRelaiId());
            ps.setBigDecimal(5, commande.getPrixTotal());
            ps.setString(6, commande.getStatut().name());
            ps.setInt(7, commande.getId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Échec de la mise à jour de la commande ID: " + commande.getId() + ", aucune ligne affectée (peut-être ID inexistant?).");
            }
            return commande;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la commande ID: " + commande.getId(), e);
        }
    }

    /**
     * Supprime une commande de la base de données par son id
     * @param id id de la commande à supprimer
     * @throws RuntimeException en cas d'erreur SQL
     */
    public void delete(Integer id) {
        String query = "DELETE FROM commande WHERE id=?";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("Tentative de suppression de la commande ID: " + id + ", mais aucune ligne n'a été affectée (peut-être ID inexistant?).");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la commande ID: " + id, e);
        }
    }
}