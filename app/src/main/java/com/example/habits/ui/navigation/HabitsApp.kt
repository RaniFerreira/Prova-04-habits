package com.example.habits.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.habits.AppContainer
import com.example.habits.data.model.AuthUser
import com.example.habits.data.repository.AuthRepository
import com.example.habits.ui.auth.AuthViewModel
import com.example.habits.ui.auth.LoginRoute
import com.example.habits.ui.auth.RegisterRoute
import com.example.habits.ui.habits.detail.HabitDetailRoute
import com.example.habits.ui.habits.detail.HabitDetailViewModel
import com.example.habits.ui.habits.form.HabitFormRoute
import com.example.habits.ui.habits.form.HabitFormViewModel
import com.example.habits.ui.habits.list.HabitListRoute
import com.example.habits.ui.habits.list.HabitListViewModel
import com.example.habits.ui.home.HomeRoute
import com.example.habits.ui.home.HomeViewModel
import com.example.habits.ui.profile.ProfileRoute
import com.example.habits.ui.profile.ProfileViewModel
import com.example.habits.ui.stats.StatsRoute
import com.example.habits.ui.stats.StatsViewModel
import com.example.habits.ui.theme.AppBackground
import com.example.habits.ui.theme.CardWhite
import com.example.habits.ui.theme.Purple100
import com.example.habits.ui.theme.Purple600
import com.example.habits.ui.theme.TextPrimary
import com.example.habits.ui.theme.TextSecondary

/** ViewModel de sessão: expõe o usuário logado para escolher qual fluxo exibir. */
class SessionViewModel(authRepository: AuthRepository) : ViewModel() {
    val currentUser = authRepository.currentUser

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { SessionViewModel(container.authRepository) }
        }
    }
}

private object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val HABITS = "habits"
    const val STATS = "stats"
    const val PROFILE = "profile"
    const val FORM = "habit/form"
    const val DETAIL = "habit/detail"
    const val ARG_ID = "id"

    fun form(id: String? = null) = if (id == null) FORM else "$FORM?$ARG_ID=$id"
    fun detail(id: String) = "$DETAIL/$id"
}

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    HOME(Routes.HOME, "Início", Icons.Outlined.Home),
    HABITS(Routes.HABITS, "Hábitos", Icons.Outlined.Checklist),
    STATS(Routes.STATS, "Estatísticas", Icons.Outlined.BarChart),
    PROFILE(Routes.PROFILE, "Perfil", Icons.Outlined.Person),
}

/**
 * Raiz da UI. Sem usuário -> fluxo de autenticação; com usuário -> gerenciador de hábitos.
 * Como [SessionViewModel.currentUser] é observável, o redirecionamento após login,
 * cadastro ou logout acontece automaticamente.
 */
@Composable
fun HabitsApp(container: AppContainer) {
    val session: SessionViewModel = viewModel(factory = SessionViewModel.factory(container))
    val user by session.currentUser.collectAsStateWithLifecycle()
    val current = user
    if (current == null) {
        AuthNavHost(container)
    } else {
        key(current.uid) { MainNavHost(container, current) }
    }
}

@Composable
private fun AuthNavHost(container: AppContainer) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginRoute(
                viewModel = viewModel(factory = AuthViewModel.factory(container)),
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
            )
        }
        composable(Routes.REGISTER) {
            RegisterRoute(
                viewModel = viewModel(factory = AuthViewModel.factory(container)),
                onNavigateToLogin = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun MainNavHost(container: AppContainer, user: AuthUser) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val context = LocalContext.current

    Scaffold(
        containerColor = AppBackground,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (Tab.entries.any { it.route == currentRoute }) {
                BottomBar(currentRoute) { tab -> navController.navigateToTab(tab.route) }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
        ) {
            composable(Routes.HOME) {
                HomeRoute(
                    viewModel = viewModel(factory = HomeViewModel.factory(container, user.uid)),
                    onOpenHabit = { navController.navigate(Routes.detail(it)) },
                    onAddHabit = { navController.navigate(Routes.form()) },
                    onSeeAll = { navController.navigateToTab(Routes.HABITS) },
                    onOpenProfile = { navController.navigateToTab(Routes.PROFILE) },
                    onOpenNotifications = {
                        Toast.makeText(context, "Lembretes diários ativos.", Toast.LENGTH_SHORT).show()
                    },
                )
            }
            composable(Routes.HABITS) {
                HabitListRoute(
                    viewModel = viewModel(factory = HabitListViewModel.factory(container, user.uid)),
                    onOpenHabit = { navController.navigate(Routes.detail(it)) },
                    onAddHabit = { navController.navigate(Routes.form()) },
                    onOpenFilters = {
                        Toast.makeText(context, "Use as categorias abaixo para filtrar.", Toast.LENGTH_SHORT).show()
                    },
                )
            }
            composable(Routes.STATS) {
                StatsRoute(viewModel(factory = StatsViewModel.factory(container, user.uid)))
            }
            composable(Routes.PROFILE) {
                ProfileRoute(viewModel(factory = ProfileViewModel.factory(container, user.uid)))
            }
            composable(
                route = "${Routes.FORM}?${Routes.ARG_ID}={${Routes.ARG_ID}}",
                arguments = listOf(navArgument(Routes.ARG_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }),
            ) { entry ->
                val habitId = entry.arguments?.getString(Routes.ARG_ID)
                HabitFormRoute(
                    viewModel = viewModel(factory = HabitFormViewModel.factory(container, user.uid, habitId)),
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = "${Routes.DETAIL}/{${Routes.ARG_ID}}",
                arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.StringType }),
            ) { entry ->
                val habitId = requireNotNull(entry.arguments?.getString(Routes.ARG_ID))
                HabitDetailRoute(
                    viewModel = viewModel(factory = HabitDetailViewModel.factory(container, user.uid, habitId)),
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.form(it)) },
                )
            }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Barra inferior do protótipo: fundo branco com cantos superiores arredondados e "pílula" no item ativo. */
@Composable
private fun BottomBar(currentRoute: String?, onSelect: (Tab) -> Unit) {
    Surface(
        color = CardWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        shadowElevation = 12.dp,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            Tab.entries.forEach { tab ->
                val selected = tab.route == currentRoute
                Column(
                    Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(tab) },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .width(58.dp)
                            .height(32.dp)
                            .background(if (selected) Purple100 else CardWhite, RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(tab.icon, contentDescription = null, tint = if (selected) Purple600 else TextPrimary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        tab.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selected) Purple600 else TextSecondary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
