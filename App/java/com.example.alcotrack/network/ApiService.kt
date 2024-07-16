package com.example.alcotrack.network

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    @FormUrlEncoded
    @POST("register.php")
    fun registerUser(
        @Field("firstName") firstName: String,
        @Field("lastName") lastName: String,
        @Field("phone") phone: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("contactFirstName") contactFirstName: String,
        @Field("contactLastName") contactLastName: String,
        @Field("contactPhone") contactPhone: String,
        @Field("contactEmail") contactEmail: String
    ): Call<RegisterResponse>

}
