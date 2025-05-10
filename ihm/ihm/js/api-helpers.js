// ihm/js/api-helpers.js

// Define constants first
const API_PRODUCTS_USERS_URL_JS = 'http://localhost:8080/utilisateur/api';
const API_PANIERS_URL_JS = 'http://localhost:8080/paniers/api';
const API_COMMANDES_URL_JS = 'http://localhost:8080/commandes/api';

// Define functions directly in the global scope
function getAuthTokenJs() {
    return localStorage.getItem('authToken');
}
function getUserIdJs() {
    return localStorage.getItem('userId');
}
// getPanierIdJs et setPanierIdJs ne sont plus nécessaires pour l'API paniers/mine
// function getPanierIdJs() { ... }
// function setPanierIdJs(panierId) { ... }

// Generic API Fetch for JS
async function fetchApiJs(url, method = 'GET', data = null, forceRefresh = false) { // Ajout forceRefresh
    const headers = {
        'Accept': 'application/json'
    };
    const token = getAuthTokenJs(); // Récupérer le token à chaque appel
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const options = {
        method: method.toUpperCase(),
        headers: headers,
    };

    // Ajouter 'cache: no-store' si forceRefresh est vrai
    if (forceRefresh) {
        options.cache = 'no-store';
        console.log(`JS Fetch: ${options.method} ${url} (Cache Disabled)`);
    } else {
         // Comportement de cache par défaut du navigateur
         console.log(`JS Fetch: ${options.method} ${url} (Cache Default)`);
    }


    if (data && ['POST', 'PUT', 'PATCH'].includes(options.method)) {
        headers['Content-Type'] = 'application/json';
        options.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(url, options);
        const responseText = await response.text();
        let responseData = null;

        try {
            responseData = responseText ? JSON.parse(responseText) : null;
        } catch (e) {
            // Si ce n'est pas du JSON mais la requête est OK, on peut retourner le texte brut
            if (response.ok && responseText) {
                // Gérer les cas spécifiques comme "created" qui ne sont pas du JSON
                // On pourrait retourner un objet standardisé ou le texte
                responseData = { successMessage: responseText }; // Exemple
                console.log("Received non-JSON success response:", responseText);
            } else {
                console.error("JSON Parsing Error:", e, "Response Text:", responseText);
                 // Si la réponse n'est pas OK, créer une erreur avec le texte si possible
                 if (!response.ok) {
                      const error = new Error(responseText || `Erreur HTTP ${response.status}`);
                      error.status = response.status;
                      throw error;
                 }
                 // Si la réponse est OK mais le JSON est invalide, c'est une erreur serveur
                 throw new Error("Réponse JSON invalide du serveur");
            }
        }

        if (!response.ok) {
            const message = responseData?.message || responseData?.error || responseData?.successMessage || `Erreur HTTP ${response.status}`;
            console.error(`API Error (${options.method} ${url}): Status ${response.status}`, responseData);
            const error = new Error(message);
            error.status = response.status;
            error.data = responseData;
            throw error;
        }

        // Gérer 204 No Content explicitement
        if (response.status === 204) {
            return null; // Ou { success: true, status: 204 } si vous préférez
        }

        return responseData;

    } catch (error) {
        // Log l'erreur avant de la relancer
        console.error(`Fetch API Error Catch (${options.method} ${url}):`, error.message, error.status, error.data);
        throw error; // Relancer pour que l'appelant puisse la gérer
    }
}


// Function to update the floating cart count
async function updateCartCount() {
    const token = getAuthTokenJs();
    const cartCountElement = document.querySelector('.cart-count');
    if (!cartCountElement) return;

    // Si pas de token, l'utilisateur n'est pas connecté, panier vide
    if (!token) {
        cartCountElement.textContent = '0';
        return;
    }

    // Utiliser l'endpoint /mine qui retourne le panier de l'utilisateur connecté
    const apiUrl = `${API_PANIERS_URL_JS}/paniers/mine`; // NOUVEL ENDPOINT

    try {
        // On force le refresh pour avoir le compte à jour
        const panierData = await fetchApiJs(apiUrl, 'GET', null, true); // Force refresh
        // Le panier peut être null si l'API renvoie 204 ou si erreur non gérée
        const count = panierData?.panierProduits?.length ?? 0;
        cartCountElement.textContent = count;
        console.log("Cart count updated:", count);
    } catch (error) {
        console.warn("Erreur récupération compte panier:", error.message);
        // Si 401/403, token invalide, l'utilisateur est déconnecté
        if (error.status === 401 || error.status === 403) {
             cartCountElement.textContent = '0';
             // Optionnel: Déconnecter l'utilisateur localement
             // localStorage.removeItem('authToken');
             // localStorage.removeItem('userId');
             // window.location.href = '/login.php?error=Session expirée';
        } else {
             // Pour d'autres erreurs (500, réseau...), on ne sait pas, on laisse le compteur tel quel ou on met '?'
             // cartCountElement.textContent = '?';
             console.error("Impossible de mettre à jour le compteur du panier:", error);
        }
    }
}

console.log("api-helpers.js loaded and functions defined."); // Verification log