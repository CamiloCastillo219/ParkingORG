package com.example.parkingorg

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class ChangeThemeActivity : AppCompatActivity() {

    private lateinit var previewImage: ImageView
    private lateinit var confirmButton: Button
    private var email: String? = null
    private var themeName: String? = null
    private var selectedButton: Button? = null
    private var matricula: String? = null
    private var currentTheme: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme)

        previewImage = findViewById(R.id.imageView)
        confirmButton = findViewById(R.id.confirmtheme)

        // Obtener datos desde el Intent
        email = intent.getStringExtra("email")
        matricula = intent.getStringExtra("matricula")
        currentTheme = intent.getStringExtra("theme") // Puede ser null si no se pasó

        val colorConfig = mapOf(
            R.id.button_1 to Triple("#2997d8", R.drawable.azul, "azul"),
            R.id.button_2 to Triple("#a9aac3", R.drawable.gris, "gris"),
            R.id.button_3 to Triple("#fdc99b", R.drawable.naranja, "naranja"),
            R.id.button_4 to Triple("#c2e3da", R.drawable.verde, "verde"),
            R.id.button_5 to Triple("#cc4940", R.drawable.rojo, "rojo"),
            R.id.button_6 to Triple("#facfc8", R.drawable.rosa, "rosa"),
            R.id.button_7 to Triple("#7e388d", R.drawable.morado, "morado"),
            R.id.button_8 to Triple("#fff5e3", R.drawable.beige, "beige")
        )

        val buttons = listOf(
            findViewById<Button>(R.id.button_1),
            findViewById<Button>(R.id.button_2),
            findViewById<Button>(R.id.button_3),
            findViewById<Button>(R.id.button_4),
            findViewById<Button>(R.id.button_5),
            findViewById<Button>(R.id.button_6),
            findViewById<Button>(R.id.button_7),
            findViewById<Button>(R.id.button_8)
        )

        // Listener para cada botón de tema
        buttons.forEach { button ->
            button.setOnClickListener {
                selectThemeButton(button, colorConfig)
            }
        }

        // Preseleccionar el tema actual si existe
        currentTheme?.let { theme ->
            colorConfig.entries.find { it.value.third == theme }?.let { entry ->
                val button = findViewById<Button>(entry.key)
                selectThemeButton(button, colorConfig)
            }
        }

        // Confirmar selección
        confirmButton.setOnClickListener {
            saveTheme()
        }
    }

    private fun selectThemeButton(button: Button, colorConfig: Map<Int, Triple<String, Int, String>>) {
        selectedButton?.let { prevButton ->
            prevButton.setBackgroundResource(R.drawable.button_default)
            resizeButton(prevButton, 96)
        }

        selectedButton = button
        button.setBackgroundResource(R.drawable.button_selected)
        resizeButton(button, 96)

        val (color, drawable, name) = colorConfig[button.id] ?: return
        themeName = name

        confirmButton.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(color))
        confirmButton.setTextColor(getColorText(button.id))
        previewImage.setImageResource(drawable)
    }

    private fun saveTheme() {
        if (themeName == null) {
            Toast.makeText(this, "Selecciona un tema primero", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = email?.substringBefore("@")
        val carKey = matricula ?: "--- ---"

        if (userId == null) {
            Toast.makeText(this, "Error: Email inválido", Toast.LENGTH_SHORT).show()
            return
        }

        val carRef = FirebaseDatabase.getInstance()
            .reference.child("usuarios").child(userId)
            .child("managed_cars").child(carKey)

        val data = mapOf("theme" to themeName)

        carRef.updateChildren(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Tema actualizado correctamente", Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar el tema", Toast.LENGTH_SHORT).show()
            }
    }

    private fun resizeButton(button: Button, newSize: Int) {
        val params = button.layoutParams as ViewGroup.LayoutParams
        val animator = ValueAnimator.ofInt(params.width, newSize)
        animator.duration = 200
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            params.width = value
            params.height = value
            button.layoutParams = params
        }
        animator.start()
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("email", email)
        startActivity(intent)
        finish()
    }

    private fun getColorText(buttonId: Int): Int {
        return if (buttonId in listOf(R.id.button_5, R.id.button_7, R.id.button_1)) Color.WHITE else Color.BLACK
    }
}
