# فایل‌های آماده بک‌ند (PHP/MySQL)
تمام این فایل‌ها را در پوشه `public_html/appol/` قرار دهید.

## ۱. ساخت جداول (در phpMyAdmin اجرا کنید)
```sql
CREATE TABLE users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(50),
    isLoggedIn TINYINT(1) DEFAULT 0,
    balanceUsdt DOUBLE DEFAULT 0,
    totalRecharged DOUBLE DEFAULT 0,
    vipLevel INT DEFAULT 0,
    walletAddress VARCHAR(255) DEFAULT '',
    invitationCode VARCHAR(20),
    role VARCHAR(10) DEFAULT 'USER',
    profileImage VARCHAR(255) DEFAULT '',
    isGoogleVerified TINYINT(1) DEFAULT 0,
    lastIp VARCHAR(50) DEFAULT '',
    isIpSuspicious TINYINT(1) DEFAULT 0,
    registrationDate BIGINT
);

CREATE TABLE transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20),
    amount DOUBLE,
    status VARCHAR(20),
    network VARCHAR(20),
    address VARCHAR(255),
    timestamp BIGINT,
    username VARCHAR(50)
);

CREATE TABLE investments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    planTitle VARCHAR(100),
    amount DOUBLE,
    apr DOUBLE,
    durationDays INT,
    startDate BIGINT,
    endDate BIGINT,
    status VARCHAR(20),
    username VARCHAR(50)
);

CREATE TABLE chat_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    text TEXT,
    sender VARCHAR(50),
    timestamp BIGINT,
    username VARCHAR(50)
);

CREATE TABLE invitation_codes (
    code VARCHAR(20) PRIMARY KEY,
    createdBy VARCHAR(50),
    timestamp BIGINT
);
```

## ۲. db.php
```php
<?php
header('Content-Type: application/json; charset=utf-8');
$host = "localhost";
$dbname = "parir1_bank"; 
$user = "parir1_bank"; 
$pass = "A18803611@"; 

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    die(json_encode(["success" => false, "message" => "Connection failed"]));
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

## ۳. login.php
```php
<?php
require 'db.php';
$input = getJsonInput();
$user = $input['username'] ?? '';
$pass = $input['password'] ?? '';

$stmt = $pdo->prepare("SELECT * FROM users WHERE username = ? AND password = ?");
$stmt->execute([$user, $pass]);
$userData = $stmt->fetch(PDO::FETCH_ASSOC);

if ($userData) {
    $userData['isLoggedIn'] = (bool)$userData['isLoggedIn'];
    $userData['isGoogleVerified'] = (bool)$userData['isGoogleVerified'];
    $userData['isIpSuspicious'] = (bool)$userData['isIpSuspicious'];
    sendResponse(true, $userData);
} else {
    sendResponse(false, null, "Invalid credentials");
}
?>
```

## ۴. register.php
```php
<?php
require 'db.php';
$input = getJsonInput();
$user = $input['username'] ?? '';
$pass = $input['password'] ?? '';
$code = $input['inviteCode'] ?? '';

$stmt = $pdo->prepare("SELECT COUNT(*) FROM invitation_codes WHERE code = ?");
$stmt->execute([$code]);
$isSystemCode = $stmt->fetchColumn() > 0;

$stmt = $pdo->prepare("SELECT COUNT(*) FROM users WHERE invitationCode = ?");
$stmt->execute([$code]);
$isUserCode = $stmt->fetchColumn() > 0;

if (!$isSystemCode && !$isUserCode) sendResponse(false, null, "Invalid invite code");

$stmt = $pdo->prepare("SELECT COUNT(*) FROM users WHERE username = ?");
$stmt->execute([$user]);
if ($stmt->fetchColumn() > 0) sendResponse(false, null, "User exists");

$newCode = "ATOM" . rand(1000, 9999);
$stmt = $pdo->prepare("INSERT INTO users (username, password, invitationCode, registrationDate) VALUES (?, ?, ?, ?)");
if ($stmt->execute([$user, $pass, $newCode, round(microtime(true) * 1000)])) {
    $stmt = $pdo->prepare("SELECT * FROM users WHERE username = ?");
    $stmt->execute([$user]);
    sendResponse(true, $stmt->fetch(PDO::FETCH_ASSOC));
} else {
    sendResponse(false, null, "Failed");
}
?>
```

## ۵. users.php
```php
<?php
require 'db.php';
$stmt = $pdo->query("SELECT * FROM users");
sendResponse(true, $stmt->fetchAll(PDO::FETCH_ASSOC));
?>
```

## ۶. update_user.php
```php
<?php
require 'db.php';
$u = getJsonInput();
$stmt = $pdo->prepare("UPDATE users SET password=?, balanceUsdt=?, totalRecharged=?, vipLevel=?, walletAddress=?, invitationCode=?, role=?, profileImage=?, isGoogleVerified=?, lastIp=?, isIpSuspicious=? WHERE username=?");
$res = $stmt->execute([$u['password'], $u['balanceUsdt'], $u['totalRecharged'], $u['vipLevel'], $u['walletAddress'], $u['invitationCode'], $u['role'], $u['profileImage'], $u['isGoogleVerified']?1:0, $u['lastIp'], $u['isIpSuspicious']?1:0, $u['username']]);
sendResponse($res, $res);
?>
```

## ۷. transactions.php
```php
<?php
require 'db.php';
$username = $_GET['username'] ?? '';
$stmt = $pdo->prepare("SELECT * FROM transactions WHERE username = ? ORDER BY timestamp DESC");
$stmt->execute([$username]);
sendResponse(true, $stmt->fetchAll(PDO::FETCH_ASSOC));
?>
```

## ۸. add_transaction.php
```php
<?php
require 'db.php';
$t = getJsonInput();
$stmt = $pdo->prepare("INSERT INTO transactions (type, amount, status, network, address, timestamp, username) VALUES (?, ?, ?, ?, ?, ?, ?)");
$res = $stmt->execute([$t['type'], $t['amount'], $t['status'], $t['network'], $t['address'], $t['timestamp'], $t['username']]);
sendResponse($res, $res);
?>
```

## ۹. investments.php
```php
<?php
require 'db.php';
$username = $_GET['username'] ?? '';
$stmt = $pdo->prepare("SELECT * FROM investments WHERE username = ? ORDER BY startDate DESC");
$stmt->execute([$username]);
sendResponse(true, $stmt->fetchAll(PDO::FETCH_ASSOC));
?>
```

## ۱۰. add_investment.php
```php
<?php
require 'db.php';
$i = getJsonInput();
$stmt = $pdo->prepare("INSERT INTO investments (planTitle, amount, apr, durationDays, startDate, endDate, status, username) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
$res = $stmt->execute([$i['planTitle'], $i['amount'], $i['apr'], $i['durationDays'], $i['startDate'], $i['endDate'], $i['status'], $i['username']]);
sendResponse($res, $res);
?>
```

## ۱۱. messages.php
```php
<?php
require 'db.php';
$username = $_GET['username'] ?? '';
$stmt = $pdo->prepare("SELECT * FROM chat_messages WHERE username = ? ORDER BY timestamp ASC");
$stmt->execute([$username]);
sendResponse(true, $stmt->fetchAll(PDO::FETCH_ASSOC));
?>
```

## ۱۲. send_message.php
```php
<?php
require 'db.php';
$m = getJsonInput();
$stmt = $pdo->prepare("INSERT INTO chat_messages (text, sender, timestamp, username) VALUES (?, ?, ?, ?)");
$res = $stmt->execute([$m['text'], $m['sender'], $m['timestamp'], $m['username']]);
sendResponse($res, $res);
?>
```

## ۱۳. invite_codes.php
```php
<?php
require 'db.php';
$stmt = $pdo->query("SELECT * FROM invitation_codes ORDER BY timestamp DESC");
sendResponse(true, $stmt->fetchAll(PDO::FETCH_ASSOC));
?>
```

## ۱۴. add_invite_code.php
```php
<?php
require 'db.php';
$c = getJsonInput();
$stmt = $pdo->prepare("INSERT INTO invitation_codes (code, createdBy, timestamp) VALUES (?, ?, ?)");
$res = $stmt->execute([$c['code'], $c['createdBy'], $c['timestamp']]);
sendResponse($res, $res);
?>
```

## ۱۵. delete_invite_code.php
```php
<?php
require 'db.php';
$code = $_GET['code'] ?? '';
$stmt = $pdo->prepare("DELETE FROM invitation_codes WHERE code = ?");
$res = $stmt->execute([$code]);
sendResponse($res, $res);
?>
```
