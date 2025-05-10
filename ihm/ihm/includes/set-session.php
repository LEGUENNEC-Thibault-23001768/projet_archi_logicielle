<?php
if (session_status() == PHP_SESSION_NONE) {
    session_start();
}

header('Content-Type: application/json');
header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');
header('Cache-Control: post-check=0, pre-check=0', false);
header('Pragma: no-cache');

$input = json_decode(file_get_contents('php://input'), true);

if (isset($input['userId']) && isset($input['token'])) {
    $_SESSION['user_id'] = $input['userId']; // stocke l'ID utilisateur
    $_SESSION['auth_token'] = $input['token']; // stocke le token JWT

    // supprimer l'ancien ID de panier
    unset($_SESSION['panier_id']);

    error_log("PHP Session set for user ID: " . $input['userId']); 
    echo json_encode(['status' => 'success', 'message' => 'Session PHP mise à jour.']);

} else {
     error_log("set-session.php: Missing userId or token in request data: " . json_encode($input)); 
     http_response_code(400);
     echo json_encode(['status' => 'error', 'message' => 'Données manquantes (userId ou token).']);
}
?>