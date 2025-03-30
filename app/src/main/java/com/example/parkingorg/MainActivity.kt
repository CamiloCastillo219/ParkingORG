package com.example.parkingorg

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Color_azul_base)
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Configurar el listener para los insets de la ventana
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Usar un Handler para ejecutar un retraso de 2 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            // Crear un Intent para iniciar LogoCargaActivity
            val intent = Intent(this, LogoCargaActivity::class.java)
            startActivity(intent)
            // Finalizar la actividad actual si no deseas que el usuario pueda volver a ella
            finish()
        }, 2000) // 2000 milisegundos = 2 segundos
    }
}