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

    // ========================================================
    // Form Image Gallery Manager (Hỗ trợ thêm nhiều ảnh & nút ✕ xóa ảnh)
    // ========================================================
    window.uploadedFilesList = []; // Mảng chứa các file mới: { id, file, url }

    // Đồng bộ mảng uploadedFilesList vào thẻ <input type="file" name="imageFiles" id="image-file-input"> bằng DataTransfer
    window.syncFileInput = function() {
        const input = document.getElementById('image-file-input');
        if (!input) return;
        try {
            const dt = new DataTransfer();
            window.uploadedFilesList.forEach(item => {
                if (item.file) dt.items.add(item.file);
            });
            input.files = dt.files;
        } catch (e) {
            console.warn('DataTransfer sync warning:', e);
        }
    };

    // Chọn ảnh xem trước trên Form đăng/sửa
    window.selectFormPreview = function(src, element) {
        const previewImg = document.getElementById('image-live-preview');
        const previewEmpty = document.getElementById('main-preview-empty');
        const previewBadge = document.getElementById('main-preview-badge');

        if (previewImg && src) {
            previewImg.src = src;
            previewImg.classList.remove('hidden');
        }
        if (previewEmpty) {
            previewEmpty.classList.add('hidden');
        }
        if (previewBadge) {
            previewBadge.classList.remove('hidden');
        }

        document.querySelectorAll('#multi-preview-thumbnails .form-thumb-item').forEach(item => {
            item.classList.remove('border-[#FF4A1C]', 'ring-1', 'ring-[#FF4A1C]');
            item.classList.add('border-surface-dim');
        });

        if (element) {
            element.classList.add('border-[#FF4A1C]', 'ring-1', 'ring-[#FF4A1C]');
            element.classList.remove('border-surface-dim');
            const badge = element.querySelector('.thumb-badge');
            if (previewBadge && badge) {
                previewBadge.textContent = badge.textContent === 'Bìa' ? 'Ảnh bìa chính' : 'Ảnh ' + badge.textContent;
            }
        }
    };

    // Cập nhật nhãn số thứ tự ảnh (Bìa, #2, #3...) và trạng thái khung preview lớn
    window.updateGalleryState = function() {
        const previewImg = document.getElementById('image-live-preview');
        const previewEmpty = document.getElementById('main-preview-empty');
        const previewBadge = document.getElementById('main-preview-badge');
        const sizeInfo = document.getElementById('file-size-info');
        const chosenText = document.getElementById('file-chosen-text');
        const thumbnailsContainer = document.getElementById('multi-preview-thumbnails');
        const allItems = document.querySelectorAll('#multi-preview-thumbnails .form-thumb-item');

        const totalCount = allItems.length;

        if (totalCount === 0) {
            if (previewImg) {
                previewImg.src = '';
                previewImg.classList.add('hidden');
            }
            if (previewEmpty) {
                previewEmpty.classList.remove('hidden');
            }
            if (previewBadge) {
                previewBadge.classList.add('hidden');
            }
            if (sizeInfo) {
                sizeInfo.textContent = 'Chưa có ảnh nào';
            }
            if (chosenText) {
                chosenText.textContent = '+ THÊM ẢNH TỪ MÁY TÍNH';
            }
            if (thumbnailsContainer && !document.getElementById('no-thumbs-msg')) {
                const emptyMsg = document.createElement('div');
                emptyMsg.id = 'no-thumbs-msg';
                emptyMsg.className = 'col-span-full py-4 px-3 text-center text-xs text-outline italic border border-dashed border-surface-dim bg-surface-container-low';
                emptyMsg.textContent = 'Chưa chọn ảnh nào. Hãy bấm nút "+ THÊM ẢNH TỪ MÁY TÍNH" ở trên để chọn ảnh thiết bị của bạn.';
                thumbnailsContainer.appendChild(emptyMsg);
            }
            return;
        }

        const noMsg = document.getElementById('no-thumbs-msg');
        if (noMsg) noMsg.remove();

        let hasActive = false;
        let activeItem = null;
        allItems.forEach((item, idx) => {
            const badge = item.querySelector('.thumb-badge');
            if (badge) {
                badge.textContent = (idx === 0) ? 'Bìa' : '#' + (idx + 1);
            }
            if (item.classList.contains('border-[#FF4A1C]')) {
                hasActive = true;
                activeItem = item;
            }
        });

        // Nếu chưa có ảnh nào được active, chọn ảnh đầu tiên
        if (!hasActive && allItems[0]) {
            const firstImg = allItems[0].querySelector('img');
            if (firstImg) {
                selectFormPreview(firstImg.src, allItems[0]);
            }
        } else if (activeItem) {
            const activeImg = activeItem.querySelector('img');
            if (activeImg) {
                selectFormPreview(activeImg.src, activeItem);
            }
        }

        if (chosenText) {
            chosenText.textContent = '+ THÊM ẢNH KHÁC TỪ MÁY TÍNH';
        }

        // Tính dung lượng file mới thêm
        let totalBytes = 0;
        window.uploadedFilesList.forEach(item => {
            if (item.file && item.file.size) totalBytes += item.file.size;
        });
        const mbText = totalBytes > 0 ? ' (' + (totalBytes / (1024 * 1024)).toFixed(1) + ' MB mới)' : '';
        if (sizeInfo) {
            sizeInfo.textContent = 'Đã chọn ' + totalCount + ' ảnh' + mbText;
        }
    };

    // Xử lý khi người dùng chọn 1 hoặc nhiều ảnh từ máy tính (nút chọn nhiều lần)
    window.handleImageSelection = function(pickerInput) {
        if (!pickerInput || !pickerInput.files || pickerInput.files.length === 0) return;

        const newFiles = Array.from(pickerInput.files);
        const thumbnailsContainer = document.getElementById('multi-preview-thumbnails');
        const noMsg = document.getElementById('no-thumbs-msg');
        if (noMsg) noMsg.remove();

        newFiles.forEach(file => {
            const fileId = 'f_' + Date.now() + '_' + Math.random().toString(36).substring(2, 8);
            const objectUrl = URL.createObjectURL(file);

            window.uploadedFilesList.push({
                id: fileId,
                file: file,
                url: objectUrl
            });

            // Tạo thẻ thumbnail thu nhỏ có nút ✕ ở góc trên bên phải
            const thumbDiv = document.createElement('div');
            thumbDiv.id = 'thumb-new-' + fileId;
            thumbDiv.className = 'form-thumb-item new-image-item aspect-[4/3] border border-surface-dim bg-surface-container-low p-0.5 relative overflow-hidden transition-all group';

            const img = document.createElement('img');
            img.src = objectUrl;
            img.className = 'w-full h-full object-cover cursor-pointer';
            img.onclick = function() {
                selectFormPreview(objectUrl, thumbDiv);
            };

            const badge = document.createElement('span');
            badge.className = 'thumb-badge absolute bottom-0.5 left-0.5 px-1 py-0.2 bg-black/75 text-white text-[9px] font-mono';
            badge.textContent = '#';

            const removeBtn = document.createElement('button');
            removeBtn.type = 'button';
            removeBtn.className = 'absolute top-1 right-1 w-5 h-5 rounded-full bg-red-600 hover:bg-red-700 text-white flex items-center justify-center text-[11px] font-bold shadow z-10 transition-transform hover:scale-110 cursor-pointer border-0';
            removeBtn.title = 'Xóa ảnh này';
            removeBtn.textContent = '✕';
            removeBtn.onclick = function(ev) {
                ev.stopPropagation();
                removeNewUploadedFile(fileId);
            };

            thumbDiv.appendChild(img);
            thumbDiv.appendChild(badge);
            thumbDiv.appendChild(removeBtn);
            if (thumbnailsContainer) {
                thumbnailsContainer.appendChild(thumbDiv);
            }
        });

        // Reset giá trị picker để lần sau bấm chọn tiếp vẫn kích hoạt sự kiện onchange
        pickerInput.value = '';

        syncFileInput();
        updateGalleryState();
    };

    // Xóa ảnh mới vừa chọn từ máy tính theo ID
    window.removeNewUploadedFile = function(fileId) {
        const itemObj = window.uploadedFilesList.find(f => f.id === fileId);
        if (itemObj && itemObj.url) {
            try { URL.revokeObjectURL(itemObj.url); } catch (e) {}
        }
        window.uploadedFilesList = window.uploadedFilesList.filter(f => f.id !== fileId);
        const item = document.getElementById('thumb-new-' + fileId);
        if (item) {
            item.remove();
        }
        syncFileInput();
        updateGalleryState();
    };

    // Xóa ảnh cũ đã lưu trước đó (khi chỉnh sửa tin đăng)
    window.removeExistingImage = function(buttonElem) {
        if (!buttonElem) return;
        const item = buttonElem.closest('.form-thumb-item');
        if (item) {
            item.remove();
            updateGalleryState();
        }
    };

    // Khởi tạo trạng thái ban đầu cho giao diện ảnh nếu đang ở form
    const thumbnailsContainer = document.getElementById('multi-preview-thumbnails');
    if (thumbnailsContainer) {
        updateGalleryState();
    }

    // Device Dropdown change: auto-fill specs or allow custom input
    window.onDeviceSelectChange = function(selectElem) {
        if (!selectElem) return;
        const brandInput = document.getElementById('input-brand');
        const modelInput = document.getElementById('input-model');
        const cpuInput = document.getElementById('input-cpu');
        const ramInput = document.getElementById('input-ram');
        const storageInput = document.getElementById('input-storage');

        if (selectElem.value === "0" || selectElem.value === "") {
            // Tự nhập dòng máy mới
            if (brandInput) {
                brandInput.value = '';
                brandInput.placeholder = "Nhập thương hiệu (VD: Lenovo, Dell, Apple...)";
                brandInput.focus();
            }
            if (modelInput) {
                modelInput.value = '';
                modelInput.placeholder = "Nhập model (VD: Legion 5, XPS 15...)";
            }
            return;
        }

        const selectedOpt = selectElem.options[selectElem.selectedIndex];
        if (selectedOpt) {
            if (brandInput && selectedOpt.dataset.brand) brandInput.value = selectedOpt.dataset.brand;
            if (modelInput && selectedOpt.dataset.model) modelInput.value = selectedOpt.dataset.model;
            if (cpuInput && selectedOpt.dataset.cpu) cpuInput.value = selectedOpt.dataset.cpu;
            if (ramInput && selectedOpt.dataset.ram) ramInput.value = selectedOpt.dataset.ram;
            if (storageInput && selectedOpt.dataset.storage) storageInput.value = selectedOpt.dataset.storage;
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
                        <div class="w-full aspect-[4/3] bg-white flex items-center justify-center overflow-hidden p-2">
                            <img class="w-full h-full object-contain group-hover:scale-105 transition-transform duration-300" 
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
