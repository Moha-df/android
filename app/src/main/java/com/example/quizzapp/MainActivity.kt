package com.example.quizzapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.quizzapp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Configuration de la barre de navigation
        binding.bottomNavigation.setupWithNavController(navController)

        // Gestion de la sélection dans la barre de navigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.playlistDetailFragment -> {
                    // Quand on est dans une playlist détaillée, on sélectionne l'onglet Playlists
                    binding.bottomNavigation.menu.findItem(R.id.navigation_playlist)?.isChecked = true
                }
            }
        }
    }
}