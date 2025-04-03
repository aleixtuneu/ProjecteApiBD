package cat.itb.dam.m78.dbdemo3.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
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

// 4. Classe que fa servir la api
object PokemonsApi {
    // Atributs
    val url = "https://pokeapi.co/api/v2/pokemon?limit=100000&offset=0";
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    // Funcions
    //suspend fun list() = client.get(url).body<List<Pokemon>>()
    suspend fun list(): List<Pokemon> { // La función sigue devolviendo List<Pokemon>
        try {
            // 1. Pide la respuesta completa y pársala a PokemonListResponse
            val response = client.get(url).body<PokemonListResponse>()
            // 2. Devuelve solo la lista 'results' de la respuesta
            return response.results
        } catch (e: Exception) {
            // Puedes hacer un log más específico aquí si quieres
            println("Error en PokemonsApi.list: ${e.message}")
            // Relanzar o devolver lista vacía según tu manejo de errores preferido
            return emptyList()
            // O throw RuntimeException("Fallo al obtener Pokemons", e)
        }
    }
}

// Pantalla inicial
@Composable
fun PokemonsScreen(navigateToPokemonInfoScreen: (String) -> Unit) {
    val viewModel = viewModel { PokemonsViewModel() }
    val pokemons = viewModel.pokemons.value

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (pokemons.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(pokemons) { pokemon ->
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Nom
                        Text(
                            text = "${pokemon.name}",
                            modifier = Modifier.clickable {
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
fun PokemonInfoScreen(pokemonId: String) {
    val viewModel = viewModel { PokemonsViewModel() }
    val pokemons = viewModel.pokemons.value
    val pokemonsInfo = pokemons.filter { it.name == pokemonId }

    if (pokemonsInfo != null) {
        LazyColumn(
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            items(pokemonsInfo) { pokemonInfo ->
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Nom
                    Text( text = "${pokemonInfo.name}")

                    //

                    // Resta atributs

                }
            }
        }
    } else {
        Text("Pokemon no trobat.")
    }
}



//
@Composable
@Preview
fun App(viewModel: DatabaseViewModel=DatabaseViewModel()) {
    // PokemonsScreen()
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Destination.PokemonsScreen) {
        composable<Destination.PokemonsScreen> {
            PokemonsScreen { id ->
                navController.navigate(Destination.PokemonInfoScreen(id))
            }
        }
        composable<Destination.PokemonInfoScreen> { backStack ->
            val pokemonId = backStack.arguments?.getString("pokemonId") ?: ""
            PokemonInfoScreen(pokemonId)
        }
    }
    /*
    MaterialTheme {

        //Llista amb tots els registres, obtinguda del ViewModel
        val all = viewModel.allTexts.value
        var inputText by remember { mutableStateOf("") }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            // Text field and button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(MaterialTheme.colors.surface, shape = RoundedCornerShape(8.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    //El textField està enllaçat al camp inputText.  No està al ViewModel per què és funcionament de la pantalla
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("Enter text") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                            .background(MaterialTheme.colors.background, shape = RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        // Quanes fa click, el botó cirda al ViewModel per fer un insert a la base de dades
                        onClick = { viewModel.insertText(inputText) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Add", color = MaterialTheme.colors.onPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of items
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(all) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(MaterialTheme.colors.surface, shape = RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.text, style = MaterialTheme.typography.body1)
                        IconButton(onClick = {viewModel.deleteText(item.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
    */
}