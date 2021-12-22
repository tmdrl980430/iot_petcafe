package com.example.petcafeteria.src.main.petStatus

import com.example.petcafeteria.src.main.petStatus.model.GetPetStatusResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface PetStatusRetrofitInterface {

    @GET("get_ip")
    fun getIP() : Call<GetPetStatusResponse>
}