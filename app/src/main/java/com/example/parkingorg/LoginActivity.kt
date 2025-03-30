package com.example.parkingorg

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Encontrar el botón por su ID
        val entryLoginButton = findViewById<ImageButton>(R.id.RegistroLogin)

        // Configurar el OnClickListener
        entryLoginButton.setOnClickListener {
            // Crear un Intent para iniciar RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}