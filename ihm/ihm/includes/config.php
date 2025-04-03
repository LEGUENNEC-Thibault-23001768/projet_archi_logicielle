<?php

define('API_PRODUCTS_USERS_URL', 'http://localhost:8080/utilisateur/api'); //Produits/Utilisateurs/Auth
define('API_PANIERS_URL',        'http://localhost:8080/paniers/api');     // Paniers
define('API_COMMANDES_URL',      'http://localhost:8080/commandes/api');   //Commandes

session_start();

/**
 * Fetches data from the appropriate microservice API.
 *
 * @param string $urlPath The specific API path (e.g., '/products/101', '/paniers/5', '/auth/login').
 *                        This path should START WITH A SLASH and contain the endpoint part AFTER the base URL defined above.
 * @param string $method HTTP method ('GET', 'POST', 'PUT', 'DELETE').
 * @param mixed|null $data Data payload for POST/PUT requests.
 * @param string|null $token JWT Authentication token.
 * @return array|null Decoded JSON response as an associative array, null on severe errors or specific 404s.
 *                    Returns ['error' => 'message', 'statusCode' => code] on API errors.
 */
function fetchFromApi($urlPath, $method = 'GET', $data = null, $token = null) {

    $baseUrl = null;
    if (strpos($urlPath, '/auth') === 0 || strpos($urlPath, '/users') === 0 || strpos($urlPath, '/products') === 0) {
        $baseUrl = API_PRODUCTS_USERS_URL;
    }

    elseif (strpos($urlPath, '/paniers') === 0) {
        $baseUrl = API_PANIERS_URL;
    }

    elseif (strpos($urlPath, '/commandes') === 0) {
        $baseUrl = API_COMMANDES_URL;
    }
    else {
        error_log("fetchFromApi: Unrecognized API path - cannot determine base URL for: " . $urlPath);
        return ['error' => "Unrecognized API path: $urlPath", 'statusCode' => 500];
    }

    $url = $baseUrl . $urlPath; 
    $method = strtoupper($method);

    $headers = [
        "Accept: application/json"
    ];
    if ($token) {
        $headers[] = "Authorization: Bearer " . $token; 
    }

    $options = [
        'http' => [
            'method' => $method,
            'ignore_errors' => true, 
            'timeout' => 10,
        ]
    ];


    if ($data !== null && in_array($method, ['POST', 'PUT', 'PATCH'])) {
        $jsonData = json_encode($data);
        if ($jsonData === false) {
             error_log("fetchFromApi: Failed to encode JSON data for $method $url");
             return ['error' => "Failed to encode JSON data", 'statusCode' => 500];
        }
        $headers[] = "Content-Type: application/json";
        $headers[] = "Content-Length: " . strlen($jsonData);
        $options['http']['content'] = $jsonData;
    }

    $options['http']['header'] = implode("\r\n", $headers);

    // appelle d'api
    error_log("fetchFromApi: Calling $method $url");
    $context = stream_context_create($options);
    $response = @file_get_contents($url, false, $context);

    // processus de réponse
    $http_response_header = $http_response_header ?? []; 
    $http_status_line = $http_response_header[0] ?? 'HTTP/1.1 503 Service Unavailable'; 

    preg_match('{HTTP\/\S*\s(\d{3})}', $http_status_line, $match);
    $statusCode = isset($match[1]) ? intval($match[1]) : 503;

    if ($response === FALSE) {
        $error = error_get_last();
        $errorMessage = $error['message'] ?? "Failed to connect to the API service at $url. Please ensure it's running.";
        error_log("API Connection Error ($method $url): Status $statusCode - $errorMessage");
        $userFriendlyError = "Erreur de connexion au service API ($statusCode). Vérifiez si le service est démarré et accessible.";
        if (strpos($errorMessage, 'Connection refused') !== false) {
            $userFriendlyError .= " (Connection Refused)";
        }
        return ['error' => $userFriendlyError, 'statusCode' => $statusCode];
    }

    $decodedResponse = json_decode($response, true);
    $jsonError = json_last_error();

     if ($jsonError !== JSON_ERROR_NONE && !($response === '' && $statusCode >= 200 && $statusCode < 300)) { 
        error_log("API JSON Decode Error ($method $url): Status $statusCode, Error: " . json_last_error_msg() . ", Response: " . substr($response, 0, 200));
         if ($statusCode >= 200 && $statusCode < 300) {
             return ['error' => "Réponse invalide (JSON) reçue de l'API.", 'statusCode' => 500];
         }
          $errorMessage = isset($decodedResponse['message']) ? $decodedResponse['message'] : (is_string($decodedResponse) ? $decodedResponse : "Erreur API (Status $statusCode) avec réponse JSON invalide.");
          return ['error' => $errorMessage . " (JSON Error: " . json_last_error_msg() . ")", 'statusCode' => $statusCode];
    }


    if ($statusCode < 200 || $statusCode >= 300) {
        error_log("API Error ($method $url): Status $statusCode, Response: " . $response);
        $errorMessage = "Erreur API (Status $statusCode)"; 
         if (is_array($decodedResponse)) {
            $errorMessage = $decodedResponse['message'] ?? $decodedResponse['error'] ?? $errorMessage;
             if (isset($decodedResponse['details']) && $decodedResponse['details'] !== $errorMessage) {
                 $errorMessage .= " Détails: " . (is_array($decodedResponse['details']) ? json_encode($decodedResponse['details']) : $decodedResponse['details']);
             }
         } elseif (is_string($decodedResponse) && !empty($decodedResponse)) {
             $errorMessage = $decodedResponse;
         } elseif (!empty($response) && $decodedResponse === null && $jsonError === JSON_ERROR_NONE) {
            $errorMessage = substr($response, 0, 250);
         }


        if ($statusCode === 404 && $method === 'GET' && (preg_match('/^\/(paniers|produits|users)\/\d+$/', $urlPath) || preg_match('/^\/users\/email\/.+$/', $urlPath) ) ) {
            return null; 
        }
        if ($statusCode === 401) {
             $errorMessage = "Authentification échouée ou requise.";
        }
         if ($statusCode === 403) { 
              $errorMessage = "Accès refusé à cette ressource.";
         }
         if ($statusCode === 409) { 
              $errorMessage = $errorMessage ?: "Conflit de données (ex: email déjà utilisé)."; 
         }
          if ($statusCode === 400) { 
               $errorMessage = "Requête invalide : " . ($errorMessage ?: "Vérifiez les données envoyées.");
          }


        return ['error' => $errorMessage, 'statusCode' => $statusCode];
    }

    if (empty($response) && $statusCode >= 200 && $statusCode < 300 && $statusCode != 200) {
        return [];
    }

    return $decodedResponse ?? [];
}

function getAuthToken() {
    return $_SESSION['auth_token'] ?? null;
}

function getUserId() {
     return $_SESSION['user_id'] ?? null;
}

function getCurrentPanierId() {
    return $_SESSION['panier_id'] ?? null;
}

function setCurrentPanierId($panierId) {
    $_SESSION['panier_id'] = $panierId;
}

?>