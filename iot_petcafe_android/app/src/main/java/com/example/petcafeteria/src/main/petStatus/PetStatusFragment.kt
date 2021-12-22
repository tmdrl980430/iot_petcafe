package com.example.petcafeteria.src.main.petStatus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebViewClient
import com.example.petcafeteria.R
import com.example.petcafeteria.config.BaseFragment
import com.example.petcafeteria.databinding.FragmentPetstatusBinding
import com.example.petcafeteria.src.main.petStatus.model.GetPetStatusResponse

class PetStatusFragment : BaseFragment<FragmentPetstatusBinding>(FragmentPetstatusBinding::bind, R.layout.fragment_petstatus), PetStatusFragmentView {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//
//        binding.petStatusLoadingBtn.setOnClickListener {
//
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("192.168.0.33"))
//            startActivity(intent)
//
//        }

        //val intent : Intent = ActionV
        val webview = binding.petStatusStatusIv
        webview.webViewClient = WebViewClient()
        webview.loadUrl("192.168.0.33")
    }

    override fun onGetUserSuccess(response: GetPetStatusResponse) {
        Log.d("ip 주소",response.IP)
    }

    override fun onGetUserFailure(message: String) {
        TODO("Not yet implemented")
    }

}