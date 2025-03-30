package com.example.parkingorg

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.AnimationUtils

class LogoCargaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carga_logo)

        // Cargar la animación
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        findViewById<View>(R.id.logo).startAnimation(animation)

        // Usar un Handler para ejecutar un retraso de 2 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            // Crear un Intent para iniciar LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // Finalizar la actividad actual
            finish()
            // Aplicar animación de transición
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 2000) // 2000 milisegundos = 2 segundos
    }
}