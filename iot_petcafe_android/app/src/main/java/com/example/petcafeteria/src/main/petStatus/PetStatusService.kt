package com.example.petcafeteria.src.main.petStatus

import com.example.petcafeteria.config.ApplicationClass
import com.example.petcafeteria.src.main.petStatus.model.GetPetStatusResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PetStatusService (val view : PetStatusFragmentView){
    fun tryGetIPData() {
        val userinerface = ApplicationClass.sRetrofit.create(PetStatusRetrofitInterface::class.java)
        userinerface.getIP().enqueue(object : Callback<GetPetStatusResponse> {
            override fun onResponse(call: Call<GetPetStatusResponse>, response: Response<GetPetStatusResponse>) {
                view.onGetUserSuccess(response.body() as GetPetStatusResponse)

            }

            override fun onFailure(call: Call<GetPetStatusResponse>, t: Throwable) {
                view.onGetUserFailure(t.message ?: "통신 오류")
            }
        })
    }
}