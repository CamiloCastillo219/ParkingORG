package com.example.parkingorg

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.Auth0

class RegisterActivity : AppCompatActivity() {

    private lateinit var account: Auth0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        account = Auth0(
            getString(R.string.com_auth0_client_id),
            getString(R.string.com_auth0_domain)
        )

        val registerButton = findViewById<Button>(R.id.confirmarRegistroButton)
        registerButton.setOnClickListener {
            val name = findViewById<EditText>(R.id.nombreCompleto).text.toString()
            val email = findViewById<EditText>(R.id.correoLogin).text.toString()
            val password = findViewById<EditText>(R.id.contraseña).text.toString()
            val confirmPassword = findViewById<EditText>(R.id.confirmarContraseña).text.toString()

            if (password == confirmPassword) {
                // Enviar datos a PinActivity con "modo: Registro"
                val intent = Intent(this, pinActivity::class.java).apply {
                    putExtra("name", name)
                    putExtra("email", email)
                    putExtra("password", password)
                    putExtra("modo", "Registro") // Agregamos el modo
                }
                startActivity(intent)
            } else {
                showToast("Las contraseñas no coinciden")
            }
        }

        val backArrow = findViewById<ImageView>(R.id.backarrow)
        backArrow.setOnClickListener {
            finish() // Regresa a la actividad anterior
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
