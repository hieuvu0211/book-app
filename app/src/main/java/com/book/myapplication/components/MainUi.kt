package com.book.myapplication.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.book.myapplication.GlobalState.UserData
import com.book.myapplication.R
import com.book.myapplication.ViewModel.BookVM
import com.book.myapplication.model.Book
import com.book.myapplication.model.User


@Composable
fun ImageFromLocalhostUrl(
    book: Book,
    onBookClick: (Book) -> Unit,
) {
    val url: String = "http://10.0.2.2:8080/Books/${book.book_id}/image.png"
    val painter =
        rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current).data(data = url)
                .apply(block = fun ImageRequest.Builder.() {
                    // You can customize image loading parameters here
                }).build()
        )
    Box(modifier = Modifier
//        .fillMaxWidth()
        .clickable { onBookClick(book) }) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(25.dp))
        )
    }
}

@Composable
fun StoryCard(
    book: Book,
    onBookClick: (Book) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
    ) {
        ImageFromLocalhostUrl(book, onBookClick)
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (book.book_name.length > 10) {
                "${book.book_name.take(10)}..."
            } else {
                book.book_name
            }, style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

    }
}

//render List Book data
@Composable
fun BookList(listBooks: List<Book>, onBookClick: (Book) -> Unit) {
    if (listBooks.isNotEmpty()) {
        val sizeListBook = listBooks.size - 1
        Column {
            for (i in 0..<sizeListBook step 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(200.dp)
                ) {
                    for (j in i..i + 3) {
                        if (j <= sizeListBook) {
                            StoryCard(listBooks[j], onBookClick)
                        }
                    }
                }
            }
        }
    }

}

fun handleResultSearch(textToSearch: String, listBooks: List<Book>): MutableList<Book> {
    val resultSearch = mutableListOf<Book>()
    if(textToSearch.isNotEmpty() && textToSearch != " ") {
        for (book in listBooks) {
            if (book.book_name.contains(textToSearch, ignoreCase = true)) {
                resultSearch.add(book)
            }
        }
    }

    return resultSearch
}

@Composable
fun ListBookResult(text : String, listBooks: List<Book>,navController: NavController) {
    val listItem = handleResultSearch(text, listBooks)
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)) {
        items(listItem) {item ->
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)) {
                ImageFromLocalhostUrl(item) {
                    navController.navigate("about-book/${it.book_id}")
                }
                Column {
                    Text(text = item.book_name, fontSize = 20.sp)
                    Text(text = stringResource(id = R.string.Chapter) + ": ${item.number_of_chapter}")
                    Text(text = stringResource(id = R.string.like2) + ": ${item.number_of_likes}")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarSample(navController: NavController, listBooks: List<Book>) {
    var text by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }

    var resultSearch = rememberSaveable() {
        mutableListOf<Book>()
    }
    Box(
        Modifier
            .fillMaxWidth()
            .semantics { isTraversalGroup = true }) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .semantics { traversalIndex = -1f },
            query = text,
            onQueryChange = {
                text = it
            },
            onSearch = {
                active = false
                if (text != "") {
                    navController.navigate("result-search/$text")
                }
            },
            active = active,
            onActiveChange = {
                active = it
            },
            placeholder = { Text(text = stringResource(id = R.string.search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
//            trailingIcon = { Icon(Icons.Default.MoreVert, contentDescription = null) },
        ) {
            ListBookResult(text, listBooks, navController)
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainUi(navController: NavController) {
//    val res = data.data.value // handle data receive
    val bookViewModel = viewModel<BookVM>()
    LaunchedEffect(Unit) {
        bookViewModel.loadListBooks()
    }
    val listBooks by bookViewModel.bookLists.collectAsStateWithLifecycle()
    val themeIcons = Modifier.size(100.dp)
    val context = LocalContext.current
    val dataUserStore = UserData(context)
    val getDataUserFromLocal =
        dataUserStore.getDataUserFromLocal.collectAsStateWithLifecycle(
            initialValue = User(
                0,
                "",
                ""
            )
        )
    var idUser by rememberSaveable {
        mutableIntStateOf(0)
    }
    idUser = getDataUserFromLocal.value?.user_id ?: 0

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .size(40.dp)
                    .border(0.1.dp, Color(202, 247, 183))
            ) {
                Surface(
                    shape = CircleShape,
                    contentColor = Color.White,
                    color = Color.White,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            navController.navigate("main")
                        }) {
                            Icon(
                                Icons.Filled.Home,
                                contentDescription = null,
                                modifier = themeIcons,
                                tint = Color.Black
                            )
                        }
                        IconButton(onClick = {
                            Log.i("resultAPI", "List Follow")
                            navController.navigate("follow/${idUser}")
                        }) {
                            Icon(
                                Icons.Default.Favorite,
                                "",
                                modifier = themeIcons,
                                tint = Color.Black
                            )
                        }
                        IconButton(onClick = {
                            Log.i("resultAPI", "Search")
                        }) {
                            Icon(
                                Icons.Default.Search,
                                "",
                                modifier = themeIcons,
                                tint = Color.Black
                            )
                        }
                        IconButton(onClick = {
                            if (getDataUserFromLocal.value != null) {
                                navController.navigate("account")
                            } else navController.navigate("login")
                        }) {
                            Icon(
                                Icons.Default.AccountBox,
                                "",
                                modifier = themeIcons,
                                tint = Color.Black
                            )

                        }
                    }
                }


            }
        },

        ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
//                .background(color = Color(245, 245, 245))
            ,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SearchBarSample(navController, listBooks)
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier
                    .padding(start = 12.dp),
//                    .background(color = Color(245, 245, 245))
            ) {
                item {
                    Text(text = stringResource(id = R.string.title_topfavorite))
                    BookListHorizon(bookViewModel) { book ->
                        navController.navigate("about-book/${book.book_id}")
                    }
                    Text(text = stringResource(id = R.string.list_stories))
                    BookList(listBooks) { book ->
                        navController.navigate("about-book/${book.book_id}")
                    }

                }
            }
        }
    }


}

@Preview(showBackground = true)
@Composable
fun ShowMainUiPreview(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    MainUi(navController = navController)
}