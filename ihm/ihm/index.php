<?php
include 'includes/config.php';
include 'includes/header.php';

$productsApiResponse = fetchFromApi('/products'); // utilise API_PRODUCTS_USERS_URL
$allProduits = [];
$errorProduits = null;
if (isset($productsApiResponse['error'])) {
    $errorProduits = "Impossible de charger les produits: " . $productsApiResponse['error'];
    error_log($errorProduits . " Status Code: " . ($productsApiResponse['statusCode'] ?? 'N/A')); 
    $allProduits = $productsApiResponse;
} else {
     $errorProduits = "Réponse inattendue ou invalide de l'API produits.";
     error_log($errorProduits . " Réponse reçue: " . json_encode($productsApiResponse));
}
$produitsParCategorie = [];
if (is_array($allProduits)) {
    foreach ($allProduits as $produit) {
        if (isset($produit['id'], $produit['nom'])) {
            $categorie = $produit['categorie'] ?? 'Autre';
            $produitsParCategorie[$categorie][] = $produit;
        } else {
            error_log("Produit invalide reçu de l'API: " . json_encode($produit));
        }
    }
}
?>

<main class="homepage">
    <section class="hero">
        <h1>Nos Produits Frais</h1>
        <p>Directement de nos producteurs locaux.</p>
    </section>

    <div class="products-container">
        <?php if ($errorProduits): ?>
             <div class="alert error"><?= htmlspecialchars($errorProduits) ?></div>
        <?php elseif (empty($produitsParCategorie)): ?>
             <p style="text-align: center; margin: 30px 0;">Aucun produit n'est disponible pour le moment.</p>
        <?php else: ?>
             <?php foreach ($produitsParCategorie as $categorie => $produits): ?>
                 <section class="product-category">
                     <h2><?= htmlspecialchars($categorie) ?></h2>
                     <div class="products-grid">
                         <?php foreach ($produits as $produit): ?>
                             <div class="product-card">
                                 <img src="/images/<?= htmlspecialchars($produit['image'] ?? 'placeholder.jpg') ?>" alt="<?= htmlspecialchars($produit['nom']) ?>" onerror="this.onerror=null;this.src='/images/placeholder.jpg';">
                                 <h3><?= htmlspecialchars($produit['nom']) ?></h3>
                                 <p class="price"><?= number_format($produit['prix'] ?? 0, 2, ',', ' ') ?> € / <?= htmlspecialchars($produit['unite'] ?? 'unité') ?></p>
                                 <p class="stock">Stock: <?= htmlspecialchars($produit['stock'] ?? 'N/A') ?></p>
                                 <form class="add-to-cart-form" method="POST" data-product-id="<?= htmlspecialchars($produit['id']) ?>" data-product-unit="<?= htmlspecialchars($produit['unite'] ?? 'unité') ?>">
                                     <div class="quantity-selector">
                                         <button type="button" class="qty-btn minus" aria-label="Diminuer quantité">-</button>
                                         <input type="number" name="quantite" value="1"
                                                min="<?= ($produit['unite'] ?? 'unité') === 'kg' ? '0.1' : '1' ?>"
                                                max="<?= htmlspecialchars($produit['stock'] ?? '999') ?>"
                                                step="<?= ($produit['unite'] ?? 'unité') === 'kg' ? '0.1' : '1' ?>"
                                                aria-label="Quantité" required>
                                         <button type="button" class="qty-btn plus" aria-label="Augmenter quantité">+</button>
                                     </div>
                                     <button type="submit" class="add-to-cart-btn">Ajouter au panier</button>
                                     <div class="add-to-cart-feedback" style="font-size: 0.8em; margin-top: 5px; color: green; display: none; text-align: center;"></div>
                                 </form>
                             </div>
                         <?php endforeach; ?>
                     </div>
                 </section>
             <?php endforeach; ?>
        <?php endif; ?>

    </div>

    <div class="floating-cart">
        <a href="/panier.php" class="cart-link" aria-label="Voir le panier"> 
            <span class="cart-icon" aria-hidden="true">🛒</span>
            <span class="cart-count">0</span> 
        </a>
    </div>
</main>

<script src="/js/api-helpers.js"></script>

<script>
document.addEventListener('DOMContentLoaded', () => {

    if (typeof getAuthTokenJs !== 'function' || typeof fetchApiJs !== 'function' || typeof updateCartCount !== 'function') {
        console.error("Fonctions de api-helpers.js manquantes ! Vérifiez l'ordre et le contenu.");
        return;
    }
    

    // ajouter/mettre à jour un article dans le panier
    async function updateCartItemIndex(productId, quantity, unit, feedbackElement) {
        const token = getAuthTokenJs();
        if (!token) {
            alert("Veuillez vous connecter pour ajouter des articles au panier.");
            window.location.href = '/login.php?redirect=index.php'; 
            return;
        }

        feedbackElement.textContent = 'Ajout...';
        feedbackElement.style.color = 'orange';
        feedbackElement.style.display = 'block';

        try {
            const getUrl = `${API_PANIERS_URL_JS}/paniers/mine`;
            const currentPanier = await fetchApiJs(getUrl, 'GET', null, true); 

            if (!currentPanier || !currentPanier.panierId) {
                throw new Error("Impossible de récupérer le panier utilisateur.");
            }

            let panierProduits = currentPanier.panierProduits || [];

            const existingItemIndex = panierProduits.findIndex(item => item.productId === productId);
            let itemExisted = false;
            let quantityChanged = false;

            if (existingItemIndex > -1) {
                itemExisted = true;
                const newTotalQuantity = panierProduits[existingItemIndex].quantity + quantity;
                if (panierProduits[existingItemIndex].quantity !== newTotalQuantity) {
                    panierProduits[existingItemIndex].quantity = newTotalQuantity;
                    quantityChanged = true;
                     console.log(`Item ${productId} quantity updated to ${newTotalQuantity}.`);
                }
            } else {
                // ajouter comme nouvel article si la quantité est sup a 0
                 if (quantity > 0 || (unit === 'kg' && quantity >= 0.1) ) {
                     panierProduits.push({ productId: productId, quantity: quantity, unit: unit });
                     quantityChanged = true;
                     console.log(`Item ${productId} added with quantity ${quantity}.`);
                 } else {
                      console.log(`Item ${productId} not added due to zero/negative quantity.`);
                 }
            }

            if (!quantityChanged) {
                 feedbackElement.textContent = 'Quantité inchangée.';
                 feedbackElement.style.color = 'grey';
                 setTimeout(() => { feedbackElement.style.display = 'none'; }, 1500);
                 return;
            }

             panierProduits = panierProduits.filter(item => item.quantity > 0 || (item.unit === 'kg' && item.quantity >= 0.1));

            const putUrl = `${API_PANIERS_URL_JS}/paniers/mine`;
            const panierPayload = {
                panierProduits: panierProduits
            };

            await fetchApiJs(putUrl, 'PUT', panierPayload); // fetchApiJs le met deja

            // mettre à jour l'UI
            console.log("Panier updated successfully via PUT /mine.");
            await updateCartCount(); 
            feedbackElement.textContent = itemExisted ? 'Quantité ajoutée !' : 'Ajouté';
            feedbackElement.style.color = 'green';
            setTimeout(() => { feedbackElement.style.display = 'none'; }, 1500);

        } catch (error) {
            console.error("Error updating cart item:", error);
             let errorMsg = "Erreur ajout panier";
             if (error.status === 401 || error.status === 403) {
                 errorMsg = "Session expirée. Veuillez vous reconnecter.";
             } else if (error.message) {
                  errorMsg += ": " + error.message;
             }
            alert(errorMsg);
            feedbackElement.textContent = 'Erreur';
            feedbackElement.style.color = 'red';
            setTimeout(() => { feedbackElement.style.display = 'none'; }, 3000);
        }
    }


    

    // Gestion des boutons
    document.querySelectorAll('.quantity-selector').forEach(selector => {
        const minus = selector.querySelector('.qty-btn.minus');
        const plus = selector.querySelector('.qty-btn.plus');
        const input = selector.querySelector('input[name="quantite"]');
        if (!input) return;

        const step = parseFloat(input.step) || 1;
        const min = parseFloat(input.min) || 0;
        const maxAttr = input.getAttribute('max');
        const max = maxAttr && !isNaN(parseFloat(maxAttr)) ? parseFloat(maxAttr) : Infinity;

        minus?.addEventListener('click', () => {
            let currentValue = parseFloat(input.value) || min;
            let newValue = currentValue - step;
            if (step < 1) newValue = parseFloat(newValue.toFixed(1));
            newValue = Math.max(min, newValue);
            input.value = newValue;
        });

        plus?.addEventListener('click', () => {
            let currentValue = parseFloat(input.value) || min;
            let newValue = currentValue + step;
            if (step < 1) newValue = parseFloat(newValue.toFixed(1));
            newValue = Math.min(max, newValue);
            input.value = newValue;
        });

        input?.addEventListener('change', () => { 
            let currentValue = parseFloat(input.value);
            if (isNaN(currentValue) || currentValue < min) {
                input.value = min;
            } else if (currentValue > max) {
                input.value = max;
            } else if (step < 1) {
                const roundedValue = Math.round(currentValue / step) * step;
                input.value = parseFloat(roundedValue.toFixed(1));
            } else {
                input.value = Math.round(currentValue);
            }
        });
    });



    // formulaire d'ajout au panier
    document.querySelectorAll('.add-to-cart-form').forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const submitButton = form.querySelector('.add-to-cart-btn');
            if (!submitButton) return;
            submitButton.disabled = true;

            const productId = form.dataset.productId;
            const unit = form.dataset.productUnit;
            const quantiteInput = form.querySelector('input[name="quantite"]');
            const quantity = parseFloat(quantiteInput.value);
            const feedback = form.querySelector('.add-to-cart-feedback');
            const minQuantity = parseFloat(quantiteInput.min) || 0;

            if (isNaN(quantity) || quantity < minQuantity) {
                 if (minQuantity > 0) {
                      alert(`Quantité invalide. Minimum requis: ${minQuantity} ${unit}.`);
                      submitButton.disabled = false;
                      return;
                 }
                 if (quantity <= 0) {
                      feedback.textContent = ''; feedback.style.display = 'none';
                      submitButton.disabled = false;
                      return;
                 }
            }

            updateCartItemIndex(productId, quantity, unit, feedback)
                .catch(err => { console.error("Add to cart failed:", err); })
                .finally(() => { submitButton.disabled = false; });
        });
    });

    updateCartCount();

});
</script>

<?php include 'includes/footer.php'; ?>