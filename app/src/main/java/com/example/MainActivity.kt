package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.data.ShinobiDatabase
import com.example.data.ShinobiRepository
import com.example.feature.auth.AuthUiState
import com.example.feature.auth.AuthViewModel
import com.example.feature.auth.LoginScreen
import com.example.feature.dsaprojects.DsaProjectsScreen
import com.example.feature.dsaprojects.DsaProjectsSection
import com.example.feature.dsaprojects.DsaProjectsViewModel
import com.example.feature.focus.FocusScreen
import com.example.feature.focus.FocusViewModel
import com.example.feature.habits.HabitScreen
import com.example.feature.habits.HabitViewModel
import com.example.feature.hackathons.HackathonScreen
import com.example.feature.hackathons.HackathonViewModel
import com.example.feature.home.HomeScreen
import com.example.feature.home.HomeViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ShinobiRed
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment

enum class ShinobiTab(val title: String, val icon: ImageVector, val tag: String) {
  HOME("Home", Icons.Default.Dashboard, "tab_home"),
  HACKATHONS("Hackathons", Icons.Default.EmojiEvents, "tab_hackathons"),
  HABITS("Habits", Icons.Default.Repeat, "tab_habits"),
  TIMER("Timer", Icons.Default.HourglassBottom, "tab_timer"),
  DSA_PROJECTS("DSA/Projects", Icons.Default.Code, "tab_dsa_projects"),
}

class MainActivity : ComponentActivity() {
  private val repository by lazy {
    ShinobiRepository(ShinobiDatabase.getInstance(applicationContext))
  }

  private val authViewModel: AuthViewModel by viewModels {
    AuthViewModel.provideFactory(applicationContext)
  }

  private val homeViewModel: HomeViewModel by viewModels {
    HomeViewModel.provideFactory(repository)
  }

  private val hackathonViewModel: HackathonViewModel by viewModels {
    HackathonViewModel.provideFactory(repository)
  }

  private val habitViewModel: HabitViewModel by viewModels {
    HabitViewModel.provideFactory(repository)
  }

  private val focusViewModel: FocusViewModel by viewModels {
    FocusViewModel.provideFactory(repository)
  }

  private val dsaProjectsViewModel: DsaProjectsViewModel by viewModels {
    DsaProjectsViewModel.provideFactory(repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val authState by authViewModel.uiState.collectAsState()

        when (val state = authState) {
          is AuthUiState.Loading -> {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(),
              contentAlignment = Alignment.Center
            ) {
              CircularProgressIndicator(color = ShinobiRed)
            }
          }

          is AuthUiState.Unauthenticated, is AuthUiState.Error -> {
            LoginScreen(viewModel = authViewModel)
          }

          is AuthUiState.Authenticated -> {
            var selectedTabIndex by remember { mutableIntStateOf(0) } // Home is the start destination

            Scaffold(
              modifier = Modifier.fillMaxSize(),
              bottomBar = {
                NavigationBar(
                  modifier = Modifier.testTag("main_navigation_bar")
                ) {
                  ShinobiTab.values().forEachIndexed { index, tab ->
                    NavigationBarItem(
                      selected = selectedTabIndex == index,
                      onClick = { selectedTabIndex = index },
                      icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                      label = { Text(tab.title) },
                      modifier = Modifier.testTag(tab.tag)
                    )
                  }
                }
              }
            ) { innerPadding ->
              Surface(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
              ) {
                when (selectedTabIndex) {
                  0 -> HomeScreen(
                    viewModel = homeViewModel,
                    currentUser = state.user,
                    onSignOut = { authViewModel.signOut(this@MainActivity) },
                    onNavigateToHackathons = { selectedTabIndex = 1 },
                    onNavigateToHabits = { selectedTabIndex = 2 },
                    onNavigateToTimer = { selectedTabIndex = 3 },
                    onNavigateToDsa = {
                      dsaProjectsViewModel.setSection(DsaProjectsSection.DSA)
                      selectedTabIndex = 4
                    },
                    onNavigateToProjects = {
                      dsaProjectsViewModel.setSection(DsaProjectsSection.PROJECTS)
                      selectedTabIndex = 4
                    }
                  )
                  1 -> HackathonScreen(viewModel = hackathonViewModel)
                  2 -> HabitScreen(viewModel = habitViewModel)
                  3 -> FocusScreen(viewModel = focusViewModel)
                  4 -> DsaProjectsScreen(viewModel = dsaProjectsViewModel)
                }
              }
            }
          }
        }
      }
    }
  }
}


