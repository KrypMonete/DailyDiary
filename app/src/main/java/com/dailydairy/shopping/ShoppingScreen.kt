package com.dailydairy.shopping

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Icecream
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.LocalGroceryStore
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.outlined.LunchDining
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.dailydairy.diary.DiaryImages
import com.dailydairy.diary.barColors
import com.dailydairy.home.clickableCard
import kotlinx.coroutines.launch

@Composable
fun ShoppingRoute(onBack: () -> Unit, viewModel: ShoppingViewModel = viewModel()) {
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    var openId by remember { mutableStateOf<Long?>(null) }
    var lastKind by remember { mutableStateOf(LIST_PLAIN) }
    val open = lists.find { it.list.id == openId }
    if (open != null) {
        ShoppingDetail(
            card = open,
            items = viewModel.items(open.list.id).collectAsState(emptyList()).value,
            onBack = { openId = null },
            onAdd = { viewModel.addItem(open.list.id, it, open.list.kind) },
            onToggle = viewModel::toggle,
            onCount = viewModel::setCount,
            onUnit = viewModel::cycleUnit,
            onPrice = viewModel::setPrice,
            onRewrite = { item, raw -> viewModel.rewriteItem(item, raw, open.list.kind) },
            onDeleteItems = viewModel::deleteItems,
            onClearDone = { viewModel.clearDone(open.list.id) },
            onDeleteList = {
                viewModel.deleteList(open.list.id)
                openId = null
            },
            onIcon = { viewModel.setIcon(open.list.id, it) },
            onRename = { viewModel.rename(open.list.id, it) },
            onSetCover = { uri -> viewModel.setCoverImage(open.list.id, uri) },
            onRemoveCover = { viewModel.removeCoverImage(open.list.id) },
        )
        return
    }
    ShoppingLists(
        lists = lists,
        items = allItems,
        initialKind = lastKind,
        onBack = onBack,
        onOpen = {
            lastKind = it.list.kind.ifBlank { LIST_PLAIN }
            openId = it.list.id
        },
        onCreate = { name, kind -> viewModel.createList(name, -1, kind) },
        onDelete = viewModel::deleteLists,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingLists(
    lists: List<ShoppingListCard>,
    items: List<ShoppingItem>,
    initialKind: String,
    onBack: () -> Unit,
    onOpen: (ShoppingListCard) -> Unit,
    onCreate: (String, String) -> Unit,
    onDelete: (Collection<Long>) -> Unit,
) {
    var askName by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(LIST_PLAIN) }
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var picked by remember { mutableStateOf(setOf<Long>()) }
    var askDelete by remember { mutableStateOf(false) }
    var shelf by remember { mutableStateOf(LIST_PLAIN) }
    val kinds = listOf(LIST_PLAIN, LIST_SHOP, LIST_RECIPE)
    val kindLabels = listOf("Liste", "Alışveriş", "Tarif")
    val initialPage = kinds.indexOf(initialKind).coerceAtLeast(0)
    val pager = rememberPagerState(initialPage = initialPage, pageCount = { kinds.size })
    val scope = rememberCoroutineScope()
    LaunchedEffect(pager) {
        snapshotFlow { pager.currentPage }.collect { shelf = kinds[it] }
    }
    val needle = query.trim()
    val shelved = remember(lists, shelf, searching) {
        if (searching) lists else lists.filter { it.list.kind.ifBlank { LIST_PLAIN } == shelf }
    }
    val shown = remember(shelved, items, needle) {
        if (needle.isEmpty()) shelved
        else shelved.filter { card ->
            card.list.kindLabel().contains(needle, ignoreCase = true) ||
                card.list.name.contains(needle, ignoreCase = true) ||
                items.any { it.listId == card.list.id && it.name.contains(needle, ignoreCase = true) }
        }
    }
    val goBack = {
        if (picked.isNotEmpty()) picked = emptySet() else onBack()
    }
    BackHandler(onBack = goBack)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    when {
                        picked.isNotEmpty() -> Text(
                            "${picked.size} seçildi",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        searching -> OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Tür, liste veya ürün") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { }),
                        )
                        else -> Text("Listeler", style = MaterialTheme.typography.headlineMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    if (picked.isNotEmpty()) {
                        val visible = shown.map { it.list.id }.toSet()
                        val allOn = visible.isNotEmpty() && visible.all { it in picked }
                        IconButton(onClick = {
                            picked = if (allOn) emptySet() else visible
                        }) {
                            Icon(
                                Icons.Outlined.SelectAll,
                                contentDescription = "Hepsini seç",
                                tint = if (allOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        IconButton(onClick = { askDelete = true }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Seçilenleri sil")
                        }
                    } else {
                        IconButton(onClick = {
                            if (searching) {
                                searching = false
                                query = ""
                            } else {
                                searching = true
                                picked = emptySet()
                            }
                        }) {
                            Icon(
                                if (searching) Icons.Outlined.Close else Icons.Outlined.Search,
                                contentDescription = if (searching) "Aramayı kapat" else "Ara",
                            )
                        }
                    }
                },
                colors = barColors(),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (!searching && picked.isEmpty()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        kindLabels.forEachIndexed { index, label ->
                            KindChip(label, pager.currentPage == index) {
                                scope.launch { pager.animateScrollToPage(index) }
                            }
                        }
                    }
                }
                HorizontalPager(state = pager, modifier = Modifier.fillMaxSize(), userScrollEnabled = !searching && picked.isEmpty()) { page ->
                    val pageKind = kinds[page]
                    val pageLists = if (searching) shown else shown.filter { it.list.kind.ifBlank { LIST_PLAIN } == pageKind }
                    when {
                        pageLists.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                            ) {
                                Text(
                                    text = if (needle.isEmpty()) "Bu tarafta liste yok." else "“$needle” için sonuç yok.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                item { Spacer(Modifier.height(4.dp)) }
                                items(pageLists, key = { it.list.id }) { card ->
                                    val hit = if (needle.isEmpty()) {
                                        null
                                    } else {
                                        items.firstOrNull {
                                            it.listId == card.list.id && it.name.contains(needle, ignoreCase = true)
                                        }?.name
                                    }
                                    ListRow(
                                        card,
                                        hint = hit,
                                        selected = card.list.id in picked,
                                        selecting = picked.isNotEmpty(),
                                        onOpen = {
                                            if (picked.isNotEmpty()) {
                                                picked = if (card.list.id in picked) picked - card.list.id else picked + card.list.id
                                            } else {
                                                onOpen(card)
                                            }
                                        },
                                        onHold = {
                                            picked = if (card.list.id in picked) picked - card.list.id else picked + card.list.id
                                        },
                                    )
                                }
                                item { Spacer(Modifier.height(96.dp)) }
                            }
                        }
                    }
                }
            }
            if (picked.isEmpty() && !searching) {
                FloatingActionButton(
                    onClick = {
                        kind = kinds[pager.currentPage]
                        askName = true
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 20.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Yeni liste")
                }
            }
        }
    }
    if (askName) {
        AlertDialog(
            onDismissRequest = { askName = false },
            title = { Text("Yeni liste") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        singleLine = true,
                        label = { Text("Liste adı") },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        KindChip("Liste", kind == LIST_PLAIN) { kind = LIST_PLAIN }
                        KindChip("Alışveriş", kind == LIST_SHOP) { kind = LIST_SHOP }
                        KindChip("Tarif", kind == LIST_RECIPE) { kind = LIST_RECIPE }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCreate(name, kind)
                        val page = kinds.indexOf(kind)
                        if (page >= 0) scope.launch { pager.animateScrollToPage(page) }
                        name = ""
                        askName = false
                    },
                    enabled = name.isNotBlank(),
                ) { Text("Oluştur") }
            },
            dismissButton = {
                TextButton(onClick = {
                    name = ""
                    askName = false
                }) { Text("Vazgeç") }
            },
        )
    }
    if (askDelete && picked.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { askDelete = false },
            title = { Text(if (picked.size == 1) "Liste silinsin mi?" else "${picked.size} liste silinsin mi?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(picked)
                    picked = emptySet()
                    askDelete = false
                }) { Text("Sil") }
            },
            dismissButton = {
                TextButton(onClick = { askDelete = false }) { Text("Vazgeç") }
            },
        )
    }
}

@Composable
private fun ListRow(
    card: ShoppingListCard,
    hint: String? = null,
    selected: Boolean = false,
    selecting: Boolean = false,
    onOpen: () -> Unit,
    onHold: () -> Unit = {},
) {
    val tint = MaterialTheme.colorScheme.primary
    val line = when {
        hint != null -> hint
        card.total == 0 -> "Henüz bir şey yok"
        card.list.kind == LIST_SHOP && card.remaining == 0 -> "Sepet doldu"
        card.remaining == 0 -> "Hepsi bitti"
        else -> "${card.remaining} kaldı"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                else MaterialTheme.colorScheme.surface,
            )
            .clickableCard(onLongClick = onHold, onClick = onOpen)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BagRing(
            tint = tint,
            total = card.total,
            remaining = card.remaining,
            icon = listIcon(card.list.tint),
        )
        Column(modifier = Modifier.padding(start = 14.dp)) {
            Text(
                text = card.list.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    hint != null -> tint
                    card.total > 0 && card.remaining == 0 -> tint
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (hint != null || (card.remaining == 0 && card.total > 0)) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                },
            )
        }
        if (selecting) {
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BagRing(
    tint: Color,
    total: Int,
    remaining: Int,
    icon: ImageVector?,
    modifier: Modifier = Modifier,
) {
    val done = (total - remaining).coerceAtLeast(0)
    val target = if (total == 0) 0f else done / total.toFloat()
    val sweep = remember { Animatable(target) }
    LaunchedEffect(target) { sweep.animateTo(target, spring(dampingRatio = 0.62f, stiffness = 280f)) }
    val track = MaterialTheme.colorScheme.surfaceVariant
    Box(modifier.size(52.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
            drawArc(track, 0f, 360f, false, style = stroke)
            if (sweep.value > 0f) {
                drawArc(tint, -90f, 360f * sweep.value, false, style = stroke)
            }
        }
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingDetail(
    card: ShoppingListCard,
    items: List<ShoppingItem>,
    onBack: () -> Unit,
    onAdd: (String) -> Unit,
    onToggle: (ShoppingItem) -> Unit,
    onCount: (ShoppingItem, Int) -> Unit,
    onUnit: (ShoppingItem) -> Unit,
    onPrice: (ShoppingItem, Int) -> Unit,
    onRewrite: (ShoppingItem, String) -> Unit,
    onDeleteItems: (Collection<ShoppingItem>) -> Unit,
    onClearDone: () -> Unit,
    onDeleteList: () -> Unit,
    onIcon: (Int) -> Unit,
    onRename: (String) -> Unit,
    onSetCover: (Uri) -> Unit,
    onRemoveCover: () -> Unit,
) {
    var draft by remember { mutableStateOf("") }
    var pricing by remember { mutableStateOf<ShoppingItem?>(null) }
    var priceDraft by remember { mutableStateOf("") }
    var askDelete by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf(false) }
    var picked by remember { mutableStateOf(setOf<Long>()) }
    var editing by remember { mutableStateOf<ShoppingItem?>(null) }
    var editDraft by remember { mutableStateOf("") }
    var renaming by remember { mutableStateOf(false) }
    var nameDraft by remember(card.list.name) { mutableStateOf(card.list.name) }
    val kind = card.list.kind
    val shop = kind == LIST_SHOP
    val recipe = kind == LIST_RECIPE
    val context = androidx.compose.ui.platform.LocalContext.current
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onSetCover(it) }
    }
    val open = if (recipe) items else items.filter { !it.done }
    val done = if (recipe) emptyList() else items.filter { it.done }
    val tint = MaterialTheme.colorScheme.primary
    val full = card.total > 0 && card.remaining == 0
    val goBack = {
        if (picked.isNotEmpty()) picked = emptySet() else onBack()
    }
    BackHandler(onBack = goBack)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    if (picked.isNotEmpty()) {
                        Text("${picked.size} seçildi", style = MaterialTheme.typography.headlineMedium)
                    } else {
                        Text(
                            card.list.name,
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickableCard { renaming = true },
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    if (picked.isNotEmpty()) {
                        val visible = items.map { it.id }.toSet()
                        val allOn = visible.isNotEmpty() && visible.all { it in picked }
                        IconButton(onClick = {
                            picked = if (allOn) emptySet() else visible
                        }) {
                            Icon(
                                Icons.Outlined.SelectAll,
                                contentDescription = "Hepsini seç",
                                tint = if (allOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        if (picked.size == 1) {
                            IconButton(onClick = {
                                val item = items.find { it.id in picked } ?: return@IconButton
                                editDraft = item.editText(recipe, shop)
                                editing = item
                            }) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Düzenle")
                            }
                        }
                        IconButton(onClick = {
                            onDeleteItems(items.filter { it.id in picked })
                            picked = emptySet()
                        }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Seçilenleri sil")
                        }
                    } else {
                        IconButton(onClick = { askDelete = true }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Listeyi sil")
                        }
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
                .imePadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val hover = remember { MutableInteractionSource() }
                val hovered by hover.collectIsHoveredAsState()
                LaunchedEffect(hovered) { if (hovered) picking = true }
                BagRing(
                    tint = tint,
                    total = card.total,
                    remaining = card.remaining,
                    icon = if (card.list.tint < 0) Icons.Outlined.Edit else listIcon(card.list.tint),
                    modifier = Modifier
                        .size(64.dp)
                        .hoverable(hover)
                        .clickableCard(onClick = { picking = !picking }),
                )
                Column(modifier = Modifier.padding(start = 14.dp)) {
                    AnimatedContent(
                        targetState = full,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "bag",
                    ) { filled ->
                        Text(
                            text = when {
                                recipe -> if (card.total == 0) "Tarif boş" else "${card.total} malzeme"
                                full && shop -> "Sepet doldu"
                                full -> "Hepsi bitti"
                                card.total == 0 -> "Liste boş"
                                else -> "${card.remaining} kaldı"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (filled) tint else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = when {
                            card.total == 0 -> if (recipe) "Malzeme ya da adım yaz" else "Alta bir madde yaz"
                            recipe -> "${items.count { it.isStep() }} adım"
                            shop -> "${card.total - card.remaining} / ${card.total}  ·  ${kurusText(items.openTotal().toInt())}"
                            else -> "${card.total - card.remaining} / ${card.total}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (picking) {
                IconChoices(card.list.tint) {
                    onIcon(it)
                    picking = false
                }
            }
            if (recipe) {
                val coverFile = remember(card.list.coverImage) {
                    card.list.coverImage.takeIf { it.isNotEmpty() }
                        ?.let { DiaryImages.file(context, it, "recipe_covers") }
                }
                if (coverFile != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(18.dp)),
                    ) {
                        AsyncImage(
                            model = coverFile,
                            contentDescription = "Tarif görseli",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                            contentScale = ContentScale.Crop,
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                    .clickableCard(onClick = { coverPicker.launch("image/*") })
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Text("Değiştir", style = MaterialTheme.typography.labelMedium)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                    .clickableCard(onClick = onRemoveCover)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Text("Kaldır", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickableCard(onClick = { coverPicker.launch("image/*") })
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            Icons.Outlined.AddPhotoAlternate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            "Tarife görsel ekle",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            if (items.isEmpty()) {
                Text(
                    text = if (recipe) "Tarif boş. Malzeme ya da adım yaz." else "Liste boş. Alta bir madde yaz.",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.weight(1f))
            } else {
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    items(open, key = { it.id }) { item ->
                        val order = if (recipe) open.indexOfFirst { row -> row.id == item.id } + 1 else 0
                        ItemRow(
                            item,
                            tint,
                            selecting = picked.isNotEmpty(),
                            selected = item.id in picked,
                            onToggle = { onToggle(item) },
                            onCount = { onCount(item, it) },
                            onUnit = { onUnit(item) },
                            onPrice = {
                                pricing = item
                                priceDraft = if (item.price == 0) "" else item.moneyText().removeSuffix(" ₺")
                            },
                            measured = recipe && !item.isStep(),
                            numbered = order,
                            shop = shop,
                            onHold = {
                                picked = if (item.id in picked) picked - item.id else picked + item.id
                            },
                            onPick = {
                                picked = if (item.id in picked) picked - item.id else picked + item.id
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                    if (done.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = if (shop) "Alınanlar" else "Bitenler",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                TextButton(onClick = onClearDone) { Text("Temizle") }
                            }
                        }
                        items(done, key = { it.id }) { item ->
                            ItemRow(
                                item,
                                tint,
                                selecting = picked.isNotEmpty(),
                                selected = item.id in picked,
                                onToggle = { onToggle(item) },
                                onCount = { onCount(item, it) },
                                onUnit = { onUnit(item) },
                                onPrice = {
                                    pricing = item
                                    priceDraft = if (item.price == 0) "" else item.moneyText().removeSuffix(" ₺")
                                },
                                measured = recipe && !item.isStep(),
                                numbered = 0,
                                shop = shop,
                                onHold = {
                                    picked = if (item.id in picked) picked - item.id else picked + item.id
                                },
                                onPick = {
                                    picked = if (item.id in picked) picked - item.id else picked + item.id
                                },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text(if (recipe) "Malzeme veya adım" else "Madde") },
                )
                IconButton(
                    onClick = {
                        onAdd(draft)
                        draft = ""
                    },
                    enabled = draft.isNotBlank(),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Ekle")
                }
            }
        }
    }
    if (renaming) {
        AlertDialog(
            onDismissRequest = { renaming = false },
            title = { Text("Liste adı") },
            text = {
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = { nameDraft = it },
                    singleLine = true,
                    label = { Text("Ad") },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRename(nameDraft)
                        renaming = false
                    },
                    enabled = nameDraft.isNotBlank(),
                ) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { renaming = false }) { Text("Vazgeç") }
            },
        )
    }
    if (askDelete) {
        AlertDialog(
            onDismissRequest = { askDelete = false },
            title = { Text("Liste silinsin mi?") },
            text = { Text(card.list.name) },
            confirmButton = {
                TextButton(onClick = onDeleteList) { Text("Sil") }
            },
            dismissButton = {
                TextButton(onClick = { askDelete = false }) { Text("Vazgeç") }
            },
        )
    }
    editing?.let { item ->
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Düzenle") },
            text = {
                OutlinedTextField(
                    value = editDraft,
                    onValueChange = { editDraft = it },
                    singleLine = true,
                    label = { Text(if (recipe) "Malzeme" else "Madde") },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRewrite(item, editDraft)
                        editing = null
                        picked = emptySet()
                    },
                    enabled = editDraft.isNotBlank(),
                ) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) { Text("Vazgeç") }
            },
        )
    }
    pricing?.let { item ->
        val parsed = parseKurus(priceDraft)
        AlertDialog(
            onDismissRequest = { pricing = null },
            title = { Text("Fiyat") },
            text = {
                OutlinedTextField(
                    value = priceDraft,
                    onValueChange = { priceDraft = it },
                    singleLine = true,
                    label = { Text("Tutar") },
                    suffix = { Text("₺") },
                    isError = parsed == null,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        parsed?.let { onPrice(item, it) }
                        pricing = null
                    },
                    enabled = parsed != null,
                ) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { pricing = null }) { Text("Vazgeç") }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemRow(
    item: ShoppingItem,
    tint: Color,
    selecting: Boolean,
    selected: Boolean,
    measured: Boolean,
    numbered: Int = 0,
    shop: Boolean,
    onToggle: () -> Unit,
    onCount: (Int) -> Unit,
    onUnit: () -> Unit,
    onPrice: () -> Unit,
    onHold: () -> Unit,
    onPick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fill by animateFloatAsState(
        targetValue = if (item.done || selected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
        label = "check",
    )
    val empty = MaterialTheme.colorScheme.surfaceVariant
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                else MaterialTheme.colorScheme.surface,
            )
            .clickableCard(onLongClick = onHold) {
                if (selecting) onPick() else if (numbered == 0) onToggle()
            }
            .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (numbered > 0) {
            Text(
                text = "$numbered",
                modifier = Modifier.width(28.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = tint,
            )
        } else {
            Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(empty),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = fill
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    }
                    .background(tint),
            )
            if (fill > 0.55f) {
                Icon(
                    Icons.Outlined.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp),
                )
            }
            }
        }
        val nameColor = if (item.done) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurface
        }
        val note = item.name.substringAfterLast(" (", "").substringBeforeLast(")")
        val hasNote = !measured && item.name.endsWith(")") && note.isNotEmpty() && item.name.contains(" (")
        Text(
            text = buildAnnotatedString {
                if (hasNote) {
                    append(item.name.substringBeforeLast(" ("))
                    withStyle(SpanStyle(color = tint)) {
                        append(" ($note)")
                    }
                } else {
                    append(item.name)
                }
            },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (item.done) FontWeight.Normal else FontWeight.Medium,
            textDecoration = if (item.done) TextDecoration.LineThrough else null,
            color = nameColor,
        )
        if (measured) {
            CountButton(Icons.Outlined.Remove, "Azalt", !selecting && item.count > 1) {
                onCount(item.count - item.measureStep())
            }
            Text(
                text = item.amountText(),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickableCard(onClick = { if (!selecting && !shop) onUnit() })
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            CountButton(Icons.Outlined.Add, "Arttır", !selecting && item.count < item.measureMax()) {
                onCount(item.count + item.measureStep())
            }
        }
        if (shop) {
            Text(
                text = if (item.price == 0) "Fiyat" else item.moneyText(),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickableCard(onClick = { if (!selecting) onPrice() })
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                color = if (item.price == 0) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

@Composable
private fun KindChip(label: String, on: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickableCard(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.labelLarge,
    )
}

@Composable
private fun CountButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(36.dp)) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            },
            modifier = Modifier.size(18.dp),
        )
    }
}

private val listIcons = listOf(
    Icons.Outlined.ShoppingBag,
    Icons.Outlined.LocalGroceryStore,
    Icons.Outlined.Restaurant,
    Icons.Outlined.LunchDining,
    Icons.Outlined.BakeryDining,
    Icons.Outlined.Cake,
    Icons.Outlined.Icecream,
    Icons.Outlined.Coffee,
    Icons.Outlined.LocalFlorist,
    Icons.Outlined.Spa,
    Icons.Outlined.Home,
    Icons.Outlined.Bedtime,
    Icons.Outlined.Pets,
    Icons.Outlined.Favorite,
    Icons.Outlined.Checkroom,
    Icons.Outlined.FitnessCenter,
    Icons.Outlined.LocalPharmacy,
    Icons.Outlined.School,
    Icons.Outlined.Book,
    Icons.Outlined.Work,
    Icons.Outlined.Flight,
    Icons.Outlined.DirectionsCar,
    Icons.Outlined.Movie,
    Icons.Outlined.MusicNote,
    Icons.Outlined.Brush,
)

private fun listIcon(index: Int): ImageVector? =
    if (index < 0) null else listIcons.getOrNull(index)

@Composable
private fun IconChoices(selected: Int, onPick: (Int) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(listIcons.size) { index ->
            val icon = listIcons[index]
            val on = index == selected
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (on) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface,
                    )
                    .clickableCard(onClick = { onPick(index) }),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
