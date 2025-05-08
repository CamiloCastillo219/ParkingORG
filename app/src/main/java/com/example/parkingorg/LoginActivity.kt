package com.example.parkingorg

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var correoLogin: EditText
    private lateinit var entryLoginButton: Button
    private lateinit var registroLoginButton: ImageButton
    private lateinit var accesoVisitantesBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        database = FirebaseDatabase.getInstance().reference.child("usuarios")
        correoLogin = findViewById(R.id.correoLogin)
        entryLoginButton = findViewById(R.id.EntryLogin)
        registroLoginButton = findViewById(R.id.RegistroLogin)
        accesoVisitantesBtn = findViewById(R.id.Acceso_visitantes)

        // Mostrar el dialog del PIN cuando se toca "Acceso de visitantes"
        accesoVisitantesBtn.setOnClickListener {
            val dialog = PinDialogFragment()
            dialog.show(supportFragmentManager, "PinDialog")
        }

        // Ir a registro
        registroLoginButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Verificar correo en Firebase
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
        val usuarioCorreo = email.substringBefore("@")

        database.child(usuarioCorreo).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val intent = Intent(this@LoginActivity, pinActivity::class.java).apply {
                        putExtra("email", email)
                        putExtra("modo", "Acceso")
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
