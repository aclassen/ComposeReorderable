/*
 * Copyright 2021 André Claßen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.burnoutcrew.android.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.burnoutcrew.android.R
import org.burnoutcrew.android.ui.reorderlist.ReorderImageList
import org.burnoutcrew.android.ui.reorderlist.ReorderList
import org.burnoutcrew.android.ui.theme.ReorderListTheme


@Composable
fun Root() {
    ReorderListTheme {
        val navController = rememberNavController()
        val navigationItems = listOf(
            NavigationItem.Lists,
            NavigationItem.Music,
        )
        Scaffold(
            topBar = {
                TopAppBar(title = { Text(stringResource(R.string.app_name)) })
            },
            bottomBar = {
                BottomNavigationBar(navigationItems, navController)
            }
        ) {
            Navigation(
                navController,
                Modifier.padding(top = it.calculateTopPadding(), bottom = it.calculateBottomPadding())
            )
        }
    }
}

@Composable
private fun Navigation(navController: NavHostController, modifier: Modifier) {
    NavHost(navController, startDestination = NavigationItem.Lists.route, modifier = modifier) {
        composable(NavigationItem.Lists.route) {
            ReorderList()
        }
        composable(NavigationItem.Music.route) {
            ReorderImageList()
        }
    }
}


@Composable
private fun BottomNavigationBar(items: List<NavigationItem>, navController: NavController) {
    BottomNavigation(contentColor = Color.White) {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(item.icon, item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

private sealed class NavigationItem(var route: String, var icon: ImageVector, var title: String) {
    object Lists : NavigationItem("lists", Icons.Default.List, "Lists")
    object Music : NavigationItem("fixed", Icons.Default.Star, "Fixed")
}
