package com.example.petcafeteria.src.main.petStatus

import com.example.petcafeteria.src.main.petStatus.model.GetPetStatusResponse

interface PetStatusFragmentView {
    fun onGetUserSuccess(response: GetPetStatusResponse)

    fun onGetUserFailure(message: String)

}