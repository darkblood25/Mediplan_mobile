package com.example.mediplan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediplan.RoomDB.MedicationData
import com.example.mediplan.UserDatabase
import com.example.mediplan.ViewModel.MedicationState
import com.example.mediplan.ViewModel.MedicationViewModel
import com.example.mediplan.ViewModel.Repository
import com.example.mediplan.ui.components.AdaptiveOutlinedTextField
import com.example.mediplan.ui.components.GradientButton
import com.example.mediplan.ui.theme.LightGreen
import com.example.mediplan.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

//ecra de adicionar medicamentos
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    userId: String,
    onBackClick: () -> Unit = {},
    onMedicationAdded: () -> Unit = {}
) {
    val context = LocalContext.current
    val database = UserDatabase.getDatabase(context)
    val repository = Repository(database.dao)
    val viewModel = remember { MedicationViewModel(repository) }

    val scrollState = rememberScrollState()

    var medicationName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // frequencia das medicações
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

    val medicationState by viewModel.medicationState.collectAsState()


    // obesrvação do estado de medicação
    LaunchedEffect(medicationState) {
        when (medicationState) {
            is MedicationState.Success -> {
                onMedicationAdded()
                viewModel.resetState()
            }
            is MedicationState.Error -> {
                errorMessage = (medicationState as MedicationState.Error).message
            }
            MedicationState.Idle -> {
                errorMessage = ""
            }
            MedicationState.Loading -> {
                // Handle loading state if needed
                // For example, you might want to show a loading indicator
                // or disable user interactions while loading
            }
        }
    }

    // data atual para o campo de data de início
    val currentDate = remember {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateFormat.format(calendar.time)
    }

    // inicialização do campo de data de início
    LaunchedEffect(Unit) {
        if (startDate.isEmpty()) {
            startDate = currentDate
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        // barra de navegação superior
        TopAppBar(
            title = {
                Text(
                    text = "Adicionar Medicamento",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = LightGreen
            )
        )

        // conteudo do card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth()
                    ) {


                        Text(
                            text = "Informações do Medicamento",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black, // JÁ ESTÁ CORRETO
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Nome de medicação
                        AdaptiveOutlinedTextField(
                            value = medicationName,
                            onValueChange = { medicationName = it },
                            label = { Text("Nome do Medicamento", color = Color.Black) }, // <--- MUDANÇA AQUI
                            placeholder = { Text("Ex: Paracetamol") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            textColor = Color.Black // JÁ ESTÁ CORRETO
                        )

                        // Descrição/Indicação do medicamento
                        AdaptiveOutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Descrição/Indicação", color = Color.Black) }, // <--- MUDANÇA AQUI
                            placeholder = { Text("Ex: Para dor de cabeça") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            maxLines = 3,
                            textColor = Color.Black // JÁ ESTÁ CORRETO
                        )

                        // Dosagem
                        AdaptiveOutlinedTextField(
                            value = dosage,
                            onValueChange = { dosage = it },
                            label = { Text("Dosagem", color = Color.Black) }, // <--- MUDANÇA AQUI
                            placeholder = { Text("Ex: 500mg") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            textColor = Color.Black // JÁ ESTÁ CORRETO
                        )

                        // Frequência
                        @OptIn(ExperimentalMaterial3Api::class) // Adicione se não estiver no escopo da função
                        ExposedDropdownMenuBox(
                            expanded = frequencyExpanded,
                            onExpandedChange = { frequencyExpanded = !frequencyExpanded },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            OutlinedTextField(
                                value = frequency,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Frequência", color = Color.Black) }, // <--- MUDANÇA AQUI (para garantir)
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors( // JÁ ESTÁ BOM, mas pode ser mais completo
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    cursorColor = Color.Black,
                                    focusedLabelColor = Color.Black, // Adicionado para consistência
                                    unfocusedLabelColor = Color.DarkGray, // Ou Color.Black se preferir sempre preto
                                    focusedBorderColor = LightGreen, // Mantendo sua cor de foco
                                    unfocusedBorderColor = Color.Gray
                                )
                            )

                            // Dropdown Menu para Frequência
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
                        AdaptiveOutlinedTextField(
                            value = startDate,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { it.isDigit() }.take(8)
                                startDate = when {
                                    filtered.length > 4 -> "${filtered.take(2)}/${filtered.substring(2, 4)}/${filtered.substring(4)}"
                                    filtered.length > 2 -> "${filtered.take(2)}/${filtered.substring(2)}"
                                    else -> filtered
                                }
                            },
                            label = { Text("Data de Início", color = Color.Black) }, // <--- MUDANÇA AQUI
                            placeholder = { Text("DD/MM/AAAA") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            textColor = Color.Black // JÁ ESTÁ CORRETO
                        )

                        // data de fim (opcional)
                        AdaptiveOutlinedTextField(
                            value = endDate,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { it.isDigit() }.take(8)
                                endDate = when {
                                    filtered.length > 4 -> "${filtered.take(2)}/${filtered.substring(2, 4)}/${filtered.substring(4)}"
                                    filtered.length > 2 -> "${filtered.take(2)}/${filtered.substring(2)}"
                                    else -> filtered
                                }
                            },
                            label = { Text("Data de Fim (Opcional)", color = Color.Black) }, // <--- MUDANÇA AQUI
                            placeholder = { Text("DD/MM/AAAA") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            textColor = Color.Black // JÁ ESTÁ CORRETO
                        )

                        // mensagem de erro
                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        // botão de adicionar medicamento
                        GradientButton(
                            text = if (medicationState is MedicationState.Loading) "ADICIONANDO..." else "ADICIONAR MEDICAMENTO",
                            onClick = {
                                when {
                                    medicationName.isBlank() -> errorMessage = "Por favor, insira o nome do medicamento"
                                    dosage.isBlank() -> errorMessage = "Por favor, insira a dosagem"
                                    frequency.isBlank() -> errorMessage = "Por favor, selecione a frequência"
                                    startDate.isBlank() -> errorMessage = "Por favor, insira a data de início"
                                    startDate.length != 10 -> errorMessage = "Por favor, insira uma data válida"
                                    endDate.isNotEmpty() && endDate.length != 10 -> errorMessage = "Por favor, insira uma data de fim válida"
                                    else -> {
                                        errorMessage = "" // Limpa mensagem de erro antes de tentar adicionar
                                        val medication = MedicationData(
                                            medName = medicationName,
                                            description = description,
                                            dosage = dosage,
                                            frequency = frequency,
                                            startDate = startDate,
                                            endDate = endDate,
                                            userId = userId
                                        )
                                        viewModel.insertMedication(medication)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(25.dp))
                        )

                        // indicador de progresso
                        if (medicationState is MedicationState.Loading) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = LightGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Preview da tela de adicionar medicamento
@Preview(showBackground = true)
@Composable
fun AddMedicationScreenPreview() {
    AddMedicationScreen(userId = "test-user-id")
}