<?php
session_start();

$config_file = __DIR__ . '/config.json';
$movies_file = __DIR__ . '/movies.json';
$tx_file = __DIR__ . '/transactions.json';

// Ensure data files exist
if (!file_exists($config_file)) {
    file_put_contents($config_file, json_encode([
        "domain" => "dramelio.com",
        "tripayApiKey" => "DEV-MOCK-TRIPAY-KEY-12345",
        "tripayPrivateKey" => "DEV-MOCK-TRIPAY-PRIVATE-12345",
        "tripayMerchantCode" => "T12345",
        "tmdbApiKey" => "",
        "subscriptionBasicPrice" => 29000,
        "subscriptionPremiumPrice" => 59000,
        "appThemePrimaryHex" => "#EAB308",
        "appThemeBgHex" => "#050505",
        "appThemeAccentHex" => "#FACC15",
        "remoteConfigUrl" => "",
        "isRemoteConfigEnabled" => true,
        "announcementTitle" => "PENGUMUMAN RESMI",
        "announcementContent" => "Nikmati nonton Layangan Putus dan Gadis Kretek eksklusif VIP tanpa buffering!",
        "isAnnouncementActive" => true,
        "supportUrl" => "https://wa.me/6281212121298",
        "isSupportActive" => true,
        "isQrisActive" => true,
        "isVaActive" => true,
        "isEwalletActive" => true,
        "isRetailActive" => true,
        "adminPassword" => "admin"
    ], JSON_PRETTY_PRINT));
}
if (!file_exists($movies_file)) {
    file_put_contents($movies_file, json_encode([], JSON_PRETTY_PRINT));
}
if (!file_exists($tx_file)) {
    file_put_contents($tx_file, json_encode([], JSON_PRETTY_PRINT));
}

// Load configurations
$conf = json_decode(file_get_contents($config_file), true);
$movies = json_decode(file_get_contents($movies_file), true);
$transactions = json_decode(file_get_contents($tx_file), true);

$error_msg = '';
$success_msg = '';

// Handle Logout
if (isset($_GET['logout'])) {
    session_destroy();
    header("Location: " . strtok($_SERVER["REQUEST_URI"], '?'));
    exit();
}

// Handle Login Authentication
if (isset($_POST['login'])) {
    $password = isset($_POST['password']) ? $_POST['password'] : '';
    $correct_password = isset($conf['adminPassword']) ? $conf['adminPassword'] : 'admin';
    if ($password === $correct_password) {
        $_SESSION['admin_auth'] = true;
        header("Location: " . $_SERVER['PHP_SELF']);
        exit();
    } else {
        $error_msg = "Sandi administrator salah!";
    }
}

// Require Authorization
$authenticated = isset($_SESSION['admin_auth']) && $_SESSION['admin_auth'] === true;

if (!$authenticated) {
    // Elegant Dark Login UI
    ?>
    <!DOCTYPE html>
    <html lang="id">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>DRAMELIO Cloud Portal - Login</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    </head>
    <body class="bg-neutral-950 text-white min-h-screen flex items-center justify-center font-sans">
        <div class="bg-neutral-900 border border-neutral-800 p-8 rounded-xl shadow-2xl max-w-md w-full">
            <div class="text-center mb-6">
                <span class="text-3xl font-black tracking-widest text-yellow-500">DRAMELIO</span>
                <p class="text-neutral-400 text-xs mt-2 uppercase tracking-wider font-semibold">cPanel Cloud Administration Control</p>
            </div>
            
            <?php if (!empty($error_msg)): ?>
                <div class="bg-red-550/20 border border-red-500 text-red-300 px-4 py-3 rounded-lg text-sm mb-4 flex items-center gap-2">
                    <i class="fas fa-exclamation-triangle"></i> <?php echo $error_msg; ?>
                </div>
            <?php endif; ?>

            <form method="POST" action="">
                <div class="mb-5">
                    <label class="block text-xs font-bold text-neutral-400 uppercase mb-2">Password Administrator</label>
                    <div class="relative">
                        <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-neutral-500">
                            <i class="fas fa-lock"></i>
                        </span>
                        <input type="password" name="password" required placeholder="Masukkan sandi..." 
                               class="w-full pl-10 pr-4 py-3 bg-neutral-950 border border-neutral-800 rounded-lg focus:outline-none focus:border-yellow-500 text-white placeholder-neutral-600 transition">
                    </div>
                </div>
                <button type="submit" name="login" class="w-full bg-yellow-500 hover:bg-yellow-600 text-black font-extrabold uppercase py-3 rounded-lg tracking-wider transition">
                    MASUK KE BACKEND <i class="fas fa-sign-in-alt ml-1"></i>
                </button>
            </form>
            <div class="text-center mt-6 text-[10px] text-neutral-600">
                Dramelio Entertainment Inc. &bull; PHP Native Framework v8.x API secure
            </div>
        </div>
    </body>
    </html>
    <?php
    exit();
}

// Action Form Handlers
// 1. UPDATE SYSTEM CONFIGS
if (isset($_POST['save_settings'])) {
    $conf['domain'] = $_POST['domain'];
    $conf['tripayApiKey'] = $_POST['tripayApiKey'];
    $conf['tripayPrivateKey'] = $_POST['tripayPrivateKey'];
    $conf['tripayMerchantCode'] = $_POST['tripayMerchantCode'];
    $conf['tmdbApiKey'] = $_POST['tmdbApiKey'];
    $conf['subscriptionBasicPrice'] = (int)$_POST['subscriptionBasicPrice'];
    $conf['subscriptionPremiumPrice'] = (int)$_POST['subscriptionPremiumPrice'];
    $conf['appThemePrimaryHex'] = $_POST['appThemePrimaryHex'];
    $conf['appThemeBgHex'] = $_POST['appThemeBgHex'];
    $conf['appThemeAccentHex'] = $_POST['appThemeAccentHex'];
    $conf['announcementTitle'] = $_POST['announcementTitle'];
    $conf['announcementContent'] = $_POST['announcementContent'];
    $conf['isAnnouncementActive'] = isset($_POST['isAnnouncementActive']);
    $conf['supportUrl'] = $_POST['supportUrl'];
    $conf['isSupportActive'] = isset($_POST['isSupportActive']);
    $conf['isQrisActive'] = isset($_POST['isQrisActive']);
    $conf['isVaActive'] = isset($_POST['isVaActive']);
    $conf['isEwalletActive'] = isset($_POST['isEwalletActive']);
    $conf['isRetailActive'] = isset($_POST['isRetailActive']);
    
    if (!empty($_POST['new_password'])) {
        $conf['adminPassword'] = $_POST['new_password'];
    }

    file_put_contents($config_file, json_encode($conf, JSON_PRETTY_PRINT));
    $success_msg = "Konfigurasi sistem & warna tema berhasil diperbarui!";
}

// 2. DAILY CONTENT MANAGER: ADD OR EDIT CONTENT
if (isset($_POST['save_movie'])) {
    $movie_id = $_POST['movie_id'];
    $title = $_POST['title'];
    $overview = $_POST['overview'];
    $genres_raw = explode(',', $_POST['genres']);
    $genres = array_map('trim', $genres_raw);
    
    // Structure video source resolution streams list
    $res_labels = $_POST['vid_labels'];
    $res_urls = $_POST['vid_urls'];
    $video_sources = [];
    for ($i = 0; $i < count($res_urls); $i++) {
        if (!empty($res_urls[$i])) {
            $video_sources[] = [
                "label" => !empty($res_labels[$i]) ? $res_labels[$i] : "HD Stream",
                "url" => $res_urls[$i]
            ];
        }
    }
    
    // Structure custom series season & episodes if selected as Series
    $is_series = isset($_POST['is_series']);
    $seasons_payload = [];
    if ($is_series) {
        $ep_titles = $_POST['ep_titles'];
        $ep_durations = $_POST['ep_durations'];
        $ep_urls = $_POST['ep_urls'];
        $ep_overviews = $_POST['ep_overviews'];
        $ep_thumbs = $_POST['ep_thumbs'];
        
        $episodes = [];
        for ($k = 0; $k < count($ep_urls); $k++) {
            if (!empty($ep_urls[$k])) {
                $ep_num = $k + 1;
                $episodes[] = [
                    "id" => $movie_id . "_s1_e" . $ep_num,
                    "episodeNumber" => $ep_num,
                    "title" => !empty($ep_titles[$k]) ? $ep_titles[$k] : "Episode " . $ep_num,
                    "duration" => !empty($ep_durations[$k]) ? $ep_durations[$k] : "45m",
                    "thumbnail" => !empty($ep_thumbs[$k]) ? $ep_thumbs[$k] : $_POST['backdropUrl'],
                    "overview" => !empty($ep_overviews[$k]) ? $ep_overviews[$k] : "Kisah lanjutan episode " . $ep_num . " dari judul serial populer " . $title,
                    "videoUrl" => $ep_urls[$k]
                ];
            }
        }
        $seasons_payload[] = [
            "id" => 1,
            "name" => "Season 1",
            "episodes" => $episodes
        ];
    }

    $new_entry = [
        "id" => $movie_id,
        "title" => $title,
        "overview" => $overview,
        "posterUrl" => $_POST['posterUrl'],
        "backdropUrl" => $_POST['backdropUrl'],
        "rating" => (float)$_POST['rating'],
        "releaseDate" => $_POST['releaseDate'],
        "isSeries" => $is_series,
        "genres" => $genres,
        "seasons" => $seasons_payload,
        "videoSources" => $video_sources,
        "ageRating" => $_POST['ageRating'],
        "duration" => $_POST['duration'],
        "quality" => $_POST['quality']
    ];

    // Check for existing element and overwrite, or insert new
    $replaced = false;
    foreach ($movies as &$m) {
        if ($m['id'] === $movie_id) {
            $m = $new_entry;
            $replaced = true;
            break;
        }
    }
    if (!$replaced) {
        array_unshift($movies, $new_entry);
    }

    file_put_contents($movies_file, json_encode($movies, JSON_PRETTY_PRINT));
    $success_msg = "Konten harian '{$title}' berhasil disimpan!";
}

// 3. DAILY CONTENT MANAGER: DELETE MOVIE
if (isset($_GET['delete_movie'])) {
    $target_id = $_GET['delete_movie'];
    $filtered = [];
    foreach ($movies as $m) {
        if ($m['id'] !== $target_id) {
            $filtered[] = $m;
        }
    }
    file_put_contents($movies_file, json_encode($filtered, JSON_PRETTY_PRINT));
    $movies = $filtered;
    $success_msg = "Konten dengan ID '{$target_id}' berhasil dihapus.";
}

// 4. INVOICES LEDGER: MARK TRANSACTION STATE
if (isset($_GET['mark_paid'])) {
    $ref = $_GET['mark_paid'];
    foreach ($transactions as &$tx) {
        if ($tx['reference'] === $ref || (isset($tx['merchant_ref']) && $tx['merchant_ref'] === $ref)) {
            $tx['status'] = 'PAID';
            break;
        }
    }
    file_put_contents($tx_file, json_encode($transactions, JSON_PRETTY_PRINT));
    $success_msg = "Kwitansi transaksi {$ref} berhasil ditandai LUNAS (PAID) secara manual!";
}

// AJAX TMDB Proxy Search route
if (isset($_GET['search_tmdb_query'])) {
    header("Content-Type: application/json; charset=utf-8");
    $q = urlencode($_GET['search_tmdb_query']);
    $api_key = !empty($conf['tmdbApiKey']) ? $conf['tmdbApiKey'] : '';
    
    if (empty($api_key)) {
        echo json_encode(["error" => "Konfigurasi TMDB API Key kosong di tab Setelan."]);
        exit();
    }
    
    // Request multi-search search API from TMDB
    $tmdb_url = "https://api.themoviedb.org/3/search/multi?api_key={$api_key}&query={$q}&language=id-ID&include_adult=false";
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $tmdb_url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 6);
    $res = curl_exec($ch);
    curl_close($ch);
    
    echo $res;
    exit();
}

$active_tab = isset($_GET['tab']) ? $_GET['tab'] : 'settings';
?>
<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DRAMELIO Admin Panel cPanel Control</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        .custom-scrollbar::-webkit-scrollbar {
            width: 6px;
        }
        .custom-scrollbar::-webkit-scrollbar-track {
            background: #171717;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb {
            background: #3f3f46;
            border-radius: 4px;
        }
    </style>
</head>
<body class="bg-neutral-950 text-neutral-100 flex min-h-screen font-sans custom-scrollbar">

    <!-- Primary Vertical Sidebar -->
    <aside class="w-64 bg-neutral-900 border-r border-neutral-800 flex flex-col shrink-0">
        <!-- Logo Header -->
        <div class="p-6 border-b border-neutral-800 text-center">
            <span class="text-2xl font-black tracking-widest text-[#EAB308]">DRAMELIO</span>
            <p class="text-[9px] text-neutral-500 uppercase mt-1 tracking-widest font-extrabold flex items-center justify-center gap-1">
                <i class="fas fa-cloud text-sky-500"></i> cPanel Web server
            </p>
        </div>

        <!-- Navigation Tabs Links -->
        <nav class="flex-1 p-4 space-y-2">
            <a href="?tab=settings" class="flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-semibold transition <?php echo $active_tab === 'settings' ? 'bg-yellow-500 text-black shadow-lg shadow-yellow-500/10' : 'text-neutral-400 hover:bg-neutral-800 hover:text-white'; ?>">
                <i class="fas fa-sliders-h w-5 text-center"></i>
                <span>Setelan Server & Tema</span>
            </a>
            <a href="?tab=content" class="flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-semibold transition <?php echo $active_tab === 'content' ? 'bg-yellow-500 text-black shadow-lg shadow-yellow-500/10' : 'text-neutral-400 hover:bg-neutral-800 hover:text-white'; ?>">
                <i class="fas fa-video w-5 text-center"></i>
                <span>Update Konten Harian</span>
                <span class="ml-auto bg-neutral-800 text-neutral-300 text-[10px] px-2 py-0.5 rounded-full font-bold"><?php echo count($movies); ?></span>
            </a>
            <a href="?tab=transactions" class="flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-semibold transition <?php echo $active_tab === 'transactions' ? 'bg-yellow-500 text-black shadow-lg shadow-yellow-500/10' : 'text-neutral-400 hover:bg-neutral-800 hover:text-white'; ?>">
                <i class="fas fa-receipt w-5 text-center"></i>
                <span>Rincian Transaksi VIP</span>
                <span class="ml-auto bg-neutral-800 text-neutral-300 text-[10px] px-2 py-0.5 rounded-full font-bold"><?php echo count($transactions); ?></span>
            </a>
        </nav>

        <!-- Current Whitelist Node Info -->
        <div class="p-4 m-4 bg-neutral-950 border border-neutral-800 rounded-lg">
            <div class="flex items-center gap-2 text-xs text-neutral-400 font-bold uppercase mb-1">
                <i class="fas fa-id-card text-emerald-500"></i> Server Node IP
            </div>
            <code class="text-[11px] text-[#00ff66] break-all font-mono font-bold"><?php echo $_SERVER['SERVER_ADDR'] ?: '127.0.0.1'; ?></code>
            <p class="text-[9px] text-neutral-500 mt-2 leading-relaxed">
                *Salin IP ini ke panel <strong class="text-neutral-400">TriPay Merchant</strong> Anda untuk whitelist callback server!
            </p>
        </div>

        <!-- Footer actions -->
        <div class="p-4 border-t border-neutral-800">
            <a href="?logout" class="flex items-center justify-center gap-2 w-full bg-red-650 hover:bg-red-700 text-white border border-red-500/30 text-xs font-bold uppercase py-2.5 rounded-lg transition">
                <i class="fas fa-sign-out-alt"></i> Keluar Portal
            </a>
        </div>
    </aside>

    <!-- Main Dynamic Content Canvas -->
    <main class="flex-1 p-8 overflow-y-auto custom-scrollbar">

        <!-- Top Announcement Alerts notifications -->
        <?php if (!empty($success_msg)): ?>
            <div class="mb-6 bg-emerald-550/20 border border-emerald-500 text-emerald-300 px-4 py-3.5 rounded-lg text-sm flex items-center justify-between shadow-lg">
                <span class="flex items-center gap-2"><i class="fas fa-check-circle"></i> <?php echo $success_msg; ?></span>
                <button onclick="parent.location.reload();" class="text-sm opacity-60 hover:opacity-100"><i class="fas fa-times"></i></button>
            </div>
        <?php endif; ?>

        <?php if ($active_tab === 'settings'): ?>
            <!-- TAB 1: SETELAN SERVER, THEME & GATEWAYS -->
            <div class="mb-4">
                <h1 class="text-2xl font-black">Setelan Server & Tema Aplikasi</h1>
                <p class="text-xs text-neutral-500 mt-1 uppercase tracking-wider">Remote configuration parameters for payment codes & branding design styles</p>
            </div>

            <form method="POST" action="">
                <!-- Grid layouts -->
                <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mt-6">
                    <!-- Left column block settings -->
                    <div class="space-y-6">
                        <!-- Card 1: Domain / Backend Setup -->
                        <div class="bg-neutral-900 border border-neutral-800 rounded-xl p-6 shadow-md">
                            <span class="text-sm font-black uppercase text-yellow-500 tracking-wider"><i class="fas fa-globe mr-2"></i> Konfigurasi Server Utama</span>
                            <div class="grid grid-cols-1 gap-4 mt-4">
                                <div>
                                    <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">Domain Hosting (Aplikasi Anda)</label>
                                    <input type="text" name="domain" required value="<?php echo htmlspecialchars($conf['domain']); ?>" class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-4 py-2.5 focus:outline-none focus:border-yellow-500 text-sm">
                                </div>
                                <div class="p-3 bg-neutral-950 border border-neutral-800 rounded-lg flex items-center justify-between">
                                    <div>
                                        <p class="text-xs font-bold text-neutral-300">Endpoint Live Sync URL untuk Aplikasi APK:</p>
                                        <code class="text-[10px] text-yellow-500 break-all font-mono">http://<?php echo $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/api.php'; ?></code>
                                    </div>
                                    <button type="button" onclick="navigator.clipboard.writeText('http://<?php echo $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/api.php'; ?>'); alert('Salin sukses!');" class="p-2 bg-neutral-900 text-neutral-400 hover:text-white rounded border border-neutral-800 transition"><i class="fas fa-copy text-xs"></i></button>
                                </div>
                            </div>
                        </div>

                        <!-- Card 2: Subscription Pricing -->
                        <div class="bg-neutral-900 border border-neutral-800 rounded-xl p-6 shadow-md">
                            <span class="text-sm font-black uppercase text-yellow-500 tracking-wider"><i class="fas fa-tags mr-2"></i> Tarif Langganan VIP</span>
                            <div class="grid grid-cols-2 gap-4 mt-4">
                                <div>
                                    <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">Akses Basic (Rupiah/IDR)</label>
                                    <input type="number" name="subscriptionBasicPrice" required value="<?php echo htmlspecialchars($conf['subscriptionBasicPrice']); ?>" class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-4 py-2.5 focus:outline-none focus:border-yellow-500 text-sm">
                                </div>
                                <div>
                                    <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">Akses Premium (Rupiah/IDR)</label>
                                    <input type="number" name="subscriptionPremiumPrice" required value="<?php echo htmlspecialchars($conf['subscriptionPremiumPrice']); ?>" class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-4 py-2.5 focus:outline-none focus:border-yellow-500 text-sm">
                                </div>
                            </div>
                        </div>

                        <!-- Card 3: TriPay Settings Whitelisting api -->
                        <div class="bg-neutral-900 border border-neutral-800 rounded-xl p-6 shadow-md">
                            <span class="text-sm font-black uppercase text-yellow-500 tracking-wider"><i class="fas fa-credit-card mr-2"></i> Integrasi TriPay Payment Gateway</span>
                            <div class="space-y-4 mt-4">
                                <div>
                                    <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">TriPay API Key (Sandbox/Production)</label>
                                    <input type="text" name="tripayApiKey" value="<?php echo htmlspecialchars($conf['tripayApiKey']); ?>" class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-4 py-2.5 focus:outline-none focus:border-yellow-500 text-sm font-mono">
                                </div>
                                <div class="grid grid-cols-2 gap-4">
                                    <div>
                                        <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">TriPay Private Key</label>
                                        <input type="password" name="tripayPrivateKey" value="<?php echo htmlspecialchars($conf['tripayPrivateKey']); ?>" class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-4 py-2.5 focus:outline-none focus:border-yellow-500 text-sm font-mono">
                                    </div>
                                    <div>
                                        <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">Merchant Code</label>
                                        <input type="text" name="tripayMerchantCode" value="<?php echo htmlspecialchars($conf['tripayMerchantCode']); ?>" class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-4 py-2.5 focus:outline-none focus:border-yellow-500 text-sm font-mono">
                                    </div>
                                </div>
                                <div class="p-3 bg-neutral-950 border border-neutral-800 rounded-lg">
                                    <p class="text-xs font-bold text-neutral-300">Setelan URL Callback Anda di Dashboard TriPay:</p>
                                    <code class="text-[10px] text-green-400 break-all font-mono font-bold">http://<?php echo $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/tripay_callback.php'; ?></code>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Right column space -->
                    <div class="space-y-6">
                        <!-- Card 4: Theme Hex Color control -->
                        <div class="bg-neutral-900 border border-neutral-800 rounded-xl p-6 shadow-md">
                            <span class="text-sm font-black uppercase text-yellow-500 tracking-wider"><i class="fas fa-palette mr-2"></i> Skema Warna Tema Android</span>
                            <div class="grid grid-cols-3 gap-4 mt-4">
                                <div>
                                    <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">Primary accent</label>
                                    <div class="flex gap-2">
                                        <input type="color" value="<?php echo $conf['appThemePrimaryHex']; ?>" onchange="document.getElementById('primary_hex').value = this.value;" class="w-8 h-8 rounded border-none cursor-pointer bg-transparent">
                                        <input type="text" id="primary_hex" name="appThemePrimaryHex" value="<?php echo htmlspecialchars($conf['appThemePrimaryHex']); ?>" class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-2 py-1 text-xs text-center font-mono focus:outline-none">
                                    </div>
                                </div>
                                <div>
                                    <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">Background</label>
                                    <div class="flex gap-2">
                                        <input type="color" value="<?php echo $conf['appThemeBgHex']; ?>" onchange="document.getElementById('bg_hex').value = this.value;" class="w-8 h-8 rounded border-none cursor-pointer bg-transparent">
                                        <input type="text" id="bg_hex" name="appThemeBgHex" value="<?php echo htmlspecialchars($conf['appThemeBgHex']); ?>" class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-2 py-1 text-xs text-center font-mono focus:outline-none">
                                    </div>
                                </div>
                                <div>
                                    <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">Accent Text</label>
                                    <div class="flex gap-2">
                                        <input type="color" value="<?php echo $conf['appThemeAccentHex']; ?>" onchange="document.getElementById('accent_hex').value = this.value;" class="w-8 h-8 rounded border-none cursor-pointer bg-transparent">
                                        <input type="text" id="accent_hex" name="appThemeAccentHex" value="<?php echo htmlspecialchars($conf['appThemeAccentHex']); ?>" class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-2 py-1 text-xs text-center font-mono focus:outline-none">
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Card 5: TMDB API key -->
                        <div class="bg-neutral-900 border border-neutral-800 rounded-xl p-6 shadow-md">
                            <span class="text-sm font-black uppercase text-yellow-500 tracking-wider"><i class="fas fa-search mr-2"></i> TMDb API Key (Pencarian Otomatis)</span>
                            <div class="space-y-3 mt-4">
                                <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">TheMovieDB Developer API Key (v3 auth)</label>
                                <input type="text" name="tmdbApiKey" value="<?php echo htmlspecialchars($conf['tmdbApiKey']); ?>" class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-4 py-2.5 focus:outline-none focus:border-yellow-500 text-sm font-mono" placeholder="Masukkan TMDB API Key untuk mengimpor info film...">
                            </div>
                        </div>

                        <!-- Card 6: Dynamic cloud switches Announcement & Help -->
                        <div class="bg-neutral-900 border border-neutral-800 rounded-xl p-6 shadow-md">
                            <span class="text-sm font-black uppercase text-yellow-500 tracking-wider"><i class="fas fa-megaphone mr-2"></i> Aktivasi Fitur Megaphone & Bantuan</span>
                            <div class="space-y-4 mt-4">
                                <!-- Banner announcement -->
                                <div class="border-b border-neutral-800 pb-4">
                                    <div class="flex items-center justify-between mb-2">
                                        <label class="text-xs font-bold text-neutral-300 uppercase">Banner Pengumuman Layar Utama</label>
                                        <input type="checkbox" name="isAnnouncementActive" class="w-4 h-4 accent-yellow-500" <?php echo $conf['isAnnouncementActive'] ? 'checked' : ''; ?>>
                                    </div>
                                    <input type="text" name="announcementTitle" value="<?php echo htmlspecialchars($conf['announcementTitle']); ?>" placeholder="Judul Pengumuman" class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-3 py-2 text-xs mb-2 focus:outline-none">
                                    <textarea name="announcementContent" rows="2" placeholder="Teks Lengkap Pengumuman" class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-3 py-2 text-xs focus:outline-none"><?php echo htmlspecialchars($conf['announcementContent']); ?></textarea>
                                </div>

                                <!-- Customer service -->
                                <div class="border-b border-neutral-800 pb-4">
                                    <div class="flex items-center justify-between mb-2">
                                        <label class="text-xs font-bold text-neutral-300 uppercase">Saluran Layanan Admin (WhatsApp)</label>
                                        <input type="checkbox" name="isSupportActive" class="w-4 h-4 accent-yellow-500" <?php echo $conf['isSupportActive'] ? 'checked' : ''; ?>>
                                    </div>
                                    <input type="text" name="supportUrl" value="<?php echo htmlspecialchars($conf['supportUrl']); ?>" placeholder="Link Admin WhatsApp (e.g. wa.me/xxx)" class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-3 py-2 text-xs focus:outline-none">
                                </div>

                                <!-- Active Payment channels -->
                                <div>
                                    <label class="block text-xs font-bold text-neutral-400 uppercase mb-3 text-yellow-500"><i class="fas fa-cash-register mr-1"></i> Channel Pembayaran Aktif (TriPay)</label>
                                    <div class="grid grid-cols-2 gap-3">
                                        <label class="flex items-center gap-2 text-xs bg-neutral-950 border border-neutral-800 p-3 rounded-lg cursor-pointer">
                                            <input type="checkbox" name="isQrisActive" class="accent-yellow-500" <?php echo $conf['isQrisActive'] ? 'checked' : ''; ?>>
                                            <span>GPN QRIS</span>
                                        </label>
                                        <label class="flex items-center gap-2 text-xs bg-neutral-950 border border-neutral-800 p-3 rounded-lg cursor-pointer">
                                            <input type="checkbox" name="isVaActive" class="accent-yellow-500" <?php echo $conf['isVaActive'] ? 'checked' : ''; ?>>
                                            <span>Virtual Account Bank</span>
                                        </label>
                                        <label class="flex items-center gap-2 text-xs bg-neutral-950 border border-neutral-800 p-3 rounded-lg cursor-pointer">
                                            <input type="checkbox" name="isEwalletActive" class="accent-yellow-500" <?php echo $conf['isEwalletActive'] ? 'checked' : ''; ?>>
                                            <span>E-Wallet Instan</span>
                                        </label>
                                        <label class="flex items-center gap-2 text-xs bg-neutral-950 border border-neutral-800 p-3 rounded-lg cursor-pointer">
                                            <input type="checkbox" name="isRetailActive" class="accent-yellow-500" <?php echo $conf['isRetailActive'] ? 'checked' : ''; ?>>
                                            <span>Retail (Minimarket)</span>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Card 7: Password Access config -->
                        <div class="bg-neutral-900 border border-neutral-800 rounded-xl p-6 shadow-md">
                            <span class="text-sm font-black uppercase text-yellow-500 tracking-wider"><i class="fas fa-user-shield mr-2"></i> Pengaturan Sandi Administrator</span>
                            <div class="space-y-3 mt-4">
                                <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">Ubah Sandi Keamanan Portal</label>
                                <input type="password" name="new_password" class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-4 py-2.5 focus:outline-none focus:border-yellow-500 text-sm placeholder-neutral-700" placeholder="Biarkan kosong jika tetap menggunakan sandi lama...">
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Submit update action -->
                <div class="mt-8">
                    <button type="submit" name="save_settings" class="bg-yellow-500 hover:bg-yellow-600 text-black font-extrabold px-8 py-3.5 rounded-xl uppercase tracking-wider text-sm shadow-xl shadow-yellow-500/10 transition">
                        SIMPAN SEMUA PERUBAHAN CONFIG SERVER <i class="fas fa-save ml-1"></i>
                    </button>
                </div>
            </form>

        <?php elseif ($active_tab === 'content'): ?>
            <!-- TAB 2: UPDATE KONTEN HARIAN (DAILY CONTENT UPDATES) -->
            <div class="mb-4">
                <h1 class="text-2xl font-black">Management Update Konten Harian</h1>
                <p class="text-xs text-neutral-500 mt-1 uppercase tracking-wider">Configure, add and synchronize multimedia catalogs of Indonesian Drama streaming libraries</p>
            </div>

            <div class="grid grid-cols-1 xl:grid-cols-3 gap-8 mt-6">
                <!-- Left panel: TV Add Form / Edit form -->
                <div class="xl:col-span-1 bg-neutral-900 border border-neutral-800 rounded-xl p-6 shadow-md h-fit">
                    <span class="text-sm font-black uppercase text-yellow-500 tracking-wider"><i class="fas fa-plus-circle mr-2"></i> Tambah / Edit Konten</span>
                    
                    <!-- Form template -->
                    <form id="movie_form" method="POST" action="" class="space-y-4 mt-4">
                        <div>
                            <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">ID Konten Unik (Satu Kata, e.g. layangan_putus)</label>
                            <input type="text" id="m_id" name="movie_id" required class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-4 py-2 focus:outline-none text-sm">
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">Judul Serial / Film</label>
                            <input type="text" id="m_title" name="title" required class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-4 py-2 focus:outline-none text-sm">
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">Sinopsis Lengkap</label>
                            <textarea id="m_overview" name="overview" required rows="3" class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-4 py-2 focus:outline-none text-sm"></textarea>
                        </div>
                        <div class="grid grid-cols-2 gap-4">
                            <div>
                                <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">Release Tanggal</label>
                                <input type="text" id="m_release" name="releaseDate" required placeholder="YYYY-MM-DD" class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-4 py-2 focus:outline-none text-xs">
                            </div>
                            <div>
                                <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">Rating (0 - 10)</label>
                                <input type="number" step="0.1" id="m_rating" name="rating" required class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-4 py-2 focus:outline-none text-xs">
                            </div>
                        </div>

                        <div>
                            <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">Poster Image URL</label>
                            <input type="text" id="m_poster" name="posterUrl" required class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-4 py-2 focus:outline-none text-xs font-mono">
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">Backdrop Banner URL</label>
                            <input type="text" id="m_backdrop" name="backdropUrl" required class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-4 py-2 focus:outline-none text-xs font-mono font-bold">
                        </div>

                        <div class="grid grid-cols-3 gap-2">
                            <div>
                                <label class="block text-[10px] font-bold text-neutral-400 mb-1.5 uppercase">Kualitas</label>
                                <input type="text" id="m_quality" name="quality" value="Ultra HD" required class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-3 py-1.5 focus:outline-none text-xs">
                            </div>
                            <div>
                                <label class="block text-[10px] font-bold text-neutral-400 mb-1.5 uppercase">Durasi / Eps</label>
                                <input type="text" id="m_duration" name="duration" value="1 Season" required class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-3 py-1.5 focus:outline-none text-xs">
                            </div>
                            <div>
                                <label class="block text-[10px] font-bold text-neutral-400 mb-1.5 uppercase">Batas Usia</label>
                                <input type="text" id="m_age" name="ageRating" value="13+" required class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-3 py-1.5 focus:outline-none text-xs">
                            </div>
                        </div>

                        <div>
                            <label class="block text-xs font-bold text-neutral-400 mb-1.5 uppercase">Genres (pisahkan koma, e.g. Drama, Komedi)</label>
                            <input type="text" id="m_genres" name="genres" value="Drama, Romantis" required class="w-full bg-neutral-950 border border-neutral-800 rounded-lg px-4 py-2 focus:outline-none text-xs">
                        </div>

                        <!-- Class type switcher content -->
                        <div class="p-3 bg-neutral-950 border border-neutral-800 rounded-lg">
                            <div class="flex items-center justify-between mb-2">
                                <label class="text-xs font-bold text-neutral-300 uppercase">Apakah Serial TV (Banyak Episode)?</label>
                                <input type="checkbox" id="m_is_series" name="is_series" class="w-4 h-4 accent-yellow-500" onchange="toggle_series_episodes(this.checked);">
                            </div>
                        </div>

                        <!-- Section A: Standard Movie Stream sources -->
                        <div id="movie_source_block" class="space-y-2">
                            <label class="block text-xs font-bold text-yellow-500 uppercase"><i class="fas fa-play-circle mr-1"></i> Resolusi Aliran Video (Movie)</label>
                            <div class="grid grid-cols-3 gap-2">
                                <input type="text" name="vid_labels[]" value="1080p Ultra HD" placeholder="Label" class="col-span-1 bg-neutral-950 border border-neutral-800 rounded px-2 py-1.5 text-xs text-white">
                                <input type="text" name="vid_urls[]" value="https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" placeholder="URL .mp4" class="col-span-2 bg-neutral-950 border border-neutral-800 rounded px-2 py-1.5 text-xs font-mono">
                            </div>
                        </div>

                        <!-- Section B: Serial Episodes Block -->
                        <div id="series_source_block" class="hidden space-y-3">
                            <label class="block text-xs font-bold text-yellow-500 uppercase"><i class="fas fa-list-ol mr-1"></i> Daftar Episode (Season 1)</label>
                            <div id="ep_container" class="space-y-4 max-h-56 overflow-y-auto custom-scrollbar p-1">
                                <!-- Episode rows items template -->
                                <div class="bg-neutral-950 border border-neutral-800 p-3 rounded-lg relative">
                                    <span class="text-[10px] font-bold text-yellow-500 uppercase block mb-2">Episode 1</span>
                                    <input type="text" name="ep_titles[]" value="Awal Mula Keretakan" placeholder="Judul Episode" class="w-full bg-neutral-900 border border-neutral-800 rounded px-2 py-1 text-xs mb-1 mb-2">
                                    <input type="text" name="ep_urls[]" value="https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" placeholder="URL Video .mp4" class="w-full bg-neutral-900 border border-neutral-800 rounded px-2 py-1 text-xs font-mono mb-2">
                                    <div class="grid grid-cols-2 gap-2">
                                        <input type="text" name="ep_durations[]" value="45m" placeholder="Durasi (e.g. 45m)" class="bg-neutral-900 border border-neutral-800 rounded px-2 py-1 text-xs">
                                        <input type="text" name="ep_thumbs[]" placeholder="Custom Thumbnail URL" class="bg-neutral-900 border border-neutral-800 rounded px-2 py-1 text-xs font-mono">
                                    </div>
                                    <textarea name="ep_overviews[]" placeholder="Kisah singkat episode 1..." class="w-full bg-neutral-900 border border-neutral-800 rounded px-2 py-1 text-xs mt-2" rows="1">Kinana mulai menaruh kecurigaan akan gerak gerik pasangannya.</textarea>
                                </div>
                            </div>
                            <button type="button" onclick="add_episode_row()" class="w-full border border-dashed border-neutral-700 hover:border-yellow-500 text-neutral-400 hover:text-white text-[11px] font-bold uppercase py-2 rounded transition">
                                <i class="fas fa-plus"></i> Tambah Episode Lain
                            </button>
                        </div>

                        <!-- Submit button Add -->
                        <div class="pt-2 flex gap-3">
                            <button type="submit" name="save_movie" class="flex-1 bg-yellow-500 hover:bg-yellow-600 text-black font-extrabold uppercase py-3 rounded-lg text-xs tracking-wider transition">
                                SIMPAN KONTEN <i class="fas fa-save ml-1"></i>
                            </button>
                            <button type="button" onclick="reset_movie_form()" class="bg-neutral-800 hover:bg-neutral-700 text-white font-extrabold py-3 px-4 rounded-lg text-xs transition">
                                BATAL
                            </button>
                        </div>
                    </form>
                </div>

                <!-- Right panel: TMDB Autocomplete + Live Movies List Grid -->
                <div class="xl:col-span-2 space-y-6">
                    <!-- TMDB Autocomplete widget -->
                    <div class="bg-neutral-900 border border-neutral-800 rounded-xl p-6 shadow-md">
                        <span class="text-sm font-black uppercase text-[#EAB308] tracking-wider"><i class="fas fa-magnifying-glass mr-2"></i> Cari & Impor Otomatis Via TMDb API</span>
                        <p class="text-[10px] text-neutral-500 uppercase tracking-widest mt-1 mb-4">Query database TMDB directly and auto-inject movie details into cPanel database</p>
                        
                        <div class="flex gap-2">
                            <input type="text" id="tmdb_query_input" placeholder="Masukkan judul serial TV atau Film (e.g. Gadis Kretek, Layangan Putus)..." 
                                   class="flex-1 bg-neutral-950 border border-neutral-800 rounded-lg px-4 py-2.5 focus:outline-none focus:border-yellow-500 text-sm">
                            <button type="button" onclick="search_tmdb_ajax()" class="bg-yellow-500 hover:bg-yellow-600 text-black font-extrabold px-6 rounded-lg text-xs uppercase tracking-wider flex items-center gap-1 transition">
                                <i class="fas fa-search"></i> CARI
                            </button>
                        </div>

                        <!-- AJax result lists container -->
                        <div id="tmdb_results_container" class="hidden mt-4 bg-neutral-950 border border-neutral-800 rounded-lg max-h-64 overflow-y-auto custom-scrollbar p-2 divide-y divide-neutral-900">
                            <!-- Populates matches dynamically -->
                        </div>
                    </div>

                    <!-- Live catalogs grid list -->
                    <div class="bg-neutral-900 border border-neutral-800 rounded-xl p-6 shadow-md">
                        <span class="text-sm font-black uppercase text-yellow-500 tracking-wider"><i class="fas fa-film mr-2"></i> Daftar Konten Aktif Saat Ini</span>
                        
                        <!-- Cards listings -->
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                            <?php if (empty($movies)): ?>
                                <div class="col-span-2 text-center p-8 border border-dashed border-neutral-800 rounded-lg text-neutral-500">
                                    <i class="fas fa-film text-3xl mb-2 block"></i>
                                    Database masih kosong. Gunakan panel kiri atau cari via TMDb untuk menambahkan konten harian pertama Anda!
                                </div>
                            <?php else: ?>
                                <?php foreach ($movies as $m): ?>
                                    <div class="bg-neutral-950 border border-neutral-800 rounded-xl p-4 flex gap-4 hover:border-neutral-700 transition relative">
                                        <img src="<?php echo htmlspecialchars($m['posterUrl']); ?>" class="w-16 h-24 object-cover rounded-lg bg-neutral-900 shrink-0">
                                        <div class="flex-1 min-w-0 flex flex-col justify-between">
                                            <div>
                                                <div class="flex items-center gap-1.5 flex-wrap">
                                                    <span class="text-xs font-black truncate text-white"><?php echo htmlspecialchars($m['title']); ?></span>
                                                    <span class="text-[9px] font-extrabold uppercase px-1.5 py-0.5 rounded <?php echo $m['isSeries'] ? 'bg-indigo-650 text-indigo-200 border border-indigo-500/20' : 'bg-rose-650 text-rose-200 border border-rose-500/20'; ?>">
                                                        <?php echo $m['isSeries'] ? 'TV Show' : 'Movie'; ?>
                                                    </span>
                                                </div>
                                                <p class="text-[10px] text-neutral-400 mt-1 line-clamp-2 leading-relaxed"><?php echo htmlspecialchars($m['overview']); ?></p>
                                            </div>
                                            
                                            <!-- Stats details -->
                                            <div class="flex items-center gap-3 text-[9px] text-[#EAB308] font-bold uppercase mt-2">
                                                <span><i class="fas fa-star mr-1"></i><?php echo $m['rating']; ?></span>
                                                <span><i class="fas fa-clock mr-1"></i><?php echo htmlspecialchars($m['duration']); ?></span>
                                                <span><i class="fas fa-calendar-alt mr-1"></i><?php echo date("Y", strtotime($m['releaseDate'])); ?></span>
                                            </div>
                                        </div>

                                        <!-- floating actions button -->
                                        <div class="absolute top-3 right-3 flex gap-1">
                                            <button onclick='edit_movie_metadata(<?php echo json_encode($m); ?>)' class="p-1.5 hover:bg-neutral-800 text-yellow-500 hover:text-yellow-400 rounded transition" title="Edit Konten"><i class="fas fa-edit text-xs"></i></button>
                                            <a href="?tab=content&delete_movie=<?php echo $m['id']; ?>" onclick="return confirm('Yakin ingin menghapus konten ini?');" class="p-1.5 hover:bg-neutral-800 text-red-500 hover:text-red-400 rounded transition" title="Hapus"><i class="fas fa-trash-alt text-xs"></i></a>
                                        </div>
                                    </div>
                                <?php endforeach; ?>
                            <?php endif; ?>
                        </div>
                    </div>
                </div>
            </div>

        <?php elseif ($active_tab === 'transactions'): ?>
            <!-- TAB 3: DETAIL TRANSAKSI / INVOICE LOGS -->
            <div class="mb-4">
                <h1 class="text-2xl font-black">Rincian & Log Transaksi VIP</h1>
                <p class="text-xs text-neutral-500 mt-1 uppercase tracking-wider">Detailed auditing ledger of member payments synchronizations registered by client-side checkout requests</p>
            </div>

            <!-- Table of orders -->
            <div class="bg-neutral-900 border border-neutral-800 rounded-xl shadow-md overflow-hidden mt-6">
                <div class="p-6 border-b border-neutral-800 flex justify-between items-center bg-neutral-900/50">
                    <span class="text-sm font-black uppercase text-yellow-500 tracking-wider"><i class="fas fa-receipt mr-2"></i> Log Transaksi Masuk</span>
                    <p class="text-xs text-neutral-500">Semua riwayat tagihan terdaftar otomatis dari pemesanan aplikasi Android</p>
                </div>
                
                <div class="overflow-x-auto">
                    <table class="w-full text-left text-xs divide-y divide-neutral-800">
                        <thead class="bg-neutral-950 text-neutral-400 font-bold uppercase text-[10px]">
                            <tr>
                                <th class="px-6 py-4">Tanggal / Waktu</th>
                                <th class="px-6 py-4">Referensi Invoice</th>
                                <th class="px-6 py-4">Paket Akses</th>
                                <th class="px-6 py-4">Total Amount</th>
                                <th class="px-6 py-4">Metode Bayar</th>
                                <th class="px-6 py-4">No. Virtual Account / Instruksi</th>
                                <th class="px-6 py-4 text-center">Status Pembayaran</th>
                                <th class="px-6 py-4 text-center">Tindakan Admin</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-neutral-800 bg-neutral-900/20 font-medium">
                            <?php if (empty($transactions)): ?>
                                <tr>
                                    <td colspan="8" class="text-center py-12 text-neutral-550 italic bg-neutral-950/20">
                                        <i class="fas fa-receipt text-3xl mb-2 block text-neutral-500"></i> Belum ada log transaksi masuk dari aplikasi klien.
                                    </td>
                                </tr>
                            <?php else: ?>
                                <?php foreach ($transactions as $tx): ?>
                                    <tr class="hover:bg-neutral-950/50 transition">
                                        <td class="px-6 py-4 text-neutral-400 font-semibold whitespace-nowrap"><?php echo htmlspecialchars($tx['date']); ?></td>
                                        <td class="px-6 py-4 font-mono text-white font-bold whitespace-nowrap">
                                            <span class="bg-neutral-950 border border-neutral-800 px-2 py-1 rounded"><?php echo htmlspecialchars(isset($tx['reference']) ? $tx['reference'] : $tx['merchant_ref']); ?></span>
                                        </td>
                                        <td class="px-6 py-4 uppercase">
                                            <span class="px-2 py-0.5 rounded font-bold text-[10px] <?php echo $tx['planId'] === 'premium' ? 'bg-yellow-500/10 text-yellow-400 border border-yellow-500/20' : 'bg-neutral-700/30 text-neutral-300'; ?>">
                                                Dramelio <?php echo htmlspecialchars($tx['planId']); ?>
                                            </span>
                                        </td>
                                        <td class="px-6 py-4 text-emerald-400 font-bold whitespace-nowrap">Rp <?php echo number_format($tx['amount'], 0, ',', '.'); ?></td>
                                        <td class="px-6 py-4 font-bold text-neutral-300"><?php echo htmlspecialchars($tx['paymentMethodCode']); ?></td>
                                        <td class="px-6 py-4">
                                            <span class="font-mono text-yellow-400 font-bold block"><?php echo htmlspecialchars($tx['vaNumber'] ?: 'Invoice Instan'); ?></span>
                                            <span class="text-[10px] text-neutral-500 line-clamp-1 truncate block max-w-xs" title="<?php echo htmlspecialchars($tx['paymentInstructions']); ?>"><?php echo htmlspecialchars($tx['paymentInstructions']); ?></span>
                                        </td>
                                        <td class="px-6 py-4 text-center whitespace-nowrap">
                                            <?php if ($tx['status'] === 'PAID'): ?>
                                                <span class="bg-emerald-500/15 border border-emerald-500 text-emerald-400 text-[10px] px-3 py-1 rounded-full font-black uppercase tracking-wider shadow shadow-emerald-500/5"><i class="fas fa-check-circle mr-1 text-[9px]"></i> LUNAS (PAID)</span>
                                            <?php elseif ($tx['status'] === 'EXPIRED'): ?>
                                                <span class="bg-neutral-850 border border-neutral-700 text-neutral-400 text-[10px] px-3 py-1 rounded-full font-bold uppercase tracking-wider">KADALUARSA</span>
                                            <?php else: ?>
                                                <span class="bg-amber-500/15 border border-amber-500 text-amber-400 text-[10px] px-3 py-1 rounded-full font-black uppercase tracking-wide animate-pulse"><i class="fas fa-circle mr-1 text-[7px] text-amber-500"></i> MENUNGGU</span>
                                            <?php endif; ?>
                                        </td>
                                        <td class="px-6 py-4 text-center whitespace-nowrap">
                                            <?php if ($tx['status'] !== 'PAID'): ?>
                                                <a href="?tab=transactions&mark_paid=<?php echo isset($tx['reference']) ? $tx['reference'] : $tx['merchant_ref']; ?>" class="bg-yellow-500 hover:bg-yellow-600 text-black text-[10px] font-black uppercase px-3 py-1.5 rounded transition shadow-lg shadow-yellow-500/5">
                                                    Tandai Lunas <i class="fas fa-check"></i>
                                                </a>
                                            <?php else: ?>
                                                <span class="text-neutral-550 text-[11px]">-</span>
                                            <?php endif; ?>
                                        </td>
                                    </tr>
                                <?php endforeach; ?>
                            <?php endif; ?>
                        </tbody>
                    </table>
                </div>
            </div>
        <?php endif; ?>

    </main>

    <!-- Custom inline helper Javascripts -->
    <script>
        // Toggle Series episode fields block
        function toggle_series_episodes(isSeries) {
            const seriesBlock = document.getElementById('series_source_block');
            const movieBlock = document.getElementById('movie_source_block');
            if (isSeries) {
                seriesBlock.classList.remove('hidden');
                movieBlock.classList.add('hidden');
            } else {
                seriesBlock.classList.add('hidden');
                movieBlock.classList.remove('hidden');
            }
        }

        // Add dynamic episode input card rows
        function add_episode_row() {
            const container = document.getElementById('ep_container');
            const count = container.children.length + 1;
            
            const div = document.createElement('div');
            div.className = "bg-neutral-950 border border-neutral-800 p-3 rounded-lg relative";
            div.innerHTML = `
                <span class="text-[10px] font-bold text-yellow-500 uppercase block mb-2">Episode ${count}</span>
                <input type="text" name="ep_titles[]" placeholder="Judul Episode" class="w-full bg-neutral-900 border border-neutral-800 rounded px-2 py-1 text-xs mb-2">
                <input type="text" name="ep_urls[]" placeholder="URL Video .mp4" class="w-full bg-neutral-900 border border-neutral-800 rounded px-2 py-1 text-xs font-mono mb-2">
                <div class="grid grid-cols-2 gap-2">
                    <input type="text" name="ep_durations[]" placeholder="Durasi (e.g. 45m)" class="bg-neutral-900 border border-neutral-800 rounded px-2 py-1 text-xs">
                    <input type="text" name="ep_thumbs[]" placeholder="Custom Thumbnail URL" class="bg-neutral-900 border border-neutral-800 rounded px-2 py-1 text-xs font-mono">
                </div>
                <textarea name="ep_overviews[]" placeholder="Kisah singkat episode ${count}..." class="w-full bg-neutral-900 border border-neutral-800 rounded px-2 py-1 text-xs mt-2" rows="1"></textarea>
            `;
            container.appendChild(div);
        }

        // Edit existing series/movie entry
        function edit_movie_metadata(movie) {
            document.getElementById('m_id').value = movie.id;
            document.getElementById('m_title').value = movie.title;
            document.getElementById('m_overview').value = movie.overview;
            document.getElementById('m_release').value = movie.releaseDate;
            document.getElementById('m_rating').value = movie.rating;
            document.getElementById('m_poster').value = movie.posterUrl;
            document.getElementById('m_backdrop').value = movie.backdropUrl;
            document.getElementById('m_quality').value = movie.quality;
            document.getElementById('m_duration').value = movie.duration;
            document.getElementById('m_age').value = movie.ageRating;
            document.getElementById('m_genres').value = movie.genres.join(', ');
            
            const isSeriesCheck = document.getElementById('m_is_series');
            isSeriesCheck.checked = movie.isSeries === true;
            toggle_series_episodes(movie.isSeries);

            if (!movie.isSeries && movie.videoSources && movie.videoSources[0]) {
                const labels = document.getElementsByName('vid_labels[]');
                const urls = document.getElementsByName('vid_urls[]');
                if (labels[0] && urls[0]) {
                    labels[0].value = movie.videoSources[0].label;
                    urls[0].value = movie.videoSources[0].url;
                }
            } else if (movie.isSeries && movie.seasons && movie.seasons[0]) {
                const container = document.getElementById('ep_container');
                container.innerHTML = '';
                const eps = movie.seasons[0].episodes || [];
                eps.forEach((ep) => {
                    const div = document.createElement('div');
                    div.className = "bg-neutral-950 border border-neutral-800 p-3 rounded-lg relative";
                    div.innerHTML = `
                        <span class="text-[10px] font-bold text-yellow-500 uppercase block mb-2">Episode ${ep.episodeNumber}</span>
                        <input type="text" name="ep_titles[]" value="${ep.title}" placeholder="Judul Episode" class="w-full bg-neutral-900 border border-neutral-800 rounded px-2 py-1 text-xs mb-2">
                        <input type="text" name="ep_urls[]" value="${ep.videoUrl}" placeholder="URL Video .mp4" class="w-full bg-neutral-900 border border-neutral-800 rounded px-2 py-1 text-xs font-mono mb-2">
                        <div class="grid grid-cols-2 gap-2">
                            <input type="text" name="ep_durations[]" value="${ep.duration}" placeholder="Durasi (e.g. 45m)" class="bg-neutral-900 border border-neutral-800 rounded px-2 py-1 text-xs">
                            <input type="text" name="ep_thumbs[]" value="${ep.thumbnail}" placeholder="Custom Thumbnail URL" class="bg-neutral-900 border border-neutral-800 rounded px-2 py-1 text-xs font-mono">
                        </div>
                        <textarea name="ep_overviews[]" placeholder="Kisah singkat episode..." class="w-full bg-neutral-900 border border-neutral-800 rounded px-2 py-1 text-xs mt-2" rows="1">${ep.overview}</textarea>
                    `;
                    container.appendChild(div);
                });
            }

            // Scroll form to view
            document.getElementById('movie_form').scrollIntoView({ behavior: 'smooth' });
        }

        // Reset TV forms action
        function reset_movie_form() {
            document.getElementById('movie_form').reset();
            toggle_series_episodes(false);
        }

        // AJAX TMDB Autocomplete list search
        function search_tmdb_ajax() {
            const query = document.getElementById('tmdb_query_input').value.trim();
            const container = document.getElementById('tmdb_results_container');
            if (query.length < 2) {
                alert("Masukkan minimal 2 karakter kata kunci pencarian!");
                return;
            }
            
            container.innerHTML = `<div class="text-xs text-neutral-400 p-4 text-center flex items-center justify-center gap-2 font-bold uppercase"><i class="fas fa-spinner animate-spin text-yellow-500"></i> Menghubungi Server TMDB...</div>`;
            container.classList.remove('hidden');

            fetch(`?search_tmdb_query=${encodeURIComponent(query)}`)
                .then(res => res.json())
                .then(data => {
                    container.innerHTML = '';
                    const results = data.results || [];
                    if (results.length === 0) {
                        container.innerHTML = `<div class="text-xs text-neutral-500 p-4 text-center">Tidak ada film atau serial TV ditemukan di TMDB. Periksa kata kunci atau pastikan TMDB API Key sudah benar.</div>`;
                        return;
                    }

                    results.forEach(item => {
                        const title = item.title || item.name || "Untitled";
                        const isTv = item.media_type === 'tv';
                        const year = (item.release_date || item.first_air_date || '').substring(0, 4) || "-";
                        const poster = item.poster_path ? `https://images.tmdb.org/t/p/w500${item.poster_path}` : 'https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=200';
                        const backdrop = item.backdrop_path ? `https://images.tmdb.org/t/p/original${item.backdrop_path}` : 'https://images.unsplash.com/photo-1594909122845-11baa439b7bf?w=800';
                        const rating = item.vote_average ? item.vote_average.toFixed(1) : "0.0";
                        const originalName = item.original_name || item.original_title || '';
                        
                        // Construct clickable layout item
                        const div = document.createElement('div');
                        div.className = "flex items-center gap-3 p-2 hover:bg-neutral-900 cursor-pointer rounded transition";
                        div.onclick = () => {
                            // Populate core layout values immediately
                            document.getElementById('m_id').value = title.toLowerCase().replace(/[^a-z0-9_]+/g, '_').replace(/^_+|_+$/g, '');
                            document.getElementById('m_title').value = title;
                            document.getElementById('m_overview').value = item.overview || "Tidak ada sinopsis dalam bahasa Indonesia.";
                            document.getElementById('m_release').value = item.release_date || item.first_air_date || '2026-06-04';
                            document.getElementById('m_rating').value = rating;
                            document.getElementById('m_poster').value = poster;
                            document.getElementById('m_backdrop').value = backdrop;
                            document.getElementById('m_is_series').checked = isTv;
                            toggle_series_episodes(isTv);

                            alert(`Info '${title}' berhasil diambil! Silakan tambahkan file link video streaming .mp4 di form sebelah kiri sebelum menyimpannya!`);
                            container.classList.add('hidden');
                            document.getElementById('movie_form').scrollIntoView({ behavior: 'smooth' });
                        };

                        div.innerHTML = `
                            <img src="${poster}" class="w-10 h-14 object-cover rounded shadow">
                            <div class="flex-1 min-w-0">
                                <p class="text-xs font-bold text-white truncate">${title} <span class="text-neutral-500 font-mono">(${year})</span></p>
                                <p class="text-[9px] text-neutral-400 truncate">${originalName}</p>
                                <span class="bg-neutral-800 text-yellow-500 text-[8px] font-black uppercase px-2 py-0.5 rounded-full"><i class="fas fa-star mr-0.5"></i> ${rating} &bull; ${isTv ? 'Serial TV' : 'Film'}</span>
                            </div>
                        `;
                        container.appendChild(div);
                    });
                })
                .catch(err => {
                    container.innerHTML = `<div class="text-xs text-red-400 p-4 text-center">Gagal memproses pencarian API. ${err}</div>`;
                });
        }
    </script>

</body>
</html>
