package com.example.mrs_tango.core.data.repository

import com.example.mrs_tango.core.data.model.UserModel
import com.example.mrs_tango.core.data.remote.SupabaseClient
import com.example.mrs_tango.core.utils.Result
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {
    suspend fun registerCustomer(user: UserModel): Result<UserModel> {
        return withContext(Dispatchers.IO) {
            try {
                val existing = SupabaseClient.client
                    .from("users")
                    .select {
                        filter { eq("email", user.email) }
                    }
                    .decodeSingleOrNull<UserModel>()

                if (existing != null) {
                    return@withContext Result.Error("Email sudah terdaftar")
                }

                // 1. Timpa/set value 'role' secara spesifik menjadi "customer"
                val customerUser = user.copy(role = "customer", password_set = 1)

                // 2. Insert menggunakan objek 'customerUser' yang baru
                val result = SupabaseClient.client
                    .from("users")
                    .insert(customerUser) {
                        select()
                    }
                    .decodeSingle<UserModel>()

                Result.Success(result)

            } catch (e: Exception) {
                Result.Error(e.message ?: "Terjadi kesalahan saat menghubungi server")
            }
        }
    }

    suspend fun loginUser(emailInput: String, passwordInput: String): Result<UserModel> {
        return withContext(Dispatchers.IO) {
            try {
                val user = SupabaseClient.client // 1. Cari user berdasarkan email
                    .from("users")
                    .select {
                        filter {
                            eq("email", emailInput)
                        }
                    }
                    .decodeSingleOrNull<UserModel>()

                if (user == null || user.password != passwordInput) { // 3. Validasi kecocokan password (plain text sementara)
                    return@withContext Result.Error("Email atau Password salah")
                }

                if (user.is_active == 0) { // 4. Validasi status akun aktif (opsional tapi disarankan)
                    return@withContext Result.Error("Akun Anda dinonaktifkan")
                }

                Result.Success(user) // 5. Kembalikan objek UserModel lengkap (termasuk informasi role)

            } catch (e: Exception) {
                Result.Error(e.message ?: "Terjadi kesalahan saat menghubungi server")
            }
        }
    }
}