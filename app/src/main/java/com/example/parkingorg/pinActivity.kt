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
    private var pinConfirmacion: String? = null
    private val maxPinLength = 4
    private lateinit var pinViews: List<TextView>
    private lateinit var database: DatabaseReference
    private var email: String? = null
    private var modo: String? = null

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
                pinConfirmacion = pin
                pin = ""
                updatePinViews()
                Toast.makeText(this, "Vuelve a ingresar el PIN para confirmarlo", Toast.LENGTH_SHORT).show()
            } else {
                if (pin == pinConfirmacion) {
                    Toast.makeText(this, "PIN confirmado correctamente", Toast.LENGTH_SHORT).show()
                    registerUser()
                } else {
                    Toast.makeText(this, "Los PINs no coinciden. Inténtalo de nuevo", Toast.LENGTH_SHORT).show()
                    pinConfirmacion = null
                    pin = ""
                    updatePinViews()
                }
            }
        } else {
            verifyPin()
        }
    }

    private fun registerUser() {
        if (pinConfirmacion == null || pinConfirmacion!!.length != maxPinLength) {
            Toast.makeText(this, "Error: PIN inválido", Toast.LENGTH_SHORT).show()
            return
        }

        val emailUser = email!!.substringBefore("@")
        val userRef = database.child("usuarios").child(emailUser)

        val userData = mapOf(
            "email" to email,
            "pin" to pinConfirmacion,
            "FT" to true,
            "vehiculos" to mapOf(
                "--- ---" to mapOf("theme" to "default")
            )
        )

        userRef.setValue(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                navigateToLogin()
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
                    val isFirstTime = snapshot.child("FT").getValue(Boolean::class.java) ?: false

                    if (storedPin == pin) {
                        Toast.makeText(this@pinActivity, "Acceso autorizado", Toast.LENGTH_SHORT).show()
                        if (isFirstTime) {
                            userRef.child("FT").setValue(false)
                            navigateToTheme()
                        } else {
                            navigateToHome()
                        }
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

    private fun navigateToTheme() {
        val intent = Intent(this, ThemeActivity::class.java)
        intent.putExtra("email", email)
        startActivity(intent)
        finish()
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("email", email)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
