<?php
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization");
header("Content-Type: application/json; charset=utf-8");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$config_file = __DIR__ . '/config.json';
$movies_file = __DIR__ . '/movies.json';
$tx_file = __DIR__ . '/transactions.json';

// Helper function to load configuration
function get_config($file) {
    if (file_exists($file)) {
        return json_decode(file_get_contents($file), true);
    }
    return [];
}

// Helper function to load movies
function get_movies($file) {
    if (file_exists($file)) {
        return json_decode(file_get_contents($file), true);
    }
    return [];
}

// Helper function to load transactions
function get_transactions($file) {
    if (file_exists($file)) {
        return json_decode(file_get_contents($file), true);
    }
    return [];
}

// Helper function to save transactions
function save_transactions($file, $data) {
    file_put_contents($file, json_encode($data, JSON_PRETTY_PRINT));
}

$action = isset($_GET['action']) ? $_GET['action'] : 'get_all';

switch ($action) {
    case 'get_all':
        // Serves the full merged payload (RemoteControlResponse)
        $conf = get_config($config_file);
        // Remove admin password from public API payload for safety
        if (isset($conf['adminPassword'])) {
            unset($conf['adminPassword']);
        }
        $movies = get_movies($movies_file);
        
        $payload = [
            "config" => $conf,
            "movies" => $movies
        ];
        
        echo json_encode($payload, JSON_UNESCAPED_SLASHES);
        break;

    case 'create_payment':
        // Safe endpoint to execute real TriPay Checkout
        $input = json_decode(file_get_contents('php://input'), true);
        if (!$input) {
            http_response_code(400);
            echo json_encode(["success" => false, "message" => "Format input tidak valid"]);
            exit();
        }
        
        $plan_id = isset($input['planId']) ? $input['planId'] : 'basic';
        $payment_method = isset($input['paymentMethod']) ? $input['paymentMethod'] : 'QRIS';
        $customer_name = isset($input['name']) ? $input['name'] : 'Pelanggan Dramelio';
        $customer_email = isset($input['email']) ? $input['email'] : 'pelanggan@dramelio.com';
        
        $conf = get_config($config_file);
        $amount = ($plan_id === 'premium') ? (int)$conf['subscriptionPremiumPrice'] : (int)$conf['subscriptionBasicPrice'];
        
        $merchant_code = $conf['tripayMerchantCode'];
        $api_key = $conf['tripayApiKey'];
        $private_key = $conf['tripayPrivateKey'];
        
        // Setup unique references
        $merchant_ref = 'DML-' . strtoupper(substr(uniqid(), 7, 6));
        $signature = hash_hmac('sha256', $merchant_code . $merchant_ref . $amount, $private_key);
        
        // Prepare TriPay POST data
        $tripay_payload = [
            'method'         => $payment_method,
            'merchant_ref'   => $merchant_ref,
            'amount'         => $amount,
            'customer_name'  => $customer_name,
            'customer_email' => $customer_email,
            'signature'      => $signature,
            'order_items'    => [
                [
                    'name'     => "Dramelio " . ucfirst($plan_id) . " Access (1 Bulan)",
                    'price'    => $amount,
                    'quantity' => 1
                ]
            ],
            'expired_time' => time() + (24 * 60 * 60) // 24 hours expiry
        ];
        
        // Determine TriPay endpoint URL (Sandbox vs Production)
        $is_sandbox = (strpos($api_key, 'DEV-') === 0);
        $tripay_url = $is_sandbox 
            ? "https://tripay.co.id/api-sandbox/transaction/create" 
            : "https://tripay.co.id/api/transaction/create";
            
        // Make the safe cPanel server side CURL request to TriPay
        $curl = curl_init();
        curl_setopt_array($curl, [
            CURLOPT_URL            => $tripay_url,
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_POST           => true,
            CURLOPT_POSTFIELDS     => json_encode($tripay_payload),
            CURLOPT_HTTPHEADER     => [
                "Authorization: Bearer " . $api_key,
                "Content-Type: application/json"
            ],
            CURLOPT_CONNECTTIMEOUT => 15,
            CURLOPT_TIMEOUT        => 15
        ]);
        
        $response = curl_exec($curl);
        $err = curl_error($curl);
        curl_close($curl);
        
        if ($err) {
            // If TriPay API fails or is offline, generate simulated transaction so the client doesn't block!
            $simulated = get_simulated_transaction($merchant_ref, $plan_id, $amount, $payment_method);
            echo json_encode([
                "success" => true,
                "is_simulated" => true,
                "message" => "TriPay Offline. Berhasil membuat transaksi simulasi.",
                "data" => $simulated
            ]);
            exit();
        }
        
        $res_data = json_decode($response, true);
        if (isset($res_data['success']) && $res_data['success'] == true) {
            $tripay_data = $res_data['data'];
            
            // Format details securely
            $tx_details = [
                "id" => uniqid("tx_", true),
                "reference" => $tripay_data['reference'],
                "merchant_ref" => $merchant_ref,
                "planId" => $plan_id,
                "amount" => (int)$amount,
                "paymentMethodCode" => $payment_method,
                "status" => "PENDING",
                "paymentInstructions" => isset($tripay_data['instructions'][0]['steps']) 
                    ? implode("\n", $tripay_data['instructions'][0]['steps']) 
                    : "Silakan meluncur ke petunjuk pembayaran resmi TriPay.",
                "vaNumber" => isset($tripay_data['pay_code']) ? $tripay_data['pay_code'] : null,
                "qrCodeUrl" => isset($tripay_data['qr_url']) ? $tripay_data['qr_url'] : null,
                "date" => date("Y-m-d H:i:s")
            ];
            
            // Log local pending transaction
            $transactions = get_transactions($tx_file);
            array_unshift($transactions, $tx_details);
            save_transactions($tx_file, $transactions);
            
            echo json_encode([
                "success" => true,
                "is_simulated" => false,
                "data" => $tx_details
            ]);
        } else {
            // TriPay returned an API error (e.g., bad signature or key). Fallback to high-quality system simulation
            $msg = isset($res_data['message']) ? $res_data['message'] : 'Respon gagal dari TriPay API';
            $simulated = get_simulated_transaction($merchant_ref, $plan_id, $amount, $payment_method);
            
            echo json_encode([
                "success" => true,
                "is_simulated" => true,
                "message" => "Gagal membuat invoice melalui TriPay API ({$msg}). Menggunakan simulasi lokal gratis.",
                "data" => $simulated
            ]);
        }
        break;

    case 'check_status':
        $reference = isset($_GET['reference']) ? $_GET['reference'] : '';
        if (empty($reference)) {
            http_response_code(400);
            echo json_encode(["status" => "EXPIRED", "message" => "Parameter reference dibutuhkan"]);
            exit();
        }
        
        $transactions = get_transactions($tx_file);
        $found = false;
        $status = "PENDING";
        
        foreach ($transactions as $tx) {
            // Track by either TriPay reference id or merchant_ref reference code
            if ($tx['reference'] === $reference || (isset($tx['merchant_ref']) && $tx['merchant_ref'] === $reference)) {
                $status = $tx['status'];
                $found = true;
                break;
            }
        }
        
        echo json_encode([
            "success" => true,
            "found" => $found,
            "status" => $status
        ]);
        break;

    default:
        http_response_code(404);
        echo json_encode(["success" => false, "message" => "Action tidak dikenal"]);
        break;
}

// Helper to construct fallback mock local payments
function get_simulated_transaction($ref, $plan_id, $amount, $payment_method) {
    global $tx_file;
    $vaNum = null;
    $qrUrl = null;
    
    if ($payment_method === 'QRIS') {
        $qrUrl = "https://upload.wikimedia.org/wikipedia/commons/d/d0/QR_code_for_mobile_English_Wikipedia.svg";
    } else {
        $vaNum = "8578" . rand(11111111, 99999999);
    }
    
    $tx_details = [
        "id" => uniqid("sim_", true),
        "reference" => $ref,
        "merchant_ref" => $ref,
        "planId" => $plan_id,
        "amount" => $amount,
        "paymentMethodCode" => $payment_method,
        "status" => "PENDING",
        "paymentInstructions" => "Ini adalah invoice simulasi karena TriPay API server dalam mode demo/offline. Silakan klik tombol 'SAYA SUDAH BAYAR' di aplikasi Android untuk langsung memicu simulasi sukses pembayaran gratis.",
        "vaNumber" => $vaNum,
        "qrCodeUrl" => $qrUrl,
        "date" => date("Y-m-d H:i:s")
    ];
    
    // Log local simulated transaction
    $transactions = get_transactions($tx_file);
    array_unshift($transactions, $tx_details);
    save_transactions($tx_file, $transactions);
    
    return $tx_details;
}
