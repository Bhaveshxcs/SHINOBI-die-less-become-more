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
import com.example.feature.focus.FocusScreen
import com.example.feature.focus.FocusViewModel
import com.example.feature.habits.HabitScreen
import com.example.feature.habits.HabitViewModel
import com.example.feature.hackathons.HackathonScreen
import com.example.feature.hackathons.HackathonViewModel
import com.example.ui.theme.MyApplicationTheme

enum class ShinobiTab(val title: String, val icon: ImageVector, val tag: String) {
  HACKATHONS("Hackathons", Icons.Default.EmojiEvents, "tab_hackathons"),
  HABITS("Habits", Icons.Default.Repeat, "tab_habits"),
  FOCUS("Focus", Icons.Default.HourglassBottom, "tab_focus"),
}

class MainActivity : ComponentActivity() {
  private val repository by lazy {
    ShinobiRepository(ShinobiDatabase.getInstance(applicationContext))
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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        var selectedTabIndex by remember { mutableIntStateOf(2) } // Default to Focus for Step 4 review

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
              0 -> HackathonScreen(viewModel = hackathonViewModel)
              1 -> HabitScreen(viewModel = habitViewModel)
              2 -> FocusScreen(viewModel = focusViewModel)
            }
          }
        }
      }
    }
  }
}


