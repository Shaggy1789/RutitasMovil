package mx.edu.uttt.rutitas.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.edu.uttt.rutitas.data.DemoData
import mx.edu.uttt.rutitas.model.DriverStatus
import mx.edu.uttt.rutitas.navigation.Routes
import mx.edu.uttt.rutitas.ui.components.*
import mx.edu.uttt.rutitas.ui.theme.RColors
import mx.edu.uttt.rutitas.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onTab: (String) -> Unit, onLogin: () -> Unit, onMyData: () -> Unit, onSchedule: () -> Unit,
    onTrips: () -> Unit, onPrivacy: () -> Unit, onDriver: (DriverStatus) -> Unit,
    onDisabled: () -> Unit, onLoggedOut: () -> Unit, vm: ProfileViewModel = hiltViewModel()
) {
    val s by vm.session.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val user = s.user
    var showDelete by remember { mutableStateOf(false) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = { PassengerBar(Routes.PROFILE, onTab) }) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Perfil", style = MaterialTheme.typography.headlineSmall)
            if (user == null) {
                InfoCard {
                    Text("Inicia sesión para ver tu perfil", style = MaterialTheme.typography.titleMedium)
                    Text("Guarda tus rutas, reservas y horario.", color = RColors.Muted)
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton("Iniciar sesión", onLogin)
                }
                return@Column
            }
            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(user.name, 60)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(user.name, style = MaterialTheme.typography.titleLarge)
                        Text(user.university, color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
                        Text("Turno ${s.shift.label}", color = RColors.Blue, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (s.isDisabled) DisabledBanner(onDetails = onDisabled)
            InfoCard {
                ProfileRow(Icons.Filled.Person, "Mis datos", onMyData)
                ProfileRow(Icons.Filled.Schedule, "Mi horario", onSchedule)
                ProfileRow(Icons.Filled.DirectionsCar, "Mis viajes", onTrips)
                ProfileRow(Icons.Filled.Lock, "Privacidad", onPrivacy)
            }
            InfoCard(color = RColors.CoralSoft) {
                Text("¿Tienes automóvil?", style = MaterialTheme.typography.titleMedium)
                Text("Comparte tu ruta con la comunidad UTTT y recupera gastos de gasolina.", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    when (s.driverStatus) {
                        DriverStatus.APPROVED -> "Ir al modo conductor"
                        DriverStatus.IN_REVIEW -> "Ver estado de mi registro"
                        DriverStatus.IN_PROGRESS -> "Continuar registro"
                        DriverStatus.NONE -> "Quiero ser conductor"
                    },
                    onClick = { onDriver(s.driverStatus) }, color = RColors.Coral
                )
            }
            InfoCard {
                Text("Opciones demo", style = MaterialTheme.typography.titleMedium)
                Text("Simula una cuenta inhabilitada:", color = RColors.Muted, style = MaterialTheme.typography.bodySmall)
                ChipGroup(DemoData.disabledReasons, emptySet()) { reason -> vm.disable(reason); onDisabled() }
                TextButton(onClick = { vm.resetDemo(); Toast.makeText(context, "Datos demo restablecidos", Toast.LENGTH_SHORT).show() }) {
                    Text("Restablecer datos demo")
                }
            }
            TextButton(onClick = { vm.logout(); onLoggedOut() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Icon(Icons.AutoMirrored.Filled.Logout, null); Spacer(Modifier.width(6.dp)); Text("Cerrar sesión")
            }
            TextButton(onClick = { showDelete = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Icon(Icons.Filled.Delete, null, tint = RColors.Coral); Spacer(Modifier.width(6.dp))
                Text("Eliminar cuenta", color = RColors.Coral)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
    if (showDelete) {
        DeleteAccountDialog(onDismiss = { showDelete = false }, onConfirm = {
            showDelete = false
            vm.deleteAccount {
                Toast.makeText(context, "Tu cuenta fue eliminada", Toast.LENGTH_LONG).show()
                onLoggedOut()
            }
        })
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = RColors.Blue)
        Spacer(Modifier.width(14.dp))
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = RColors.Muted)
    }
}

@Composable
fun MyDataScreen(onBack: () -> Unit, vm: ProfileViewModel = hiltViewModel()) {
    val s by vm.session.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf(s.user?.name.orEmpty()) }
    Scaffold(topBar = { RutitasTopBar("Mis datos", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(Modifier.padding(pad).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RutitasField(name, { name = it }, "Nombre", error = if (name.isBlank()) "El nombre no puede estar vacío" else null)
            RutitasField(s.user?.email.orEmpty(), {}, "Correo", enabled = false)
            RutitasField(s.user?.type?.label.orEmpty(), {}, "Tipo de usuario", enabled = false)
            RutitasField(s.zone ?: "Sin zona", {}, "Zona", enabled = false)
            RutitasField("${s.entryTime} – ${s.exitTime}", {}, "Horario", enabled = false)
            PrimaryButton("Guardar", enabled = name.isNotBlank(), onClick = {
                vm.updateName(name)
                Toast.makeText(context, "Datos guardados", Toast.LENGTH_SHORT).show()
                onBack()
            })
        }
    }
}

@Composable
fun DisabledAccountScreen(onBack: () -> Unit, onHome: () -> Unit, vm: ProfileViewModel = hiltViewModel()) {
    val s by vm.session.collectAsStateWithLifecycle()
    Scaffold(topBar = { RutitasTopBar("Estado de la cuenta", onBack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!s.isDisabled) {
                Icon(Icons.Filled.CheckCircle, null, tint = RColors.Blue, modifier = Modifier.size(64.dp))
                Text("Tu cuenta está activa", style = MaterialTheme.typography.headlineSmall)
                PrimaryButton("Volver al mapa", onHome)
                return@Column
            }
            Surface(shape = CircleShape, color = RColors.CoralSoft, modifier = Modifier.size(88.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Warning, null, tint = RColors.Coral, modifier = Modifier.size(48.dp)) }
            }
            Text("Cuenta inhabilitada", style = MaterialTheme.typography.headlineSmall)
            InfoCard {
                Text("Motivo", color = RColors.Muted, style = MaterialTheme.typography.labelMedium)
                Text(s.disabledReason ?: "Verificación pendiente", style = MaterialTheme.typography.titleMedium)
            }
            InfoCard {
                Text("Mientras tu cuenta esté inhabilitada no puedes:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text("• Reservar rutas\n• Publicar rutas\n• Aceptar solicitudes")
            }
            if (s.reviewRequested) {
                InfoCard(color = RColors.BlueSoft) { Text("Revisión solicitada. Te avisaremos cuando termine.") }
            } else {
                PrimaryButton("Solicitar revisión", onClick = { vm.requestReview() })
            }
            SecondaryButton("Volver al mapa", onHome)
            TextButton(onClick = { vm.enable() }) { Text("Reactivar cuenta (demo)") }
        }
    }
}
