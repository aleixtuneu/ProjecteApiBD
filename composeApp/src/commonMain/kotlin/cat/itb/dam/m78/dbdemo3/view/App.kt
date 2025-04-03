package cat.itb.dam.m78.dbdemo3.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.itb.dam.m78.dbdemo3.model.DatabaseViewModel
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.ContentType.Application.Json
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.client.engine.cio.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.compose.AsyncImage
import kotlinx.serialization.SerialName
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController

// Definició de destins
object Destination {
    @Serializable
    data object PokemonsScreen
    @Serializable
    data class PokemonInfoScreen(val pokemonId: String)
}

// https://pokeapi.co/api/v2/pokemon/
// https://pokeapi.co/api/v2/pokemon?limit=100000&offset=0

// 1. Model de dades
@Serializable
data class Pokemon(
    val name: String,
    val url: String
)

@Serializable
data class PokemonListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Pokemon>
)

//
@Serializable
data class PokemonDetails(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: Sprites,
    val types: List<TypeSlot>
    //
)

@Serializable
data class Sprites(
    @SerialName("front_default")
    val spriteDefault: String?
)

@Serializable
data class TypeSlot(
    val slot: Int,
    val type: TypeInfo
)

@Serializable
data class TypeInfo(
    val name: String,
    val url: String
)

// Estat per la UI de detalls
data class PokemonInfoState(
    val details: PokemonDetails? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
//

// 2. Utilitzar ViewModel
class PokemonsViewModel() : ViewModel() {
    val pokemons = mutableStateOf<List<Pokemon>>(emptyList())

    // 3. Actualitzar l'objecte fent servir la api
    init {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val totsPokemons = PokemonsApi.list()
                pokemons.value = totsPokemons
            } catch (e: Exception) {
                println("Error al obtenir les dades: ${e.message}")
                pokemons.value = emptyList()
            }
        }
    }
}
//
class PokemonInfoViewModel : ViewModel() {
    var uiState by mutableStateOf(PokemonInfoState())
        private set

    fun loadPokemonDetails(pokemonId: String) {
        if (pokemonId.isBlank()) {
            uiState = uiState.copy(error = "ID de Pokémon no vàlid.",
                isLoading = false)
            return
        }
        // Evitar recarregar si ja està carregant o si ja tenim les dades
        if (uiState.isLoading || uiState.details?.name?.equals(pokemonId, ignoreCase = true) == true || uiState.details?.id.toString() == pokemonId) {
            return
        }

        uiState = uiState.copy(isLoading = true, error = null) // Començar a carregar

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fetchedDetails = PokemonsApi.getDetails(pokemonId)
                if (fetchedDetails != null) {
                    uiState = uiState.copy(details = fetchedDetails, isLoading = false)
                } else {
                    uiState = uiState.copy(error = "No s'han pogut obtenir els detalls", isLoading = false)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = "Error: ${e.message}", isLoading = false)
            }
        }
    }
}
//

// 4. Classe que fa servir la api
object PokemonsApi {
    // Atributs
    val baseUrl = "https://pokeapi.co/api/v2/pokemon/"
    val url = "https://pokeapi.co/api/v2/pokemon?limit=100000&offset=0"
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    // Funcions
    suspend fun list(): List<Pokemon> {
        try {
            val response = client.get(url).body<PokemonListResponse>()
            return response.results
        } catch (e: Exception) {
            println("Error: ${e.message}")
            return emptyList()
        }
    }
    //
    suspend fun getDetails(pokemonId: String): PokemonDetails? {
        return try { // retornar id del pokemon per obtenir detalls
            val detailUrl = "$baseUrl$pokemonId/"
            client.get(detailUrl).body<PokemonDetails>()
        } catch (e: Exception) {
            println("Error: ${e.message}")
            null // Si falla retorna null
        }
    }
    //
}

// Pantalla inicial
@Composable
fun PokemonsScreen(navigateToPokemonInfoScreen: (String) -> Unit) {
    val viewModel = viewModel { PokemonsViewModel() }
    val allPokemons = viewModel.pokemons.value // Obtenir la llista original del ViewModel
    var searchQuery by remember { mutableStateOf("") } // Estat per guardar text per buscar
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.Center
    ) {
        if (allPokemons.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier)
        } else {
            // Buscar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it }, // Actualitzar al escriure
                label = { Text("Buscar Pokémon...") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filtrar la llista
            val filteredPokemons = if (searchQuery.isBlank()) {
                allPokemons
            } else {
                allPokemons.filter { pokemon ->
                    pokemon.name.contains(searchQuery, ignoreCase = true) // Ignorar majúscules/minúscules
                }
            }

            // Mostrar la llista
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPokemons) { pokemon ->
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        // Nom
                        Text(
                            text = pokemon.name.capitalize(),
                            modifier = Modifier.fillMaxWidth().clickable {
                                navigateToPokemonInfoScreen(pokemon.name)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Pantalla Info Pokemon
@Composable
fun PokemonInfoScreen(pokemonId: String, navController: NavController, viewModel:PokemonInfoViewModel = viewModel()) {
    LaunchedEffect(pokemonId) {
        viewModel.loadPokemonDetails(pokemonId)
    }

    val state = viewModel.uiState

    // Scaffold per el TopAppBar
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Mostrar nom del pokémon a la barra
                    val pokemonName = state.details?.name?.capitalize() ?:""
                    Text(pokemonName)
                },
                // Botó de navegació (tornar)
                navigationIcon = {
                    IconButton(onClick = {navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) // Fletxa enrere
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                }
                state.error != null -> {
                    Text("Error: ${state.error}", color = MaterialTheme.colors.error)
                }
                state.details != null -> {
                    // Mostrar detalls del Pokémon
                    val details = state.details
                    // Nom
                    Text("${details.name.capitalize()}", style = MaterialTheme.typography.h5)
                    // Id
                    Text("ID: ${details.id}")
                    // Imatge
                    AsyncImage(
                        model = details.sprites.spriteDefault,
                        contentDescription = "Sprite de ${details.name}",
                        modifier = Modifier.size(120.dp)
                    )
                    // Altura
                    Text("Altura: ${details.height / 10.0} m") // Convertir a metres
                    // Pes
                    Text("Pes: ${details.weight / 10.0} kg") // Convertir a kg

                    Text("Tipus:", style = MaterialTheme.typography.h6)
                    details.types.forEach { typeSlot ->
                        Text("- ${typeSlot.type.name.capitalize()}")
                    }
                }
                else -> {
                    // Estat inicial
                    Text("Carregant detalls...")
                }
            }
        }
    }
}



//
@Composable
@Preview
fun App(/*viewModel: DatabaseViewModel=DatabaseViewModel()*/) {
    // PokemonsScreen()
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Destination.PokemonsScreen) {
        composable<Destination.PokemonsScreen> {
            PokemonsScreen { id ->
                navController.navigate(Destination.PokemonInfoScreen(id))
            }
        }
        composable<Destination.PokemonInfoScreen> { backStack->
            val destination: Destination.PokemonInfoScreen = backStack.toRoute()
            PokemonInfoScreen(pokemonId = destination.pokemonId, // Pasar l'ID obtingut
                navController = navController,
                viewModel = viewModel(factory = ViewModelProvider.NewInstanceFactory())) // Proporcionar la factory per defecte perquè no salti error
        }
    }
}