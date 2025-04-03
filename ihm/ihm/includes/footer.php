<footer class="main-footer" style="text-align: center; padding: 20px; margin-top: 40px; background-color: #f2f2f2; color: #555;">
    </footer>
    <script src="/js/api-helpers.js"></script>

    <script>
         document.addEventListener('DOMContentLoaded', () => {
            const urlParams = new URLSearchParams(window.location.search);

            if (urlParams.has('logout')) {
                console.log('Logout detected, clearing local storage...');

                if (typeof setPanierIdJs === 'function') {
                    setPanierIdJs(null); 
                } else {
                    console.warn('setPanierIdJs helper not found during logout cleanup.'); 
                    localStorage.removeItem('panierId');
                }
                localStorage.removeItem('authToken');
                localStorage.removeItem('userId');

                if (history.replaceState) {
                     const cleanURL = window.location.protocol + "//" + window.location.host + window.location.pathname;
                     history.replaceState({path:cleanURL}, '', cleanURL);
                }

                const logoutMsg = document.createElement('div');
                logoutMsg.textContent = 'Vous avez été déconnecté.';
                logoutMsg.style.cssText = `
                    position: fixed;
                    top: 10px;
                    left: 50%;
                    transform: translateX(-50%);
                    background-color: #e8f5e9; /* Light green background */
                    color: #2e7d32; /* Dark green text */
                    padding: 10px 20px;
                    border-radius: 5px;
                    border: 1px solid #a5d6a7; /* Light green border */
                    z-index: 2000; /* Ensure it's on top */
                    box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                    font-size: 0.9em;
                    text-align: center;
                `;
                document.body.appendChild(logoutMsg);
                setTimeout(() => {
                     logoutMsg.style.transition = 'opacity 0.5s ease-out';
                     logoutMsg.style.opacity = '0';
                     setTimeout(() => { logoutMsg.remove(); }, 500); 
                }, 3000);
            }

             if (urlParams.has('commandeSuccess')) {
                 console.log('Page loaded after successful command. Parameter detected.');

             }
        });
    </script>

</body>
</html>