package com.example.shoessss

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import coil.compose.AsyncImage
import com.example.shoessss.ui.theme.SHOESSSSTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "sneakers.db"
        ).build()

        val viewModel = SneakerViewModel(
            SneakerRepository(db.sneakerDao())
        )

        setContent {
            SHOESSSSTheme {
                SneakerScreen(viewModel)
            }
        }
    }
}

@Entity(tableName = "sneakers")
data class SneakerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val brand: String,
    val price: Int,
    val imageUrl: String
)

@Dao
interface SneakerDao {

    @Query("SELECT * FROM sneakers")
    fun getAll(): Flow<List<SneakerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sneaker: SneakerEntity)

    @Delete
    suspend fun delete(sneaker: SneakerEntity)
}

@Database(entities = [SneakerEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sneakerDao(): SneakerDao
}

class SneakerRepository(private val dao: SneakerDao) {

    val sneakers = dao.getAll()

    suspend fun addSneaker(sneaker: SneakerEntity) {
        dao.insert(sneaker)
    }
}

class SneakerViewModel(
    private val repository: SneakerRepository
) : ViewModel() {

    val sneakers = repository.sneakers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun addDemoSneaker() {
        viewModelScope.launch {
            repository.addSneaker(
                SneakerEntity(
                    name = "Air Jordan 1",
                    brand = "Nike",
                    price = 25000,
                    imageUrl = "https://i.imgur.com/ZcLLrkY.jpg"
                )
            )
        }
    }
}

@Composable
fun SneakerCard(sneaker: SneakerEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            AsyncImage(
                model = sneaker.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            Column(Modifier.padding(12.dp)) {
                Text(sneaker.name, style = MaterialTheme.typography.titleMedium)
                Text(sneaker.brand, color = Color.Gray)
                Text(
                    "${sneaker.price} ₽",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SneakerScreen(viewModel: SneakerViewModel) {

    val sneakers by viewModel.sneakers.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addDemoSneaker() }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(sneakers) { sneaker ->
                SneakerCard(sneaker)
            }
        }
    }
}

