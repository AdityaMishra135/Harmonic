package com.example.mymusic.presentation

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mymusic.domain.model.Song
import com.example.mymusic.presentation.navigation.BottomBarScreen
import com.example.mymusic.presentation.navigation.BottomNavGraph
import com.example.mymusic.presentation.navigation.Screen
import com.example.mymusic.presentation.player.PlayerScreen
import com.example.mymusic.presentation.songs.ArtistInfo
import com.example.mymusic.presentation.songs.MediaPlayerController
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "SuspiciousIndentation")
@Composable
fun MainScreen(
    audioList: List<Song>,
    currentPlayingAudio: Song?,
    isAudioPlaying: Boolean,
    onStart: (Song) -> Unit,
    searchText: String,
    songs: List<Song>,
    onItemClick: (Song) -> Unit,
    onDataLoaded: () -> Unit,
) {

    val navController = rememberNavController()


    val bottomSheet = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed,
    animationSpec = tween(200)
    )

    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheet)


    LaunchedEffect(key1 = Unit) {
        onDataLoaded()
    }

    val bottomBarState = rememberSaveable { (mutableStateOf(true)) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val animatedHeight by animateDpAsState(
        targetValue = if (currentPlayingAudio == null) 0.dp
        else 130.dp
    )
    val animatedCorners by animateDpAsState(
        targetValue = if(bottomSheet.isExpanded) 0.dp else 26.dp
    )

    val coroutineScope = rememberCoroutineScope()


    when (navBackStackEntry?.destination?.route) {
        BottomBarScreen.Home.route -> {
            bottomBarState.value = true

        }
        BottomBarScreen.Songs.route -> {
            bottomBarState.value = true
        }
        BottomBarScreen.Playlists.route -> {
            bottomBarState.value = true
        }
        BottomBarScreen.Album.route -> {
            bottomBarState.value = true
        }
        Screen.SearchScreen.route -> {
            bottomBarState.value = true
        }
        Screen.PlayerScreen.route -> {
            bottomBarState.value = false
        }
    }


    Scaffold(bottomBar = {
        BottomBar(navController = navController, bottomBarState = bottomBarState)
    }) { innerPadding ->
        BottomSheetScaffold(
            sheetContent = {
                        currentPlayingAudio?.let { currentPlayingAudio ->
                            Box(modifier = Modifier
                                .padding(bottom = innerPadding.calculateBottomPadding())
                                .clickable {
                                    coroutineScope.launch {
                                        bottomSheet.expand()
                                    }
                                }) {
                                if(bottomSheet.isCollapsed){
                                    BottomBarPlayer(
                                        song = currentPlayingAudio,
                                        isAudioPlaying = isAudioPlaying,
                                        onStart = { onStart.invoke(currentPlayingAudio) },
                                    )
                                }else{
                                    PlayerScreen()
                                }
                            }
                        }
            },
            sheetShape = RoundedCornerShape(topEnd = animatedCorners, topStart = animatedCorners),
            scaffoldState = scaffoldState,
            sheetPeekHeight = animatedHeight,
        ) {

            Box(
                modifier = Modifier.padding(innerPadding)
            ) {
                BottomNavGraph(
                    navController = navController,
                    songList = audioList,
                    songs = songs,
                    searchText = searchText,
                    currentPlayingAudio = currentPlayingAudio,
                    onItemClick = onItemClick
                )
            }
        }
    }
}


@Composable
fun BottomBar(navController: NavHostController, bottomBarState: MutableState<Boolean>) {
    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.Songs,
        BottomBarScreen.Playlists,
        BottomBarScreen.Album,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    AnimatedVisibility(visible = bottomBarState.value,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        content = {
            BottomNavigation(modifier = Modifier.graphicsLayer {
                    shape = RoundedCornerShape(
                        topStart = 20.dp, topEnd = 20.dp
                    )
                    clip = true
                }) {
                screens.forEach { screen ->
                    AddItem(
                        screen = screen,
                        currentDestination = currentDestination,
                        navController = navController
                    )
                }
            }
        })
}

@Composable
fun RowScope.AddItem(
    screen: BottomBarScreen, currentDestination: NavDestination?, navController: NavHostController
) {

    BottomNavigationItem(label = {
        Text(text = screen.title)
    },
        icon = {
            Icon(imageVector = screen.icon, contentDescription = screen.title)
        },
        selected = currentDestination?.hierarchy?.any {
            it.route == screen.route
        } == true,
        unselectedContentColor = LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        })
}

@Composable
fun BottomBarPlayer(
    song: Song,
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ArtistInfo(
            audio = song, modifier = Modifier.weight(1f)
        )
        MediaPlayerController(
            isAudioPlaying = isAudioPlaying,
            onStart = { onStart.invoke() },
        )
        Spacer(modifier = Modifier.width(20.dp))
    }
}