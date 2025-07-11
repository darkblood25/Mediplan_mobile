package com.example.mediplan.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: String = "",
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

    // Get current user by ID
    var currentUser by remember { mutableStateOf<com.example.mediplan.RoomDB.UserData?>(null) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            val user = repository.getUserById(userId)
            currentUser = user
        }
    }

    // Get medications for current user
    val medications by medicationViewModel.getMedicationsByUser(userId).observeAsState(emptyList())

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
            bottomBar = {
                BottomAppBar(
                    containerColor = Color.White
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Medications") },
                        label = { Text("Medicamentos") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("Histórico") },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Configurações") },
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 }
                    )
                }
            },
            floatingActionButton = {
                if (selectedTab == 1) {
                    FloatingActionButton(
                        onClick = { showAddMedication = true },
                        containerColor = LightGreen
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Adicionar Medicamento",
                            tint = Color.White
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
            ) {
                when (selectedTab) {
                    0 -> HomeContent(medications = medications, userName = currentUser?.name ?: "Usuário")
                    1 -> MedicationsContent(medications = medications)
                    2 -> HistoryContent(
                        userId = userId,
                        onViewFullHistory = { showHistory = true }
                    )
                    3 -> SettingsScreen(
                        userName = currentUser?.name ?: "Usuário",
                        onChangePassword = { /* TODO: Implementar ação de mudar palavra-passe */ },
                        onLogout = onLogout,
                        onDeleteAccount = onAccountDeleted
                    )
                }
            }
        }
    }
}

@Composable
fun HomeContent(medications: List<MedicationData>, userName: String) {
    val todayMedications = getTodayMedications(medications)
    val upcomingMedications = getUpcomingMedications(medications)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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

        item {
            // Today's Medications
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
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
    }
}

@Composable
fun MedicationTodayItem(medication: MedicationData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(width = 4.dp,LightGreen, RoundedCornerShape(16.dp))
            .clickable { },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(LightGreen)
            )

            Spacer(modifier = Modifier.width(12.dp))

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

        Spacer(modifier = Modifier.width(12.dp))

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

        Text(
            text = medication.frequency,
            fontSize = 12.sp,
            color = LightBlue,
            fontWeight = FontWeight.Medium
        )
    }
}

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
        } else {
            items(medications) { medication ->
                MedicationCard(medication = medication)
            }
        }
    }
}

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

            if (medication.startDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Início: ${medication.startDate}${if (medication.endDate.isNotEmpty()) " - Fim: ${medication.endDate}" else ""}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit Button
                androidx.compose.material3.Button(
                    onClick = {
                        showEditDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
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
            
            // Second row of buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Taken Button
                androidx.compose.material3.Button(
                    onClick = {
                        viewModel.markMedicationAsTaken(medication)
                    },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
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

                // Complete Button
                androidx.compose.material3.Button(
                    onClick = {
                        viewModel.markMedicationAsCompleted(medication)
                    },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
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

            // Third row - Delete button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                androidx.compose.material3.Button(
                    onClick = {
                        viewModel.deleteMedication(medication)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
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
            
            // Edit Dialog
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
    
    // Frequency dropdown options
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
                    label = { Text("Nome do Medicamento", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = LightGreen,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = LightGreen,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                
                // Dosage
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosagem", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = LightGreen,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                
                // Frequency Dropdown
                ExposedDropdownMenuBox(
                    expanded = frequencyExpanded,
                    onExpandedChange = { frequencyExpanded = !frequencyExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = frequency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequência", color = Color.White) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedBorderColor = LightGreen,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = frequencyExpanded,
                        onDismissRequest = { frequencyExpanded = false }
                    ) {
                        frequencyOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = Color.White) },
                                onClick = {
                                    frequency = option
                                    frequencyExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Start Date
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
                    label = { Text("Data de Início", color = Color.White) },
                    placeholder = { Text("DD/MM/AAAA") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = LightGreen,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                
                // End Date
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
                    label = { Text("Data de Fim (Opcional)", color = Color.White) },
                    placeholder = { Text("DD/MM/AAAA") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = LightGreen,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
        },
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
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = LightGreen
                )
            ) {
                Text("Salvar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = LightGreen)
            }
        }
    )
}

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

                if (recentHistory.isNotEmpty()) {
                    androidx.compose.material3.TextButton(
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
        } else {
            items(recentHistory.take(5)) { historyItem ->
                HistoryCard(historyItem = historyItem)
            }
        }
    }
}


// Helper functions
fun getTodayMedications(medications: List<MedicationData>): List<MedicationData> {
    val today = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val todayString = dateFormat.format(today.time)

    return medications.filter { medication ->
        // Check if medication should be taken today based on start date and frequency
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

        when {
            startDate == null -> false
            endDate != null && today.time.after(endDate) -> false
            today.time.before(startDate) -> false
            else -> true // Medication is active today
        }
    }
}

fun getUpcomingMedications(medications: List<MedicationData>): List<MedicationData> {
    val today = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    return medications.filter { medication ->
        val startDate = try {
            dateFormat.parse(medication.startDate)
        } catch (e: Exception) {
            null
        }

        startDate != null && today.time.before(startDate)
    }.sortedBy { medication ->
        try {
            dateFormat.parse(medication.startDate)
        } catch (e: Exception) {
            null
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}