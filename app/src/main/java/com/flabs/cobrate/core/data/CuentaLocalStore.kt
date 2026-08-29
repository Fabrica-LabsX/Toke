package com.flabs.cobrate.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flabs.cobrate.core.model.Cuenta
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persistencia local de cuentas usando Jetpack DataStore (Preferences) con
 * serialización JSON manual vía kotlinx.serialization — MISMO patrón que
 * [OperationLocalStore].
 *
 * Se guarda la lista completa de cuentas como una única cadena JSON y, por
 * separado, el id de la "última sesión activa" (para restaurar la sesión al
 * reabrir la app sin pedir login de nuevo).
 *
 * Nota de seguridad: el hash de contraseña vive en el DTO (CuentaDto), NO en
 * el modelo de dominio [Cuenta]. Es una representación mínima de desarrollo
 * local, no un estándar de seguridad de producción.
 */
internal class CuentaLocalStore(private val context: Context) {

    private val Context.dataStore by preferencesDataStore(name = "cuentas")

    private val cuentasKey = stringPreferencesKey("lista_cuentas")
    private val sesionActivaIdKey = stringPreferencesKey("sesion_activa_id")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    // ---- Lista de cuentas (DTOs con hash) ----

    /** Lee las cuentas persistidas una sola vez (DTOs, incluyen el hash). */
    suspend fun leerUnaVez(): List<CuentaDto> =
        context.dataStore.data.map { prefs ->
            prefs[cuentasKey]?.let { raw ->
                runCatching {
                    json.decodeFromString<List<CuentaDto>>(raw)
                }.getOrElse { emptyList() }
            } ?: emptyList()
        }.first()

    /**
     * Guarda la lista completa de cuentas (sobrescribe lo anterior). Cada
     * [Cuenta] se persiste con su hash de contraseña tomado de [hashPorId].
     */
    suspend fun guardar(cuentas: List<Cuenta>, hashPorId: Map<String, String>) {
        val raw = json.encodeToString(
            cuentas.map { cuenta ->
                CuentaDto(
                    id = cuenta.id,
                    username = cuenta.username,
                    correoVerificado = cuenta.correoVerificado,
                    telefonoVerificado = cuenta.telefonoVerificado,
                    ultimoAcceso = cuenta.ultimoAcceso,
                    fechaCreacion = cuenta.fechaCreacion,
                    hashPassword = hashPorId[cuenta.id].orEmpty()
                )
            }
        )
        context.dataStore.edit { prefs ->
            prefs[cuentasKey] = raw
        }
    }

    // ---- Última sesión activa (SOLO lectura/migración; la escritura actual
    //      vive en SesionSeguraStore, almacenamiento cifrado) ----

    /**
     * Lee y elimina una posible sesión activa persistida en el DataStore viejo.
     * Se usa UNA vez al arrancar para migrar ese valor al almacenamiento
     * cifrado ([SesionSeguraStore]). Devuelve null si no había nada.
     */
    suspend fun leerSesionActivaViejaYLimpiar(): String? {
        val sesionId = context.dataStore.data
            .map { prefs -> prefs[sesionActivaIdKey] }
            .first()
        if (sesionId != null) {
            context.dataStore.edit { prefs -> prefs.remove(sesionActivaIdKey) }
        }
        return sesionId
    }

    // ---- DTO serializable (espejo del dominio + hash) ----

    @Serializable
    internal data class CuentaDto(
        val id: String,
        val username: String,
        val correoVerificado: String? = null,
        val telefonoVerificado: String? = null,
        val ultimoAcceso: Long = 0L,
        val fechaCreacion: Long = 0L,
        val hashPassword: String = ""
    )
}

// ---- Funciones de mapeo ----

/** Convierte un DTO persistido en el modelo de dominio [Cuenta] (sin hash). */
internal fun CuentaLocalStore.CuentaDto.aModelo(): Cuenta =
    Cuenta(
        id = id,
        username = username,
        correoVerificado = correoVerificado,
        telefonoVerificado = telefonoVerificado,
        ultimoAcceso = ultimoAcceso,
        fechaCreacion = fechaCreacion
    )