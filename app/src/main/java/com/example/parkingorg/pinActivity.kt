package com.example.parkingorg

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class pinActivity : AppCompatActivity() {

    private var pin = ""
    private var pinConfirmacion: String? = null  // Segunda entrada para confirmar PIN
    private val maxPinLength = 4
    private lateinit var pinViews: List<TextView>
    private lateinit var database: DatabaseReference
    private var email: String? = null
    private var modo: String? = null  // Variable para diferenciar Registro y Acceso

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        email = intent.getStringExtra("email")
        modo = intent.getStringExtra("modo") ?: "Acceso"

        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Email no recibido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        database = FirebaseDatabase.getInstance().reference

        // Inicializa los cuadros de PIN
        pinViews = listOf(
            findViewById(R.id.pin_view_1),
            findViewById(R.id.pin_view_2),
            findViewById(R.id.pin_view_3),
            findViewById(R.id.pin_view_4)
        )

        setupNumpad()

        findViewById<TextView>(R.id.back_button).setOnClickListener {
            finish()
        }
    }

    private fun setupNumpad() {
        val buttons = listOf(
            findViewById<Button>(R.id.button_0),
            findViewById<Button>(R.id.button_1),
            findViewById<Button>(R.id.button_2),
            findViewById<Button>(R.id.button_3),
            findViewById<Button>(R.id.button_4),
            findViewById<Button>(R.id.button_5),
            findViewById<Button>(R.id.button_6),
            findViewById<Button>(R.id.button_7),
            findViewById<Button>(R.id.button_8),
            findViewById<Button>(R.id.button_9)
        )

        for (button in buttons) {
            button.setOnClickListener {
                if (pin.length < maxPinLength) {
                    pin += button.text
                    updatePinViews()
                }
            }
        }

        findViewById<ImageButton>(R.id.button_delete).setOnClickListener {
            if (pin.isNotEmpty()) {
                pin = pin.dropLast(1)
                updatePinViews()
            }
        }

        findViewById<ImageButton>(R.id.button_confirm).setOnClickListener {
            if (pin.length == maxPinLength) {
                handlePinEntry()
            } else {
                Toast.makeText(this, "El PIN debe tener 4 dígitos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handlePinEntry() {
        if (modo == "Registro") {
            if (pinConfirmacion == null) {
                // Primer paso: Guardamos el PIN y pedimos confirmación
                pinConfirmacion = pin
                pin = ""
                updatePinViews()
                Toast.makeText(this, "Vuelve a ingresar el PIN para confirmarlo", Toast.LENGTH_SHORT).show()
            } else {
                // Segundo paso: Verificamos si coinciden
                if (pin == pinConfirmacion) {
                    registerUser()  // Guardamos el usuario en Firebase
                } else {
                    Toast.makeText(this, "Los PINs no coinciden. Inténtalo de nuevo", Toast.LENGTH_SHORT).show()
                    pinConfirmacion = null
                    pin = ""
                    updatePinViews()
                }
            }
        } else {
            verifyPin()  // En modo Acceso, solo verificamos el PIN
        }
    }

    private fun registerUser() {
        val emailUser = email!!.substringBefore("@")
        val userRef = database.child("usuarios").child(emailUser)

        userRef.setValue(mapOf("email" to email, "pin" to pin))
            .addOnSuccessListener {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                navigateToLogin() // Regresar a LoginActivity
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error en el registro", Toast.LENGTH_SHORT).show()
            }
    }

    private fun verifyPin() {
        val emailUser = email!!.substringBefore("@")
        val userRef = database.child("usuarios").child(emailUser)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val storedPin = snapshot.child("pin").getValue(String::class.java)

                    if (storedPin == pin) {
                        Toast.makeText(this@pinActivity, "Acceso autorizado", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                    } else {
                        Toast.makeText(this@pinActivity, "PIN incorrecto", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@pinActivity, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@pinActivity, "Error en la base de datos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updatePinViews() {
        for (i in pinViews.indices) {
            pinViews[i].text = if (i < pin.length) "●" else ""
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, ThemeActivity::class.java)
        intent.putExtra("email", email)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}

