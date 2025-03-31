package com.example.parkingorg

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var correoLogin: EditText
    private lateinit var entryLoginButton: Button
    private lateinit var registroLoginButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        database = FirebaseDatabase.getInstance().reference.child("usuarios")
        correoLogin = findViewById(R.id.correoLogin)
        entryLoginButton = findViewById(R.id.EntryLogin)
        registroLoginButton = findViewById(R.id.RegistroLogin)

        // Botón de registro para ir a RegisterActivity
        registroLoginButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Botón de login para verificar el correo en Firebase
        entryLoginButton.setOnClickListener {
            val email = correoLogin.text.toString().trim()

            if (email.isNotEmpty()) {
                verificarUsuarioEnFirebase(email)
            } else {
                Toast.makeText(this, "Ingresa un correo electrónico", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verificarUsuarioEnFirebase(email: String) {
        val usuarioCorreo = email.substringBefore("@") // Extraer la parte antes del '@'

        database.child(usuarioCorreo).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Si el usuario existe en Firebase, pasamos a la verificación del PIN
                    val intent = Intent(this@LoginActivity, pinActivity::class.java).apply {
                        putExtra("email", email)
                        putExtra("modo", "Acceso") // Modo acceso
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this@LoginActivity, "El correo no está registrado", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDB", "Error al acceder a la base de datos", error.toException())
                Toast.makeText(this@LoginActivity, "Error al verificar el usuario", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

