package com.dailydairy

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dailydairy.diary.DiaryRoute
import com.dailydairy.diary.DiaryViewModel
import com.dailydairy.home.HomeScreen
import com.dailydairy.settings.SettingsScreen
import com.dailydairy.notes.NotesRoute
import com.dailydairy.shopping.ShoppingRoute
import com.dailydairy.theme.AppTheme
import com.dailydairy.theme.ThemeViewModel
import com.dailydairy.watch.WatchRoute
import com.dailydairy.books.BooksRoute

object Routes {
    const val Home = "home"
    const val Diary = "diary"
    const val Notes = "notes"
    const val Shopping = "shopping"
    const val Watched = "watched"
    const val Books = "books"
    const val Settings = "settings"
}

@Composable
fun DailyDiaryApp(
    themeViewModel: ThemeViewModel = viewModel(),
    diaryViewModel: DiaryViewModel = viewModel(),
) {
    val palette by themeViewModel.palette.collectAsStateWithLifecycle()
    val appearance by themeViewModel.appearance.collectAsStateWithLifecycle()
    val nav = rememberNavController()

    AppTheme(palette = palette, appearance = appearance) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavHost(navController = nav, startDestination = Routes.Home) {
                composable(Routes.Home) {
                    HomeScreen(onOpen = { nav.navigate(it) })
                }
                composable(Routes.Diary) {
                    DiaryRoute(
                        onBack = { nav.backHome() },
                        viewModel = diaryViewModel,
                    )
                }
                composable(Routes.Notes) {
                    NotesRoute(onBack = { nav.backHome() })
                }
                composable(Routes.Shopping) {
                    ShoppingRoute(onBack = { nav.backHome() })
                }
                composable(Routes.Watched) {
                    WatchRoute(onBack = { nav.backHome() })
                }
                composable(Routes.Books) {
                    BooksRoute(onBack = { nav.backHome() })
                }
                composable(Routes.Settings) {
                    SettingsScreen(
                        onBack = { nav.backHome() },
                        themeViewModel = themeViewModel,
                        diaryViewModel = diaryViewModel,
                    )
                }
            }
        }
    }
}

private fun NavHostController.backHome() {
    if (previousBackStackEntry != null) popBackStack()
}
