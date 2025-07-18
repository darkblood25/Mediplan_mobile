package com.example.mediplan.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediplan.RoomDB.MedicationData
import com.example.mediplan.UserDatabase
import com.example.mediplan.ViewModel.MedicationViewModel
import com.example.mediplan.ViewModel.Repository
import com.example.mediplan.ViewModel.UserViewModel
import com.example.mediplan.ui.theme.LightBlue
import com.example.mediplan.ui.theme.LightGreen
import com.example.mediplan.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


// Função auxiliar para obter medicamentos de hoje
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: String = "",
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onLogout: () -> Unit = {},
    onAccountDeleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val database = UserDatabase.getDatabase(context)
    val repository = Repository(database.dao)
    val userViewModel = remember { UserViewModel(repository) }
    val medicationViewModel = remember { MedicationViewModel(repository) }

    var selectedTab by remember { mutableStateOf(0) }
    var showAddMedication by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showDebug by remember { mutableStateOf(false) }

    // Coloca o utilizador atual no estado
    var currentUser by remember { mutableStateOf<com.example.mediplan.RoomDB.UserData?>(null) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            Log.d("HomeScreen", "Tentando obter usuário pelo ID: $userId")
            try {
                val user = repository.getUserById(userId)
                if (user != null) {
                    Log.d("HomeScreen", "Usuário encontrado: ${user.name}")
                    currentUser = user
                } else {
                    Log.e("HomeScreen", "Usuário não encontrado para o ID: $userId")
                }
            } catch (e: Exception) {
                Log.e("HomeScreen", "Erro ao obter usuário: ${e.message}", e)
            }
        } else {
            Log.e("HomeScreen", "userId está vazio")
        }
    }

    // Observa o estado do utilizador atual
    val medications by medicationViewModel.getMedicationsByUser(userId).observeAsState(emptyList())

    // Função auxiliar para obter medicamentos de hoje
    if (showAddMedication) {
        AddMedicationScreen(
            userId = userId,
            onBackClick = { showAddMedication = false },
            onMedicationAdded = { showAddMedication = false }
        )
    } else if (showHistory) {
        HistoryScreen(
            userId = userId,
            onBackClick = { showHistory = false }
        )
    } else if (showDebug) {
        DatabaseDebugScreen()
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "MediPlan",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = LightGreen
                    )
                )
            },
            // Barra de navegação inferior
            bottomBar = {
                BottomAppBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LightGreen,
                            selectedTextColor = LightGreen,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = LightGreen.copy(alpha = 0.1f)
                        )
                    )
                    // Item de navegação para Medicamentos
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Medicamentos") },
                        label = { Text("Meds") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LightGreen,
                            selectedTextColor = LightGreen,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = LightGreen.copy(alpha = 0.1f)
                        )
                    )
                    //item de navegação para Histórico
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = "Histórico") },
                        label = { Text("Histórico") },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LightGreen,
                            selectedTextColor = LightGreen,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = LightGreen.copy(alpha = 0.1f)
                        )
                    )
                    // Item de navegação para Configurações
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Configurações") },
                        label = { Text("Config") },
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LightGreen,
                            selectedTextColor = LightGreen,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = LightGreen.copy(alpha = 0.1f)
                        )
                    )
                }
            },
            // Botão flutuante para adicionar medicamento
            floatingActionButton = {
                if (selectedTab == 1) {
                    FloatingActionButton(
                        onClick = { showAddMedication = true },
                        containerColor = LightGreen,
                        contentColor = Color.White
                    ) {
                        // Ícone de adicionar medicamento
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Adicionar Medicamento",
                            tint = Color.White
                        )
                    }
                }
            }
            // Conteúdo principal da tela
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Exibe o conteúdo baseado na aba selecionada
                when (selectedTab) {
                    0 -> HomeContent(medications = medications, userName = currentUser?.name ?: "Usuário")
                    1 -> MedicationsContent(medications = medications)
                    2 -> HistoryContent(
                        userId = userId,
                        onViewFullHistory = { showHistory = true }
                    )
                    3 -> SettingsScreen(
                        userName = currentUser?.name ?: "Usuário",
                        isDarkMode = isDarkMode,
                        onThemeChange = onThemeChange,
                        onChangePassword = { newPassword -> 
                            // Implementar diretamente a alteração de senha
                            if (currentUser != null) {
                                // Usar o userViewModel para alterar a senha
                                val updatedUser = currentUser!!.copy(password = newPassword)
                                userViewModel.updateUser(updatedUser)
                                
                                // Mostrar mensagem de sucesso
                                Toast.makeText(
                                    context,
                                    "Senha alterada com sucesso",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Erro: usuário não encontrado",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onLogout = onLogout,
                        onDeleteAccount = {
                            // Implementar diretamente a exclusão da conta
                            if (currentUser != null) {
                                // Usar o userViewModel para excluir a conta
                                userViewModel.deleteUser(currentUser!!)
                                
                                // Mostrar mensagem de sucesso
                                Toast.makeText(
                                    context,
                                    "Conta eliminada com sucesso",
                                    Toast.LENGTH_SHORT
                                ).show()
                                
                                // Chamar o callback para navegar para a tela de login
                                onAccountDeleted()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Erro: usuário não encontrado",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onShowDebug = { showDebug = true }
                    )
                }
            }
        }
    }
}

// Função auxiliar para obter medicamentos de hoje
@Composable
fun HomeContent(medications: List<MedicationData>, userName: String) {
    val todayMedications = getTodayMedications(medications)
    val upcomingMedications = getUpcomingMedications(medications)

    // Exibe o conteúdo da tela inicial
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabeçalho da tela inicial
        item {
            // Welcome Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LightGreen),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Olá, $userName!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Como está se sentindo hoje?",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Card para exibir medicamentos de hoje e próximos
        item {
            // Today's Medications
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                // Cabeçalho do card de medicamentos de hoje
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Hoje",
                            tint = LightGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Medicamentos de Hoje",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray
                        )
                    }

                    // Lista de medicamentos de hoje
                    if (todayMedications.isEmpty()) {
                        Text(
                            text = "Nenhum medicamento para hoje",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        todayMedications.forEach { medication ->
                            MedicationTodayItem(medication = medication)
                        }
                    }
                }
            }
        }

        // Card para exibir os próximos medicamentos
        item {
            // Upcoming Medications
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Próximos Medicamentos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Lista de próximos medicamentos
                    if (upcomingMedications.isEmpty()) {
                        Text(
                            text = "Nenhum medicamento próximo",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        upcomingMedications.take(3).forEach { medication ->
                            MedicationUpcomingItem(medication = medication)
                        }
                    }
                }
            }
        }

        // Espaço extra no final da lista
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Função auxiliar para obter medicamentos de hoje
@Composable
fun MedicationTodayItem(medication: MedicationData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                width = 1.dp,
                color = LightGreen,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
        // Ícone de medicamento
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(LightGreen)
            )

            // Espaço entre o ícone e o texto
            Spacer(modifier = Modifier.width(12.dp))

            // Informações do medicamento
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.medName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                )
                Text(
                    text = "${medication.dosage} - ${medication.frequency}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// Função auxiliar para obter medicamentos de hoje
@Composable
fun MedicationUpcomingItem(medication: MedicationData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(LightBlue)
        )

        // Espaço entre o ícone e o texto
        Spacer(modifier = Modifier.width(12.dp))

        // Informações do medicamento
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = medication.medName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
            Text(
                text = medication.dosage,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Frequência do medicamento
        Text(
            text = medication.frequency,
            fontSize = 12.sp,
            color = LightBlue,
            fontWeight = FontWeight.Medium
        )
    }
}

// Função auxiliar para obter medicamentos de hoje
@Composable
fun MedicationsContent(medications: List<MedicationData>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Seus Medicamentos",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = LightGreen,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Verifica se há medicamentos
        if (medications.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nenhum medicamento adicionado",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Toque no botão + para adicionar seu primeiro medicamento",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            // Espaço extra no final da lista
        } else {
            items(medications) { medication ->
                MedicationCard(medication = medication)
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Função auxiliar para obter medicamentos de hoje
@Composable
fun MedicationCard(medication: MedicationData) {
    val context = LocalContext.current
    val database = UserDatabase.getDatabase(context)
    val repository = Repository(database.dao)
    val viewModel = remember { MedicationViewModel(repository) }
    
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = medication.medName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            // Exibe a descrição do medicamento, se houver
            if (medication.description.isNotEmpty()) {
                Text(
                    text = medication.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Dosagem",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = medication.dosage,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }

                // Espaço entre as colunas
                Column {
                    Text(
                        text = "Frequência",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = medication.frequency,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }

            // Exibe a data de início e fim, se houver
            if (medication.startDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Início: ${medication.startDate}${if (medication.endDate.isNotEmpty()) " - Fim: ${medication.endDate}" else ""}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Botões de ação
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // botão de histórico
                Button(
                    onClick = {
                        showEditDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightBlue,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Editar",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // segunda linha - Botões de ação
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // botão de tomado
                Button(
                    onClick = {
                        viewModel.markMedicationAsTaken(medication)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightGreen,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Tomado",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tomado",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // botão de concluído
                Button(
                    onClick = {
                        viewModel.markMedicationAsCompleted(medication)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Concluído",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Concluir",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // botão de eliminar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.deleteMedication(medication)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Eliminar Medicamento",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Diálogo de edição
            if (showEditDialog) {
                EditMedicationDialog(
                    medication = medication,
                    onDismiss = { showEditDialog = false },
                    onSave = { updatedMedication ->
                        viewModel.updateMedication(updatedMedication)
                        showEditDialog = false
                    }
                )
            }
        }
    }
}

// Função auxiliar para obter medicamentos de hoje
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicationDialog(
    medication: MedicationData,
    onDismiss: () -> Unit,
    onSave: (MedicationData) -> Unit
) {
    var medicationName by remember { mutableStateOf(medication.medName) }
    var description by remember { mutableStateOf(medication.description) }
    var dosage by remember { mutableStateOf(medication.dosage) }
    var frequency by remember { mutableStateOf(medication.frequency) }
    var startDate by remember { mutableStateOf(medication.startDate) }
    var endDate by remember { mutableStateOf(medication.endDate) }
    
    // Opções de frequência
    val frequencyOptions = listOf(
        "Uma vez por dia",
        "Duas vezes por dia", 
        "Três vezes por dia",
        "Quatro vezes por dia",
        "A cada 6 horas",
        "A cada 8 horas",
        "A cada 12 horas",
        "Conforme necessário"
    )
    var frequencyExpanded by remember { mutableStateOf(false) }

    // Diálogo de edição de medicamento
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar Medicamento",
                fontWeight = FontWeight.Bold,
                color = LightGreen
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Medication Name
                OutlinedTextField(
                    value = medicationName,
                    onValueChange = { medicationName = it },
                    label = { Text("Nome do Medicamento", color = Color.Black) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedBorderColor = LightGreen,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                
                // Descrição
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição", color = Color.Black) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedBorderColor = LightGreen,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                
                // Dosagem
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosagem", color = Color.Black) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedBorderColor = LightGreen,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                
                // frequência
                ExposedDropdownMenuBox(
                    expanded = frequencyExpanded,
                    onExpandedChange = { frequencyExpanded = !frequencyExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = frequency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequência", color = Color.Black) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black,
                            focusedBorderColor = LightGreen,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    // Menu suspenso para opções de frequência
                    ExposedDropdownMenu(
                        expanded = frequencyExpanded,
                        onDismissRequest = { frequencyExpanded = false }
                    ) {
                        frequencyOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = Color.Black) },
                                onClick = {
                                    frequency = option
                                    frequencyExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // data de início
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }.take(8)
                        startDate = when {
                            filtered.length > 4 -> "${filtered.take(2)}/${filtered.substring(2, 4)}/${filtered.substring(4)}"
                            filtered.length > 2 -> "${filtered.take(2)}/${filtered.substring(2)}"
                            else -> filtered
                        }
                    },
                    label = { Text("Data de Início", color = Color.Black) },
                    placeholder = { Text("DD/MM/AAAA") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedBorderColor = LightGreen,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                
                // data de fim
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }.take(8)
                        endDate = when {
                            filtered.length > 4 -> "${filtered.take(2)}/${filtered.substring(2, 4)}/${filtered.substring(4)}"
                            filtered.length > 2 -> "${filtered.take(2)}/${filtered.substring(2)}"
                            else -> filtered
                        }
                    },
                    label = { Text("Data de Fim (Opcional)", color = Color.Black) },
                    placeholder = { Text("DD/MM/AAAA") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedBorderColor = LightGreen,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
        },
        // Botões de ação do diálogo
        confirmButton = {
            Button(
                onClick = {
                    val updatedMedication = medication.copy(
                        medName = medicationName,
                        description = description,
                        dosage = dosage,
                        frequency = frequency,
                        startDate = startDate,
                        endDate = endDate
                    )
                    onSave(updatedMedication)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightGreen
                )
            ) {
                Text("Salvar", color = Color.White)
            }
        },
        // Botão de cancelar
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = LightGreen)
            }
        }
    )
}

// Função auxiliar para obter medicamentos de hoje
@Composable
fun HistoryContent(
    userId: String,
    onViewFullHistory: () -> Unit
) {
    val context = LocalContext.current
    val database = UserDatabase.getDatabase(context)
    val repository = Repository(database.dao)
    val viewModel = remember { MedicationViewModel(repository) }

    val recentHistory by viewModel.getMedicationHistoryByUser(userId).observeAsState(emptyList())

    // Filtra o histórico para os últimos 5 itens
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Histórico Recente",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightGreen
                )

                // Botão para ver todo o histórico, se houver itens recentes
                if (recentHistory.isNotEmpty()) {
                    TextButton(
                        onClick = onViewFullHistory
                    ) {
                        Text(
                            text = "Ver Tudo",
                            color = LightGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Verifica se há histórico recente
        if (recentHistory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Sem histórico",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhum histórico encontrado",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Quando você tomar ou remover medicamentos, eles aparecerão aqui",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            // Espaço extra no final da lista
        } else {
            items(recentHistory.take(5)) { historyItem ->
                HistoryCard(historyItem = historyItem)
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Função auxiliar para obter medicamentos de hoje
fun getTodayMedications(medications: List<MedicationData>): List<MedicationData> {
    val today = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    return medications.filter { medication ->
        // Tenta analisar a data de início e fim do medicamento
        val startDate = try {
            dateFormat.parse(medication.startDate)
        } catch (e: Exception) {
            null
        }

        val endDate = if (medication.endDate.isNotEmpty()) {
            try {
                dateFormat.parse(medication.endDate)
            } catch (e: Exception) {
                null
            }
        } else null

        // Verifica se o medicamento está ativo hoje
        when {
            startDate == null -> false
            endDate != null && today.time.after(endDate) -> false
            today.time.before(startDate) -> false
            else -> true // Medication is active today
        }
    }
}

// Função auxiliar para obter medicamentos futuros
fun getUpcomingMedications(medications: List<MedicationData>): List<MedicationData> {
    val today = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    return medications.filter { medication ->
        val startDate = try {
            dateFormat.parse(medication.startDate)
        } catch (e: Exception) {
            null
        }

        // Verifica se a data de início do medicamento é válida e está no futuro
        startDate != null && today.time.before(startDate)
    }.sortedBy { medication ->
        try {
            dateFormat.parse(medication.startDate)
        } catch (e: Exception) {
            null
        }
    }
}

// Função auxiliar para obter medicamentos de hoje
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        userId = "previewUser",
        isDarkMode = false, 
        onThemeChange = {},
        onLogout = {},
        onAccountDeleted = {}
    )
}