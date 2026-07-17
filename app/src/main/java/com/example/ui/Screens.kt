package com.example.ui

import android.widget.Toast
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.R
import com.example.data.database.InvestmentEntity
import com.example.data.database.TransactionEntity
import com.example.data.database.UserEntity
import com.example.data.repository.CoinPair
import com.example.data.repository.WithdrawResult
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

// Sealed Navigation Routes
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Investment : Screen("investment")
    object Transfer : Screen("transfer")
    object My : Screen("my")
    object Recharge : Screen("recharge")
    object Withdraw : Screen("withdraw")
    object Vip : Screen("vip")
    object About : Screen("about")
    object Support : Screen("support")
    object SyncSettings : Screen("sync_settings")
}

object Loc {
    private val fa = mapOf(
        "home" to "خانه",
        "investment" to "سرمایه‌گذاری",
        "transfer" to "انتقال",
        "profile" to "پروفایل",
        "register_title" to "ورود به سامانه سرمایه‌گذاری اتم",
        "username" to "نام کاربری",
        "password" to "رمز عبور",
        "confirm_password" to "تکرار رمز عبور",
        "invite_code" to "کد دعوت (اختیاری)",
        "invite_code_required_for_bonus" to "توجه: هدیه ۳۵,۰۰۰,۰۰۰ دلاری با وارد کردن کد دعوت فعال می‌شود.",
        "register_btn" to "ورود به حساب کاربری",
        "register_success" to "ورود با موفقیت انجام شد!",
        "passwords_dont_match" to "رمز عبور و تکرار آن یکسان نیستند.",
        "username_empty" to "لطفاً نام کاربری را وارد کنید.",
        "logout" to "خروج از حساب",
        "withdraw_limit_error" to "خطا: حداکثر میزان برداشت روزانه ۱,۰۰۰,۰۰۰ دلار (USDT) است.",
        "withdraw_verification_error" to "جهت اثبات هویت و برداشت موجودی، به دلیل واگذاری حساب شما باید حداقل ۱۷,۵۰۰,۰۰۰ دلار (USDT) (معادل نصف بونوس دریافتی) حساب خود را شارژ کنید. مجموع شارژ فعلی شما: %,.2f دلار است.",
        "withdraw_insufficient" to "موجودی کافی نیست.",
        "withdraw_success" to "درخواست برداشت با موفقیت ثبت شد و در حال پردازش است.",
        "withdraw_title" to "برداشت وجه",
        "recharge_title" to "شارژ حساب",
        "daily_limit_label" to "حد برداشت روزانه: ۱,۰۰۰,۰۰۰ USDT",
        "verification_status" to "وضعیت اثبات هویت (شارژ ۱۷,۵۰۰,۰۰۰ USDT):",
        "verified" to "تایید شده ✅",
        "not_verified" to "تایید نشده (نیاز به شارژ بیشتر) ❌",
        "total_recharged_label" to "مجموع شارژ شما تا کنون: %,.2f USDT",
        "balance" to "موجودی",
        "recharge_btn" to "شارژ حساب",
        "withdraw_btn" to "برداشت",
        "bonus_received_alert" to "تبریک! هدیه ۳۵,۰۰۰,۰۰۰ دلاری فعال‌سازی به حساب شما واریز شد.",
        "enter_amount" to "مبلغ را وارد کنید (USDT)",
        "enter_address" to "آدرس TRC20 مقصد را وارد کنید",
        "invite_code_label" to "کد دعوت شما",
        "active_bonus_label" to "طرح هدیه فعال شد",
        "vip_badge" to "سطح VIP",
        "total_balance_usdt" to "کل دارایی کیف پول (USDT)",
        "quick_access" to "دسترسی سریع",
        "my_investments_list" to "لیست دارایی‌های من",
        "active" to "فعال",
        "nft_collection" to "NFT کلکسیون",
        "investment_page" to "صفحه سرمایه‌گذاری",
        "security_settings" to "تنظیمات امنیتی و رمز عبور",
        "about_rules" to "درباره ما و قوانین پلتفرم",
        "withdraw_success_title" to "درخواست برداشت ثبت شد!",
        "withdraw_success_desc" to "درخواست شما دریافت شد. به دلیل تغییر مالکیت و مسائل امنیتی حساب، مبلغ برداشت پس از ۱۵ روز به کیف پول شما واریز خواهد شد. لطفاً از تماس با پشتیبانی در این رابطه جداً خودداری کنید.",
        "close" to "بستن",
        "withdraw_delay_warning" to "توجه مهم: به دلیل تغییر مالکیت و مسائل امنیتی حساب، کلیه برداشت‌ها پس از تایید و واریز سپرده فعال‌سازی، با تاخیر ۱۵ روزه به کیف پول شما واریز خواهد شد. لطفاً از تماس با پشتیبانی در این زمینه جداً خودداری فرمایید.",
        "platform_title" to "سامانه سرمایه‌گذاری هوشمند اتم",
        "platform_desc" to "اکوسیستم مالی دیجیتال اتم (Atom)"
    )

    private val en = mapOf(
        "home" to "Home",
        "investment" to "Investment",
        "transfer" to "Transfer",
        "profile" to "Profile",
        "register_title" to "Login to Atom Investment Platform",
        "username" to "Username",
        "password" to "Password",
        "confirm_password" to "Confirm Password",
        "invite_code" to "Invitation Code (Optional)",
        "invite_code_required_for_bonus" to "Note: $35,000,000 bonus is activated with an invitation code.",
        "register_btn" to "Login to Account",
        "register_success" to "Login successful!",
        "passwords_dont_match" to "Passwords do not match.",
        "username_empty" to "Please enter username.",
        "logout" to "Log Out",
        "withdraw_limit_error" to "Error: Maximum daily withdrawal limit is $1,000,000 USDT.",
        "withdraw_verification_error" to "To prove identity and withdraw, due to account ownership transfer, you must recharge a cumulative minimum of 17,500,000 USDT (half of the received bonus). Current recharge: %,.2f USDT.",
        "withdraw_insufficient" to "Insufficient balance.",
        "withdraw_success" to "Withdrawal request submitted successfully and is processing.",
        "withdraw_title" to "Withdraw Funds",
        "recharge_title" to "Recharge Account",
        "daily_limit_label" to "Daily limit: 1,000,000 USDT",
        "verification_status" to "Identity Verification (Recharge 17,500,000 USDT):",
        "verified" to "Verified ✅",
        "not_verified" to "Unverified (Requires more recharge) ❌",
        "total_recharged_label" to "Your total recharge so far: %,.2f USDT",
        "balance" to "Balance",
        "recharge_btn" to "Recharge",
        "withdraw_btn" to "Withdraw",
        "bonus_received_alert" to "Congratulations! The $35,000,000 activation bonus has been credited to your account.",
        "enter_amount" to "Enter amount (USDT)",
        "enter_address" to "Enter destination TRC20 address",
        "invite_code_label" to "Your Invite Code",
        "active_bonus_label" to "Gift Bonus Activated",
        "vip_badge" to "VIP Level",
        "total_balance_usdt" to "Total Wallet Balance (USDT)",
        "quick_access" to "Quick Access",
        "my_investments_list" to "My Guaranteed Investments",
        "active" to "Active",
        "nft_collection" to "NFT Vault Collection",
        "investment_page" to "Investment Catalog",
        "security_settings" to "Security & Password Settings",
        "about_rules" to "About Us & Platform Rules",
        "withdraw_success_title" to "Withdrawal Request Submitted!",
        "withdraw_success_desc" to "Your request has been received. Due to account ownership transfer and security procedures, your withdrawal will be credited to your wallet after 15 days. Please strictly avoid contacting support regarding this matter.",
        "close" to "Close",
        "withdraw_delay_warning" to "Important Notice: Due to account ownership transfer and security procedures, all withdrawals will be credited to your wallet with a 15-day delay after verification. Please strictly avoid contacting support regarding this matter.",
        "platform_title" to "Atom Smart Investment Platform",
        "platform_desc" to "Atom Digital Financial Ecosystem"
    )

    private val ar = mapOf(
        "home" to "الرئيسية",
        "investment" to "الاستثمار",
        "transfer" to "تحويل",
        "profile" to "الملف الشخصي",
        "register_title" to "تسجيل الدخول إلى منصة آتوم للاستثمار",
        "username" to "اسم المستخدم",
        "password" to "كلمة المرور",
        "confirm_password" to "تأكيد كلمة المرور",
        "invite_code" to "رمز الدعوة (اختياري)",
        "invite_code_required_for_bonus" to "ملاحظة: يتم تفعيل مكافأة بقيمة 35,000,000 دولار باستخدام رمز الدعوة.",
        "register_btn" to "تسجيل الدخول",
        "register_success" to "تم تسجيل الدخول بنجاح!",
        "passwords_dont_match" to "كلمتا المرور غير متطابقتين.",
        "username_empty" to "الرجاء إدخال اسم المستخدم.",
        "logout" to "تسجيل الخروج",
        "withdraw_limit_error" to "خطأ: الحد الأقصى للسحب اليومي هو 1,000,000 USDT.",
        "withdraw_verification_error" to "لإثبات الهوية والسحب، وبسبب نقل ملكية الحساب، يجب عليك شحن حسابك بمبلغ تراكمي لا يقل عن 17,500,000 USDT (نصف المكافأة المستلمة). رصيد الشحن الحالي: %,.2f USDT.",
        "withdraw_insufficient" to "الرصيد غير كافٍ.",
        "withdraw_success" to "تم تقديم طلب السحب بنجاح وهو قيد المعالجة.",
        "withdraw_title" to "سحب الأموال",
        "recharge_title" to "شحن الرصيد",
        "daily_limit_label" to "الحد اليومي: 1,000,000 USDT",
        "verification_status" to "إثبات الهوية (شحن 17,500,000 USDT):",
        "verified" to "تم التحقق ✅",
        "not_verified" to "غير متحقق (يتطلب المزيد من الشحن) ❌",
        "total_recharged_label" to "إجمالي الشحن الخاص بك حتى الآن: %,.2f USDT",
        "balance" to "الرصيد",
        "recharge_btn" to "شحن حساب",
        "withdraw_btn" to "سحب",
        "bonus_received_alert" to "تهانينا! لقد تم إيداع مكافأة التنشيط البالغة 35,000,000 دولار في حسابك.",
        "enter_amount" to "أدخل المبلغ (USDT)",
        "enter_address" to "أدخل عنوان TRC20 للمستلم",
        "invite_code_label" to "رمز الدعوة الخاص بك",
        "active_bonus_label" to "مكافأة الهدية نشطة",
        "vip_badge" to "مستوى VIP",
        "total_balance_usdt" to "إجمالي رصيد المحفظة (USDT)",
        "quick_access" to "وصول سريع",
        "my_investments_list" to "قائمة استثماراتي المضمونة",
        "active" to "نشط",
        "nft_collection" to "مجموعة خزنة NFT",
        "investment_page" to "كتالوج الاستثمار",
        "security_settings" to "إعدادات الأمان وكلمة المرور",
        "about_rules" to "معلومات عنا وقوانين المنصة",
        "withdraw_success_title" to "تم تقديم طلب السحب بنجاح!",
        "withdraw_success_desc" to "تم استلام طلبك. نظرًا لنقل ملكية الحساب والإجراءات الأمنية، سيتم إيداع مبلغ السحب في محفظتك بعد 15 يومًا. يرجى الامتناع تمامًا عن الاتصال بالدعم الفني بخصوص هذا الأمر.",
        "close" to "إغلاق",
        "withdraw_delay_warning" to "تنبيه هام: نظرًا لنقل ملكية الحساب والإجراءات الأمنية، سيتم إيداع جميع عمليات السحب في محفظتك بتأخير قدره 15 يومًا بعد التحقق. يرجى الامتناع تمامًا عن الاتصال بالدعم بخصوص هذا الشأن.",
        "platform_title" to "منصة آتوم للاستثمار الذكي",
        "platform_desc" to "نظام آتوم المالي الرقمي"
    )

    private val de = mapOf(
        "home" to "Startseite",
        "investment" to "Investition",
        "transfer" to "Überweisen",
        "profile" to "Profil",
        "register_title" to "Anmeldung bei der Atom-Investmentplattform",
        "username" to "Benutzername",
        "password" to "Passwort",
        "confirm_password" to "Passwort bestätigen",
        "invite_code" to "Einladungscode (Optional)",
        "invite_code_required_for_bonus" to "Hinweis: Der 35.000.000 $-Bonus wird mit einem Einladungscode aktiviert.",
        "register_btn" to "Anmelden",
        "register_success" to "Erfolgreich angemeldet!",
        "passwords_dont_match" to "Passwörter stimmen nicht überein.",
        "username_empty" to "Bitte Benutzernamen eingeben.",
        "logout" to "Abmelden",
        "withdraw_limit_error" to "Fehler: Das maximale tägliche Auszahlungslimit beträgt 1.000.000 USDT.",
        "withdraw_verification_error" to "Zur Identitätsprüfung und Auszahlung müssen Sie aufgrund der Kontoübertragung mindestens die Hälfte des erhaltenen Bonus, also 17.500.000 USDT, einzahlen. Ihre aktuelle Gesamtaufladung: %,.2f USDT.",
        "withdraw_insufficient" to "Ungenügendes Guthaben.",
        "withdraw_success" to "Auszahlungsantrag erfolgreich eingereicht und wird bearbeitet.",
        "withdraw_title" to "Auszahlung",
        "recharge_title" to "Konto aufladen",
        "daily_limit_label" to "Tägliches Limit: 1.000.000 USDT",
        "verification_status" to "Identitätsprüfung (17.500.000 USDT aufladen):",
        "verified" to "Verifiziert ✅",
        "not_verified" to "Nicht verifiziert (Erfordert weitere Aufladung) ❌",
        "total_recharged_label" to "Ihre bisherige Gesamtaufladung: %,.2f USDT",
        "balance" to "Guthaben",
        "recharge_btn" to "Aufladen",
        "withdraw_btn" to "Auszahlen",
        "bonus_received_alert" to "Herzlichen Glückwunsch! Der Aktivierungsbonus von 35.000.000 $ wurde Ihrem Konto gutgeschrieben.",
        "enter_amount" to "Betrag eingeben (USDT)",
        "enter_address" to "Geben Sie die TRC20-Zieladresse ein",
        "invite_code_label" to "Ihr Einladungscode",
        "active_bonus_label" to "Geschenkbonus aktiviert",
        "vip_badge" to "VIP-Stufe",
        "total_balance_usdt" to "Gesamtes Brieftaschenguthaben (USDT)",
        "quick_access" to "Schnellzugriff",
        "my_investments_list" to "Meine garantierten Anlagen",
        "active" to "Aktiv",
        "nft_collection" to "NFT-Tresor-Sammlung",
        "investment_page" to "Anlagekatalog",
        "security_settings" to "Sicherheits- & Passworteinstellungen",
        "about_rules" to "Über uns & Plattformregeln",
        "withdraw_success_title" to "Auszahlungsantrag eingereicht!",
        "withdraw_success_desc" to "Ihr Antrag wurde empfangen. Aufgrund der Kontenübertragung und Sicherheitsverfahren wird der Auszahlungsbetrag nach 15 Tagen auf Ihre Brieftasche überwiesen. Bitte sehen Sie unbedingt davon ab, den Support diesbezüglich zu kontaktieren.",
        "close" to "Schließen",
        "withdraw_delay_warning" to "Wichtiger Hinweis: Aufgrund der Kontenübertragung und Sicherheitsverfahren werden alle Auszahlungen nach der Verifizierung mit einer 15-tägigen Verzögerung auf Ihr Wallet überwiesen. Bitte sehen Sie unbedingt davon ab, den Support diesbezüglich zu kontaktieren.",
        "platform_title" to "Atom Intelligente Investitionsplattform",
        "platform_desc" to "Atom Digitales Finanz-Ökosystem"
    )

    fun t(lang: String, key: String, vararg args: Any): String {
        val map = when (lang) {
            "en" -> en
            "ar" -> ar
            "de" -> de
            else -> fa
        }
        val raw = map[key] ?: fa[key] ?: key
        return try {
            if (args.isNotEmpty()) String.format(raw, *args) else raw
        } catch (e: Exception) {
            raw
        }
    }
}

@Composable
fun LanguageSelector(viewModel: PlatformViewModel) {
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }

    val languages = listOf(
        "fa" to "فارسی 🇮🇷",
        "en" to "English 🇺🇸",
        "ar" to "العربية 🇸🇦",
        "de" to "Deutsch 🇩🇪"
    )

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.testTag("lang_toggle_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Language Selector",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            languages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = name,
                            fontWeight = if (lang == code) FontWeight.Bold else FontWeight.Normal,
                            color = if (lang == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        viewModel.setLanguage(code)
                        expanded = false
                    },
                    modifier = Modifier.testTag("lang_select_$code")
                )
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: PlatformViewModel) {
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    var isRegisterTab by remember { mutableStateOf(false) } // Defaults to Login tab as requested
    val context = LocalContext.current
    val authStatus by viewModel.authStatus.collectAsStateWithLifecycle()

    if (authStatus.errorMessage != null) {
        Toast.makeText(context, authStatus.errorMessage, Toast.LENGTH_LONG).show()
    }

    // Localized translations for Auth Screen to ensure proper user experience
    val isFarsi = lang == "fa"
    val isArabic = lang == "ar"
    val isGerman = lang == "de"

    val tabLoginText = when {
        isFarsi -> "ورود"
        isArabic -> "تسجيل الدخول"
        isGerman -> "Einloggen"
        else -> "Login"
    }

    val tabRegisterText = when {
        isFarsi -> "ثبت‌نام"
        isArabic -> "التسجيل"
        isGerman -> "Registrieren"
        else -> "Register"
    }

    val loginTitleText = when {
        isFarsi -> "ورود به سامانه اتم"
        isArabic -> "تسجيل الدخول إلى آتوم"
        isGerman -> "Anmeldung bei Atom"
        else -> "Login to Atom"
    }

    val registerTitleText = when {
        isFarsi -> "ثبت‌نام در سامانه اتم"
        isArabic -> "التسجيل في منصة آتوم"
        isGerman -> "Registrierung bei Atom"
        else -> "Register on Atom"
    }

    val inviteCodeRequiredText = when {
        isFarsi -> "کد دعوت (اجباری)"
        isArabic -> "رمز الدعوة (إجباري)"
        isGerman -> "Einladungscode (Erforderlich)"
        else -> "Invitation Code (Required)"
    }

    val inviteCodeErrorText = when {
        isFarsi -> "وارد کردن کد دعوت الزامی است!"
        isArabic -> "رمز الدعوة مطلوب!"
        isGerman -> "Einladungscode ist erforderlich!"
        else -> "Invitation code is required!"
    }

    val loginErrorText = when {
        isFarsi -> "نام کاربری یا رمز عبور اشتباه است یا ابتدا باید ثبت‌نام کنید."
        isArabic -> "اسم المستخدم أو كلمة المرور غير صحيحة، أو تحتاج إلى التسجيل أولاً."
        isGerman -> "Falscher Benutzername oder Passwort, oder Sie müssen sich zuerst registrieren."
        else -> "Incorrect username or password, or you need to register first."
    }

    val passwordEmptyText = when {
        isFarsi -> "لطفاً رمز عبور را وارد کنید."
        isArabic -> "الرجاء إدخال كلمة المرور."
        isGerman -> "Bitte Passwort eingeben."
        else -> "Please enter password."
    }

    val registerSuccessText = when {
        isFarsi -> "ثبت‌نام با موفقیت انجام شد و بونوس ۳۵,۰۰۰,۰۰۰ دلاری فعال گردید!"
        isArabic -> "تم التسجيل بنجاح وتفعيل مكافأة 35,000,000 دولار!"
        isGerman -> "Registrierung erfolgreich und 35.000.000 $ Bonus aktiviert!"
        else -> "Registration successful and $35,000,000 bonus activated!"
    }

    val loginSuccessText = when {
        isFarsi -> "ورود با موفقیت انجام شد!"
        isArabic -> "تم تسجيل الدخول بنجاح!"
        isGerman -> "Erfolgreich angemeldet!"
        else -> "Login successful!"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Visible Language Switcher Dropdown (کشویی)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (lang == "fa") "انتخاب زبان برنامه / Select Language" else "Select Language / انتخاب زبان برنامه",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                var langExpanded by remember { mutableStateOf(false) }
                val languages = listOf(
                    "fa" to "فارسی 🇮🇷",
                    "en" to "English 🇺🇸",
                    "ar" to "العربية 🇸🇦",
                    "de" to "Deutsch 🇩🇪"
                )
                val currentLangName = languages.firstOrNull { it.first == lang }?.second ?: "فارسی 🇮🇷"

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { langExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Lang",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = currentLangName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = if (langExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Arrow",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = langExpanded,
                        onDismissRequest = { langExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        languages.forEach { (code, name) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = name,
                                        fontSize = 13.sp,
                                        fontWeight = if (lang == code) FontWeight.Bold else FontWeight.Normal,
                                        color = if (lang == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    viewModel.setLanguage(code)
                                    langExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Logo icon
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = "Wallet Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )

            Text(
                text = if (isRegisterTab) registerTitleText else loginTitleText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            // Dynamic Segmented Tab Switcher (Login / Register)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Login Tab Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isRegisterTab) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isRegisterTab = false }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabLoginText,
                        color = if (!isRegisterTab) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Register Tab Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isRegisterTab) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isRegisterTab = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabRegisterText,
                        color = if (isRegisterTab) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(Loc.t(lang, "username")) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("reg_username_input")
            )

            // Password Field
            var isPasswordVisible by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(Loc.t(lang, "password")) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    val icon = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(icon, contentDescription = null)
                    }
                },
                visualTransformation = if (isPasswordVisible) {
                    androidx.compose.ui.text.input.VisualTransformation.None
                } else {
                    androidx.compose.ui.text.input.PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("reg_password_input")
            )

            // Invitation Code & Warnings (Only shown on Registration Tab)
            if (isRegisterTab) {
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it },
                    label = { Text(inviteCodeRequiredText) },
                    leadingIcon = { Icon(Icons.Default.CardGiftcard, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("reg_invite_input")
                )

                Text(
                    text = Loc.t(lang, "invite_code_required_for_bonus"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            Button(
                onClick = {
                    if (username.isBlank()) {
                        Toast.makeText(context, Loc.t(lang, "username_empty"), Toast.LENGTH_SHORT).show()
                    } else if (password.isBlank()) {
                        Toast.makeText(context, passwordEmptyText, Toast.LENGTH_SHORT).show()
                    } else {
                        if (isRegisterTab) {
                            // Register tab requires inviteCode to be strictly non-empty
                            if (inviteCode.trim().isEmpty()) {
                                Toast.makeText(context, inviteCodeErrorText, Toast.LENGTH_LONG).show()
                            } else {
                                viewModel.register(username, password, inviteCode) { success ->
                                    if (success) {
                                        Toast.makeText(context, registerSuccessText, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        } else {
                            // Login tab checks credentials in DB
                            viewModel.login(username, password) { success ->
                                if (success) {
                                    Toast.makeText(context, loginSuccessText, Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, loginErrorText, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("reg_submit_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isRegisterTab) tabRegisterText else tabLoginText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: PlatformViewModel) {
    val user by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    if (user == null || user?.isLoggedIn != true) {
        LoginScreen(viewModel)
    } else if (user?.role == "ADMIN") {
        AdminScreen(viewModel, onClose = { viewModel.logout() })
    } else {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Screen list to show bottom navigation dynamically translated
        val bottomNavItems = listOf(
            Triple(Screen.Home.route, Loc.t(lang, "home"), Icons.Default.Home),
            Triple(Screen.Investment.route, Loc.t(lang, "investment"), Icons.Default.TrendingUp),
            Triple(Screen.Transfer.route, Loc.t(lang, "transfer"), Icons.Default.SwapHoriz),
            Triple(Screen.My.route, Loc.t(lang, "profile"), Icons.Default.Person)
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                // Only show BottomNav for main tabs
                val mainTabs = listOf(Screen.Home.route, Screen.Investment.route, Screen.Transfer.route, Screen.My.route)
                if (currentRoute in mainTabs) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        bottomNavItems.forEach { (route, label, icon) ->
                            val selected = currentRoute == route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != route) {
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(viewModel, navController)
                }
                composable(Screen.Investment.route) {
                    InvestmentScreen(viewModel, navController)
                }
                composable(Screen.Transfer.route) {
                    TransferScreen(viewModel)
                }
                composable(Screen.My.route) {
                    MyScreen(viewModel, navController)
                }
                composable(Screen.Recharge.route) {
                    RechargeScreen(viewModel, navController)
                }
                composable(Screen.Withdraw.route) {
                    WithdrawScreen(viewModel, navController)
                }
                composable(Screen.Vip.route) {
                    VipScreen(viewModel, navController)
                }
                composable(Screen.About.route) {
                    AboutScreen(navController)
                }
                composable(Screen.Support.route) {
                    SupportScreen(viewModel, navController)
                }
                composable(Screen.SyncSettings.route) {
                    SyncSettingsScreen(viewModel, navController)
                }
            }
        }
    }
}

// FORMAT HELPER
fun formatUsdt(amount: Double): String {
    return String.format(Locale.US, "%,.2f", amount)
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// DYNAMIC COIN AVATAR DRAWING
@Composable
fun CoinAvatar(coinName: String, modifier: Modifier = Modifier) {
    val prefix = coinName.substringBefore("/").take(4)
    val color = when (prefix) {
        "BTC" -> Color(0xFFF59E0B)
        "ETH" -> Color(0xFF8B5CF6)
        "DOGE" -> Color(0xFFD97706)
        "EOS" -> Color(0xFF3B82F6)
        "LTC" -> Color(0xFF6B7280)
        "BCH" -> Color(0xFF10B981)
        "XRP" -> Color(0xFF06B6D4)
        "ETC" -> Color(0xFF047857)
        "BSV" -> Color(0xFFF59E0B)
        "ADA" -> Color(0xFF2563EB)
        "FIL" -> Color(0xFF0EA5E9)
        "TRX" -> Color(0xFFEF4444)
        "UNI" -> Color(0xFFEC4899)
        "LINK" -> Color(0xFF1D4ED8)
        "SOL" -> Color(0xFF14B8A6)
        "AAVE" -> Color(0xFF7C3AED)
        else -> MaterialTheme.colorScheme.secondary
    }

    Box(
        modifier = modifier
            .size(42.dp)
            .background(color.copy(alpha = 0.2f), shape = CircleShape)
            .border(1.5.dp, color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = prefix,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

// -------------------------------------------------------------
// 1. HOME SCREEN
// -------------------------------------------------------------
@Composable
fun HomeScreen(viewModel: PlatformViewModel, navController: NavHostController) {
    val user by viewModel.userProfile.collectAsStateWithLifecycle()
    val marketPrices by viewModel.marketPrices.collectAsStateWithLifecycle()
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    val filteredPrices = remember(marketPrices, searchQuery) {
        if (searchQuery.isBlank()) marketPrices else {
            marketPrices.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Space above header
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Custom Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Loc.t(lang, "platform_title"),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "خوش آمدید، ${user?.username ?: "eth999"}",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }

                // VIP Badge & Language Selector Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LanguageSelector(viewModel = viewModel)
                    Box(
                        modifier = Modifier
                            .clickable { navController.navigate(Screen.Vip.route) }
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = "VIP",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "VIP ${user?.vipLevel ?: 1}",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Atom Brand Showcase Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_atom_banner),
                        contentDescription = "Atom Brand Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient overlay to make text highly readable
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                    )
                    // Overlay Text
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = Loc.t(lang, "platform_desc"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (lang == "fa") "قدرت گرفته از فناوری اتمی بلاکچین" else "Powered by atomic blockchain technology",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }

        // Automatic Sliding Banner Slider (4 banners)
        item {
            AdvertisingBannerSlider()
        }

        // Quick Deposit & Withdraw card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigate(Screen.Recharge.route) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCard,
                                contentDescription = "Recharge",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("واریز (Recharge)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigate(Screen.Withdraw.route) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Outbox,
                                contentDescription = "Withdraw",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("برداشت (Withdraw)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigate(Screen.Support.route) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HeadsetMic,
                                contentDescription = "Support",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("پشتیبانی", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Market list Header & Search
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "بازار ارزهای دیجیتال (قیمت زنده)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = { viewModel.manualRefreshPrices() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("جستجوی جفت ارز (مثلاً BTC)...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        // Crypto market coin list
        if (filteredPrices.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("جفت ارز مورد نظر یافت نشد.", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            items(filteredPrices) { pair ->
                CoinPriceCard(pair = pair, onInvestClick = {
                    navController.navigate(Screen.Investment.route)
                })
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun AdvertisingBannerSlider() {
    var currentBanner by remember { mutableIntStateOf(0) }

    // Automatic slide logic: rotates every 4 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            currentBanner = (currentBanner + 1) % 4
        }
    }

    val bannerImages = listOf(
        R.drawable.img_banner_1,
        R.drawable.img_banner_2,
        R.drawable.img_banner_3,
        R.drawable.img_banner_4
    )

    val bannerTitles = listOf(
        "فرصت استثنایی سرمایه‌گذاری",
        "شبکه واریز و برداشت TRC20",
        "صندوق امانات مالی بلاکچین",
        "کارت‌های عضویت VIP پلتفرم"
    )

    val bannerDescriptions = listOf(
        "سود سالانه تضمینی تا سقف ۱۵٪ متناسب با دوره انتخابی شما",
        "شارژ آنی حساب و برداشت‌های ایمن تتر تنها در چند دقیقه",
        "دارایی‌های شما تماماً در کیف‌پول‌های امن ذخیره و بیمه می‌شوند",
        "با افزایش موجودی، سقف برداشت روزانه خود را نجومی کنید"
    )

    val bannerColors = listOf(
        Color(0xFF0284C7), // blue
        Color(0xFF0D9488), // teal
        Color(0xFF4F46E5), // indigo
        Color(0xFFD97706)  // amber
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image with semi-transparent dark tint overlay
            Image(
                painter = painterResource(id = bannerImages[currentBanner]),
                contentDescription = "Crypto Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic Gradient overlay representing luxury dark ambiance
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(bannerColors[currentBanner], CircleShape)
                    )
                    Text(
                        text = bannerTitles[currentBanner],
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bannerDescriptions[currentBanner],
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    for (i in 0..3) {
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (i == currentBanner) 20.dp else 6.dp)
                                .background(
                                    color = if (i == currentBanner) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CoinPriceCard(pair: CoinPair, onInvestClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CoinAvatar(coinName = pair.name)
                Column {
                    Text(
                        text = pair.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "USDT Pair",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format(Locale.US, if (pair.price < 1.0) "%,.4f" else "%,.2f", pair.price)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    val isPositive = pair.change24hPercent >= 0
                    val badgeColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
                    val textSign = if (isPositive) "+" else ""

                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$textSign${String.format(Locale.US, "%.2f", pair.change24hPercent)}%",
                            color = badgeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = onInvestClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("invest_button_${pair.name.replace("/", "_")}")
                ) {
                    Text("سرمایه‌گذاری", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. INVESTMENT SCREEN
// -------------------------------------------------------------
@Composable
fun InvestmentScreen(viewModel: PlatformViewModel, navController: NavHostController) {
    val user by viewModel.userProfile.collectAsStateWithLifecycle()
    val investments by viewModel.investments.collectAsStateWithLifecycle()

    var showPurchaseDialog by remember { mutableStateOf<InvestmentPlan?>(null) }

    // Hardcoded products
    val plans = listOf(
        InvestmentPlan("طرح برنزی کریپتو", 3.5, 7),
        InvestmentPlan("طرح نقره‌ای بلاکچین", 5.0, 14),
        InvestmentPlan("طرح طلایی استیکینگ", 8.0, 30),
        InvestmentPlan("طرح پلاتینوم آلفا", 11.5, 60),
        InvestmentPlan("صندوق VIP الماس سیاه", 15.0, 90)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Screen title
        item {
            Text(
                text = "طرح‌های سرمایه‌گذاری تضمینی",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "سود بالا و مطمئن با دوره‌های منعطف زمانی تتر (USDT)",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Wallet Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("موجودی کل قابل استفاده", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${formatUsdt(user?.balanceUsdt ?: 0.0)} USDT",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = { navController.navigate(Screen.Recharge.route) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("افزایش", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Products List Header
        item {
            Text(
                text = "محصولات سرمایه‌گذاری فعال",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Display products
        items(plans) { plan ->
            InvestmentPlanCard(plan = plan, onParticipate = {
                showPurchaseDialog = plan
            })
        }

        // Recent User activities mock logs
        item {
            RecentUserActivityLogs()
        }

        // Partner Exchanges list
        item {
            PartnerExchangesList()
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Purchase Dialog
    showPurchaseDialog?.let { plan ->
        PurchasePlanDialog(
            plan = plan,
            userBalance = user?.balanceUsdt ?: 0.0,
            onDismiss = { showPurchaseDialog = null },
            onConfirm = { amount ->
                viewModel.purchaseInvestment(plan.title, plan.apr, plan.durationDays, amount) { success ->
                    if (success) {
                        showPurchaseDialog = null
                    }
                }
            }
        )
    }
}

data class InvestmentPlan(
    val title: String,
    val apr: Double,
    val durationDays: Int,
    val minLimitUsdt: Double = 5000.0
)

@Composable
fun InvestmentPlanCard(plan: InvestmentPlan, onParticipate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${plan.durationDays} روزه",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("نرخ سود سالانه (APR)", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "${plan.apr}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF10B981)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("حداقل سرمایه‌گذاری", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "${formatUsdt(plan.minLimitUsdt)} USDT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onParticipate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("participate_button_${plan.durationDays}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "شرکت در طرح سرمایه‌گذاری", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RecentUserActivityLogs() {
    // Generate live mock activities
    val listState = remember {
        mutableStateListOf(
            "کاربر usr_***23 مبلغ 8,400 USDT سرمایه‌گذاری کرد.",
            "کاربر usr_***89 مبلغ 12,000 USDT واریز کرد.",
            "کاربر usr_***15 مبلغ 5,200 USDT برداشت کرد.",
            "کاربر usr_***77 مبلغ 30,000 USDT سرمایه‌گذاری کرد."
        )
    }

    LaunchedEffect(Unit) {
        val names = listOf("usr_***90", "usr_***41", "usr_***65", "usr_***12", "usr_***57", "usr_***38")
        val actions = listOf("واریز کرد", "برداشت کرد", "سرمایه‌گذاری کرد")
        while (true) {
            delay(5000)
            val randomUser = names.random()
            val randomAction = actions.random()
            val amount = Random.nextInt(5000, 150000)
            val log = "کاربر $randomUser مبلغ ${formatUsdt(amount.toDouble())} USDT $randomAction."
            if (listState.size >= 5) {
                listState.removeAt(listState.size - 1)
            }
            listState.add(0, log)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "تراکنش‌های زنده کاربران پلتفرم",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listState.forEach { log ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Text(
                            text = log,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PartnerExchangesList() {
    val exchanges = listOf("Poloniex", "Bitfinex", "Binance", "Huobi", "OKCoin", "KuCoin", "OKEx", "FTX")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "صرافی‌های شریک و تأمین‌کننده نقدینگی",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(exchanges) { exchange ->
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .border(0.5.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = exchange,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PurchasePlanDialog(
    plan: InvestmentPlan,
    userBalance: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountString by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "شرکت در ${plan.title}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "دوره زمانی: ${plan.durationDays} روزه\nنرخ سود سالانه (APR): ${plan.apr}%\nحداقل سرمایه مورد نیاز: ${formatUsdt(plan.minLimitUsdt)} USDT",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Text(
                    text = "موجودی قابل استفاده شما: ${formatUsdt(userBalance)} USDT",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = amountString,
                    onValueChange = {
                        amountString = it
                        errorMessage = null
                    },
                    label = { Text("مبلغ سرمایه‌گذاری (USDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("investment_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("انصراف")
                    }

                    Button(
                        onClick = {
                            val amt = amountString.toDoubleOrNull()
                            if (amt == null) {
                                errorMessage = "لطفاً یک مبلغ معتبر وارد کنید."
                            } else if (amt < plan.minLimitUsdt) {
                                errorMessage = "حداقل مبلغ سرمایه‌گذاری ${formatUsdt(plan.minLimitUsdt)} USDT می‌باشد."
                            } else if (amt > userBalance) {
                                errorMessage = "موجودی کافی نیست! لطفاً حساب خود را شارژ کنید."
                            } else {
                                onConfirm(amt)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_invest_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("تایید")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. TRANSFER SCREEN
// -------------------------------------------------------------
@Composable
fun TransferScreen(viewModel: PlatformViewModel) {
    val user by viewModel.userProfile.collectAsStateWithLifecycle()
    var recipient by remember { mutableStateOf("") }
    var amountString by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "انتقال وجه داخلی کیف پول",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "انتقال بدون کارمزد و آنی به سایر کاربران پلتفرم",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("موجودی کل کیف پول شما", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${formatUsdt(user?.balanceUsdt ?: 0.0)} USDT",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = recipient,
                        onValueChange = {
                            recipient = it
                            errorMessage = null
                        },
                        label = { Text("شناسه یا آدرس گیرنده") },
                        placeholder = { Text("مثال: usr_789 or wallet_address") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_recipient_input"),
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Recipient") }
                    )

                    OutlinedTextField(
                        value = amountString,
                        onValueChange = {
                            amountString = it
                            errorMessage = null
                        },
                        label = { Text("مبلغ انتقال (USDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_amount_input"),
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Amount") }
                    )

                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val amt = amountString.toDoubleOrNull()
                            if (recipient.isBlank()) {
                                errorMessage = "لطفاً شناسه گیرنده را وارد کنید."
                            } else if (amt == null || amt <= 0) {
                                errorMessage = "لطفاً مبلغ معتبر وارد کنید."
                            } else if (amt > (user?.balanceUsdt ?: 0.0)) {
                                errorMessage = "موجودی کافی نیست."
                            } else {
                                viewModel.transfer(amt, recipient) { success ->
                                    if (success) {
                                        showSuccessDialog = true
                                        recipient = ""
                                        amountString = ""
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("transfer_submit_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("تایید و انتقال وجه", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.secondary)
                    Text(
                        text = "توجه: انتقال‌های داخلی کاملاً رایگان و فوری بوده و نیاز به تایید شبکه بلاکچین ندارد.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    if (showSuccessDialog) {
        Dialog(onDismissRequest = { showSuccessDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "انتقال با موفقیت انجام شد!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "مبلغ مورد نظر به صورت آنی به حساب کاربر انتقال یافت و در تاریخچه ثبت شد.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { showSuccessDialog = false },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("متوجه شدم")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. MY PROFILE SCREEN (MY)
// -------------------------------------------------------------
@Composable
fun MyScreen(viewModel: PlatformViewModel, navController: NavHostController) {
    val user by viewModel.userProfile.collectAsStateWithLifecycle()
    val investments by viewModel.investments.collectAsStateWithLifecycle()

    var showActiveInvestmentsBottomSheet by remember { mutableStateOf(false) }
    var showNftGalleryDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }

    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val authStatus by viewModel.authStatus.collectAsStateWithLifecycle()

    // Handle Google account linking result
    LaunchedEffect(authStatus.isAuthenticated) {
        if (authStatus.isAuthenticated && authStatus.userEmail != null) {
            viewModel.linkGoogleAccount(authStatus.userEmail!!)
            Toast.makeText(context, if (lang == "fa") "حساب گوگل با موفقیت متصل شد." else "Google account linked successfully.", Toast.LENGTH_SHORT).show()
        }
    }

    if (authStatus.errorMessage != null) {
        Toast.makeText(context, authStatus.errorMessage, Toast.LENGTH_LONG).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Screen title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Loc.t(lang, "profile"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                LanguageSelector(viewModel = viewModel)
            }
        }

        // User profile Card (GORGEOUS OVERLAPPING CARD WITH SOLID GRADIENT BACKGROUND)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Avatar
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            CircleShape
                                        )
                                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user?.username?.take(2)?.uppercase() ?: "US",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column {
                                    Text(
                                        text = user?.username ?: "user",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stars,
                                            contentDescription = "VIP",
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "${Loc.t(lang, "vip_badge")} ${user?.vipLevel ?: 1}",
                                            color = MaterialTheme.colorScheme.tertiary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Gift icon indicator for referral bonus
                            if (user?.hasReceivedBonus == true) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color(0xFF10B981).copy(alpha = 0.15f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CardGiftcard,
                                            contentDescription = "Bonus",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = Loc.t(lang, "active_bonus_label"),
                                            color = Color(0xFF10B981),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Invitation Code Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    val code = user?.invitationCode ?: user?.username ?: "VIP-INVITE"
                                    clipboardManager.setText(AnnotatedString(code))
                                    Toast.makeText(context, "کد دعوت کپی شد!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${Loc.t(lang, "invite_code_label")}: ${user?.invitationCode ?: user?.username ?: "VIP-INVITE"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(Loc.t(lang, "total_balance_usdt"), fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${formatUsdt(user?.balanceUsdt ?: 0.0)} USDT",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Quick Access Menus
        item {
            Text(Loc.t(lang, "quick_access"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column {
                    ProfileMenuRow(icon = Icons.Default.AddCard, title = "${Loc.t(lang, "recharge_btn")} (Recharge)", onClick = {
                        navController.navigate(Screen.Recharge.route)
                    })
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(icon = Icons.Default.Outbox, title = "${Loc.t(lang, "withdraw_btn")} (Withdraw)", onClick = {
                        navController.navigate(Screen.Withdraw.route)
                    })
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(icon = Icons.Default.SwapHoriz, title = "${Loc.t(lang, "transfer")} (Transfer)", onClick = {
                        navController.navigate(Screen.Transfer.route)
                    })
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(icon = Icons.Default.AccountBalanceWallet, title = Loc.t(lang, "my_investments_list"), onClick = {
                        showActiveInvestmentsBottomSheet = true
                    }, badgeText = if (investments.isNotEmpty()) "${investments.size} ${Loc.t(lang, "active")}" else null)
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(icon = Icons.Default.Category, title = Loc.t(lang, "nft_collection"), onClick = {
                        showNftGalleryDialog = true
                    })
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(icon = Icons.Default.TrendingUp, title = Loc.t(lang, "investment_page"), onClick = {
                        navController.navigate(Screen.Investment.route)
                    })
                }
            }
        }

        // --- NEW: Account Verification & Invite Code Section ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == "fa") "احراز هویت و کد دعوت" else "Verification & Invite Code",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Google Account Linking
                    if (user?.isGoogleVerified == true) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().background(Color.Green.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).padding(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(if (lang == "fa") "حساب گوگل متصل است" else "Google Connected", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(user?.googleEmail ?: "", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.signInWithGoogle() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !authStatus.isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                        ) {
                            if (authStatus.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (lang == "fa") "تایید هویت با حساب گوگل" else "Verify with Google Account", fontSize = 13.sp)
                            }
                        }
                    }

                    // 2. Late Invite Code Submission
                    if (user?.invitationCode.isNullOrEmpty() || user?.invitationCode == "None") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        var inputInviteCode by remember { mutableStateOf("") }
                        Text(if (lang == "fa") "ثبت کد دعوت (اگر فراموش کردید)" else "Submit Invite Code (If missed)", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = inputInviteCode,
                                onValueChange = { inputInviteCode = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("کد دعوت", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (inputInviteCode.isBlank()) return@Button
                                    viewModel.updateInviteCode(inputInviteCode) { success ->
                                        if (success) {
                                            Toast.makeText(context, "کد دعوت با موفقیت ثبت شد!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "خطا در ثبت کد دعوت.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (lang == "fa") "ثبت" else "Submit", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Settings and Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column {


                    ProfileMenuRow(icon = Icons.Default.Security, title = Loc.t(lang, "security_settings"), onClick = {
                        showSecurityDialog = true
                    })
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                    // Language Selector Dropdown Row
                    var profileLangExpanded by remember { mutableStateOf(false) }
                    val languages = listOf(
                        "fa" to "فارسی 🇮🇷",
                        "en" to "English 🇺🇸",
                        "ar" to "العربية 🇸🇦",
                        "de" to "Deutsch 🇩🇪"
                    )
                    val currentLangName = languages.firstOrNull { it.first == lang }?.second ?: "فارسی 🇮🇷"

                    Box {
                        ProfileMenuRow(
                            icon = Icons.Default.Language,
                            title = if (lang == "fa") "زبان برنامه (${currentLangName})" else "App Language (${currentLangName})",
                            onClick = { profileLangExpanded = true }
                        )

                        DropdownMenu(
                            expanded = profileLangExpanded,
                            onDismissRequest = { profileLangExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            languages.forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = name,
                                            fontSize = 13.sp,
                                            fontWeight = if (lang == code) FontWeight.Bold else FontWeight.Normal,
                                            color = if (lang == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.setLanguage(code)
                                        profileLangExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(icon = Icons.Default.Cloud, title = if (lang == "fa") "اتصال به دیتابیس آنلاین (Cloud Sync)" else "Online Cloud Database Sync", onClick = {
                        navController.navigate(Screen.SyncSettings.route)
                    })

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuRow(icon = Icons.Default.Info, title = Loc.t(lang, "about_rules"), onClick = {
                        navController.navigate(Screen.About.route)
                    })
                }
            }
        }

        // Log out button
        item {
            Button(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text(Loc.t(lang, "logout"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Modal Active Investments Dialog
    if (showActiveInvestmentsBottomSheet) {
        Dialog(onDismissRequest = { showActiveInvestmentsBottomSheet = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "سرمایه‌گذاری‌های فعال من",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (investments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("در حال حاضر هیچ طرح سرمایه‌گذاری فعالی ندارید.", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(investments) { inv ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(inv.planTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${inv.durationDays} روزه", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("مبلغ: ${formatUsdt(inv.amountInvested)} USDT", fontSize = 11.sp, color = Color.LightGray)
                                            Text("سود: ${formatUsdt(inv.interestEarned)} USDT", fontSize = 11.sp, color = Color(0xFF10B981))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("تاریخ سررسید: ${formatTime(inv.maturityDate)}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showActiveInvestmentsBottomSheet = false },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("بستن")
                    }
                }
            }
        }
    }

    // Mock NFT Gallery Dialog
    if (showNftGalleryDialog) {
        Dialog(onDismissRequest = { showNftGalleryDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("کلکسیون NFT ویژه کاربران", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Drawing Mock NFT visuals
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
                                        ),
                                        RoundedCornerShape(6.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Cyber Skull #10", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF3B82F6), Color(0xFF10B981))
                                        ),
                                        RoundedCornerShape(6.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Apex VIP Card", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("کلکسیون NFT به زودی در دسترس کاربران VIP3 و بالاتر قرار خواهد گرفت.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)

                    Button(
                        onClick = { showNftGalleryDialog = false },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("بستن")
                    }
                }
            }
        }
    }

    // Security Settings Dialog
    if (showSecurityDialog) {
        Dialog(onDismissRequest = { showSecurityDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("تنظیمات امنیتی حساب", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = "123456",
                        onValueChange = {},
                        label = { Text("رمز عبور ورود") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = "فعال شده (TRC20 Verify)",
                        onValueChange = {},
                        label = { Text("رمز تراکنش امنیتی") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = "Verified", tint = Color(0xFF10B981))
                        Text("امنیت حساب شما در سطح فوق‌العاده بالایی قرار دارد.", fontSize = 11.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (lang == "fa") "سامانه پایش IP و تغییر مالکیت" else "IP Monitoring & Ownership Transfer",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (user?.isIpSuspicious == true) MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                                else Color(0xFF10B981).copy(alpha = 0.08f),
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (user?.isIpSuspicious == true) MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                else Color(0xFF10B981).copy(alpha = 0.2f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (lang == "fa") "آدرس IP ثبت‌شده اولیه:" else "Registered IP (Secure):",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = user?.registeredIp ?: "185.120.45.12",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (lang == "fa") "آدرس IP فعلی اتصال:" else "Current Device IP:",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = user?.currentIp ?: "185.120.45.12",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (user?.isIpSuspicious == true) MaterialTheme.colorScheme.error else Color(0xFF10B981)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (user?.isIpSuspicious == true) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = "Status Icon",
                                tint = if (user?.isIpSuspicious == true) MaterialTheme.colorScheme.error else Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (user?.isIpSuspicious == true) {
                                    if (lang == "fa") "تغییر مشکوک مالکیت حساب! ⚠️" else "Suspicious Ownership Transfer! ⚠️"
                                } else {
                                    if (lang == "fa") "مالکیت حساب تایید شده ✅" else "Account Ownership Verified ✅"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (user?.isIpSuspicious == true) MaterialTheme.colorScheme.error else Color(0xFF10B981)
                            )
                        }
                    }

                    // Simulation Switcher buttons
                    if (user?.isIpSuspicious == true) {
                        Button(
                            onClick = {
                                viewModel.simulateIpChange("185.120.45.12")
                                android.widget.Toast.makeText(context, "آدرس IP بازیابی شد و امنیت حساب تایید گردید.", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (lang == "fa") "بازیابی آدرس IP به حالت ایمن" else "Restore IP to Secure State",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.simulateIpChange("91.240.32.74")
                                android.widget.Toast.makeText(context, "تغییر ناگهانی IP شبیه‌سازی شد! وضعیت حساب: تعلیق امنیتی", android.widget.Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (lang == "fa") "شبیه‌سازی تغییر ناگهانی IP (انتقال مالکیت)" else "Simulate Sudden IP Change (Ownership Transfer)",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }

                    Button(
                        onClick = { showSecurityDialog = false },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("بستن")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    badgeText: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Text(text = title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            badgeText?.let {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = it, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Go", tint = Color.Gray)
        }
    }
}

// -------------------------------------------------------------
// 5. RECHARGE SCREEN (DEPOSIT)
// -------------------------------------------------------------
@Composable
fun RechargeScreen(viewModel: PlatformViewModel, navController: NavHostController) {
    val user by viewModel.userProfile.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val deposits = remember(transactions) {
        transactions.filter { it.type == "RECHARGE" }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Back")
                }
                Text(
                    text = "واریز وجه (Recharge)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Selected Network info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("انتخاب شبکه انتقال تتر", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("TRC20", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Custom QR Code drawing
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(170.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val size = this.size.width
                        // Draw QR Code outer border squares
                        val module = size / 10
                        // Top-Left Finder
                        drawRect(Color.Black, Offset(0f, 0f), androidx.compose.ui.geometry.Size(3 * module, 3 * module))
                        drawRect(Color.White, Offset(module, module), androidx.compose.ui.geometry.Size(module, module))
                        // Top-Right Finder
                        drawRect(Color.Black, Offset(7 * module, 0f), androidx.compose.ui.geometry.Size(3 * module, 3 * module))
                        drawRect(Color.White, Offset(8 * module, module), androidx.compose.ui.geometry.Size(module, module))
                        // Bottom-Left Finder
                        drawRect(Color.Black, Offset(0f, 7 * module), androidx.compose.ui.geometry.Size(3 * module, 3 * module))
                        drawRect(Color.White, Offset(module, 8 * module), androidx.compose.ui.geometry.Size(module, module))

                        // Draw random QR code dots
                        val rand = java.util.Random(42L)
                        for (r in 0..9) {
                            for (c in 0..9) {
                                if ((r < 3 && c < 3) || (r < 3 && c > 6) || (r > 6 && c < 3)) continue
                                if (rand.nextBoolean()) {
                                    drawRect(Color.Black, Offset(c * module, r * module), androidx.compose.ui.geometry.Size(module, module))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Wallet Address Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("آدرس واریز تتر (TRC20) شما", fontSize = 12.sp, color = Color.Gray)

                    Text(
                        text = user?.walletAddress ?: "TYv9zNf2K8W7ZgHjK9XsD1FpQ6L3Rm8VbA",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val addr = user?.walletAddress ?: "TYv9zNf2K8W7ZgHjK9XsD1FpQ6L3Rm8VbA"
                            clipboardManager.setText(AnnotatedString(addr))
                            Toast.makeText(context, "آدرس کپی شد!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("copy_address_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("کپی آدرس کیف پول")
                    }
                }
            }
        }

        // Sandbox simulator button to add funds for testing
        item {
            Button(
                onClick = {
                    viewModel.recharge(50000.0)
                    Toast.makeText(context, "۵۰,۰۰۰ تتر شبیه‌سازی و واریز شد!", Toast.LENGTH_LONG).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("simulate_recharge_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Simulate")
                Spacer(modifier = Modifier.width(6.dp))
                Text("شبیه‌سازی واریز ۵۰,۰۰۰ USDT برای تست", fontWeight = FontWeight.Bold)
            }
        }

        // History Header
        item {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                Text(
                    text = "تاریخچه واریزها",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // History logs
        if (deposits.isEmpty()) {
            item {
                Text("هیچ سابقه واریزی یافت نشد.", color = Color.Gray, fontSize = 12.sp)
            }
        } else {
            items(deposits) { dep ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("واریز تتر شبکه TRC20", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(formatTime(dep.timestamp), fontSize = 11.sp, color = Color.Gray)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+${formatUsdt(dep.amount)} USDT",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF10B981), CircleShape))
                                Text(dep.status, fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// -------------------------------------------------------------
// 6. WITHDRAW SCREEN
// -------------------------------------------------------------
@Composable
fun WithdrawScreen(viewModel: PlatformViewModel, navController: NavHostController) {
    val user by viewModel.userProfile.collectAsStateWithLifecycle()
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var address by remember { mutableStateOf("") }
    var amountString by remember { mutableStateOf("") }
    var network by remember { mutableStateOf("TRC20") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Daily Limits according to VIP level
    val vipLimits = listOf(
        Pair(1, 10.0),
        Pair(2, 100.0),
        Pair(3, 25000.0),
        Pair(4, 50000.0),
        Pair(5, 100000.0)
    )

    val currentLimit = remember(user) {
        val level = user?.vipLevel ?: 1
        vipLimits.firstOrNull { it.first == level }?.second ?: 10.0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Back")
                }
                Text(
                    text = "برداشت وجه (Withdraw)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Balance and VIP Limit Overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("موجودی کل قابل استفاده", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = "${formatUsdt(user?.balanceUsdt ?: 0.0)} USDT",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF59E0B).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "VIP ${user?.vipLevel ?: 1}",
                                color = Color(0xFFF59E0B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("محدودیت برداشت روزانه VIP شما:", fontSize = 12.sp, color = Color.LightGray)
                        Text(
                            text = "${formatUsdt(currentLimit)} USDT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if ((user?.vipLevel ?: 1) < 3) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "هشدار: در حال حاضر به عنوان کاربر VIP ${user?.vipLevel ?: 1} تنها مجاز به برداشت روزانه ${currentLimit} تتر هستید. برای برداشت مبالغ بالاتر، لطفاً در صفحه VIP سطح خود را ارتقا دهید.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.error,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // 15-day security warning banner
        if (user?.isIpSuspicious == true) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (lang == "fa") "اطلاعیه امنیتی مهم (قانون ۱۵ روز)" else "Important Security Notice (15-Day Rule)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = Loc.t(lang, "withdraw_delay_warning"),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            errorMessage = null
                        },
                        label = { Text("آدرس کیف پول مقصد تتر (TRC20)") },
                        placeholder = { Text("مثال: TYv9zNf2K8W7Zg...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_address_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = amountString,
                        onValueChange = {
                            amountString = it
                            errorMessage = null
                        },
                        label = { Text("مبلغ برداشت (USDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_amount_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Network selection read only TRC20 as requested
                    OutlinedTextField(
                        value = "TRC20",
                        onValueChange = {},
                        enabled = false,
                        label = { Text("شبکه انتقال") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val amt = amountString.toDoubleOrNull()
                            if (address.isBlank()) {
                                errorMessage = "لطفاً آدرس ولت مقصد را وارد کنید."
                            } else if (amt == null || amt <= 0) {
                                errorMessage = "لطفاً مبلغ معتبر وارد کنید."
                            } else if (amt > currentLimit) {
                                errorMessage = "مبلغ وارد شده بیشتر از حد برداشت روزانه VIP شما (${currentLimit} USDT) است."
                            } else if (amt > (user?.balanceUsdt ?: 0.0)) {
                                errorMessage = "موجودی کیف پول شما کافی نیست."
                            } else {
                                viewModel.withdraw(amt, address) { res ->
                                    when (res) {
                                        WithdrawResult.SUCCESS -> {
                                            showSuccessDialog = true
                                            address = ""
                                            amountString = ""
                                            errorMessage = null
                                        }
                                        WithdrawResult.LIMIT_EXCEEDED -> {
                                            errorMessage = Loc.t(lang, "withdraw_limit_error")
                                        }
                                        WithdrawResult.VERIFICATION_REQUIRED -> {
                                            errorMessage = Loc.t(lang, "withdraw_verification_error", user?.totalRecharged ?: 0.0)
                                        }
                                        WithdrawResult.INSUFFICIENT_BALANCE -> {
                                            errorMessage = Loc.t(lang, "withdraw_insufficient")
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("withdraw_submit_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("تایید و برداشت از کیف پول", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Limit overview for reference
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("جدول محدودیت برداشت بر اساس سطح VIP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    vipLimits.forEach { (level, limit) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("VIP $level", fontSize = 11.sp, color = Color.Gray)
                            Text("${formatUsdt(limit)} USDT در روز", fontSize = 11.sp, color = Color.LightGray)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showSuccessDialog) {
        Dialog(onDismissRequest = { showSuccessDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = Loc.t(lang, "withdraw_success_title"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = Loc.t(lang, "withdraw_success_desc"),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { showSuccessDialog = false },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(Loc.t(lang, "close"))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. VIP SCREEN
// -------------------------------------------------------------
@Composable
fun VipScreen(viewModel: PlatformViewModel, navController: NavHostController) {
    val user by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedLevelForUpgrade by remember { mutableStateOf<Int?>(null) }

    val vipDetails = listOf(
        VipLevelData(1, 0.0, 10.0, "نرخ سود پایه", "پشتیبانی بات تلگرام", "بدون دسترسی"),
        VipLevelData(2, 1000.0, 100.0, "نرخ سود پایه + ۱٪", "پشتیبانی ۲۴ ساعته", "رویدادهای هفتگی"),
        VipLevelData(3, 3000.0, 25000.0, "نرخ سود پایه + ۲٪", "گروه کارشناسی VIP اختصاصی", "جلسات تحلیلی هفتگی"),
        VipLevelData(4, 30000.0, 50000.0, "نرخ سود پایه + ۳.۵٪", "مدیر حساب اختصاصی و تلن", "رویدادها و همایش‌های بزرگ"),
        VipLevelData(5, 50000.0, 100000.0, "نرخ سود پایه + ۵٪", "خدمات VIP لوکس و پرایوت", "عضویت در کلوپ سلطنتی")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Back")
                }
                Text(
                    text = "ارتقای سطح کاربری VIP",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Current status card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("سطح VIP فعلی شما", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                "VIP ${user?.vipLevel ?: 1}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "موجودی: ${formatUsdt(user?.balanceUsdt ?: 0.0)} USDT",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Comparative Levels Grid
        item {
            Text(
                text = "جداول سطوح و هزینه‌های فعال‌سازی",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(vipDetails) { details ->
            val isCurrent = (user?.vipLevel ?: 1) == details.level
            val isHigher = (user?.vipLevel ?: 1) < details.level

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = BorderStroke(
                    width = if (isCurrent) 1.5.dp else 0.5.dp,
                    color = if (isCurrent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VIP ${details.level}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onBackground
                        )

                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("سطح فعلی", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (isHigher) {
                            Button(
                                onClick = { selectedLevelForUpgrade = details.level },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("upgrade_button_${details.level}")
                            ) {
                                Text("ارتقا سطح", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("حداقل دارایی مورد نیاز", fontSize = 10.sp, color = Color.Gray)
                            Text("${formatUsdt(details.minAsset)} USDT", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("حد برداشت روزانه", fontSize = 10.sp, color = Color.Gray)
                            Text("${formatUsdt(details.dailyLimit)} USDT", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("• مزایا و نرخ اختصاصی: ${details.rates}", fontSize = 11.sp, color = Color.LightGray)
                    Text("• کانال سبز ارتباطی: ${details.supportChannel}", fontSize = 11.sp, color = Color.LightGray)
                    Text("• رویدادها و گزارش‌ها: ${details.events}", fontSize = 11.sp, color = Color.LightGray)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Upgrade Confirmation Dialog
    selectedLevelForUpgrade?.let { targetLvl ->
        val details = vipDetails.first { it.level == targetLvl }
        val cost = details.minAsset

        Dialog(onDismissRequest = { selectedLevelForUpgrade = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("تایید ارتقای حساب کاربری", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    Text(
                        text = "آیا تمایل دارید حساب خود را به VIP ${targetLvl} ارتقا دهید؟\n\nهزینه ارتقا: ${formatUsdt(cost)} USDT\nحد برداشت روزانه جدید شما: ${formatUsdt(details.dailyLimit)} USDT خواهد بود.",
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedLevelForUpgrade = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("انصراف")
                        }

                        Button(
                            onClick = {
                                viewModel.upgradeVipLevel(targetLvl, cost) { success ->
                                    if (success) {
                                        Toast.makeText(context, "حساب شما با موفقیت به VIP $targetLvl ارتقا یافت!", Toast.LENGTH_LONG).show()
                                        selectedLevelForUpgrade = null
                                    } else {
                                        Toast.makeText(context, "خطا! موجودی ولت شما برای این ارتقا کافی نیست.", Toast.LENGTH_LONG).show()
                                        selectedLevelForUpgrade = null
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("confirm_upgrade_button"),
                        ) {
                            Text("ارتقا سطح", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

data class VipLevelData(
    val level: Int,
    val minAsset: Double,
    val dailyLimit: Double,
    val rates: String,
    val supportChannel: String,
    val events: String
)

// -------------------------------------------------------------
// 8. ABOUT SCREEN
// -------------------------------------------------------------
@Composable
fun AboutScreen(navController: NavHostController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Back")
                }
                Text(
                    text = "درباره پلتفرم سرمایه‌گذاری اتم (Atom)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_atom_banner),
                    contentDescription = "Atom Brand Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "پلتفرم سرمایه‌گذاری هوشمند اتم (Atom)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "این پلتفرم یکی از پیشگامان ارائه راه‌حل‌های مالی غیرمتمرکز و استخر نقدینگی تحت شبکه بلاکچین ترون (TRC20) می‌باشد. ما با اتصال به صرافی‌های بین‌المللی رده‌اول نظیر بایننس، هوبی و کوین‌بیس، دارایی‌های کاربران را در استخرهای نقدینگی توزیع کرده و سود سالانه پایدار بین ۳٪ تا ۱۵٪ را به ارمغان می‌آوریم.",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        item {
            Text("قوانین و مقررات کلی", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("۱. تراکنش‌های واریز و برداشت منحصراً در شبکه TRC20 تتر انجام می‌شود.", fontSize = 11.sp, color = Color.Gray)
                    Text("۲. سود به طور روزانه در حساب کاربری شما محاسبه و پس از سررسید قابل برداشت است.", fontSize = 11.sp, color = Color.Gray)
                    Text("۳. سقف برداشت روزانه شما کاملاً وابسته به سطح VIP حساب کاربری شماست.", fontSize = 11.sp, color = Color.Gray)
                    Text("۴. انصراف زودتر از موعد از طرح‌های سرمایه‌گذاری شامل جریمه کارمزد خواهد بود.", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Verified, contentDescription = "Legal", tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "تمام سرمایه‌گذاری‌ها و قراردادهای مالی پلتفرم توسط صندوق بیمه تأمین مالی بلاکچین به طور کامل بیمه شده‌اند.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 9. ONLINE SUPPORT (LIVE CHAT & TICKETS)
// -------------------------------------------------------------
@Composable
fun SupportScreen(viewModel: PlatformViewModel, navController: NavHostController) {
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val tickets by viewModel.tickets.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Live Chat, 1: Tickets
    var chatText by remember { mutableStateOf("") }

    var ticketSubject by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Recharge") }
    val categories = listOf("Recharge", "Withdrawal", "VIP Level", "Security")

    val chatListState = rememberLazyListState()

    // Scroll chat to bottom when messages list changes
    LaunchedEffect(chatMessages) {
        if (chatMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Back")
            }
            Text(
                text = "پشتیبانی آنلاین و تیکت‌ها",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Tabs
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("چت زنده (Live Chat)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("تیکت‌های من", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activeTab == 0) {
            // Live Chat View
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = chatListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(chatMessages) { msg ->
                        val isUser = msg.sender == "USER"
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 260.dp)
                                    .background(
                                        color = if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isUser) 12.dp else 0.dp,
                                            bottomEnd = if (isUser) 0.dp else 12.dp
                                        )
                                    )
                                    .border(
                                        width = 0.5.dp,
                                        color = if (isUser) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isUser) 12.dp else 0.dp,
                                            bottomEnd = if (isUser) 0.dp else 12.dp
                                        )
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatText,
                        onValueChange = { chatText = it },
                        placeholder = { Text("سوال خود را اینجا بپرسید...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    IconButton(
                        onClick = {
                            if (chatText.isNotBlank()) {
                                viewModel.sendChatMessage(chatText)
                                chatText = ""
                            }
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .size(48.dp)
                            .testTag("chat_send_button"),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        } else {
            // Tickets View
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ticket creation form
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("ارسال تیکت پشتیبانی جدید", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                            OutlinedTextField(
                                value = ticketSubject,
                                onValueChange = { ticketSubject = it },
                                label = { Text("موضوع تیکت") },
                                placeholder = { Text("مثال: عدم واریز سود طرح ۳۰ روزه") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ticket_subject_input"),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Text("دسته‌بندی موضوعی:", fontSize = 12.sp, color = Color.Gray)

                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                categories.forEach { cat ->
                                    val selected = selectedCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .border(0.5.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                            .clickable { selectedCategory = cat }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = cat,
                                            color = if (selected) Color.White else Color.Gray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (ticketSubject.isNotBlank()) {
                                        viewModel.createTicket(ticketSubject, selectedCategory)
                                        ticketSubject = ""
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("create_ticket_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("ثبت و ارسال تیکت", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Text("تیکت‌های قبلی شما", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }

                if (tickets.isEmpty()) {
                    item {
                        Text("تاکنون هیچ تیکتی ثبت نکرده‌اید.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    items(tickets) { ticket ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(ticket.subject, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("شناسه: ${ticket.id} | بخش: ${ticket.category}", fontSize = 11.sp, color = Color.Gray)
                                    }

                                    val isOpen = ticket.status == "OPEN"
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isOpen) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                else Color.Gray.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (isOpen) "در حال بررسی" else "پاسخ داده شده",
                                            color = if (isOpen) MaterialTheme.colorScheme.primary else Color.Gray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (ticket.status == "OPEN") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.resolveTicket(ticket.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("حل شده و بستن تیکت", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminScreen(viewModel: PlatformViewModel, onClose: () -> Unit) {
    var isLoggedIn by remember { mutableStateOf(false) }
    var loginUser by remember { mutableStateOf("") }
    var loginPass by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf(false) }

    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allSystemCodes by viewModel.allInvitationCodes.collectAsStateWithLifecycle()
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()

    var selectedAdminUser by remember { mutableStateOf<UserEntity?>(null) }

    // Synchronize selection when list loads
    LaunchedEffect(allUsers) {
        if (selectedAdminUser == null && allUsers.isNotEmpty()) {
            selectedAdminUser = allUsers.firstOrNull()
        } else if (selectedAdminUser != null) {
            // keep it updated if the db updates
            selectedAdminUser = allUsers.find { it.id == selectedAdminUser?.id }
        }
    }

    // Local states for editing user details (reacts to selectedAdminUser change)
    var usernameField by remember(selectedAdminUser) { mutableStateOf(selectedAdminUser?.username ?: "eth999") }
    var vipLevelField by remember(selectedAdminUser) { mutableStateOf((selectedAdminUser?.vipLevel ?: 1).toFloat()) }
    var balanceField by remember(selectedAdminUser) { mutableStateOf(selectedAdminUser?.balanceUsdt?.toString() ?: "0.0") }
    var totalRechargedField by remember(selectedAdminUser) { mutableStateOf(selectedAdminUser?.totalRecharged?.toString() ?: "0.0") }
    var walletAddressField by remember(selectedAdminUser) { mutableStateOf(selectedAdminUser?.walletAddress ?: "") }
    var invitationCodeField by remember(selectedAdminUser) { mutableStateOf(selectedAdminUser?.invitationCode ?: "") }
    var registeredIpField by remember(selectedAdminUser) { mutableStateOf(selectedAdminUser?.registeredIp ?: "185.120.45.12") }
    var currentIpField by remember(selectedAdminUser) { mutableStateOf(selectedAdminUser?.currentIp ?: "185.120.45.12") }
    var isIpSuspiciousField by remember(selectedAdminUser) { mutableStateOf(selectedAdminUser?.isIpSuspicious ?: false) }

    // States for injecting transaction
    var txType by remember { mutableStateOf("RECHARGE") } // "RECHARGE", "WITHDRAW", "TRANSFER"
    var txAmount by remember { mutableStateOf("5000.0") }
    var txAddress by remember { mutableStateOf("TYv9zNf2K8W7...VbA") }
    var txStatus by remember { mutableStateOf("COMPLETED") } // "COMPLETED", "PENDING"

    // State for support reply
    var supportReplyText by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Crimson administrative theme
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFE53935), // Crimson Red
            onPrimary = Color.White,
            secondary = Color(0xFFFFB300), // Amber Accent
            background = Color(0xFF121212), // Deep black
            surface = Color(0xFF1E1E1E), // Dark grey
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        if (!isLoggedIn) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(72.dp)
                    )

                    Text(
                        text = "ترمینال امنیتی مدیریت اتم",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "جهت دسترسی به پنل مدیریت، اطلاعات امنیتی خود را وارد کنید.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = loginUser,
                        onValueChange = {
                            loginUser = it
                            loginError = false
                        },
                        label = { Text("نام کاربری (Username)", color = Color.Gray, fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_login_user"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE53935),
                            focusedLabelColor = Color(0xFFE53935),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )

                    OutlinedTextField(
                        value = loginPass,
                        onValueChange = {
                            loginPass = it
                            loginError = false
                        },
                        label = { Text("کلمه عبور (Password)", color = Color.Gray, fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                        },
                        trailingIcon = {
                            val icon = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(icon, contentDescription = null, tint = Color.Gray)
                            }
                        },
                        visualTransformation = if (isPasswordVisible) {
                            androidx.compose.ui.text.input.VisualTransformation.None
                        } else {
                            androidx.compose.ui.text.input.PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_login_pass"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE53935),
                            focusedLabelColor = Color(0xFFE53935),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )

                    if (loginError) {
                        Text(
                            text = "نام کاربری یا رمز عبور اشتباه است!",
                            color = Color(0xFFE53935),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (loginUser == "adelbtc" && loginPass == "A18803611@") {
                                isLoggedIn = true
                            } else {
                                loginError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_login_submit"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "ورود ایمن به پنل",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "خروج و بازگشت",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            Scaffold(
                topBar = {
                    Surface(
                        color = Color(0xFF1E1E1E),
                        tonalElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onClose) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Back to User App",
                                        tint = Color.White
                                    )
                                }
                                Text(
                                    text = "ترمینال مدیریتی اتم (Atom Admin Terminal)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            
                            // Admin Badge
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE53935).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFE53935), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ADMIN",
                                    color = Color(0xFFE53935),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                containerColor = Color(0xFF121212)
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Section 1: DB Sync Status
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(Color.Green, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "اتصال زنده به سیستم و سینک فراملی: برقرار",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Green
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "هرگونه تغییر در مبالغ، وضعیت‌ها، مشخصات کاربران و پیام‌های چت، به صورت آنی و دوطرفه از طریق ساختار دیتابیس سینک شده و در اپلیکیشن کاربر اعمال می‌گردد. در صورت استقرار آنلاین، تراکنش‌ها روی کلاینت کابر (مثلاً در آمریکا) و پنل مدیر (در ایران) از طریق لایه وب‌سوکت در صدم ثانیه همگام می‌شوند.",
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Section 2: Registered Users Carousel
                    item {
                        Text(
                            text = "کاربران ثبت‌نام شده (${allUsers.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935)
                        )
                    }
                    
                    // NEW Section: Invite Code Management
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "مدیریت کدهای دعوت (Invite Codes)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFE53935))
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Button(
                                    onClick = { 
                                        viewModel.generateInvitationCode()
                                        Toast.makeText(context, "کد دعوت جدید تولید شد!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تولید کد دعوت جدید (Generate Code)", fontSize = 11.sp)
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "کدهای سیستمی فعال:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                
                                if (allSystemCodes.isEmpty()) {
                                    Text(
                                        text = "هیچ کد سیستمی فعالی وجود ندارد.",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    allSystemCodes.forEach { sysCode ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = sysCode.code,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFFFB300)
                                            )
                                            Row {
                                                IconButton(
                                                    onClick = {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        val clip = ClipData.newPlainText("Invite Code", sysCode.code)
                                                        clipboard.setPrimaryClip(clip)
                                                        Toast.makeText(context, "کپی شد!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(
                                                    onClick = { viewModel.deleteInvitationCode(sysCode.code) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935).copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = Color.White.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "کدهای دعوت کاربران:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                
                                allUsers.take(5).forEach { u ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = u.username, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(text = if(u.role == "ADMIN") "مدیر کل" else "کاربر عادی", fontSize = 9.sp, color = Color.Gray)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = u.invitationCode,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    val clip = ClipData.newPlainText("Invite Code", u.invitationCode)
                                                    clipboard.setPrimaryClip(clip)
                                                    Toast.makeText(context, "کپی شد!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        if (allUsers.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("هیچ کاربری ثبت‌نام نکرده است.", color = Color.Gray, fontSize = 12.sp)
                            }
                        } else {
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(allUsers) { u ->
                                    val isSelected = selectedAdminUser?.id == u.id
                                    val isActiveUser = viewModel.userProfile.value?.id == u.id
                                    Card(
                                        modifier = Modifier
                                            .width(200.dp)
                                            .clickable { selectedAdminUser = u },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Color(0xFFE53935).copy(alpha = 0.2f)
                                            else Color(0xFF1E1E1E)
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color(0xFFE53935) else Color.Gray.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = u.username,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color.White
                                                )
                                                if (isActiveUser) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0xFF2E7D32), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("فعال", color = Color.White, fontSize = 8.sp)
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "موجودی: ${formatUsdt(u.balanceUsdt)} USDT",
                                                fontSize = 11.sp,
                                                color = Color.LightGray
                                            )
                                            Text(
                                                text = "سطح: VIP ${u.vipLevel}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    viewModel.login(u.username, u.password) { }
                                                    Toast.makeText(context, "سوئیچ به حساب کاربری ${u.username} انجام شد!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("تست و ورود به اکانت", fontSize = 10.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section 3: Pending Withdrawal Requests (CRITICAL REQUIREMENT)
                    item {
                        Text(
                            text = "درخواست‌های برداشت در حال انتظار",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935)
                        )
                    }

                    val pendingWithdrawals = allTransactions.filter { it.type == "WITHDRAW" && it.status == "PENDING" }
                    if (pendingWithdrawals.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("هیچ درخواست برداشتی در حال انتظار نیست.", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    } else {
                        items(pendingWithdrawals) { tx ->
                            var editedAmountText by remember(tx) { mutableStateOf(tx.amount.toString()) }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                                border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "کاربر: ${tx.username}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "شبکه: TRC20",
                                            color = Color(0xFFFFB300),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "آدرس مقصد: ${tx.address}",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                    Text(
                                        text = "مبلغ درخواستی اولیه: ${formatUsdt(tx.amount)} USDT",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Edit Payout Amount field
                                    OutlinedTextField(
                                        value = editedAmountText,
                                        onValueChange = { editedAmountText = it },
                                        label = { Text("مبلغ پرداخت نهایی (امکان کم یا زیاد کردن)", color = Color.Gray) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFE53935),
                                            focusedLabelColor = Color(0xFFE53935),
                                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val amt = editedAmountText.toDoubleOrNull() ?: tx.amount
                                                viewModel.adminApproveWithdrawal(tx.id, amt)
                                                Toast.makeText(context, "درخواست برداشت کاربر ${tx.username} با مبلغ ${formatUsdt(amt)} تایید و تکمیل شد!", Toast.LENGTH_LONG).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("قبول و تایید نهایی", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.adminRejectWithdrawal(tx.id)
                                                Toast.makeText(context, "درخواست برداشت کاربر ${tx.username} رد شد و مبلغ عودت گردید.", Toast.LENGTH_LONG).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("رد درخواست و عودت", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section 4: Full Supervision & Parameter management form
                    item {
                        Text(
                            text = "نظارت کامل و ویرایش مشخصات کاربر: ${selectedAdminUser?.username ?: "هیچکدام"}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935)
                        )
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Username
                                OutlinedTextField(
                                    value = usernameField,
                                    onValueChange = { usernameField = it },
                                    label = { Text("نام کاربری (Username)", color = Color.Gray) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFE53935),
                                        focusedLabelColor = Color(0xFFE53935),
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )

                                // Balance
                                OutlinedTextField(
                                    value = balanceField,
                                    onValueChange = { balanceField = it },
                                    label = { Text("موجودی تتر (USDT Balance)", color = Color.Gray) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFE53935),
                                        focusedLabelColor = Color(0xFFE53935),
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )

                                // Quick Balance Shifters (+/- 5k, +/- 50k, +/- 1M)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(5000.0, 50000.0, 1000000.0).forEach { amount ->
                                        Button(
                                            onClick = {
                                                val cur = balanceField.toDoubleOrNull() ?: 0.0
                                                balanceField = (cur + amount).toString()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("+${formatUsdt(amount)}", fontSize = 10.sp, color = Color.White)
                                        }
                                        Button(
                                            onClick = {
                                                val cur = balanceField.toDoubleOrNull() ?: 0.0
                                                val next = if (cur - amount < 0) 0.0 else cur - amount
                                                balanceField = next.toString()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("-${formatUsdt(amount)}", fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                }

                                // Total Recharged
                                OutlinedTextField(
                                    value = totalRechargedField,
                                    onValueChange = { totalRechargedField = it },
                                    label = { Text("کل واریزی‌ها (Total Recharged)", color = Color.Gray) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFE53935),
                                        focusedLabelColor = Color(0xFFE53935),
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )

                                // VIP Level Slider
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "سطح کاربری VIP: VIP ${vipLevelField.toInt()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Slider(
                                        value = vipLevelField,
                                        onValueChange = { vipLevelField = it },
                                        valueRange = 1f..5f,
                                        steps = 3,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFFE53935),
                                            activeTrackColor = Color(0xFFE53935),
                                            inactiveTrackColor = Color.Gray
                                        )
                                    )
                                }

                                // Wallet Address
                                OutlinedTextField(
                                    value = walletAddressField,
                                    onValueChange = { walletAddressField = it },
                                    label = { Text("آدرس کیف پول (TRC20 Wallet)", color = Color.Gray) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFE53935),
                                        focusedLabelColor = Color(0xFFE53935),
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )

                                // Invitation Code
                                OutlinedTextField(
                                    value = invitationCodeField,
                                    onValueChange = { invitationCodeField = it },
                                    label = { Text("کد دعوت اختصاصی کاربر", color = Color.Gray) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFE53935),
                                        focusedLabelColor = Color(0xFFE53935),
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )

                                // Registered & Current IP
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = registeredIpField,
                                        onValueChange = { registeredIpField = it },
                                        label = { Text("IP ثبت‌نام", color = Color.Gray, fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFE53935),
                                            focusedLabelColor = Color(0xFFE53935),
                                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                        )
                                    )
                                    OutlinedTextField(
                                        value = currentIpField,
                                        onValueChange = { currentIpField = it },
                                        label = { Text("IP فعلی", color = Color.Gray, fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFE53935),
                                            focusedLabelColor = Color(0xFFE53935),
                                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                        )
                                    )
                                }

                                // Suspicious IP Switch (CRITICAL COMPONENT)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "تگ امنیتی IP مشکوک (Suspicious IP)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isIpSuspiciousField) Color(0xFFE53935) else Color.White
                                        )
                                        Text(
                                            text = "در صورت فعال بودن، کاربر برای برداشت نیاز به واریز ۱۷.۵ میلیون تتر خواهد داشت.",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Switch(
                                        checked = isIpSuspiciousField,
                                        onCheckedChange = { isIpSuspiciousField = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFFE53935),
                                            checkedTrackColor = Color(0xFFE53935).copy(alpha = 0.5f)
                                        )
                                    )
                                }

                                // Apply Button
                                Button(
                                    onClick = {
                                        val bal = balanceField.toDoubleOrNull() ?: 0.0
                                        val rech = totalRechargedField.toDoubleOrNull() ?: 0.0
                                        val curSelected = selectedAdminUser ?: return@Button
                                        
                                        val updated = curSelected.copy(
                                            username = usernameField,
                                            vipLevel = vipLevelField.toInt(),
                                            balanceUsdt = bal,
                                            walletAddress = walletAddressField,
                                            totalRecharged = rech,
                                            invitationCode = invitationCodeField,
                                            registeredIp = registeredIpField,
                                            currentIp = currentIpField,
                                            isIpSuspicious = isIpSuspiciousField
                                        )
                                        
                                        viewModel.adminUpdateAnyUser(updated)
                                        Toast.makeText(context, "مشخصات کاربر با موفقیت در سیستم ذخیره شد!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("ثبت و به روز رسانی آنی مشخصات کاربر", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Section 5: Direct Transaction Injection
                    item {
                        Text(
                            text = "ابزار تزریق مستقیم تراکنش به تاریخچه",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935)
                        )
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Transaction Type
                                Text("نوع تراکنش:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("RECHARGE" to "واریز", "WITHDRAW" to "برداشت", "TRANSFER" to "انتقال").forEach { (type, label) ->
                                        val isSelected = txType == type
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) Color(0xFFE53935) else Color.Gray.copy(alpha = 0.2f))
                                                .clickable { txType = type }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(label, color = if (isSelected) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Amount
                                OutlinedTextField(
                                    value = txAmount,
                                    onValueChange = { txAmount = it },
                                    label = { Text("مبلغ (Amount in USDT)", color = Color.Gray) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFE53935),
                                        focusedLabelColor = Color(0xFFE53935),
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )

                                // Address/Destination
                                OutlinedTextField(
                                    value = txAddress,
                                    onValueChange = { txAddress = it },
                                    label = { Text("آدرس مقصد / مبدا", color = Color.Gray) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFE53935),
                                        focusedLabelColor = Color(0xFFE53935),
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )

                                // Status Selector
                                Text("وضعیت تراکنش:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("COMPLETED" to "کامل شده", "PENDING" to "در حال بررسی").forEach { (status, label) ->
                                        val isSelected = txStatus == status
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) Color(0xFFE53935) else Color.Gray.copy(alpha = 0.2f))
                                                .clickable { txStatus = status }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(label, color = if (isSelected) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Inject Action Button
                                Button(
                                    onClick = {
                                        val amt = txAmount.toDoubleOrNull() ?: 0.0
                                        viewModel.adminAddTransaction(
                                            type = txType,
                                            amount = amt,
                                            network = "TRC20",
                                            address = txAddress,
                                            status = txStatus
                                        )
                                        Toast.makeText(context, "تراکنش جدید با موفقیت به تاریخچه تزریق شد!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("تزریق تراکنش به دیتابیس", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Section 6: Real-time Persistent Chat terminal (CRITICAL REQUIREMENT)
                    val chatUsername = selectedAdminUser?.username ?: "eth999"
                    item {
                        Text(
                            text = "ترمینال گفتگوی زنده دوطرفه با کاربر: $chatUsername",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935)
                        )
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("تاریخچه چت پایگاه داده با کاربر منتخب:", fontSize = 11.sp, color = Color.Gray)
                                
                                val chatHistory by viewModel.getMessagesForUser(chatUsername).collectAsStateWithLifecycle(emptyList())

                                if (chatHistory.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("هیچ پیامی رد و بدل نشده است.", color = Color.Gray, fontSize = 11.sp)
                                    }
                                } else {
                                    chatHistory.takeLast(10).forEach { msg ->
                                        val isUser = msg.sender == "USER"
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = if (isUser) Arrangement.Start else Arrangement.End
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isUser) Color(0xFFE53935).copy(alpha = 0.15f) else Color(0xFF2E7D32).copy(alpha = 0.15f))
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "${if (isUser) "کاربر" else "پشتیبان (شما)"}: ${msg.text}",
                                                    fontSize = 11.sp,
                                                    color = if (isUser) Color(0xFFE53935) else Color.White
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Chat Reply field
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = supportReplyText,
                                        onValueChange = { supportReplyText = it },
                                        placeholder = { Text("پاسخ به چت پشتیبانی...", fontSize = 12.sp, color = Color.Gray) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1.0f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFE53935),
                                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                        )
                                    )

                                    IconButton(
                                        onClick = {
                                            if (supportReplyText.isNotBlank()) {
                                                viewModel.sendSupportReply(chatUsername, supportReplyText)
                                                supportReplyText = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .background(Color(0xFFE53935), CircleShape)
                                            .size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Send",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section 7: Database Maintenance actions
                    item {
                        Text(
                            text = "پاکسازی دیتابیس و ریست",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935)
                        )
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.adminClearTransactions()
                                        Toast.makeText(context, "تمام تاریخچه تراکنش‌ها پاک شد!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("پاکسازی کامل تاریخچه تراکنش‌ها", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.adminClearInvestments()
                                        Toast.makeText(context, "تمام طرح‌های سرمایه‌گذاری فعال پاک شدند!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("پاکسازی کامل طرح‌های فعال سرمایه‌گذاری", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SyncSettingsScreen(viewModel: PlatformViewModel, navController: NavHostController) {
    val lang by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val syncStatus by viewModel.databaseSyncStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var restUrlInput by remember { mutableStateOf(syncStatus.restApiUrl) }
    var restKeyInput by remember { mutableStateOf(syncStatus.restApiKey) }

    LaunchedEffect(syncStatus.restApiUrl, syncStatus.restApiKey) {
        restUrlInput = syncStatus.restApiUrl
        restKeyInput = syncStatus.restApiKey
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = if (lang == "fa") Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = if (lang == "fa") "همگام‌سازی دیتابیس آنلاین" else "Online Database Sync",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (lang == "fa") "وضعیت اتصال شبکه داده" else "Database Network State",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhoneAndroid,
                                        contentDescription = "Local DB",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (lang == "fa") "دیتابیس محلی" else "Local SQLite",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (lang == "fa") "فعال و متصل" else "Active (Room)",
                                    fontSize = 9.sp,
                                    color = Color(0xFF10B981)
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                val isCloudActive = syncStatus.isFirebaseSyncEnabled || syncStatus.isRestApiSyncEnabled
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = "Syncing",
                                        tint = if (isCloudActive) MaterialTheme.colorScheme.tertiary else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "• • •",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCloudActive) MaterialTheme.colorScheme.tertiary else Color.Gray,
                                        letterSpacing = 2.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Syncing",
                                        tint = if (isCloudActive) MaterialTheme.colorScheme.tertiary else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isCloudActive) "همگام‌سازی زنده" else "آفلاین / محلی",
                                    fontSize = 10.sp,
                                    color = if (isCloudActive) MaterialTheme.colorScheme.tertiary else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val hasActiveCloud = syncStatus.isFirebaseSyncEnabled || syncStatus.isRestApiSyncEnabled
                                val cloudColor = if (hasActiveCloud) MaterialTheme.colorScheme.tertiary else Color.Gray
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(cloudColor.copy(alpha = 0.15f), CircleShape)
                                        .border(2.dp, cloudColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudQueue,
                                        contentDescription = "Cloud DB",
                                        tint = cloudColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (lang == "fa") "دیتابیس ابری" else "Cloud Database",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (hasActiveCloud) (if (lang == "fa") "همگام متصل" else "Online") else (if (lang == "fa") "غیرفعال" else "Offline"),
                                    fontSize = 9.sp,
                                    color = cloudColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = syncStatus.connectionMessage,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SendToMobile,
                                    contentDescription = "Firebase",
                                    tint = if (syncStatus.isFirebaseConfigured) Color(0xFFFFCA28) else Color.Gray
                                )
                                Column {
                                    Text(
                                        text = "پایگاه داده فایربیس (Firebase Firestore)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (syncStatus.isFirebaseConfigured) "پیکربندی شده و آماده همگام‌سازی" else "عدم پیکربندی google-services.json",
                                        fontSize = 10.sp,
                                        color = if (syncStatus.isFirebaseConfigured) Color(0xFF10B981) else Color.Gray
                                    )
                                }
                            }

                            Switch(
                                checked = syncStatus.isFirebaseSyncEnabled,
                                onCheckedChange = { viewModel.setFirebaseSyncEnabled(it) },
                                enabled = syncStatus.isFirebaseConfigured
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "توضیح: این پایگاه داده ابری به شما امکان می‌دهد اطلاعات حساب کاربران، تاریخچه تراکنش‌ها و چت پشتیبانی را به صورت بلادرنگ در فضای ابری گوگل همگام و نگهداری کنید. در صورت فعال بودن، هر تغییری در ثانیه در سرور ابری منعکس می‌شود.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )

                        if (!syncStatus.isFirebaseConfigured) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "راهنما: برای اتصال کامل به فایربیس اختصاصی خود، فایل google-services.json دریافتی از پنل فایربیس را دانلود و در پوشه ریشه پروژه (app/) در AI Studio بارگذاری کنید تا بیلد برنامه آن را فعال کند. برنامه در حال حاضر به صورت محلی و خودکار بدون باگ کار می‌کند.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "REST API Sync",
                                    tint = if (syncStatus.isRestApiSyncEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                                Column {
                                    Text(
                                        text = "اتصال به وب دیتابیس (REST Cloud Sync)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (syncStatus.isRestApiSyncEnabled) "همگام‌سازی HTTP فعال است" else "همگام‌سازی HTTP غیرفعال است",
                                        fontSize = 10.sp,
                                        color = if (syncStatus.isRestApiSyncEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                }
                            }

                            Switch(
                                checked = syncStatus.isRestApiSyncEnabled,
                                onCheckedChange = { viewModel.setRestApiSyncEnabled(it) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "تنظیمات آدرس اتصال REST API:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = restUrlInput,
                            onValueChange = { restUrlInput = it },
                            label = { Text("آدرس دیتابیس ابری (Cloud URL)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = restKeyInput,
                            onValueChange = { restKeyInput = it },
                            label = { Text("کلید دسترسی خصوصی (API Key/Token)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.updateRestSettings(restUrlInput.trim(), restKeyInput.trim())
                                Toast.makeText(context, "تنظیمات اتصال دیتابیس وب ذخیره و تست شد!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("ثبت و بررسی اتصال وب دیتابیس", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "پشتیبان‌گیری اضطراری و انتقال سراسری",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "با فشردن دکمه زیر، کل داده‌های ذخیره شده شامل تمام اطلاعات پروفایل‌ها، تاریخچه تراکنش‌ها، پیام‌های چت پشتیبانی و پلن‌های سرمایه‌گذاری فعال را به دیتابیس‌های ابری متصل بالا به صورت یکجا پشتیبان‌گیری و ارسال کنید.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.backupAllToCloud()
                                Toast.makeText(context, "دستور همگام‌سازی و پشتیبان‌گیری ابری ارسال شد!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Backup")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ارسال فوری نسخه پشتیبان دیتابیس به ابر", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        if (syncStatus.lastSyncTimestamp > 0L) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "آخرین همگام‌سازی موفق: ${formatTime(syncStatus.lastSyncTimestamp)}",
                                fontSize = 10.sp,
                                color = Color(0xFF10B981),
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                    border = BorderStroke(1.dp, Color(0xFF333333))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFF10B981), CircleShape)
                                )
                                Text(
                                    text = "کنسول پایش همگام‌سازی (Live Sync Terminal)",
                                    color = Color(0xFFEEEEEE),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = "ONLINE",
                                color = Color(0xFF10B981),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            val logs = syncStatus.syncLog
                            if (logs.isEmpty()) {
                                Text(
                                    text = "در انتظار رخداد جدید...\nبرنامه به دیتابیس محلی متصل است.",
                                    color = Color(0xFF888888),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(logs) { logMsg ->
                                        Text(
                                            text = logMsg,
                                            color = if (logMsg.contains("خطا") || logMsg.contains("خطای")) Color(0xFFEF4444) else Color(0xFF10B981),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
