package com.example.threadsclone.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.threadsclone.Screens.AddThreads
import com.example.threadsclone.Screens.BottomNav
import com.example.threadsclone.Screens.Home
import com.example.threadsclone.Screens.Login
import com.example.threadsclone.Screens.Notification
import com.example.threadsclone.Screens.Profile
import com.example.threadsclone.Screens.Register
import com.example.threadsclone.Screens.Search
import com.example.threadsclone.Screens.Splash

@Composable
fun NavGraph(navController: NavHostController){

    NavHost(navController = navController,
        startDestination = Routes.Splash.routes) {

        composable(Routes.Splash.routes){
            Splash(navController)
        }
        composable(Routes.Home.routes){
            Home()
        }
        composable(Routes.Notification.routes){
            Notification()
        }
        composable(Routes.AddThreads.routes){
            AddThreads()
        }
        composable(Routes.Search.routes){
            Search()
        }
        composable(Routes.Profile.routes){
            Profile()
        }
        composable(Routes.BottomNav.routes){
            BottomNav(navController)
        }
        composable(Routes.Login.routes){
            Login(navController)
        }
        composable(Routes.Register.routes){
            Register(navController)
        }
    }
}