package com.example.mrs_tango.core.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://dzkwoogyrikmulcmnctz.supabase.co", // dari dashboard Supabase
        supabaseKey = "sb_publishable_put5MLIn6WKPSwOnXbAI4A_q-yiOFvC" // anon key
    ) {
        defaultSerializer = KotlinXSerializer(
            Json {
                ignoreUnknownKeys = true
            }
        )
        install(Postgrest){
            defaultSchema = "mobile"
        }
        install(Auth)
        install(Storage)
    }
}