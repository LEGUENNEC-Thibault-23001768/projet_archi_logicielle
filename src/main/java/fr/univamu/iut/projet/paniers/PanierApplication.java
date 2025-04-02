package fr.univamu.iut.projet.paniers;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Définit le chemin de base pour toutes les API de l'application sous "/api".
 */
@ApplicationPath("/api")
public class PanierApplication extends Application {
}