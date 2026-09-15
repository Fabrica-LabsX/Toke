package com.flabs.cobrate.core.data

import com.flabs.cobrate.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.engine.okhttp.OkHttp

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
            httpEngine = OkHttp.create()
            install(Postgrest)
            install(Auth)
            install(Realtime)
        }
    }

    val supabase: SupabaseClient
        get() = client
}