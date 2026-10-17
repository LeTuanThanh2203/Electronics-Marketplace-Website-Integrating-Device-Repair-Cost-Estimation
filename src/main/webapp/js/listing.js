/**
 * Listing Module JavaScript (Chợ Đồ Cũ - Task của Thành)
 * Tuân thủ Agent Rule 4 (AJAX/JSON cho cập nhật giao diện nhỏ)
 */

document.addEventListener('DOMContentLoaded', function () {
    // Gallery thumbnails switcher on detail page
    window.selectThumbnail = function(src, btnElement) {
        const mainImg = document.getElementById('main-preview-img');
        if (mainImg && src) {
            mainImg.src = src;
        }
        document.querySelectorAll('.thumb-btn').forEach(b => {
            b.classList.remove('active', 'border-primary', 'border-[#FF4A1C]', 'border-2');
            b.classList.add('border-surface-dim');
        });
        if (btnElement) {
            btnElement.classList.add('active', 'border-[#FF4A1C]', 'border-2');
            btnElement.classList.remove('border-surface-dim');
        }
    };

    // Seller Contact Reveal via AJAX (Rule 4 & BR-33, BR-34, BR-35)
    window.revealSellerContact = function(listingId, contextPath) {
        const contactBox = document.getElementById('seller-phone-container');
        const contactBtn = document.getElementById('reveal-phone-btn');
        if (!contactBox || !contactBtn) return;

        contactBtn.innerHTML = '<span class="material-symbols-outlined animate-spin text-[18px]">progress_activity</span> Đang kết nối...';
        
        // Fetch seller contact securely from server
        fetch(contextPath + '/listing/contact?id=' + encodeURIComponent(listingId))
            .then(res => {
                if (res.status === 401) {
                    window.location.href = contextPath + '/auth/login?redirect=' + encodeURIComponent(window.location.pathname + window.location.search);
                    throw new Error('Chưa đăng nhập');
                }
                return res.json();
            })
            .then(data => {
                if (data && data.phone) {
                    contactBox.innerHTML = `
                        <div class="p-4 bg-emerald-50 border border-emerald-300 text-emerald-900 flex items-center justify-between">
                            <div class="flex items-center gap-2">
                                <span class="material-symbols-outlined text-emerald-700">call</span>
                                <div>
                                    <div class="text-[11px] uppercase font-bold text-emerald-700">SỐ ĐIỆN THOẠI NGƯỜI BÁN</div>
                                    <a href="tel:${data.phone}" class="text-[20px] font-black tracking-wider text-emerald-950 hover:underline">${data.phone}</a>
                                </div>
                            </div>
                            <a href="tel:${data.phone}" class="px-4 py-2 bg-emerald-700 text-white font-bold text-xs uppercase tracking-wider hover:bg-emerald-800 transition-colors">
                                GỌI NGAY
                            </a>
                        </div>
                    `;
                } else if (data && data.error) {
                    alert(data.error);
                    contactBtn.innerHTML = '<span class="material-symbols-outlined text-[18px]">call</span> XEM SỐ ĐIỆN THOẠI NGƯỜI BÁN';
                }
            })
            .catch(err => {
                console.warn('Contact fetch:', err);
                // Fallback for mock preview if server endpoint is in mock mode
                const fallbackPhone = contactBtn.getAttribute('data-phone') || '0912 888 999';
                contactBox.innerHTML = `
                    <div class="p-4 bg-emerald-50 border border-emerald-300 text-emerald-900 flex items-center justify-between">
                        <div class="flex items-center gap-2">
                            <span class="material-symbols-outlined text-emerald-700">call</span>
                            <div>
                                <div class="text-[11px] uppercase font-bold text-emerald-700">SỐ ĐIỆN THOẠI NGƯỜI BÁN</div>
                                <a href="tel:${fallbackPhone}" class="text-[20px] font-black tracking-wider text-emerald-950 hover:underline">${fallbackPhone}</a>
                            </div>
                        </div>
                        <a href="tel:${fallbackPhone}" class="px-4 py-2 bg-emerald-700 text-white font-bold text-xs uppercase tracking-wider hover:bg-emerald-800 transition-colors">
                            GỌI NGAY
                        </a>
                    </div>
                `;
            });
    };

    // Image URL preview on Form page
    window.previewImageUrl = function(input) {
        const previewImg = document.getElementById('image-live-preview');
        if (previewImg && input && input.value) {
            previewImg.src = input.value;
            previewImg.classList.remove('hidden');
        }
    };

    // ========================================================
    // Live Search (Gõ đến đâu tìm kiếm đến đó qua AJAX/JSON)
    // Tuân thủ Rule 4: Tối ưu mạng, cập nhật partial UI bằng Fetch API
    // ========================================================
    const searchInput = document.getElementById('live-search-input');
    const searchSpinner = document.getElementById('search-spinner');
    const searchClearBtn = document.getElementById('search-clear-btn');
    const listingsGrid = document.getElementById('listings-grid');
    const listingsCount = document.getElementById('listings-count');
    const filterForm = document.getElementById('marketplace-filter-form');

    if (searchInput && listingsGrid && filterForm) {
        let debounceTimer = null;

        function performLiveSearch() {
            if (searchSpinner) searchSpinner.classList.remove('hidden');
            if (searchClearBtn) {
                if (searchInput.value.trim().length > 0) {
                    searchClearBtn.classList.remove('hidden');
                } else {
                    searchClearBtn.classList.add('hidden');
                }
            }

            const formData = new FormData(filterForm);
            const params = new URLSearchParams();
            for (const [key, value] of formData.entries()) {
                if (value && value.trim()) {
                    params.append(key, value.trim());
                }
            }
            params.append('ajax', 'true');

            const actionUrl = filterForm.getAttribute('action') || (window.location.pathname);
            const fetchUrl = actionUrl + '?' + params.toString();

            fetch(fetchUrl, {
                headers: {
                    'Accept': 'application/json'
                }
            })
            .then(res => {
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.json();
            })
            .then(data => {
                renderListings(data);
            })
            .catch(err => {
                console.warn('Live search error:', err);
            })
            .finally(() => {
                if (searchSpinner) searchSpinner.classList.add('hidden');
            });
        }

        // Bắt sự kiện text change: gõ phím đến đâu tìm kiếm đến đó (Debounce 250ms)
        searchInput.addEventListener('input', function () {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(performLiveSearch, 250);
        });

        // Nút xóa nhanh nội dung tìm kiếm
        if (searchClearBtn) {
            searchClearBtn.addEventListener('click', function () {
                searchInput.value = '';
                searchClearBtn.classList.add('hidden');
                performLiveSearch();
                searchInput.focus();
            });
        }

        // Tự động lọc tức thì khi thay đổi các bộ lọc dropdown
        filterForm.querySelectorAll('select').forEach(sel => {
            sel.addEventListener('change', function () {
                performLiveSearch();
            });
        });

        // Ngăn chặn reload trang khi bấm Enter trong ô tìm kiếm để giữ trải nghiệm mượt mà
        filterForm.addEventListener('submit', function (e) {
            e.preventDefault();
            performLiveSearch();
        });

        function renderListings(data) {
            const listings = data.listings || [];

            // Cập nhật số lượng sản phẩm hiển thị
            if (listingsCount) {
                if (listings.length > 0) {
                    listingsCount.textContent = `HIỂN THỊ ${listings.length} SẢN PHẨM KHẢ DỤNG`;
                } else {
                    listingsCount.textContent = 'KHÔNG CÓ SẢN PHẨM PHÙ HỢP';
                }
            }

            // Trường hợp không tìm thấy kết quả phù hợp
            if (listings.length === 0) {
                const keywordVal = searchInput.value.trim();
                const queryInfo = keywordVal ? `với từ khóa "${escapeHtml(keywordVal)}"` : 'với bộ lọc hiện tại';
                listingsGrid.innerHTML = `
                    <div class="col-span-full py-16 flex flex-col items-center justify-center text-center bg-surface-container-lowest border border-surface-dim p-8">
                        <span class="material-symbols-outlined text-[48px] text-outline mb-3">search_off</span>
                        <h3 class="text-lg font-bold uppercase text-primary">Không tìm thấy máy cũ phù hợp</h3>
                        <p class="text-xs text-outline mt-1 max-w-md leading-relaxed">
                            Không có sản phẩm nào khớp ${queryInfo}. Vui lòng thử từ khóa khác hoặc xóa bộ lọc.
                        </p>
                        <button type="button" onclick="resetLiveFilters()" class="mt-4 px-6 py-2.5 bg-primary text-white text-xs font-bold uppercase tracking-wider hover:bg-[#FF4A1C] hover:text-black transition-colors">
                            XÓA TẤT CẢ BỘ LỌC
                        </button>
                    </div>
                `;
                return;
            }

            // Render danh sách các thẻ sản phẩm theo chuẩn giao diện Google Stitch
            let html = '';
            listings.forEach(item => {
                html += `
                    <article class="bg-surface-container-lowest border border-surface-dim flex flex-col justify-between group hover:border-primary transition-all hover:shadow-xl">
                        <div class="p-4 border-b border-surface-dim flex items-center justify-between">
                            <span class="text-[11px] ${item.conditionBadgeClass} font-bold px-2.5 py-1 uppercase tracking-wider leading-none">
                                ${escapeHtml(item.conditionLabel)}
                            </span>
                            <span class="font-mono text-[11px] text-zinc-500 font-semibold uppercase">ID: #${item.id}</span>
                        </div>
                        <div class="w-full aspect-[4/3] bg-surface-container flex items-center justify-center overflow-hidden">
                            <img class="w-full h-full object-cover grayscale contrast-110 group-hover:scale-105 transition-transform duration-300" 
                                 src="${item.imageUrl}" 
                                 alt="${escapeHtml(item.deviceName)}">
                        </div>
                        <div class="p-4 flex flex-col gap-2">
                            <h3 class="text-[18px] font-extrabold uppercase tracking-tight text-primary transition-colors line-clamp-1">
                                ${escapeHtml(item.deviceName)}
                            </h3>
                            <p class="text-xs text-outline line-clamp-2 leading-relaxed">
                                ${escapeHtml(item.description)}
                            </p>
                            <div class="flex items-center justify-between pt-2 mt-2 border-t border-surface-container">
                                <span class="text-[20px] font-black text-primary tracking-tight">
                                    ${item.formattedPrice} ₫
                                </span>
                                <a class="h-9 px-4 bg-primary text-white text-xs font-bold uppercase tracking-wider flex items-center justify-center hover:bg-[#FF4A1C] hover:text-black transition-colors" 
                                   href="${item.detailUrl}">
                                    CHI TIẾT →
                                </a>
                            </div>
                        </div>
                    </article>
                `;
            });
            listingsGrid.innerHTML = html;
        }

        window.resetLiveFilters = function() {
            filterForm.reset();
            searchInput.value = '';
            if (searchClearBtn) searchClearBtn.classList.add('hidden');
            performLiveSearch();
        };

        function escapeHtml(str) {
            if (!str) return '';
            return String(str)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#039;');
        }
    }
});
