package com.example.mrs_tango.core.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://dzkwoogyrikmulcmnctz.supabase.co",   // dari dashboard Supabase
        supabaseKey = "sb_publishable_put5MLIn6WKPSwOnXbAI4A_q-yiOFvC"                    // anon key
    ) {
        install(Postgrest){
            defaultSchema = "mobile"
        }
        install(Auth)
    }
}