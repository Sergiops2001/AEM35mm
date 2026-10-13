(function () {
    function initCarousels() {
        var carousels = document.querySelectorAll('.carouselbanner-comp');
        
        carousels.forEach(function (carousel) {
            // Evitar reinicializar si ya tiene el intervalo en marcha
            if (carousel.dataset.initialized === 'true') {
                return;
            }

            var slides = carousel.querySelectorAll('.carousel-slide');
            var totalSlides = slides.length;

            if (totalSlides <= 1) return;

            carousel.dataset.initialized = 'true';

            // Buscar la slide que tiene la clase active (HTL la puso)
            var currentIndex = 0;
            var hasActive = false;
            slides.forEach(function (s, index) {
                if (s.classList.contains('active')) {
                    currentIndex = index;
                    hasActive = true;
                }
            });

            // Fallback de seguridad: si por algún motivo HTL no puso active, lo ponemos aquí
            if (!hasActive) {
                slides[0].classList.add('active');
            }

            function nextSlide() {
                slides[currentIndex].classList.remove('active');
                currentIndex = (currentIndex + 1) % totalSlides;
                slides[currentIndex].classList.add('active');
            }

            // Guardamos el ID del intervalo
            carousel.dataset.intervalId = setInterval(nextSlide, 5000);
        });
    }

    // 1. Ejecutamos inmediatamente
    initCarousels();

    // 2. Ejecutamos cuando el DOM esté completo
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initCarousels);
    }

    // 3. AEM Author Mode MutationObserver
    var observer = new MutationObserver(function (mutations) {
        var needsInit = false;
        mutations.forEach(function (mutation) {
            if (mutation.addedNodes && mutation.addedNodes.length > 0) {
                needsInit = true;
            }
        });
        if (needsInit) {
            initCarousels();
        }
    });
    
    observer.observe(document.body, { childList: true, subtree: true });
})();
