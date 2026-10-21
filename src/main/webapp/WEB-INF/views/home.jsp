<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ElectronicsMarketplace - Nâng cấp Laptop hay Chọn sửa chữa?</title>
    
    <link href="https://fonts.googleapis.com" rel="preconnect">
    <link crossorigin="" href="https://fonts.gstatic.com" rel="preconnect">
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;800;900&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet">
    
    <!-- Custom Styles -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">

    <!-- Tailwind Configuration & CDN for Stitch Source of Truth -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script id="tailwind-config">
    tailwind.config = {
      darkMode: "class",
      theme: {
        extend: {
          colors: {
            "surface": "#f9f9f7", "surface-dim": "#dadad8", "surface-container-lowest": "#ffffff",
            "surface-container-low": "#f4f4f2", "surface-container": "#eeeeec", "surface-container-high": "#e8e8e6",
            "on-surface": "#1a1c1b", "on-surface-variant": "#444748", "outline": "#747878",
            "primary": "#000000", "on-primary": "#ffffff", "primary-container": "#1c1b1b",
            "secondary-container": "#dc3200"
          },
          fontFamily: {
            "label-sm": ["Be Vietnam Pro"], "body-md": ["Be Vietnam Pro"], "headline-lg-mobile": ["Be Vietnam Pro"],
            "display-xl": ["Be Vietnam Pro"], "spec-numeral": ["Be Vietnam Pro"], "body-lg": ["Be Vietnam Pro"],
            "label-md": ["Be Vietnam Pro"], "headline-sm": ["Be Vietnam Pro"], "headline-md": ["Be Vietnam Pro"],
            "body-sm": ["Be Vietnam Pro"], "headline-lg": ["Be Vietnam Pro"]
          }
        }
      }
    };
    </script>
</head>
<body class="bg-background font-body-md text-on-surface antialiased">

<header class="fixed top-0 left-0 right-0 z-50 bg-surface-container-lowest border-b border-surface-dim">
    <div class="max-w-[1440px] mx-auto px-4 md:px-8 lg:px-12 h-16 flex items-center justify-between">
        <div class="flex items-center shrink-0">
            <a class="font-headline-sm text-[18px] text-primary tracking-wider uppercase font-semibold" href="${pageContext.request.contextPath}/">ELECTRONICSMARKETPLACE</a>
        </div>
        <nav class="hidden md:flex items-center h-full gap-6">
            <a class="font-label-md text-[13px] uppercase tracking-wider text-on-surface-variant hover:text-primary transition-colors py-2 font-semibold border-b-2 border-transparent hover:border-primary" href="${pageContext.request.contextPath}/product/list">LAPTOP MỚI</a>
            <a class="font-label-md text-[13px] uppercase tracking-wider text-on-surface-variant hover:text-primary transition-colors py-2 border-b-2 border-transparent hover:border-primary" href="${pageContext.request.contextPath}/listing/marketplace">LAPTOP CŨ</a>
            <a class="font-label-md text-[13px] uppercase tracking-wider text-on-surface-variant hover:text-primary transition-colors py-2 border-b-2 border-transparent hover:border-primary" href="${pageContext.request.contextPath}/repair/request">SỬA CHỮA</a>
        </nav>
        <div class="flex items-center gap-4">
            <button class="flex items-center gap-1 font-label-sm text-[11px] uppercase text-on-surface-variant hover:text-primary transition-colors" type="button">
                <span class="material-symbols-outlined text-[18px]">search</span>
                <span class="hidden lg:inline">Tìm kiếm</span>
            </button>
            <a class="flex items-center gap-1 font-label-sm text-[11px] uppercase text-on-surface-variant hover:text-primary transition-colors" href="${pageContext.request.contextPath}/cart">
                <span class="material-symbols-outlined text-[18px]">shopping_bag</span>
                <span class="hidden lg:inline">Giỏ hàng</span>
            </a>
            <a class="flex items-center gap-1 font-label-sm text-[11px] uppercase text-on-surface-variant hover:text-primary transition-colors" href="${pageContext.request.contextPath}/auth/login">
                <span class="material-symbols-outlined text-[18px]">person</span>
                <span class="hidden lg:inline">Tài khoản</span>
            </a>
        </div>
    </div>
</header>

<main class="w-full pt-16 bg-surface min-h-[calc(100vh-16rem)]">
<div class="flex flex-col w-full bg-surface-container-low text-on-surface antialiased">

<!-- ==========================================
       1. HERO SECTION (Split Swiss Composition, #0B0B0B)
       ========================================== -->
<section class="relative w-full bg-[#050505] text-on-primary border-b border-zinc-800/80 pt-12 pb-32 overflow-hidden" id="hero-architectural" style="min-height: 680px;">
    <!-- Deep Monochromatic Architectural Studio Ambient Lighting & Depth Planes -->
    <div class="absolute inset-0 pointer-events-none transition-transform duration-300 ease-out" id="hero-spatial-bg">
        <div class="absolute inset-0 bg-[#050505]"></div>
        <div class="absolute inset-0 bg-[radial-gradient(ellipse_75%_55%_at_65%_40%,#181818_0%,#090909_60%,#050505_100%)]"></div>
        <div class="absolute inset-x-0 bottom-0 h-72 opacity-30 perspective-grid-container pointer-events-none">
            <div class="w-full h-full perspective-grid"></div>
        </div>
        <div class="absolute inset-0 bg-gradient-to-t from-[#050505] via-transparent to-transparent"></div>
    </div>

    <div class="max-w-[1440px] mx-auto px-4 md:px-8 lg:px-24 relative z-10">
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-center min-h-[580px]">
            <!-- Left: 7 Columns 3D Architectural Extruded Headline -->
            <div class="lg:col-span-7 flex flex-col justify-center pr-0 lg:pr-6 transition-transform duration-200 ease-out" id="hero-headline-plane">
                <div class="flex items-center gap-2 mb-4">
                    <span class="font-mono text-zinc-500 uppercase tracking-widest text-[11px] font-medium">
                        01 — GỢI Ý THEO NGÂN SÁCH & NHU CẦU
                    </span>
                </div>
                <h1 class="text-[42px] sm:text-[68px] lg:text-[96px] font-black uppercase tracking-tight leading-[0.95] text-white select-none text-left mb-2">
                    <span class="block">NÂNG CẤP</span>
                    <span class="block">LAPTOP</span>
                    <span class="block">HAY CHỌN</span>
                    <span class="block leading-tight mt-6">
                        <a class="inline-block text-[#FF4A1C] hover:text-[#ff6a42] transition-transform duration-200 hover:-translate-y-1 no-underline cursor-pointer" href="${pageContext.request.contextPath}/repair/request">SỬA CHỮA?</a>
                    </span>
                </h1>
                <p class="text-[14px] text-zinc-400 max-w-lg leading-relaxed mt-6 mb-12 font-normal">
                    Tìm chiếc laptop phù hợp với nhu cầu, ngân sách hoặc kiểm tra chi phí sửa chữa.
                </p>
                <div class="flex items-center gap-6 text-zinc-400 font-mono text-[11px] uppercase tracking-wider">
                    <span class="flex items-center gap-1"><span class="w-1.5 h-1.5 bg-zinc-300"></span> TIÊU CHUẨN KIỂM KÊ 48 BƯỚC</span>
                    <span class="flex items-center gap-1"><span class="w-1.5 h-1.5 bg-zinc-300"></span> BÁO GIÁ LINH KIỆN GỐC</span>
                </div>
            </div>

            <!-- Right: 5 Columns Product Visual -->
            <div class="lg:col-span-5 relative flex items-center justify-center h-full transition-transform duration-200 ease-out py-8" id="hero-product-plane">
                <div class="relative w-full max-w-[480px] aspect-[4/3] flex items-center justify-center">
                    <div class="absolute -bottom-6 w-4/5 h-12 bg-black/90 rounded-full blur-[24px] pointer-events-none transform scale-y-50"></div>
                    <div class="relative z-10 w-full h-full flex items-center justify-center p-2">
                        <img class="w-full h-full object-contain filter contrast-125 brightness-95 drop-shadow-[0_25px_35px_rgba(0,0,0,0.9)] transition-transform duration-500 hover:scale-[1.02]" src="https://lh3.googleusercontent.com/aida-public/AB6AXuB7bpZp2wnhJgHBu9uyrWzR1Xw2Y3lf9SBKve3Oi1UU6gu6ewIp3-a-OVEkSeR4SST6D57mhJzkvqoU9HHhiUZGmWlnzrfK-I2G9b-AfQkMw3enVfu95-_JJXurHmFrEJARMeExaJ_8zyMYtXTAMAUuDCVwMoDD69LkqB6TPp0zMvPDUT13WYpxVSNv7c3LgNn7dIseGF_RwJ03hx8HtBnxnepuT8dTQ9UqdO94RuQ" alt="Workstation Spec 1:1">
                    </div>
                    <div class="absolute bottom-1 right-2 z-20 font-mono text-[10px] text-zinc-400 bg-[#0c0d10]/90 backdrop-blur-sm px-3 py-1 border border-zinc-800 tracking-widest uppercase flex items-center gap-2 shadow-2xl">
                        <span class="w-1.5 h-1.5 bg-zinc-400"></span> ARCHIVE // WORKSTATION SPEC 1:1
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Overlapping Recommendation Panel -->
    <div class="max-w-[1440px] mx-auto px-4 md:px-8 lg:px-24 relative z-20" style="margin-bottom: -48px;">
        <div class="w-full bg-surface-container-lowest border border-surface-dim p-6 lg:p-12 shadow-xl">
            <div class="flex flex-col lg:flex-row items-stretch lg:items-end gap-6 justify-between">
                <div class="flex flex-col gap-1 min-w-[200px]">
                    <span class="text-[11px] uppercase tracking-widest text-outline font-semibold">BỘ LỌC CẤU HÌNH</span>
                    <h2 class="text-[18px] uppercase text-primary font-semibold tracking-tight">TÌM LAPTOP PHÙ HỢP</h2>
                </div>
                <!-- Field 1: Ngân Sách -->
                <div class="flex flex-col gap-1 flex-1 max-w-xs">
                    <label class="text-[11px] uppercase text-on-surface-variant tracking-wider font-semibold">NGÂN SÁCH MỤC TIÊU</label>
                    <div class="relative flex items-center">
                        <input class="w-full h-12 px-4 bg-surface-container-lowest border border-surface-dim text-[15px] text-on-surface focus:border-primary outline-none transition-colors" type="text" value="25.000.000 ₫">
                        <span class="absolute right-4 text-[11px] text-outline uppercase pointer-events-none font-semibold">VNĐ</span>
                    </div>
                </div>
                <!-- Field 2: Nhu Cầu -->
                <div class="flex flex-col gap-1 flex-1">
                    <label class="text-[11px] uppercase text-on-surface-variant tracking-wider font-semibold">NHU CẦU SỬ DỤNG</label>
                    <div class="grid grid-cols-3 gap-0 border border-surface-dim h-12" id="spec-picker">
                        <button class="spec-btn active bg-primary text-on-primary flex items-center justify-center text-[11px] uppercase tracking-wider border-r border-surface-dim transition-colors" type="button">VĂN PHÒNG</button>
                        <button class="spec-btn bg-surface-container-lowest text-on-surface flex items-center justify-center text-[11px] uppercase tracking-wider border-r border-surface-dim transition-colors" type="button">GAMING</button>
                        <button class="spec-btn bg-surface-container-lowest text-on-surface flex items-center justify-center text-[11px] uppercase tracking-wider transition-colors" type="button">ĐỒ HỌA</button>
                    </div>
                </div>
                <!-- Primary CTA -->
                <div class="flex items-end">
                    <a class="w-full lg:w-auto h-12 px-12 flex items-center justify-center gap-2 bg-[#FF4A1C] text-[#000000] text-[13px] uppercase tracking-widest font-bold border border-[#FF4A1C] hover:brightness-110 active:scale-[0.99] transition-all" href="${pageContext.request.contextPath}/product/list">
                        <span>GỢI Ý CHO TÔI</span>
                        <span class="text-[18px] leading-none">→</span>
                    </a>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- ==========================================
       2. TRUST STRIP
       ========================================== -->
<section class="w-full bg-primary text-on-primary pb-6 border-b border-surface-dim pt-12">
    <div class="max-w-[1440px] mx-auto px-4 md:px-8 lg:px-24">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 border-t border-b border-zinc-800 py-4 text-center">
            <div class="flex items-center justify-center py-1 text-[11px] tracking-[0.2em] uppercase text-zinc-400 font-semibold">KIỂM ĐỊNH PIN 100%</div>
            <div class="flex items-center justify-center py-1 text-[11px] tracking-[0.2em] uppercase text-zinc-400 font-semibold border-l border-zinc-800">ẢNH THỰC TẾ KHÔNG CAN THIỆP</div>
            <div class="flex items-center justify-center py-1 text-[11px] tracking-[0.2em] uppercase text-zinc-400 font-semibold border-l border-zinc-800">BÁO GIÁ MINH BẠCH</div>
            <div class="flex items-center justify-center py-1 text-[11px] tracking-[0.2em] uppercase text-zinc-400 font-semibold border-l border-zinc-800">BẢO HÀNH PHẦN CỨNG 12 THÁNG</div>
        </div>
    </div>
</section>

<!-- ==========================================
       3. SECTION 01 — LAPTOP MỚI CHÍNH HÃNG
       ========================================== -->
<section class="w-full bg-surface-container-lowest py-12 border-b border-surface-dim">
    <div class="max-w-[1440px] mx-auto px-4 md:px-8 lg:px-24">
        <div class="flex flex-col md:flex-row md:items-end justify-between mb-12 border-b border-surface-dim pb-4">
            <div>
                <span class="text-[11px] uppercase tracking-widest text-outline font-semibold">DANH MỤC TIÊU CHUẨN</span>
                <h2 class="text-[32px] md:text-[40px] uppercase text-primary font-bold tracking-tight mt-1 flex items-center gap-3">
                    <span class="w-2 h-7 bg-[#FF4A1C] inline-block"></span> LAPTOP MỚI CHÍNH HÃNG
                </h2>
            </div>
            <div class="mt-2 md:mt-0 text-[11px] uppercase tracking-widest text-on-surface-variant font-semibold">SẢN PHẨM MỚI · NGUỒN GỐC RÕ RÀNG</div>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-0 border-t border-l border-surface-dim">
<<<<<<< Updated upstream
            <!-- Product 1 -->
            <article class="group relative bg-surface-container-lowest border-r border-b border-surface-dim flex flex-col justify-between transition-all duration-200 hover:border-[#0B0B0B] hover:shadow-xl">
                <div class="p-4 flex justify-between items-start">
                    <span class="text-[15px] font-bold text-outline group-hover:text-primary">01</span>
                    <span class="text-[11px] font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 border border-emerald-200 uppercase tracking-wider">NEW SEAL</span>
                </div>
                <div class="w-full aspect-[4/3] px-4 flex items-center justify-center overflow-hidden">
                    <img class="w-full h-full object-contain grayscale group-hover:contrast-125 group-hover:grayscale-0 transition-all duration-300 group-hover:scale-105" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDuyQII0gQZnOvdoq1gfhrI9g_ysOrSAQRT0r7x_To-_rwnejMmSGVL_hcqvaGtMUkLSWXzt5WXLc_eoRG6MOZoYzCdm8t1-rlrsm8RHc9_Sy39CnpeKCfRV0hLfHbEzR-xlkH0yIrLXtYmpsz713-mQZeLsgxRcTK9o2VVhqU6KL-7O-LgM0zZeOvcLp4ox3HkizvmvdgCE2_fD_NQXMi9IhY8m9puUhHGaaHTymE" alt="ThinkPad T14 Gen 2">
                </div>
                <div class="p-4 border-t border-surface-dim flex flex-col gap-2 bg-surface-container-lowest">
                    <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary transition-colors leading-tight">ThinkPad T14 Gen 2</h3>
                    <div class="text-[12px] text-outline">RAM 16GB · SSD 512GB · Core i5-1135G7</div>
                    <div class="flex items-center justify-between pt-2 mt-2 border-t border-surface-container">
                        <span class="text-[22px] font-black tracking-tight text-primary">18.990.000 ₫</span>
                        <a class="text-[11px] uppercase tracking-widest font-bold text-primary flex items-center gap-1 group-hover:translate-x-1 transition-transform" href="${pageContext.request.contextPath}/product/detail">XEM CHI TIẾT →</a>
                    </div>
                </div>
            </article>

            <!-- Product 2 -->
            <article class="group relative bg-surface-container-lowest border-r border-b border-surface-dim flex flex-col justify-between transition-all duration-200 hover:border-[#0B0B0B] hover:shadow-xl">
                <div class="p-4 flex justify-between items-start">
                    <span class="text-[15px] font-bold text-outline group-hover:text-primary">02</span>
                    <span class="text-[11px] font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 border border-emerald-200 uppercase tracking-wider">NEW SEAL</span>
                </div>
                <div class="w-full aspect-[4/3] px-4 flex items-center justify-center overflow-hidden">
                    <img class="w-full h-full object-contain grayscale group-hover:contrast-125 group-hover:grayscale-0 transition-all duration-300 group-hover:scale-105" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCMZcQxzevhWWxy1psajWcGeBNsZAY2DVhuJZ1-iSprdXzq3AkFT-PKF-j35GPLdRXluvd4mVuviiGjM6XbPZEkV8RMG2PVuY7_WsyL6zs7xWFlSBGC9i7ME5Af6skxdmXnRMgKtQtCnaa5YzMbhb2VuSMHA5uJUYK5OzH7Dg4Seipgb1Hh1B61p4WRSdB_1rPaXhMfeyozeyggxQStu83e7ktKjNqB6U3dz58Aoy8" alt="Dell XPS 13 9310">
                </div>
                <div class="p-4 border-t border-surface-dim flex flex-col gap-2 bg-surface-container-lowest">
                    <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary transition-colors leading-tight">Dell XPS 13 9310</h3>
                    <div class="text-[12px] text-outline">RAM 16GB · SSD 512GB · FHD+ 500 Nits</div>
                    <div class="flex items-center justify-between pt-2 mt-2 border-t border-surface-container">
                        <span class="text-[22px] font-black tracking-tight text-primary">24.990.000 ₫</span>
                        <a class="text-[11px] uppercase tracking-widest font-bold text-primary flex items-center gap-1 group-hover:translate-x-1 transition-transform" href="${pageContext.request.contextPath}/product/detail">XEM CHI TIẾT →</a>
                    </div>
                </div>
            </article>

            <!-- Product 3 -->
            <article class="group relative bg-surface-container-lowest border-r border-b border-surface-dim flex flex-col justify-between transition-all duration-200 hover:border-[#FF4A1C] hover:shadow-xl">
                <div class="p-4 flex justify-between items-start">
                    <span class="text-[15px] font-bold text-outline group-hover:text-[#FF4A1C]">03</span>
                    <span class="text-[11px] bg-[#FF4A1C] text-[#000000] px-2.5 py-0.5 uppercase tracking-widest font-extrabold shadow-sm">ĐƯỢC QUAN TÂM</span>
                </div>
                <div class="w-full aspect-[4/3] px-4 flex items-center justify-center overflow-hidden">
                    <img class="w-full h-full object-contain grayscale group-hover:contrast-125 group-hover:grayscale-0 transition-all duration-300 group-hover:scale-105" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAixfwMw10z8WXwzn76NSW63OhdsL4N66DFnp7ArmcICT9XM3KUzpKEZobcmkiqsMDY0vBRoQuAnqNVzwlYbn5pgaesKQlOv8UP9cA6rKiTsxrMUZjXQGox8aaNHcLoxlvsNXuRLgc2yABEZr_KUIwhVaUyfWShG14GwN7N52yMNCFq3wtZVNTEaJnPDI78vrDR1y-dmbvZPjIF2-K7-s5IJyZrXo6gTM5K5B0MXWU" alt="MacBook Air M1 2020">
                </div>
                <div class="p-4 border-t border-surface-dim flex flex-col gap-2 bg-surface-container-lowest">
                    <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary group-hover:text-[#FF4A1C] transition-colors leading-tight">MacBook Air M1 2020</h3>
                    <div class="text-[12px] text-outline">RAM 16GB · SSD 512GB · Apple Silicon 8-Core</div>
                    <div class="flex items-center justify-between pt-2 mt-2 border-t border-surface-container">
                        <span class="text-[22px] font-black tracking-tight text-[#FF4A1C]">19.490.000 ₫</span>
                        <a class="text-[11px] uppercase tracking-widest font-bold text-primary group-hover:text-[#FF4A1C] flex items-center gap-1 group-hover:translate-x-1 transition-transform" href="${pageContext.request.contextPath}/product/detail">XEM CHI TIẾT →</a>
                    </div>
                </div>
            </article>

            <!-- Product 4 -->
            <article class="group relative bg-surface-container-lowest border-r border-b border-surface-dim flex flex-col justify-between transition-all duration-200 hover:border-[#0B0B0B] hover:shadow-xl">
                <div class="p-4 flex justify-between items-start">
                    <span class="text-[15px] font-bold text-outline group-hover:text-primary">04</span>
                    <span class="text-[11px] font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 border border-emerald-200 uppercase tracking-wider">NEW SEAL</span>
                </div>
                <div class="w-full aspect-[4/3] px-4 flex items-center justify-center overflow-hidden">
                    <img class="w-full h-full object-contain grayscale group-hover:contrast-125 group-hover:grayscale-0 transition-all duration-300 group-hover:scale-105" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCYZrOchXXYzGtQ01eVG6H9S3nf-BXbWjEg9McOKK_0gfllqU7GQlRNkKoODJM-ABVxYUeMBRh1wZEiys7j1lV6MzlnaZXrJJiA9JqxfjdL6Lqd-QRCU80G3aawpilK2o2my2KGLa41HVJ9W2Db4QpSxWE0K-dbEFc6j9jyFdKLBwM-TvIJSE6QEpLN8FsCPeiCwPH6SzoeIHmMbPv3Sc-nQn6R1BI_4pyrATA0X1Y" alt="ASUS Vivobook 15">
                </div>
                <div class="p-4 border-t border-surface-dim flex flex-col gap-2 bg-surface-container-lowest">
                    <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary transition-colors leading-tight">ASUS Vivobook 15</h3>
                    <div class="text-[12px] text-outline">RAM 16GB · SSD 512GB · OLED Display</div>
                    <div class="flex items-center justify-between pt-2 mt-2 border-t border-surface-container">
                        <span class="text-[22px] font-black tracking-tight text-primary">14.500.000 ₫</span>
                        <a class="text-[11px] uppercase tracking-widest font-bold text-primary flex items-center gap-1 group-hover:translate-x-1 transition-transform" href="${pageContext.request.contextPath}/product/detail">XEM CHI TIẾT →</a>
                    </div>
                </div>
            </article>
=======
            <c:choose>
                <c:when test="${not empty featuredProducts}">
                    <c:forEach items="${featuredProducts}" var="prod" varStatus="loop">
                        <article class="group relative bg-surface-container-lowest border-r border-b border-surface-dim flex flex-col justify-between transition-all duration-200 hover:border-[#0B0B0B] hover:shadow-xl">
                            <div class="p-4 flex justify-between items-start">
                                <span class="text-[15px] font-bold text-outline group-hover:text-primary">0${loop.count}</span>
                                <span class="text-[11px] font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 border border-emerald-200 uppercase tracking-wider">
                                    <c:out value="${not empty prod.badge ? prod.badge : 'NEW SEAL'}"/>
                                </span>
                            </div>
                            <div class="w-full aspect-[4/3] px-4 bg-white flex items-center justify-center overflow-hidden">
                                <img class="w-full h-full object-contain transition-all duration-300 group-hover:scale-105" src="${prod.imageUrl}" alt="${prod.name}">
                            </div>
                            <div class="p-4 border-t border-surface-dim flex flex-col gap-2 bg-surface-container-lowest">
                                <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary transition-colors leading-tight line-clamp-1"><c:out value="${prod.name}"/></h3>
                                <div class="text-[12px] text-outline line-clamp-1"><c:out value="${not empty prod.description ? prod.description : prod.series}"/></div>
                                <div class="flex items-center justify-between pt-2 mt-2 border-t border-surface-container">
                                    <span class="text-[22px] font-black tracking-tight text-primary">
                                        <fmt:formatNumber value="${prod.price}" pattern="#,###"/> ₫
                                    </span>
                                    <a class="text-[11px] uppercase tracking-widest font-bold text-primary flex items-center gap-1 group-hover:translate-x-1 transition-transform" href="${pageContext.request.contextPath}/product/detail?id=${prod.id}">XEM CHI TIẾT →</a>
                                </div>
                            </div>
                        </article>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <!-- Product 1 -->
                    <article class="group relative bg-surface-container-lowest border-r border-b border-surface-dim flex flex-col justify-between transition-all duration-200 hover:border-[#0B0B0B] hover:shadow-xl">
                        <div class="p-4 flex justify-between items-start">
                            <span class="text-[15px] font-bold text-outline group-hover:text-primary">01</span>
                            <span class="text-[11px] font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 border border-emerald-200 uppercase tracking-wider">NEW SEAL</span>
                        </div>
                        <div class="w-full aspect-[4/3] px-4 bg-white flex items-center justify-center overflow-hidden">
                            <img class="w-full h-full object-contain transition-all duration-300 group-hover:scale-105" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDuyQII0gQZnOvdoq1gfhrI9g_ysOrSAQRT0r7x_To-_rwnejMmSGVL_hcqvaGtMUkLSWXzt5WXLc_eoRG6MOZoYzCdm8t1-rlrsm8RHc9_Sy39CnpeKCfRV0hLfHbEzR-xlkH0yIrLXtYmpsz713-mQZeLsgxRcTK9o2VVhqU6KL-7O-LgM0zZeOvcLp4ox3HkizvmvdgCE2_fD_NQXMi9IhY8m9puUhHGaaHTymE" alt="ThinkPad T14 Gen 2">
                        </div>
                        <div class="p-4 border-t border-surface-dim flex flex-col gap-2 bg-surface-container-lowest">
                            <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary transition-colors leading-tight">ThinkPad T14 Gen 2</h3>
                            <div class="text-[12px] text-outline">RAM 16GB · SSD 512GB · Core i5-1135G7</div>
                            <div class="flex items-center justify-between pt-2 mt-2 border-t border-surface-container">
                                <span class="text-[22px] font-black tracking-tight text-primary">18.990.000 ₫</span>
                                <a class="text-[11px] uppercase tracking-widest font-bold text-primary flex items-center gap-1 group-hover:translate-x-1 transition-transform" href="${pageContext.request.contextPath}/product/detail?id=1">XEM CHI TIẾT →</a>
                            </div>
                        </div>
                    </article>

                    <!-- Product 2 -->
                    <article class="group relative bg-surface-container-lowest border-r border-b border-surface-dim flex flex-col justify-between transition-all duration-200 hover:border-[#0B0B0B] hover:shadow-xl">
                        <div class="p-4 flex justify-between items-start">
                            <span class="text-[15px] font-bold text-outline group-hover:text-primary">02</span>
                            <span class="text-[11px] font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 border border-emerald-200 uppercase tracking-wider">NEW SEAL</span>
                        </div>
                        <div class="w-full aspect-[4/3] px-4 bg-white flex items-center justify-center overflow-hidden">
                            <img class="w-full h-full object-contain transition-all duration-300 group-hover:scale-105" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCMZcQxzevhWWxy1psajWcGeBNsZAY2DVhuJZ1-iSprdXzq3AkFT-PKF-j35GPLdRXluvd4mVuviiGjM6XbPZEkV8RMG2PVuY7_WsyL6zs7xWFlSBGC9i7ME5Af6skxdmXnRMgKtQtCnaa5YzMbhb2VuSMHA5uJUYK5OzH7Dg4Seipgb1Hh1B61p4WRSdB_1rPaXhMfeyozeyggxQStu83e7ktKjNqB6U3dz58Aoy8" alt="Dell XPS 13 9310">
                        </div>
                        <div class="p-4 border-t border-surface-dim flex flex-col gap-2 bg-surface-container-lowest">
                            <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary transition-colors leading-tight">Dell XPS 13 9310</h3>
                            <div class="text-[12px] text-outline">RAM 16GB · SSD 512GB · FHD+ 500 Nits</div>
                            <div class="flex items-center justify-between pt-2 mt-2 border-t border-surface-container">
                                <span class="text-[22px] font-black tracking-tight text-primary">24.990.000 ₫</span>
                                <a class="text-[11px] uppercase tracking-widest font-bold text-primary flex items-center gap-1 group-hover:translate-x-1 transition-transform" href="${pageContext.request.contextPath}/product/detail?id=2">XEM CHI TIẾT →</a>
                            </div>
                        </div>
                    </article>

                    <!-- Product 3 -->
                    <article class="group relative bg-surface-container-lowest border-r border-b border-surface-dim flex flex-col justify-between transition-all duration-200 hover:border-[#FF4A1C] hover:shadow-xl">
                        <div class="p-4 flex justify-between items-start">
                            <span class="text-[15px] font-bold text-outline group-hover:text-[#FF4A1C]">03</span>
                            <span class="text-[11px] bg-[#FF4A1C] text-[#000000] px-2.5 py-0.5 uppercase tracking-widest font-extrabold shadow-sm">ĐƯỢC QUAN TÂM</span>
                        </div>
                        <div class="w-full aspect-[4/3] px-4 bg-white flex items-center justify-center overflow-hidden">
                            <img class="w-full h-full object-contain transition-all duration-300 group-hover:scale-105" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAixfwMw10z8WXwzn76NSW63OhdsL4N66DFnp7ArmcICT9XM3KUzpKEZobcmkiqsMDY0vBRoQuAnqNVzwlYbn5pgaesKQlOv8UP9cA6rKiTsxrMUZjXQGox8aaNHcLoxlvsNXuRLgc2yABEZr_KUIwhVaUyfWShG14GwN7N52yMNCFq3wtZVNTEaJnPDI78vrDR1y-dmbvZPjIF2-K7-s5IJyZrXo6gTM5K5B0MXWU" alt="MacBook Air M1 2020">
                        </div>
                        <div class="p-4 border-t border-surface-dim flex flex-col gap-2 bg-surface-container-lowest">
                            <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary group-hover:text-[#FF4A1C] transition-colors leading-tight">MacBook Air M1 2020</h3>
                            <div class="text-[12px] text-outline">RAM 16GB · SSD 512GB · Apple Silicon 8-Core</div>
                            <div class="flex items-center justify-between pt-2 mt-2 border-t border-surface-container">
                                <span class="text-[22px] font-black tracking-tight text-[#FF4A1C]">19.490.000 ₫</span>
                                <a class="text-[11px] uppercase tracking-widest font-bold text-primary group-hover:text-[#FF4A1C] flex items-center gap-1 group-hover:translate-x-1 transition-transform" href="${pageContext.request.contextPath}/product/detail?id=3">XEM CHI TIẾT →</a>
                            </div>
                        </div>
                    </article>

                    <!-- Product 4 -->
                    <article class="group relative bg-surface-container-lowest border-r border-b border-surface-dim flex flex-col justify-between transition-all duration-200 hover:border-[#0B0B0B] hover:shadow-xl">
                        <div class="p-4 flex justify-between items-start">
                            <span class="text-[15px] font-bold text-outline group-hover:text-primary">04</span>
                            <span class="text-[11px] font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 border border-emerald-200 uppercase tracking-wider">NEW SEAL</span>
                        </div>
                        <div class="w-full aspect-[4/3] px-4 bg-white flex items-center justify-center overflow-hidden">
                            <img class="w-full h-full object-contain transition-all duration-300 group-hover:scale-105" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCYZrOchXXYzGtQ01eVG6H9S3nf-BXbWjEg9McOKK_0gfllqU7GQlRNkKoODJM-ABVxYUeMBRh1wZEiys7j1lV6MzlnaZXrJJiA9JqxfjdL6Lqd-QRCU80G3aawpilK2o2my2KGLa41HVJ9W2Db4QpSxWE0K-dbEFc6j9jyFdKLBwM-TvIJSE6QEpLN8FsCPeiCwPH6SzoeIHmMbPv3Sc-nQn6R1BI_4pyrATA0X1Y" alt="ASUS Vivobook 15">
                        </div>
                        <div class="p-4 border-t border-surface-dim flex flex-col gap-2 bg-surface-container-lowest">
                            <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary transition-colors leading-tight">ASUS Vivobook 15</h3>
                            <div class="text-[12px] text-outline">RAM 16GB · SSD 512GB · OLED Display</div>
                            <div class="flex items-center justify-between pt-2 mt-2 border-t border-surface-container">
                                <span class="text-[22px] font-black tracking-tight text-primary">14.500.000 ₫</span>
                                <a class="text-[11px] uppercase tracking-widest font-bold text-primary flex items-center gap-1 group-hover:translate-x-1 transition-transform" href="${pageContext.request.contextPath}/product/detail?id=4">XEM CHI TIẾT →</a>
                            </div>
                        </div>
                    </article>
                </c:otherwise>
            </c:choose>
>>>>>>> Stashed changes
        </div>
    </div>
</section>

<!-- ==========================================
       4. QUICK CATEGORY NAVIGATION
       ========================================== -->
<section class="w-full bg-surface-container py-12 border-b border-surface-dim">
    <div class="max-w-[1440px] mx-auto px-4 md:px-8 lg:px-24">
        <div class="flex items-center justify-between mb-4">
            <span class="text-[11px] uppercase tracking-widest text-outline font-semibold">ĐIỀU HƯỚNG NHANH</span>
            <span class="text-[11px] uppercase tracking-widest text-primary font-semibold">PHÂN HẠNG THEO TÁC VỤ</span>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-0 border border-surface-dim">
            <a class="group p-12 bg-surface-container-low text-on-surface flex flex-col justify-between min-h-[220px] border-b md:border-b-0 md:border-r border-surface-dim hover:bg-surface-variant transition-colors" href="${pageContext.request.contextPath}/product/list">
                <div class="flex justify-between items-start">
                    <span class="text-[15px] text-outline font-semibold">01</span>
                    <span class="text-[11px] uppercase tracking-widest text-outline font-semibold">DI ĐỘNG · BỀN BỈ</span>
                </div>
                <div>
                    <span class="text-[24px] uppercase tracking-tight font-semibold flex items-center justify-between">
                        LAPTOP VĂN PHÒNG <span class="group-hover:translate-x-2 transition-transform">→</span>
                    </span>
                </div>
            </a>
            
            <a class="group p-12 bg-primary text-on-primary flex flex-col justify-between min-h-[220px] border-b md:border-b-0 md:border-r border-surface-dim hover:bg-primary-container transition-colors" href="${pageContext.request.contextPath}/product/list">
                <div class="flex justify-between items-start">
                    <span class="text-[15px] text-zinc-400 font-semibold">02</span>
                    <span class="text-[11px] uppercase tracking-widest text-zinc-400 font-semibold">HIỆU NĂNG TỐI ĐA</span>
                </div>
                <div>
                    <span class="text-[24px] uppercase tracking-tight font-semibold flex items-center justify-between text-white">
                        LAPTOP GAMING <span class="group-hover:translate-x-2 transition-transform">→</span>
                    </span>
                </div>
            </a>
            
            <a class="group p-12 bg-surface-container-high text-on-surface flex flex-col justify-between min-h-[220px] hover:bg-surface-dim transition-colors" href="${pageContext.request.contextPath}/product/list">
                <div class="flex justify-between items-start">
                    <span class="text-[15px] text-outline font-semibold">03</span>
                    <span class="text-[11px] uppercase tracking-widest text-outline font-semibold">CHUẨN MÀU 100% DCI-P3</span>
                </div>
                <div>
                    <span class="text-[24px] uppercase tracking-tight font-semibold flex items-center justify-between">
                        LAPTOP ĐỒ HỌA <span class="group-hover:translate-x-2 transition-transform">→</span>
                    </span>
                </div>
            </a>
        </div>
    </div>
</section>

<!-- ==========================================
       5. SECTION 02 — CHỢ LAPTOP CŨ
       ========================================== -->
<section class="w-full bg-surface-container-low py-12 border-b border-surface-dim">
    <div class="max-w-[1440px] mx-auto px-4 md:px-8 lg:px-24">
        <div class="flex flex-col md:flex-row md:items-end justify-between mb-12 border-b border-surface-dim pb-4">
            <div>
                <span class="text-[11px] uppercase tracking-widest text-outline font-semibold">KIỂM ĐỊNH ĐỘC LẬP</span>
                <h2 class="text-[32px] md:text-[40px] uppercase text-primary font-bold tracking-tight mt-1 flex items-center gap-3">
                    <span class="w-2 h-7 bg-[#FF4A1C] inline-block"></span> LAPTOP CŨ GIÁ TỐT
                </h2>
            </div>
            <div class="mt-4 md:mt-0">
                <a class="text-xs uppercase font-bold tracking-wider text-primary hover:text-[#FF4A1C] transition-colors flex items-center gap-1.5" href="${pageContext.request.contextPath}/listing/marketplace">
                    <span>XEM TẤT CẢ CHỢ LAPTOP CŨ</span>
                    <span class="text-[16px]">→</span>
                </a>
            </div>
        </div>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
<<<<<<< Updated upstream
            <!-- Used Card 1 -->
            <article class="bg-surface-container-lowest border border-surface-dim flex flex-col justify-between group hover:border-primary transition-all hover:shadow-xl">
                <div class="p-4 border-b border-surface-dim flex items-center justify-between">
                    <span class="text-[11px] bg-emerald-600 text-white font-bold px-2.5 py-1 uppercase tracking-wider leading-none">TÌNH TRẠNG: TỐT</span>
                    <span class="font-mono text-[11px] text-zinc-500 font-semibold tracking-wider uppercase">ID: 8841-TP</span>
                </div>
                <div class="w-full aspect-[4/3] bg-surface-container flex items-center justify-center overflow-hidden">
                    <img class="w-full h-full object-cover grayscale contrast-110 group-hover:scale-105 transition-transform duration-300" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDfW4DtavcquK1SUWDVWq4B-j-lm9TuqguAQN4Xa6MQg4U4aiGZnrk7v3ldHxLGKSeCe8M5zgisMD067mW34tC5KAdqFcN_v98c15IBo7SF0WlHF0EA-qXE2BsIcR-_VzT0LHni0Dr-Tlk-bNzyvpNTtHWVtuTOIxw8cQTE2qfcWCPYRDdie9vqz-Is7Wax6QMcB-0Odv_pp0ewcZPHPH8VU788RLkdImm_NIV1pXw" alt="ThinkPad T14">
                </div>
                <div class="p-4 flex flex-col gap-2">
                    <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary transition-colors">ThinkPad T14 Gen 2</h3>
                    <div class="grid grid-cols-2 gap-1 py-1 border-t border-b border-surface-container text-[11px] uppercase font-semibold">
                        <div class="text-outline">CHU KỲ SẠC</div>
                        <div class="text-right font-bold text-primary">142 LẦN</div>
                    </div>
                    <div class="flex items-center justify-between pt-1 mt-1">
                        <span class="text-[22px] font-black text-primary tracking-tight">12.800.000 ₫</span>
                        <a class="w-9 h-9 flex items-center justify-center bg-primary text-white font-bold hover:bg-[#FF4A1C] transition-colors" href="${pageContext.request.contextPath}/listing/detail?id=1">→</a>
                    </div>
                </div>
            </article>

            <!-- Used Card 2 -->
            <article class="bg-surface-container-lowest border border-surface-dim flex flex-col justify-between group hover:border-primary transition-all hover:shadow-xl">
                <div class="p-4 border-b border-surface-dim flex items-center justify-between">
                    <span class="text-[11px] bg-amber-500 text-white font-bold px-2.5 py-1 uppercase tracking-wider leading-none">TÌNH TRẠNG: TẠM</span>
                    <span class="font-mono text-[11px] text-zinc-500 font-semibold tracking-wider uppercase">ID: 9022-MB</span>
                </div>
                <div class="w-full aspect-[4/3] bg-surface-container flex items-center justify-center overflow-hidden">
                    <img class="w-full h-full object-cover grayscale contrast-110 group-hover:scale-105 transition-transform duration-300" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBH9khgwGVRWUr7pveEsWsrcrRTf1hc0XxMk3WqBU-iNkQ9vLl30BXCO8V3_eIq9V84sBWEa3GMJ-xWKkOjRgw2QsmmZY_4EgL3lwZ9vmdiM4v--QRzytRbb7wbkmOPA1ReAhPrYvATcEMIVJsCGWhLzMOMMItNI1l3ylHgHoaDY9SgTGoQNVrgcQUlq7PtuBWIPMJociJyfylwJ_6R_fvDDJWh33Yv3Y2t91jx9b0" alt="MacBook Air M1">
                </div>
                <div class="p-4 flex flex-col gap-2">
                    <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary transition-colors">MacBook Air M1 2020</h3>
                    <div class="grid grid-cols-2 gap-1 py-1 border-t border-b border-surface-container text-[11px] uppercase font-semibold">
                        <div class="text-outline">CHU KỲ SẠC</div>
                        <div class="text-right font-bold text-primary">310 LẦN</div>
                    </div>
                    <div class="flex items-center justify-between pt-1 mt-1">
                        <span class="text-[22px] font-black text-primary tracking-tight">13.900.000 ₫</span>
                        <a class="w-9 h-9 flex items-center justify-center bg-primary text-white font-bold hover:bg-[#FF4A1C] transition-colors" href="${pageContext.request.contextPath}/listing/detail?id=2">→</a>
                    </div>
                </div>
            </article>

            <!-- Used Card 3 -->
            <article class="bg-surface-container-lowest border border-surface-dim flex flex-col justify-between group hover:border-[#FF4A1C] transition-all hover:shadow-xl">
                <div class="p-4 border-b border-surface-dim flex items-center justify-between">
                    <span class="text-[11px] bg-[#FF4A1C] text-black font-bold px-2.5 py-1 uppercase tracking-wider leading-none">TÌNH TRẠNG: HƯ HỎNG</span>
                    <span class="font-mono text-[11px] text-zinc-500 font-semibold tracking-wider uppercase">ID: 1542-AC</span>
                </div>
                <div class="w-full aspect-[4/3] bg-surface-container flex items-center justify-center overflow-hidden">
                    <img class="w-full h-full object-cover grayscale contrast-110 group-hover:scale-105 transition-transform duration-300" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDiUuFj9nirVJgi2UB2s0CpHLB99-isqHObkrQVP4tE1GH5-Dxrwyv-Ni21oPfk9Y-00bjxVqJTbDdpplGnae49oFLSUi207gyj8DbIRrMataGx5YbzEl_wKQkcBE9TRH42EVYW1fPTpyRY7MepF4YMpVt6u7yy8XOjVm6R1mlEhDFN-_Fk8evmVFBMcosnqg8_3ICZaqkfcLfJtXaXUrglKwf1EzJrPutJ_dvH58E" alt="Acer Nitro 5">
                </div>
                <div class="p-4 flex flex-col gap-2">
                    <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary transition-colors">Acer Nitro 5</h3>
                    <div class="grid grid-cols-2 gap-1 py-1 border-t border-b border-surface-container text-[11px] uppercase font-semibold">
                        <div class="text-outline">CHU KỲ SẠC</div>
                        <div class="text-right font-bold text-primary">215 LẦN</div>
                    </div>
                    <div class="flex items-center justify-between pt-1 mt-1">
                        <span class="text-[22px] font-black text-[#FF4A1C] tracking-tight">9.900.000 ₫</span>
                        <a class="w-9 h-9 flex items-center justify-center bg-primary text-white font-bold hover:bg-[#FF4A1C] transition-colors" href="${pageContext.request.contextPath}/listing/detail?id=3">→</a>
                    </div>
                </div>
            </article>

            <!-- Used Card 4 -->
            <article class="bg-surface-container-lowest border border-surface-dim flex flex-col justify-between group hover:border-primary transition-all hover:shadow-xl">
                <div class="p-4 border-b border-surface-dim flex items-center justify-between">
                    <span class="text-[11px] bg-emerald-600 text-white font-bold px-2.5 py-1 uppercase tracking-wider leading-none">TÌNH TRẠNG: TỐT</span>
                    <span class="font-mono text-[11px] text-zinc-500 font-semibold tracking-wider uppercase">ID: 4109-DX</span>
                </div>
                <div class="w-full aspect-[4/3] bg-surface-container flex items-center justify-center overflow-hidden">
                    <img class="w-full h-full object-cover grayscale contrast-110 group-hover:scale-105 transition-transform duration-300" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDSDK_Y1Tn41n44-PHzjN4HBUkeJo0v_OSMIby1f3oGoKS1XIgWMT0OQ7x-70QP5_J2MFN6k8tKhSjLkWka7UUtU190qsoRx8njivi8sfEnxOxj0eqC61J_i-nvl-El0uUx2un5gtrIJ0X82JdlkRINC2sMFwV9_sGOrsFS2Xrq_UeQ-yYBeTdWKy0wfJSICusiILvie4qLItiyxiLfxq_uz5a50m5ER5NzpuL4wsU" alt="Dell XPS 13">
                </div>
                <div class="p-4 flex flex-col gap-2">
                    <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary transition-colors">Dell XPS 13 9310</h3>
                    <div class="grid grid-cols-2 gap-1 py-1 border-t border-b border-surface-container text-[11px] uppercase font-semibold">
                        <div class="text-outline">CHU KỲ SẠC</div>
                        <div class="text-right font-bold text-primary">88 LẦN</div>
                    </div>
                    <div class="flex items-center justify-between pt-1 mt-1">
                        <span class="text-[22px] font-black text-primary tracking-tight">16.500.000 ₫</span>
                        <a class="w-9 h-9 flex items-center justify-center bg-primary text-white font-bold hover:bg-[#FF4A1C] transition-colors" href="${pageContext.request.contextPath}/listing/detail?id=4">→</a>
                    </div>
                </div>
            </article>
=======
            <c:choose>
                <c:when test="${not empty featuredListings}">
                    <c:forEach items="${featuredListings}" var="l" varStatus="loop">
                        <article class="bg-surface-container-lowest border border-surface-dim flex flex-col justify-between group hover:border-primary transition-all hover:shadow-xl">
                            <div class="p-4 border-b border-surface-dim flex items-center justify-between">
                                <span class="text-[11px] font-bold px-2.5 py-1 uppercase tracking-wider leading-none ${l.condition == 'DAMAGED' ? 'bg-[#FF4A1C] text-black' : (l.condition == 'USED_FAIR' ? 'bg-amber-500 text-white' : 'bg-emerald-600 text-white')}">
                                    <c:choose>
                                        <c:when test="${l.condition == 'DAMAGED'}">TÌNH TRẠNG: HƯ HỎNG</c:when>
                                        <c:when test="${l.condition == 'USED_FAIR'}">TÌNH TRẠNG: TẠM (95%)</c:when>
                                        <c:otherwise>TÌNH TRẠNG: LIKE NEW (99%)</c:otherwise>
                                    </c:choose>
                                </span>
                                <span class="font-mono text-[11px] text-zinc-500 font-semibold tracking-wider uppercase">ID: #${l.id}</span>
                            </div>
                            <div class="w-full aspect-[4/3] bg-white flex items-center justify-center overflow-hidden p-2">
                                <img class="w-full h-full object-contain group-hover:scale-105 transition-transform duration-300" src="${l.mainImageUrl}" alt="${not empty l.device ? l.device.name : 'Laptop Cũ'}">
                            </div>
                            <div class="p-4 flex flex-col gap-2">
                                <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary transition-colors leading-tight line-clamp-1">
                                    <c:out value="${not empty l.device ? l.device.name : l.title}"/>
                                </h3>
                                <div class="grid grid-cols-2 gap-1 py-1 border-t border-b border-surface-container text-[11px] uppercase font-semibold">
                                    <div class="text-outline">CẤU HÌNH</div>
                                    <div class="text-right font-bold text-primary">${not empty l.device ? l.device.ram : 'RAM 16GB'}</div>
                                </div>
                                <div class="flex items-center justify-between pt-1 mt-1">
                                    <span class="text-[22px] font-black text-primary tracking-tight">
                                        <fmt:formatNumber value="${l.price}" pattern="#,###"/> ₫
                                    </span>
                                    <a class="w-9 h-9 flex items-center justify-center bg-primary text-white font-bold hover:bg-[#FF4A1C] transition-colors" href="${pageContext.request.contextPath}/listing/detail?id=${l.id}">→</a>
                                </div>
                            </div>
                        </article>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <!-- Used Card 1 -->
                    <article class="bg-surface-container-lowest border border-surface-dim flex flex-col justify-between group hover:border-primary transition-all hover:shadow-xl">
                        <div class="p-4 border-b border-surface-dim flex items-center justify-between">
                            <span class="text-[11px] bg-emerald-600 text-white font-bold px-2.5 py-1 uppercase tracking-wider leading-none">TÌNH TRẠNG: TỐT</span>
                            <span class="font-mono text-[11px] text-zinc-500 font-semibold tracking-wider uppercase">ID: 8841-TP</span>
                        </div>
                        <div class="w-full aspect-[4/3] bg-white flex items-center justify-center overflow-hidden p-2">
                            <img class="w-full h-full object-contain group-hover:scale-105 transition-transform duration-300" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDfW4DtavcquK1SUWDVWq4B-j-lm9TuqguAQN4Xa6MQg4U4aiGZnrk7v3ldHxLGKSeCe8M5zgisMD067mW34tC5KAdqFcN_v98c15IBo7SF0WlHF0EA-qXE2BsIcR-_VzT0LHni0Dr-Tlk-bNzyvpNTtHWVtuTOIxw8cQTE2qfcWCPYRDdie9vqz-Is7Wax6QMcB-0Odv_pp0ewcZPHPH8VU788RLkdImm_NIV1pXw" alt="ThinkPad T14">
                        </div>
                        <div class="p-4 flex flex-col gap-2">
                            <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary transition-colors">ThinkPad T14 Gen 2</h3>
                            <div class="grid grid-cols-2 gap-1 py-1 border-t border-b border-surface-container text-[11px] uppercase font-semibold">
                                <div class="text-outline">CHU KỲ SẠC</div>
                                <div class="text-right font-bold text-primary">142 LẦN</div>
                            </div>
                            <div class="flex items-center justify-between pt-1 mt-1">
                                <span class="text-[22px] font-black text-primary tracking-tight">12.800.000 ₫</span>
                                <a class="w-9 h-9 flex items-center justify-center bg-primary text-white font-bold hover:bg-[#FF4A1C] transition-colors" href="${pageContext.request.contextPath}/listing/detail?id=1">→</a>
                            </div>
                        </div>
                    </article>

                    <!-- Used Card 2 -->
                    <article class="bg-surface-container-lowest border border-surface-dim flex flex-col justify-between group hover:border-primary transition-all hover:shadow-xl">
                        <div class="p-4 border-b border-surface-dim flex items-center justify-between">
                            <span class="text-[11px] bg-amber-500 text-white font-bold px-2.5 py-1 uppercase tracking-wider leading-none">TÌNH TRẠNG: TẠM</span>
                            <span class="font-mono text-[11px] text-zinc-500 font-semibold tracking-wider uppercase">ID: 9022-MB</span>
                        </div>
                        <div class="w-full aspect-[4/3] bg-white flex items-center justify-center overflow-hidden p-2">
                            <img class="w-full h-full object-contain group-hover:scale-105 transition-transform duration-300" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBH9khgwGVRWUr7pveEsWsrcrRTf1hc0XxMk3WqBU-iNkQ9vLl30BXCO8V3_eIq9V84sBWEa3GMJ-xWKkOjRgw2QsmmZY_4EgL3lwZ9vmdiM4v--QRzytRbb7wbkmOPA1ReAhPrYvATcEMIVJsCGWhLzMOMMItNI1l3ylHgHoaDY9SgTGoQNVrgcQUlq7PtuBWIPMJociJyfylwJ_6R_fvDDJWh33Yv3Y2t91jx9b0" alt="MacBook Air M1">
                        </div>
                        <div class="p-4 flex flex-col gap-2">
                            <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary transition-colors">MacBook Air M1 2020</h3>
                            <div class="grid grid-cols-2 gap-1 py-1 border-t border-b border-surface-container text-[11px] uppercase font-semibold">
                                <div class="text-outline">CHU KỲ SẠC</div>
                                <div class="text-right font-bold text-primary">310 LẦN</div>
                            </div>
                            <div class="flex items-center justify-between pt-1 mt-1">
                                <span class="text-[22px] font-black text-primary tracking-tight">13.900.000 ₫</span>
                                <a class="w-9 h-9 flex items-center justify-center bg-primary text-white font-bold hover:bg-[#FF4A1C] transition-colors" href="${pageContext.request.contextPath}/listing/detail?id=2">→</a>
                            </div>
                        </div>
                    </article>

                    <!-- Used Card 3 -->
                    <article class="bg-surface-container-lowest border border-surface-dim flex flex-col justify-between group hover:border-[#FF4A1C] transition-all hover:shadow-xl">
                        <div class="p-4 border-b border-surface-dim flex items-center justify-between">
                            <span class="text-[11px] bg-[#FF4A1C] text-black font-bold px-2.5 py-1 uppercase tracking-wider leading-none">TÌNH TRẠNG: HƯ HỎNG</span>
                            <span class="font-mono text-[11px] text-zinc-500 font-semibold tracking-wider uppercase">ID: 1542-AC</span>
                        </div>
                        <div class="w-full aspect-[4/3] bg-white flex items-center justify-center overflow-hidden p-2">
                            <img class="w-full h-full object-contain group-hover:scale-105 transition-transform duration-300" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDiUuFj9nirVJgi2UB2s0CpHLB99-isqHObkrQVP4tE1GH5-Dxrwyv-Ni21oPfk9Y-00bjxVqJTbDdpplGnae49oFLSUi207gyj8DbIRrMataGx5YbzEl_wKQkcBE9TRH42EVYW1fPTpyRY7MepF4YMpVt6u7yy8XOjVm6R1mlEhDFN-_Fk8evmVFBMcosnqg8_3ICZaqkfcLfJtXaXUrglKwf1EzJrPutJ_dvH58E" alt="Acer Nitro 5">
                        </div>
                        <div class="p-4 flex flex-col gap-2">
                            <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary transition-colors">Acer Nitro 5</h3>
                            <div class="grid grid-cols-2 gap-1 py-1 border-t border-b border-surface-container text-[11px] uppercase font-semibold">
                                <div class="text-outline">CHU KỲ SẠC</div>
                                <div class="text-right font-bold text-primary">215 LẦN</div>
                            </div>
                            <div class="flex items-center justify-between pt-1 mt-1">
                                <span class="text-[22px] font-black text-[#FF4A1C] tracking-tight">9.900.000 ₫</span>
                                <a class="w-9 h-9 flex items-center justify-center bg-primary text-white font-bold hover:bg-[#FF4A1C] transition-colors" href="${pageContext.request.contextPath}/listing/detail?id=3">→</a>
                            </div>
                        </div>
                    </article>

                    <!-- Used Card 4 -->
                    <article class="bg-surface-container-lowest border border-surface-dim flex flex-col justify-between group hover:border-primary transition-all hover:shadow-xl">
                        <div class="p-4 border-b border-surface-dim flex items-center justify-between">
                            <span class="text-[11px] bg-emerald-600 text-white font-bold px-2.5 py-1 uppercase tracking-wider leading-none">TÌNH TRẠNG: TỐT</span>
                            <span class="font-mono text-[11px] text-zinc-500 font-semibold tracking-wider uppercase">ID: 4109-DX</span>
                        </div>
                        <div class="w-full aspect-[4/3] bg-white flex items-center justify-center overflow-hidden p-2">
                            <img class="w-full h-full object-contain group-hover:scale-105 transition-transform duration-300" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDSDK_Y1Tn41n44-PHzjN4HBUkeJo0v_OSMIby1f3oGoKS1XIgWMT0OQ7x-70QP5_J2MFN6k8tKhSjLkWka7UUtU190qsoRx8njivi8sfEnxOxj0eqC61J_i-nvl-El0uUx2un5gtrIJ0X82JdlkRINC2sMFwV9_sGOrsFS2Xrq_UeQ-yYBeTdWKy0wfJSICusiILvie4qLItiyxiLfxq_uz5a50m5ER5NzpuL4wsU" alt="Dell XPS 13">
                        </div>
                        <div class="p-4 flex flex-col gap-2">
                            <h3 class="text-[20px] font-extrabold uppercase tracking-tight text-primary transition-colors">Dell XPS 13 9310</h3>
                            <div class="grid grid-cols-2 gap-1 py-1 border-t border-b border-surface-container text-[11px] uppercase font-semibold">
                                <div class="text-outline">CHU KỲ SẠC</div>
                                <div class="text-right font-bold text-primary">88 LẦN</div>
                            </div>
                            <div class="flex items-center justify-between pt-1 mt-1">
                                <span class="text-[22px] font-black text-primary tracking-tight">16.500.000 ₫</span>
                                <a class="w-9 h-9 flex items-center justify-center bg-primary text-white font-bold hover:bg-[#FF4A1C] transition-colors" href="${pageContext.request.contextPath}/listing/detail?id=4">→</a>
                            </div>
                        </div>
                    </article>
                </c:otherwise>
            </c:choose>
>>>>>>> Stashed changes
        </div>
    </div>
</section>

<!-- ==========================================
       6. CTA SECTION
       ========================================== -->
<section class="w-full border-b border-surface-dim">
    <a class="group block w-full bg-surface-container-lowest text-primary border-t border-b border-surface-dim hover:bg-surface-container-low transition-colors py-12" href="${pageContext.request.contextPath}/listing/marketplace">
        <div class="max-w-[1440px] mx-auto px-4 md:px-8 lg:px-24">
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-4 items-center">
                <div class="lg:col-span-2 text-[64px] text-outline font-semibold leading-none">01</div>
                <div class="lg:col-span-5"><h3 class="text-[64px] uppercase tracking-tighter font-semibold leading-none">Chợ đồ cũ Laptop</h3></div>
                <div class="lg:col-span-4 text-[16px] text-on-surface-variant">Khám phá laptop đã qua sử dụng với thông tin minh bạch tuyệt đối.</div>
                <div class="lg:col-span-1 flex justify-end"><span class="text-[64px] leading-none group-hover:translate-x-3 transition-transform">→</span></div>
            </div>
        </div>
    </a>
    <a class="group block w-full bg-primary text-on-primary border-b border-surface-dim hover:bg-primary-container transition-colors py-12" href="${pageContext.request.contextPath}/product/list">
        <div class="max-w-[1440px] mx-auto px-4 md:px-8 lg:px-24">
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-4 items-center">
                <div class="lg:col-span-2 text-[64px] text-zinc-600 font-semibold leading-none">02</div>
                <div class="lg:col-span-5"><h3 class="text-[64px] uppercase tracking-tighter font-semibold leading-none text-white">Mua Laptop mới</h3></div>
                <div class="lg:col-span-4 text-[16px] text-zinc-400">Chọn laptop chính hãng phân phối tiêu chuẩn theo đúng thông số kỹ thuật.</div>
                <div class="lg:col-span-1 flex justify-end"><span class="text-[64px] leading-none group-hover:translate-x-3 transition-transform text-white">→</span></div>
            </div>
        </div>
    </a>
    <a class="group block w-full bg-[#FF4A1C] text-[#000000] border-b border-surface-dim hover:brightness-105 transition-all py-12" href="${pageContext.request.contextPath}/repair/request">
        <div class="max-w-[1440px] mx-auto px-4 md:px-8 lg:px-24">
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-4 items-center">
                <div class="lg:col-span-2 text-[64px] text-[#7a1f06] font-semibold leading-none">03</div>
                <div class="lg:col-span-5"><h3 class="text-[64px] uppercase tracking-tighter font-semibold leading-none">Tra cứu phí sửa chữa</h3></div>
                <div class="lg:col-span-4 text-[15px] text-[#000000] font-medium max-w-sm">Ước tính chi phí sửa chữa và thời gian xử lý trước khi mang máy đến phòng kỹ thuật.</div>
                <div class="lg:col-span-1 flex justify-end"><span class="text-[64px] leading-none group-hover:translate-x-3 transition-transform">→</span></div>
            </div>
        </div>
    </a>
</section>

<!-- ==========================================
       7. REPAIR SERVICE MATRIX
       ========================================== -->
<section class="w-full bg-surface-container-lowest py-12">
    <div class="max-w-[1440px] mx-auto px-4 md:px-8 lg:px-24">
        <div class="border border-surface-dim p-6 lg:p-12 bg-surface-container-lowest shadow-sm">
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-center">
                <div class="lg:col-span-5 flex flex-col gap-1">
                    <span class="text-[11px] uppercase tracking-widest text-outline font-semibold">TRẠM BẢO TRÌ & KHẮC PHỤC</span>
                    <h2 class="text-[40px] uppercase text-primary font-semibold tracking-tight">Máy đang gặp vấn đề?</h2>
                    <p class="text-[14px] text-on-surface-variant max-w-sm mt-1">Chọn các hạng mục cần can thiệp để chuẩn bị linh kiện thay thế chuẩn nhà máy.</p>
                </div>
                <div class="lg:col-span-7 flex flex-col md:flex-row items-stretch md:items-center justify-between gap-6">
                    <div class="grid grid-cols-2 sm:grid-cols-3 gap-2 flex-1">
                        <label class="flex items-center gap-2 p-2 border border-surface-dim bg-surface-container-low cursor-pointer hover:border-primary transition-colors">
                            <input checked class="w-4 h-4 accent-primary rounded-none cursor-pointer" type="checkbox">
                            <span class="text-[13px] text-primary uppercase font-medium">Màn hình</span>
                        </label>
                        <label class="flex items-center gap-2 p-2 border border-surface-dim bg-surface-container-low cursor-pointer hover:border-primary transition-colors">
                            <input class="w-4 h-4 accent-primary rounded-none cursor-pointer" type="checkbox">
                            <span class="text-[13px] text-primary uppercase font-medium">Bàn phím</span>
                        </label>
                        <label class="flex items-center gap-2 p-2 border border-surface-dim bg-surface-container-low cursor-pointer hover:border-primary transition-colors">
                            <input class="w-4 h-4 accent-primary rounded-none cursor-pointer" type="checkbox">
                            <span class="text-[13px] text-primary uppercase font-medium">Pin</span>
                        </label>
                        <label class="flex items-center gap-2 p-2 border border-surface-dim bg-surface-container-low cursor-pointer hover:border-primary transition-colors">
                            <input class="w-4 h-4 accent-primary rounded-none cursor-pointer" type="checkbox">
                            <span class="text-[13px] text-primary uppercase font-medium">Mainboard</span>
                        </label>
                        <label class="flex items-center gap-2 p-2 border border-surface-dim bg-surface-container-low cursor-pointer hover:border-primary transition-colors">
                            <input class="w-4 h-4 accent-primary rounded-none cursor-pointer" type="checkbox">
                            <span class="text-[13px] text-primary uppercase font-medium">Bản lề</span>
                        </label>
                        <label class="flex items-center gap-2 p-2 border border-surface-dim bg-surface-container-low cursor-pointer hover:border-primary transition-colors">
                            <input class="w-4 h-4 accent-primary rounded-none cursor-pointer" type="checkbox">
                            <span class="text-[13px] text-primary uppercase font-medium">Khác</span>
                        </label>
                    </div>
                    <div class="flex items-end shrink-0">
                        <a class="h-12 px-6 flex items-center justify-center gap-2 bg-primary text-on-primary text-[13px] uppercase tracking-widest font-semibold border border-primary hover:bg-[#FF4A1C] hover:border-[#FF4A1C] hover:text-[#000000] transition-all" href="${pageContext.request.contextPath}/repair/request">
                            <span>XEM BÁO GIÁ</span>
                            <span class="text-[18px] leading-none">→</span>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

</div>
</main>

<!-- ==========================================
       8. FOOTER (Architectural Minimal)
       ========================================== -->
<footer class="w-full bg-[#050505] text-on-primary border-t border-zinc-800">
    <div class="max-w-[1440px] mx-auto px-4 md:px-8 lg:px-24 py-12">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-6 border-b border-zinc-800 pb-8 mb-8">
            <div class="flex flex-col gap-2">
                <a class="text-[18px] text-white tracking-wider uppercase font-semibold" href="${pageContext.request.contextPath}/">ELECTRONICSMARKETPLACE</a>
                <p class="text-[12px] text-zinc-400 mt-2">Giải pháp nâng cấp và bảo trì thiết bị tối ưu. Tiêu chuẩn minh bạch 100%.</p>
            </div>
            <div class="flex flex-col gap-2">
                <span class="text-[11px] uppercase tracking-widest text-zinc-500 mb-1 font-semibold">DANH MỤC</span>
                <a href="${pageContext.request.contextPath}/product/list" class="text-[13px] text-zinc-300 hover:text-[#FF4A1C] transition-colors uppercase font-medium">Máy Mới</a>
                <a href="${pageContext.request.contextPath}/listing/marketplace" class="text-[13px] text-zinc-300 hover:text-[#FF4A1C] transition-colors uppercase font-medium">Chợ Đồ Cũ</a>
                <a href="${pageContext.request.contextPath}/repair/request" class="text-[13px] text-zinc-300 hover:text-[#FF4A1C] transition-colors uppercase font-medium">Sửa Chữa</a>
            </div>
            <div class="flex flex-col gap-2">
                <span class="text-[11px] uppercase tracking-widest text-zinc-500 mb-1 font-semibold">HỖ TRỢ</span>
                <a href="#" class="text-[13px] text-zinc-300 hover:text-[#FF4A1C] transition-colors uppercase font-medium">Tra cứu đơn hàng</a>
                <a href="#" class="text-[13px] text-zinc-300 hover:text-[#FF4A1C] transition-colors uppercase font-medium">Chính sách bảo hành</a>
                <a href="#" class="text-[13px] text-zinc-300 hover:text-[#FF4A1C] transition-colors uppercase font-medium">Liên hệ kỹ thuật</a>
            </div>
            <div class="flex flex-col gap-2">
                <span class="text-[11px] uppercase tracking-widest text-zinc-500 mb-1 font-semibold">LIÊN HỆ</span>
                <span class="text-[13px] text-zinc-300 uppercase font-medium">1900 8888</span>
                <span class="text-[13px] text-zinc-300 uppercase font-medium">support@electronics.vn</span>
            </div>
        </div>
        <div class="flex flex-col md:flex-row items-center justify-between font-mono text-[10px] text-zinc-600 uppercase tracking-widest">
            <span>© 2026 ELECTRONICSMARKETPLACE. ALL RIGHTS RESERVED.</span>
            <span>DESIGNED VIA STITCH ARCHITECTURE</span>
        </div>
    </div>
</footer>

<!-- Scripts -->
<script src="${pageContext.request.contextPath}/js/main.js"></script>
<script src="${pageContext.request.contextPath}/js/home.js"></script>
</body>
</html>
