package com.example.parkingorg

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ThemeActivity : AppCompatActivity() {

    private lateinit var previewImage: ImageView
    private lateinit var confirmButton: Button
    private var selectedButton: Button? = null // Variable para el botón seleccionado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme)

        // Referencias a los elementos
        previewImage = findViewById(R.id.imageView)
        confirmButton = findViewById(R.id.confirmtheme)

        // Lista de botones de colores
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

        // Mapeo de colores y drawables
        val colorConfig = mapOf(
            R.id.button_1 to Pair("#2997d8", R.drawable.azul),
            R.id.button_2 to Pair("#a9aac3", R.drawable.gris),
            R.id.button_3 to Pair("#fdc99b", R.drawable.naranja),
            R.id.button_4 to Pair("#c2e3da", R.drawable.verde),
            R.id.button_5 to Pair("#cc4940", R.drawable.rojo),
            R.id.button_6 to Pair("#facfc8", R.drawable.rosa),
            R.id.button_7 to Pair("#7e388d", R.drawable.morado),
            R.id.button_8 to Pair("#fff5e3", R.drawable.beige)
        )

        buttons.forEach { button ->
            button.setOnClickListener {
                // Restaurar el tamaño y color del botón anterior
                selectedButton?.let { prevButton ->
                    prevButton.setBackgroundResource(R.drawable.button_default) // Restaurar el fondo
                    resizeButton(prevButton, 96) // Restaurar tamaño original
                }

                // Seleccionar nuevo botón y poner contorno
                selectedButton = button
                button.setBackgroundResource(R.drawable.button_selected)

                // Aumentar el tamaño del botón seleccionado
                resizeButton(button, 96)

                // Obtener el color y el drawable correspondiente
                val (color, drawable) = colorConfig[button.id] ?: return@setOnClickListener

                // Aplicar cambios en el botón principal
                confirmButton.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(color))

                // Ajustar color del texto en función del fondo
                if (button.id == R.id.button_5 || button.id == R.id.button_7 || button.id == R.id.button_1) {
                    confirmButton.setTextColor(Color.WHITE)
                } else {
                    confirmButton.setTextColor(Color.BLACK)
                }

                // Cambiar la imagen
                previewImage.setImageResource(drawable)
            }
        }
    }

    // Función para cambiar el tamaño del botón con animación
    private fun resizeButton(button: Button, newSize: Int) {
        val params = button.layoutParams as ViewGroup.LayoutParams
        val animator = ValueAnimator.ofInt(params.width, newSize)
        animator.duration = 200 // Duración de la animación
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            params.width = value
            params.height = value
            button.layoutParams = params
        }
        animator.start()
    }
}

