# راهنمای کامل راه‌اندازی بک‌ند (PHP/MySQL)

تمام فایل‌های زیر را در پوشه `api` روی هاست خود قرار دهید.

## ۱. db.php (بسیار مهم: اطلاعات هاست خود را اینجا ست کنید)
```php
<?php
header('Content-Type: application/json; charset=utf-8');
$host = "localhost";
$dbname = "YOUR_DATABASE_NAME";
$user = "YOUR_DATABASE_USER";
$pass = "YOUR_DATABASE_PASS";

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    die(json_encode(["success" => false, "message" => "Connection failed: " . $e->getMessage()]));
}

function sendResponse($success, $data = null, $message = null) {
    echo json_encode(["success" => $success, "data" => $data, "message" => $message]);
    exit;
}

function getJsonInput() {
    return json_decode(file_get_contents('php://input'), true);
}
?>
```

## ۲. login.php
```php
<?php
require 'db.php';
$input = getJsonInput();
$user = $input['username'];
$pass = $input['password'];

$stmt = $pdo->prepare("SELECT * FROM users WHERE username = ? AND password = ?");
$stmt->execute([$user, $pass]);
$userData = $stmt->fetch(PDO::FETCH_ASSOC);

if ($userData) {
    // تبدیل مقادیر BOOLEAN از دیتابیس (0/1) به true/false برای اندروید
    $userData['isLoggedIn'] = (bool)$userData['isLoggedIn'];
    $userData['isGoogleVerified'] = (bool)$userData['isGoogleVerified'];
    $userData['isIpSuspicious'] = (bool)$userData['isIpSuspicious'];
    sendResponse(true, $userData);
} else {
    sendResponse(false, null, "Invalid username or password");
}
?>
```

## ۳. register.php
```php
<?php
require 'db.php';
$input = getJsonInput();
$user = $input['username'];
$pass = $input['password'];
$code = $input['inviteCode'];

// چک کردن کد دعوت
$stmt = $pdo->prepare("SELECT COUNT(*) FROM invitation_codes WHERE code = ?");
$stmt->execute([$code]);
$isSystemCode = $stmt->fetchColumn() > 0;

$stmt = $pdo->prepare("SELECT COUNT(*) FROM users WHERE invitationCode = ?");
$stmt->execute([$code]);
$isUserCode = $stmt->fetchColumn() > 0;

if (!$isSystemCode && !$isUserCode) {
    sendResponse(false, null, "Invalid invitation code");
}

// چک کردن یوزر تکراری
$stmt = $pdo->prepare("SELECT COUNT(*) FROM users WHERE username = ?");
$stmt->execute([$user]);
if ($stmt->fetchColumn() > 0) {
    sendResponse(false, null, "Username already exists");
}

$newInvite = "ATOM" . rand(1000, 9999);
$stmt = $pdo->prepare("INSERT INTO users (username, password, invitationCode, registrationDate) VALUES (?, ?, ?, ?)");
if ($stmt->execute([$user, $pass, $newInvite, round(microtime(true) * 1000)])) {
    $stmt = $pdo->prepare("SELECT * FROM users WHERE username = ?");
    $stmt->execute([$user]);
    sendResponse(true, $stmt->fetch(PDO::FETCH_ASSOC));
} else {
    sendResponse(false, null, "Registration failed");
}
?>
```

## ۴. update_user.php
```php
<?php
require 'db.php';
$u = getJsonInput();

$stmt = $pdo->prepare("UPDATE users SET 
    password = ?, balanceUsdt = ?, totalRecharged = ?, vipLevel = ?, 
    walletAddress = ?, invitationCode = ?, role = ?, profileImage = ?, 
    isGoogleVerified = ?, lastIp = ?, isIpSuspicious = ? 
    WHERE username = ?");

$res = $stmt->execute([
    $u['password'], $u['balanceUsdt'], $u['totalRecharged'], $u['vipLevel'],
    $u['walletAddress'], $u['invitationCode'], $u['role'], $u['profileImage'],
    $u['isGoogleVerified'] ? 1 : 0, $u['lastIp'], $u['isIpSuspicious'] ? 1 : 0,
    $u['username']
]);

sendResponse($res, $res);
?>
```

## ۵. transactions.php
```php
<?php
require 'db.php';
$username = $_GET['username'] ?? '';
$stmt = $pdo->prepare("SELECT * FROM transactions WHERE username = ? ORDER BY timestamp DESC");
$stmt->execute([$username]);
sendResponse(true, $stmt->fetchAll(PDO::FETCH_ASSOC));
?>
```

## ۶. add_transaction.php
```php
<?php
require 'db.php';
$t = getJsonInput();
$stmt = $pdo->prepare("INSERT INTO transactions (type, amount, status, network, address, timestamp, username) VALUES (?, ?, ?, ?, ?, ?, ?)");
$res = $stmt->execute([
    $t['type'], $t['amount'], $t['status'], $t['network'], $t['address'], $t['timestamp'], $t['username']
]);
sendResponse($res, $res);
?>
```

## ۷. investments.php
```php
<?php
require 'db.php';
$username = $_GET['username'] ?? '';
$stmt = $pdo->prepare("SELECT * FROM investments WHERE username = ? ORDER BY startDate DESC");
$stmt->execute([$username]);
sendResponse(true, $stmt->fetchAll(PDO::FETCH_ASSOC));
?>
```

## ۸. add_investment.php
```php
<?php
require 'db.php';
$i = getJsonInput();
$stmt = $pdo->prepare("INSERT INTO investments (planTitle, amount, apr, durationDays, startDate, endDate, status, username) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
$res = $stmt->execute([
    $i['planTitle'], $i['amount'], $i['apr'], $i['durationDays'], $i['startDate'], $i['endDate'], $i['status'], $i['username']
]);
sendResponse($res, $res);
?>
```

## ۹. messages.php
```php
<?php
require 'db.php';
$username = $_GET['username'] ?? '';
$stmt = $pdo->prepare("SELECT * FROM chat_messages WHERE username = ? ORDER BY timestamp ASC");
$stmt->execute([$username]);
sendResponse(true, $stmt->fetchAll(PDO::FETCH_ASSOC));
?>
```

## ۱۰. send_message.php
```php
<?php
require 'db.php';
$m = getJsonInput();
$stmt = $pdo->prepare("INSERT INTO chat_messages (text, sender, timestamp, username) VALUES (?, ?, ?, ?)");
$res = $stmt->execute([$m['text'], $m['sender'], $m['timestamp'], $m['username']]);
sendResponse($res, $res);
?>
```

## ۱۱. invite_codes.php
```php
<?php
require 'db.php';
$stmt = $pdo->query("SELECT * FROM invitation_codes ORDER BY timestamp DESC");
sendResponse(true, $stmt->fetchAll(PDO::FETCH_ASSOC));
?>
```

## ۱۲. add_invite_code.php
```php
<?php
require 'db.php';
$c = getJsonInput();
$stmt = $pdo->prepare("INSERT INTO invitation_codes (code, createdBy, timestamp) VALUES (?, ?, ?)");
$res = $stmt->execute([$c['code'], $c['createdBy'], $c['timestamp']]);
sendResponse($res, $res);
?>
```

## ۱۳. delete_invite_code.php
```php
<?php
require 'db.php';
$code = $_GET['code'] ?? '';
$stmt = $pdo->prepare("DELETE FROM invitation_codes WHERE code = ?");
$res = $stmt->execute([$code]);
sendResponse($res, $res);
?>
```

---
**نکته نهایی:** پس از آپلود، حتماً در فایل `app/src/main/java/com/example/data/network/NetworkModule.kt` آدرس سایت خود را جایگزین کنید:
`private const val BASE_URL = "https://your-domain.com/api/";`
