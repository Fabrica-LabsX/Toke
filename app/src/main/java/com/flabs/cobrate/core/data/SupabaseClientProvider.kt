package com.flabs.cobrate.core.data

import com.flabs.cobrate.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

/**
 * Proveedor central del cliente de Supabase.
 *
 * CAPA 7a: solo plomería. El cliente queda disponible para capas posteriores
 * (Repositories, DataSources, migración de autenticación) pero todavía NO es
 * utilizado por ningún componente de la app.
 *
 * La URL y la anon key se sirven desde BuildConfig, que se rellenan en tiempo
 * de compilación a partir de `local.properties` (ver app/build.gradle.kts).
 */
object SupabaseClientProvider {

    private val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            httpEngine = OkHttp.create {
                config {
                    connectTimeout(20, TimeUnit.SECONDS)
                    readTimeout(20, TimeUnit.SECONDS)
                    writeTimeout(20, TimeUnit.SECONDS)
                    pingInterval(20, TimeUnit.SECONDS)
                    retryOnConnectionFailure(true)
                }
            }
            install(Postgrest)
            install(Auth)
            install(Realtime) {
                heartbeatInterval = 15.seconds
                reconnectDelay = 7.seconds
            }
            install(Storage)
        }
    }

    val supabase: SupabaseClient
        get() = client
}