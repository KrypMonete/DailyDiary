package com.dailydairy.settings

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dailydairy.backup.Backup
import com.dailydairy.diary.Biometric
import com.dailydairy.diary.DiaryViewModel
import com.dailydairy.diary.barColors
import com.dailydairy.home.ThemeSection
import com.dailydairy.home.clickableCard
import com.dailydairy.notification.NotificationScheduler
import com.dailydairy.notification.NotificationStore
import com.dailydairy.theme.ThemeViewModel
import com.dailydairy.ui.PasswordField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

private enum class SettingsPage { Menu, Password, Theme, Backup, Notification }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    themeViewModel: ThemeViewModel,
    diaryViewModel: DiaryViewModel = viewModel(),
) {
    var page by rememberSaveable { mutableStateOf(SettingsPage.Menu) }
    val title = when (page) {
        SettingsPage.Menu -> "Ayarlar"
        SettingsPage.Password -> "Günlük şifresi"
        SettingsPage.Theme -> "Tema"
        SettingsPage.Backup -> "Yedek"
        SettingsPage.Notification -> "Bildirimler"
    }
    val back = {
        if (page == SettingsPage.Menu) onBack() else page = SettingsPage.Menu
    }
    BackHandler(onBack = back)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = barColors(),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            when (page) {
                SettingsPage.Menu -> {
                    SettingsRow(
                        title = "Günlük şifresi",
                        subtitle = "Şifre ve parmak izi",
                        icon = Icons.Outlined.Lock,
                        onClick = { page = SettingsPage.Password },
                    )
                    Spacer(Modifier.height(12.dp))
                    SettingsRow(
                        title = "Tema",
                        subtitle = "Açık, karanlık ve palet",
                        icon = Icons.Outlined.Palette,
                        onClick = { page = SettingsPage.Theme },
                    )
                    Spacer(Modifier.height(12.dp))
                    SettingsRow(
                        title = "Yedek",
                        subtitle = "Dışa aktar veya içe al",
                        icon = Icons.Outlined.SaveAlt,
                        onClick = { page = SettingsPage.Backup },
                    )
                    Spacer(Modifier.height(12.dp))
                    SettingsRow(
                        title = "Bildirimler",
                        subtitle = "Günlük hatırlatıcısı",
                        icon = Icons.Outlined.NotificationsNone,
                        onClick = { page = SettingsPage.Notification },
                    )
                }
                SettingsPage.Password -> PasswordSection(diaryViewModel)
                SettingsPage.Theme -> ThemeSection(themeViewModel)
                SettingsPage.Backup -> BackupSection()
                SettingsPage.Notification -> NotificationSection()
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickableCard(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PasswordSection(diaryViewModel: DiaryViewModel) {
    val lock by diaryViewModel.lock.collectAsStateWithLifecycle()
    val fingerprintOn by diaryViewModel.fingerprintEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val fingerprintReady = Biometric.fingerprintAvailable(context)
    var current by rememberSaveable { mutableStateOf("") }
    var next by rememberSaveable { mutableStateOf("") }
    var again by rememberSaveable { mutableStateOf("") }
    var hint by rememberSaveable(lock?.hint) { mutableStateOf(lock?.hint.orEmpty()) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var failed by rememberSaveable { mutableStateOf(false) }
    var unlocked by rememberSaveable { mutableStateOf(false) }
    var nudge by remember { mutableStateOf(false) }
    var confirm by rememberSaveable { mutableStateOf(false) }
    val currentFocus = remember { FocusRequester() }

    LaunchedEffect(current) {
        if (current.isEmpty()) {
            unlocked = false
            return@LaunchedEffect
        }
        delay(250)
        unlocked = withContext(Dispatchers.Default) { diaryViewModel.passwordMatches(current) }
    }

    if (lock == null) {
        Text(
            text = "Şifre henüz yok. Günlük ilk açıldığında kurulur.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    PasswordField(
        value = current,
        onValueChange = { current = it; message = null; failed = false; nudge = false },
        label = { Text("Mevcut şifre") },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(currentFocus),
        isError = failed,
    )
    Spacer(Modifier.height(12.dp))
    PasswordField(
        value = next,
        onValueChange = { next = it; message = null },
        label = { Text("Yeni şifre") },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(12.dp))
    PasswordField(
        value = again,
        onValueChange = { again = it; message = null },
        label = { Text("Yeni şifre tekrar") },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(12.dp))
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = hint,
            onValueChange = { hint = it; message = null },
            modifier = Modifier.fillMaxWidth(),
            enabled = unlocked,
            label = { Text("Yeni ipucu") },
            singleLine = true,
        )
        if (!unlocked) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickableCard(onClick = {
                        currentFocus.requestFocus()
                        nudge = true
                    }),
            )
        }
    }
    if (nudge) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Önce mevcut şifreyi yaz.",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
        )
    }
    Spacer(Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Parmak iziyle aç", style = MaterialTheme.typography.titleMedium)
            Text(
                text = when {
                    !fingerprintReady -> "Bu telefonda parmak izi yok."
                    fingerprintOn -> "Bu telefonda şifre yazmadan açılır."
                    else -> "Açmak için önce mevcut şifreyi yaz."
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = fingerprintOn && fingerprintReady,
            enabled = fingerprintReady && (fingerprintOn || unlocked),
            onCheckedChange = { want ->
                if (!want) {
                    diaryViewModel.setFingerprint(false)
                    return@Switch
                }
                if (!unlocked) return@Switch
                Biometric.askFingerprint(
                    activity,
                    onSuccess = { diaryViewModel.setFingerprint(true) },
                )
            },
        )
    }
    Spacer(Modifier.height(8.dp))
    message?.let {
        Text(
            text = it,
            color = if (failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(8.dp))
    }
    Spacer(Modifier.height(8.dp))
    Button(
        onClick = {
            val problem = when {
                current.isEmpty() -> "Mevcut şifreyi yaz."
                !unlocked -> "Mevcut şifre yanlış."
                next.isNotEmpty() && next.length < 4 -> "Yeni şifre en az 4 karakter olsun."
                next.isNotEmpty() && next == current -> "Yeni şifre eskisiyle aynı. Farklı bir şifre seç."
                next != again -> "Yeni şifreler aynı değil."
                hint.isBlank() -> "İpucu boş kalmasın."
                next.isEmpty() && hint == lock?.hint -> "Değişen bir şey yok."
                else -> null
            }
            if (problem != null) {
                message = problem
                failed = problem.startsWith("Mevcut") || problem.startsWith("Yeni şifre eskisi")
                return@Button
            }
            if (next.isNotEmpty()) {
                confirm = true
                return@Button
            }
            diaryViewModel.changePassword(current, next, hint) { ok ->
                if (ok) {
                    current = ""
                    next = ""
                    again = ""
                    failed = false
                    message = "İpucu değişti."
                } else {
                    failed = true
                    message = "Mevcut şifre yanlış."
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) { Text("Şifreyi değiştir") }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Bu şifre geri gelmez") },
            text = {
                Text("Yeni şifreyi ve ipucunu unutursan günlüğü açmanın yolu kalmaz. Yazdıkların da açılmaz. Emin misin?")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirm = false
                    val chosen = next
                    diaryViewModel.changePassword(current, chosen, hint) { ok ->
                        if (ok) {
                            current = ""
                            next = ""
                            again = ""
                            failed = false
                            message = "Şifre değişti."
                        } else {
                            failed = true
                            message = "Mevcut şifre yanlış."
                        }
                    }
                }) { Text("Eminim, kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) { Text("Geri dön") }
            },
        )
    }
}

@Composable
private fun BackupSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }
    var failed by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<Uri?>(null) }
    var busy by remember { mutableStateOf(false) }
    var kept by remember { mutableStateOf(Backup.hasKept(context)) }

    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            busy = true
            val error = withContext(Dispatchers.IO) { runCatching { Backup.export(context, uri) }.exceptionOrNull() }
            busy = false
            failed = error != null
            message = if (error == null) "Yedek kaydedildi." else "Yedek yazılamadı."
        }
    }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        pending = uri
    }

    Text(
        text = "Günlük, notlar, listeler, filmler, kitaplar, fotoğraflar, tema ve günlük şifresi tek dosyada.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(16.dp))
    Button(
        onClick = { export.launch("dailydiary-yedek.zip") },
        enabled = !busy,
        modifier = Modifier.fillMaxWidth(),
    ) { Text("Dışa aktar") }
    Spacer(Modifier.height(10.dp))
    Button(
        onClick = {
            scope.launch {
                busy = true
                val error = withContext(Dispatchers.IO) { runCatching { Backup.keep(context) }.exceptionOrNull() }
                busy = false
                failed = error != null
                kept = error == null
                message = if (error == null) {
                    "Manuel yedek alındı. Uygulamayı verileri silmeden kaldırırsan yeniden yükleyince geri gelir. Verileri silersen bu yedek de gider."
                } else {
                    "Manuel yedek alınamadı."
                }
            }
        },
        enabled = !busy,
        modifier = Modifier.fillMaxWidth(),
    ) { Text(if (kept) "Manuel yedeği yenile" else "Manuel yedekle") }
    Spacer(Modifier.height(10.dp))
    Button(
        onClick = { import.launch(arrayOf("application/zip", "application/octet-stream")) },
        enabled = !busy,
        modifier = Modifier.fillMaxWidth(),
    ) { Text("İçe al") }
    Spacer(Modifier.height(10.dp))
    Text(
        text = "İçe almak bu telefondakinin üstüne yazar. Geri alınamaz.",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    message?.let {
        Text(
            text = it,
            color = if (failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    if (pending != null) {
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text("Yedek içe alınsın mı?") },
            text = { Text("Bu telefondaki her şeyin yerine geçer.") },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pending ?: return@TextButton
                    pending = null
                    scope.launch {
                        busy = true
                        val error = withContext(Dispatchers.IO) {
                            runCatching { Backup.restore(context, uri) }.exceptionOrNull()
                        }
                        if (error == null) {
                            exitProcess(0)
                        } else {
                            busy = false
                            failed = true
                            message = "Bu dosya yedek değil."
                        }
                    }
                }) { Text("İçe al") }
            },
            dismissButton = {
                TextButton(onClick = { pending = null }) { Text("Vazgeç") }
            },
        )
    }
}

@Composable
private fun NotificationSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val enabledFlow = remember { NotificationStore.observeEnabled(context) }
    val hourFlow = remember { NotificationStore.observeHour(context) }
    val minuteFlow = remember { NotificationStore.observeMinute(context) }

    val enabled by enabledFlow.collectAsStateWithLifecycle(false)
    val savedHour by hourFlow.collectAsStateWithLifecycle(20)
    val savedMinute by minuteFlow.collectAsStateWithLifecycle(0)

    var sliderHour by rememberSaveable { mutableIntStateOf(20) }
    var sliderMinute by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(savedHour, savedMinute) {
        sliderHour = savedHour
        sliderMinute = savedMinute
    }

    val notifPermLauncher = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                scope.launch {
                    NotificationStore.setEnabled(context, true)
                    NotificationScheduler.schedule(context, sliderHour, sliderMinute)
                }
            }
        }
    } else null

    Text(
        text = "O gün hiç günlük yazmadıysan seçtiğin saatte seni hatırlatır.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(20.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Hatırlatıcı", style = MaterialTheme.typography.titleMedium)
            Text(
                text = if (enabled) "Açık" else "Kapalı",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = { want ->
                if (want) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifPermLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        scope.launch {
                            NotificationStore.setEnabled(context, true)
                            NotificationScheduler.schedule(context, sliderHour, sliderMinute)
                        }
                    }
                } else {
                    scope.launch {
                        NotificationStore.setEnabled(context, false)
                        NotificationScheduler.cancel(context)
                    }
                }
            },
        )
    }

    Spacer(Modifier.height(24.dp))

    Text(
        text = "Bildirim saati",
        style = MaterialTheme.typography.titleMedium,
    )
    Spacer(Modifier.height(16.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Saat
        TimeSpinner(
            label = "Saat",
            value = sliderHour,
            max = 23,
            onIncrease = { sliderHour = (sliderHour + 1) % 24 },
            onDecrease = { sliderHour = (sliderHour + 23) % 24 },
            onValueChange = { sliderHour = it },
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        // Dakika
        TimeSpinner(
            label = "Dakika",
            value = sliderMinute,
            max = 59,
            onIncrease = { sliderMinute = (sliderMinute + 5) % 60 },
            onDecrease = { sliderMinute = (sliderMinute + 55) % 60 },
            onValueChange = { sliderMinute = it },
        )
    }

    Spacer(Modifier.height(16.dp))
    Button(
        onClick = {
            scope.launch {
                NotificationStore.setTime(context, sliderHour, sliderMinute)
                if (enabled) {
                    NotificationScheduler.schedule(context, sliderHour, sliderMinute)
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) { Text("Saati kaydet") }
}

@Composable
private fun TimeSpinner(
    label: String,
    value: Int,
    max: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onValueChange: (Int) -> Unit,
) {
    var editing by remember { mutableStateOf(false) }
    var text by remember(value) { mutableStateOf("%02d".format(value)) }

    fun commit() {
        val parsed = text.toIntOrNull()
        if (parsed != null && parsed in 0..max) {
            onValueChange(parsed)
        } else {
            text = "%02d".format(value)
        }
        editing = false
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FilledTonalIconButton(onClick = onIncrease) {
            Icon(Icons.Outlined.Add, contentDescription = "Artır")
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp, 64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (editing) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickableCard(onClick = {
                    editing = true
                    text = "%02d".format(value)
                }),
        ) {
            if (editing) {
                BasicTextField(
                    value = text,
                    onValueChange = { input ->
                        if (input.length <= 2 && input.all { it.isDigit() }) text = input
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { commit() }),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier
                        .width(72.dp)
                        .onFocusChanged { if (!it.isFocused) commit() },
                )
            } else {
                Text(
                    text = "%02d".format(value),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        FilledTonalIconButton(onClick = onDecrease) {
            Icon(Icons.Outlined.Remove, contentDescription = "Azalt")
        }
    }
}
