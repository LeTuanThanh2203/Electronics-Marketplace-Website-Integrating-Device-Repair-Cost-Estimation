// home.js - Interaction & Effects

document.addEventListener("DOMContentLoaded", () => {
    // 1. Mouse Parallax for Hero Section (3D Architectural Planes)
    const hero = document.getElementById('hero-architectural');
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)');

    const bg = document.getElementById('hero-spatial-bg');
    const hl = document.getElementById('hero-headline-plane');
    const prod = document.getElementById('hero-product-plane');

    if (hero && !prefersReducedMotion.matches) {
        let targetSpread = 1;
        let currentSpread = 1;
        
        // Tính toán độ dàn trải dựa trên Khoảng Cách Chuột tới Tâm (Distance-based Parallax)
        // Khi chuột ở tâm (vùng click) -> Lắp ráp hoàn chỉnh
        // Khi chuột ở rìa hoặc bên ngoài -> Dàn chéo ra
        hero.addEventListener('mousemove', (e) => {
            const rect = hero.getBoundingClientRect();
            // Chuẩn hoá toạ độ [-1, 1]
            const x = ((e.clientX - rect.left) / rect.width) * 2 - 1;
            const y = ((e.clientY - rect.top) / rect.height) * 2 - 1;
            
            // Khoảng cách từ chuột tới tâm (0 ở tâm, 1.4 ở góc)
            const dist = Math.sqrt(x*x + y*y);
            
            // Vùng an toàn (dist < 0.3): Đứng im hoàn toàn để click
            // Vùng ngoài (dist > 0.8): Dàn chéo tối đa
            targetSpread = Math.max(0, Math.min(1, (dist - 0.3) / 0.5));
        });

        hero.addEventListener('mouseleave', () => {
            targetSpread = 1; // Chuột ra ngoài -> Dàn chéo
        });

        // Vòng lặp animation mượt mà
        const animate = () => {
            // Nội suy mềm mại (0.05)
            currentSpread += (targetSpread - currentSpread) * 0.05;
            
            // Layer 1: Background Grid (Slight drift)
            if (bg) {
                const bgX = currentSpread * 10;
                const bgY = currentSpread * 5;
                bg.style.transform = `translate3d(${bgX}px, ${bgY}px, 0) scale(1.05)`;
            }
            
            // Layer 2: Text/Content (Spreads Left and Up, Nhẹ nhàng)
            if (hl) {
                const hlX = currentSpread * -60; // Chỉ 60px thay vì 140px
                const hlY = currentSpread * -20;
                
                // Mờ nhẹ đi khi cuộn xa
                const opacity = 1 - (currentSpread * 0.3);
                hl.style.transform = `translate3d(${hlX}px, ${hlY}px, 0)`;
                hl.style.opacity = opacity.toFixed(3);
            }
            
            // Layer 3: Laptop (Spreads Right and Down, Nhẹ nhàng)
            if (prod) {
                const prodX = currentSpread * 60; 
                const prodY = currentSpread * 20;
                
                // Góc nghiêng 3D cực kỳ tinh tế
                const rotateX = currentSpread * -4;
                const rotateY = currentSpread * 6;
                
                prod.style.transform = `perspective(1200px) translate3d(${prodX}px, ${prodY}px, 20px) rotateX(${rotateX}deg) rotateY(${rotateY}deg)`;
            }
            
            requestAnimationFrame(animate);
        };
        
        // Start loop
        animate();
    }

    // 2. Spec Picker Interaction (Hardware Configuration Segment Control)
    const specBtns = document.querySelectorAll('.spec-btn');
    specBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            // Remove active states from all buttons
            specBtns.forEach(b => {
                b.classList.remove('active');
                b.classList.remove('bg-primary', 'text-on-primary');
                b.classList.add('bg-surface-container-lowest', 'text-on-surface');
            });
            // Apply active state to the clicked button
            this.classList.add('active');
            this.classList.remove('bg-surface-container-lowest', 'text-on-surface');
            this.classList.add('bg-primary', 'text-on-primary');
        });
    });
});
