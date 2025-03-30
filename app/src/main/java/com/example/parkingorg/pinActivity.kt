package com.example.parkingorg

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.callback.Callback
import com.auth0.android.result.DatabaseUser
import com.auth0.android.authentication.AuthenticationException

class pinActivity : AppCompatActivity() {

    private var pin = ""
    private var confirmPin = ""
    private val maxPinLength = 4
    private var isConfirming = false
    private lateinit var pinViews: List<TextView>
    private lateinit var account: Auth0
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        // Recibe los datos de RegisterActivity
        name = intent.getStringExtra("name") ?: ""
        email = intent.getStringExtra("email") ?: ""
        password = intent.getStringExtra("password") ?: ""

        account = Auth0(
            getString(R.string.com_auth0_client_id),
            getString(R.string.com_auth0_domain)
        )

        // Inicializa los cuadros de PIN
        pinViews = listOf(
            findViewById<TextView>(R.id.pin_view_1),
            findViewById<TextView>(R.id.pin_view_2),
            findViewById<TextView>(R.id.pin_view_3),
            findViewById<TextView>(R.id.pin_view_4)
        )

        // Configura los botones del numpad
        setupNumpad()

        // Configura el botón de regreso
        findViewById<TextView>(R.id.back_button).setOnClickListener {
            finish() // Regresa a la actividad anterior
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
                if (!isConfirming) {
                    confirmPin = pin
                    pin = ""
                    updatePinViews()
                    isConfirming = true
                    Toast.makeText(this, "Confirme contraseña", Toast.LENGTH_SHORT).show()
                } else {
                    if (pin == confirmPin) {
                        registerUser()
                    } else {
                        Toast.makeText(this, "PIN incorrecto, intentelo de nuevo", Toast.LENGTH_SHORT).show()
                        pin = ""
                        confirmPin = ""
                        updatePinViews()
                        isConfirming = false
                    }
                }
            }
        }
    }

    private fun registerUser() {
        val client = AuthenticationAPIClient(account)
        client.createUser(email = email, password = password, connection = "Username-Password-Authentication")
            .addParameter("name", name)
            .start(object : Callback<DatabaseUser, AuthenticationException> {
                override fun onSuccess(user: DatabaseUser) {
                    Toast.makeText(this@pinActivity, "Registro completado", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@pinActivity, LoginActivity::class.java))
                }

                override fun onFailure(exception: AuthenticationException) {
                    Toast.makeText(this@pinActivity, "Error en el registro: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updatePinViews() {
        for (i in pinViews.indices) {
            pinViews[i].text = if (i < pin.length) "●" else ""
        }
    }
}