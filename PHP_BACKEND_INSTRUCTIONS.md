# راهنمای راه‌اندازی دیتابیس آنلاین (PHP & MySQL)

برای اینکه برنامه شما آنلاین شود، باید این اسکریپت‌ها را روی هاست خود آپلود کنید.

## ۱. ساخت دیتابیس
ابتدا در پنل هاست خود (Cpanel یا DirectAdmin) یک دیتابیس MySQL بسازید و جداول زیر را در آن ایجاد کنید:

```sql
CREATE TABLE users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(50),
    isLoggedIn BOOLEAN DEFAULT FALSE,
    balanceUsdt DOUBLE DEFAULT 0,
    totalRecharged DOUBLE DEFAULT 0,
    vipLevel INT DEFAULT 0,
    walletAddress VARCHAR(255),
    invitationCode VARCHAR(20),
    role VARCHAR(10) DEFAULT 'USER'
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

CREATE TABLE invitation_codes (
    code VARCHAR(20) PRIMARY KEY,
    createdBy VARCHAR(50),
    timestamp BIGINT
);
```

## ۲. فایل اتصال به دیتابیس (db.php)
این کد را در فایلی به نام `db.php` قرار دهید و اطلاعات هاست خود را وارد کنید:

```php
<?php
$host = "localhost";
$dbname = "YOUR_DB_NAME";
$user = "YOUR_DB_USER";
$pass = "YOUR_DB_PASS";

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch (PDOException $e) {
    die("Connection failed: " . $e->getMessage());
}

function sendResponse($success, $data = null, $message = null) {
    echo json_encode([
        "success" => $success,
        "data" => $data,
        "message" => $message
    ]);
    exit;
}
?>
```

## ۳. فایل لاگین (login.php)
```php
<?php
require 'db.php';
$input = json_decode(file_get_contents('php://input'), true);
$user = $input['username'];
$pass = $input['password'];

$stmt = $pdo->prepare("SELECT * FROM users WHERE username = ? AND password = ?");
$stmt->execute([$user, $pass]);
$userData = $stmt->fetch(PDO::FETCH_ASSOC);

if ($userData) {
    sendResponse(true, $userData);
} else {
    sendResponse(false, null, "نام کاربری یا رمز عبور اشتباه است.");
}
?>
```

## ۴. فایل ثبت‌نام (register.php)
```php
<?php
require 'db.php';
$input = json_decode(file_get_contents('php://input'), true);
$user = $input['username'];
$pass = $input['password'];
$code = $input['inviteCode'];

// بررسی تکراری نبودن یوزر
$stmt = $pdo->prepare("SELECT COUNT(*) FROM users WHERE username = ?");
$stmt->execute([$user]);
if ($stmt->fetchColumn() > 0) {
    sendResponse(false, null, "این نام کاربری قبلاً ثبت شده است.");
}

// ثبت کاربر جدید
$stmt = $pdo->prepare("INSERT INTO users (username, password, invitationCode, balanceUsdt, role) VALUES (?, ?, ?, 0, 'USER')");
if ($stmt->execute([$user, $pass, "ATOM".rand(1000,9999)])) {
    $stmt = $pdo->prepare("SELECT * FROM users WHERE username = ?");
    $stmt->execute([$user]);
    sendResponse(true, $stmt->fetch(PDO::FETCH_ASSOC));
} else {
    sendResponse(false, null, "خطا در ثبت‌نام.");
}
?>
```

---
**نکته مهم:** پس از آپلود این فایل‌ها، آدرس هاست خود را در فایل `NetworkModule.kt` در اندروید استودیو جایگزین کنید:
`private const val BASE_URL = "https://your-domain.com/api/";`
