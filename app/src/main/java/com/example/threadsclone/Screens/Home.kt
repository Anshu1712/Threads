package com.example.threadsclone.Screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.threadsclone.item_View.ThreadItem
import com.example.threadsclone.viewModel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Home(navHostController: NavHostController) {
    val homeViewModel: HomeViewModel = viewModel()
    val context = LocalContext.current
    // Observe the threads and users data
    val threadAndUsers by homeViewModel.threadsAndUsers.observeAsState(null)

    LazyColumn {
        items(threadAndUsers ?: emptyList()) { pairs ->
            ThreadItem(
                thread = pairs.first, users = pairs.second, navHostController,
                FirebaseAuth.getInstance().currentUser!!.uid
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShowHome() {
    // Preview the Home composable
    Home(navHostController = NavHostController(LocalContext.current))
}
