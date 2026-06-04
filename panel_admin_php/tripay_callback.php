<?php
header("Content-Type: application/json; charset=utf-8");

$config_file = __DIR__ . '/config.json';
$tx_file = __DIR__ . '/transactions.json';

// Fetch raw POST data sent by TriPay
$json = file_get_contents('php://input');
if (!$json) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Request kosong"]);
    exit();
}

// Retrieve callback headers
$signature_header = isset($_SERVER['HTTP_X_CALLBACK_SIGNATURE']) ? $_SERVER['HTTP_X_CALLBACK_SIGNATURE'] : '';
$event_header = isset($_SERVER['HTTP_X_CALLBACK_EVENT']) ? $_SERVER['HTTP_X_CALLBACK_EVENT'] : '';

if (empty($signature_header)) {
    http_response_code(401);
    echo json_encode(["success" => false, "message" => "Signature header tidak ditemukan"]);
    exit();
}

// Load configurations to fetch TriPay Private Key
if (!file_exists($config_file)) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Konfigurasi server rusak"]);
    exit();
}
$conf = json_decode(file_get_contents($config_file), true);
$private_key = isset($conf['tripayPrivateKey']) ? $conf['tripayPrivateKey'] : '';

if (empty($private_key)) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Private key belum terkonfigurasi di Admin Panel"]);
    exit();
}

// Validate signature key to verify the source is indeed TriPay
$calculated_signature = hash_hmac('sha256', $json, $private_key);

if ($signature_header !== $calculated_signature) {
    http_response_code(403);
    echo json_encode(["success" => false, "message" => "Validasi signature gagal! Akses Ditolak."]);
    exit();
}

// Validation passed, parse payload details
$data = json_decode($json, true);
$status = isset($data['status']) ? strtoupper($data['status']) : '';
$reference = isset($data['reference']) ? $data['reference'] : '';
$merchant_ref = isset($data['merchant_ref']) ? $data['merchant_ref'] : '';

// Update transaction status locally
if (file_exists($tx_file)) {
    $transactions = json_decode(file_get_contents($tx_file), true);
    if (!is_array($transactions)) {
        $transactions = [];
    }
    
    $updated = false;
    foreach ($transactions as &$tx) {
        if ($tx['reference'] === $reference || (isset($tx['merchant_ref']) && $tx['merchant_ref'] === $merchant_ref)) {
            $tx['status'] = $status; // PAID, EXPIRED, etc.
            $updated = true;
            break;
        }
    }
    
    if ($updated) {
        file_put_contents($tx_file, json_encode($transactions, JSON_PRETTY_PRINT));
        echo json_encode(["success" => true, "message" => "Status pembayaran berhasil disimpan: " . $status]);
    } else {
        // Fallback: log new transaction if callback arrives for unknown transaction tracking
        $new_tx = [
            "id" => uniqid("cb_", true),
            "reference" => $reference,
            "merchant_ref" => $merchant_ref,
            "planId" => ($data['total_amount'] > 30000) ? "premium" : "basic",
            "amount" => $data['total_amount'],
            "paymentMethodCode" => $data['payment_method'],
            "status" => $status,
            "paymentInstructions" => "Status update received via callback.",
            "date" => date("Y-m-d H:i:s")
        ];
        array_unshift($transactions, $new_tx);
        file_put_contents($tx_file, json_encode($transactions, JSON_PRETTY_PRINT));
        
        echo json_encode(["success" => true, "message" => "Status baru dibuat dari callback: " . $status]);
    }
} else {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "File transaksi tidak ditemukan"]);
}
