<?php
include 'includes/config.php'; 

unset($_SESSION['user_id']);
unset($_SESSION['auth_token']);
unset($_SESSION['panier_id']);

header('Location: /index.php?logout=1'); 
exit();
?>
