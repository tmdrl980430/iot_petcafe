package com.example.petcafeteria.src.main

import android.os.Bundle
import android.util.Log
import com.example.petcafeteria.R
import com.example.petcafeteria.config.BaseActivity
import com.example.petcafeteria.databinding.ActivityMainBinding
import com.example.petcafeteria.src.main.home.HomeFragment
import com.example.petcafeteria.src.main.petStatus.PetStatusFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val ENDPOINT : String = "a1g1uldeb5uxbm-ats.iot.ap-northeast-2.amazonaws.com"
    private val POOL_ID : String = "ap-northeast-2"
    private val POLICY_NAME : String = "Provider"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNavigation()
    }


    private fun initNavigation() {

        supportFragmentManager.beginTransaction().replace(R.id.main_frm, HomeFragment()).commitAllowingStateLoss()

        binding.mainBtmNav.setOnNavigationItemSelectedListener(

            BottomNavigationView.OnNavigationItemSelectedListener { it ->
                when (it.itemId) {
                    R.id.menu_main_btm_nav_home -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, HomeFragment())
                            .commitAllowingStateLoss()
                        return@OnNavigationItemSelectedListener true
                    }

                    R.id.menu_main_btm_nav_pet_status -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, PetStatusFragment())
                            .commitAllowingStateLoss()
                        return@OnNavigationItemSelectedListener true
                    }
                }
                return@OnNavigationItemSelectedListener false
            })


    }
}
