package com.emergent.posapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.emergent.posapp.screens.AiScreen
import com.emergent.posapp.screens.BankScreen
import com.emergent.posapp.screens.BillingScreen
import com.emergent.posapp.screens.CashScreen
import com.emergent.posapp.screens.EstimatesScreen
import com.emergent.posapp.screens.GstScreen
import com.emergent.posapp.screens.HistoryScreen
import com.emergent.posapp.screens.InventoryScreen
import com.emergent.posapp.screens.LedgerScreen
import com.emergent.posapp.screens.PartiesScreen
import com.emergent.posapp.screens.PurchaseScreen
import com.emergent.posapp.screens.SettingsScreen
import com.emergent.posapp.ui.EmergentPOSTheme
import com.emergent.posapp.ui.FullScreenLoading
import com.emergent.posapp.ui.Screen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmergentPOSTheme {
                if (viewModel.isLoading) {
                    FullScreenLoading()
                } else {
                    PosApp(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosApp(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Screen.Billing.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = viewModel.shopProfile.shopName.ifBlank { "Emergent POS" },
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(20.dp),
                )
                Text(
                    text = viewModel.shopProfile.ownerName,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
                )
                Screen.entries.forEach { screen ->
                    NavigationDrawerItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(screen.route) {
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    )
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(Screen.entries.find { it.route == currentRoute }?.label ?: "Emergent POS") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        Icon(
                            imageVector = if (viewModel.serverReachable) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                            contentDescription = if (viewModel.serverReachable) "Connected" else "Disconnected",
                            modifier = Modifier.padding(end = 12.dp),
                        )
                    },
                )
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Billing.route,
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                composable(Screen.Billing.route) { BillingScreen(viewModel) }
                composable(Screen.Inventory.route) { InventoryScreen(viewModel) }
                composable(Screen.Parties.route) { PartiesScreen(viewModel) }
                composable(Screen.Purchase.route) { PurchaseScreen(viewModel) }
                composable(Screen.Ledger.route) { LedgerScreen(viewModel) }
                composable(Screen.History.route) { HistoryScreen(viewModel) }
                composable(Screen.Gst.route) { GstScreen(viewModel) }
                composable(Screen.Bank.route) { BankScreen(viewModel) }
                composable(Screen.Cash.route) { CashScreen(viewModel) }
                composable(Screen.Estimates.route) { EstimatesScreen(viewModel) }
                composable(Screen.Ai.route) { AiScreen(viewModel) }
                composable(Screen.Settings.route) { SettingsScreen(viewModel) }
            }
        }
    }
}
