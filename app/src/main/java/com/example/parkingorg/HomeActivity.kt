package com.example.parkingorg

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {
    private lateinit var tarjetaBase: ImageView
    private lateinit var qrGen: ImageView
    private lateinit var database: DatabaseReference
    private var email: String? = null
    private var mostrandoQR = false
    private var imagenOriginal: Bitmap? = null
    private lateinit var popup: FrameLayout
    private lateinit var botonAñadir: Button
    private lateinit var botonEliminar: Button
    private lateinit var botonCerrar: Button
    private lateinit var helpButton: Button
    private lateinit var bottomNav: BottomNavigationView

    private val themeColorHexMap = mapOf(
        "azul" to "#2997d8",
        "gris" to "#a9aac3",
        "naranja" to "#fdc99b",
        "verde" to "#c2e3da",
        "rojo" to "#cc4940",
        "rosa" to "#facfc8",
        "morado" to "#7e388d",
        "beige" to "#fff5e3"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tarjetaBase = findViewById(R.id.TarjetaBase)
        qrGen = findViewById(R.id.QRgen)
        popup = findViewById(R.id.emergente_contenedor)
        botonAñadir = findViewById(R.id.boton_añadir_vehiculo)
        botonEliminar = findViewById(R.id.boton_eliminar_vehiculo)
        botonCerrar = findViewById(R.id.boton_cerrar)
        bottomNav = findViewById(R.id.bottom_navigation)

        qrGen.visibility = View.INVISIBLE
        popup.visibility = View.GONE

        email = intent.getStringExtra("email")
        val user = email?.substringBefore("@") ?: return
        database = FirebaseDatabase.getInstance().reference
        val themeRef = database.child("usuarios").child(user).child("managed cars").child("--- ---").child("theme")

        themeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var theme = snapshot.getValue(String::class.java)?.lowercase()
                if (theme.isNullOrBlank() || !themeColorHexMap.containsKey(theme)) {
                    Toast.makeText(
                        this@HomeActivity,
                        "Tema no válido o no encontrado. Usando tema predeterminado.",
                        Toast.LENGTH_SHORT
                    ).show()
                    theme = "azul"
                }

                val resId = resources.getIdentifier(theme, "drawable", packageName)

                if (resId != 0) {
                    tarjetaBase.setImageResource(resId)
                    tarjetaBase.post {
                        tarjetaBase.isDrawingCacheEnabled = true
                        tarjetaBase.buildDrawingCache()
                        imagenOriginal = (tarjetaBase.drawable as? BitmapDrawable)?.bitmap
                    }
                } else {
                    Toast.makeText(this@HomeActivity, "Recurso de tema no encontrado", Toast.LENGTH_SHORT).show()
                }

                val color1 = themeColorHexMap[theme] ?: "#000000"
                val color2 = "#7e388d"

                val toggleQR: () -> Unit = {
                    mostrandoQR = !mostrandoQR

                    if (mostrandoQR) {
                        val qrTexto = generarTextoQR()
                        val logo = BitmapFactory.decodeResource(resources, R.drawable.logo_propio_4_3)
                        val qrBitmap = generarQRPersonalizado(qrTexto, 700, color1, color2, Color.WHITE, logo)
                        qrGen.setImageBitmap(qrBitmap)
                        qrGen.visibility = View.VISIBLE
                    } else {
                        qrGen.visibility = View.INVISIBLE
                    }
                }

                tarjetaBase.setOnClickListener { toggleQR() }
                qrGen.setOnClickListener { toggleQR() }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Error al cargar el tema", Toast.LENGTH_SHORT).show()
            }
        })

        // Manejo de botones
        botonAñadir.setOnClickListener {
            popup.visibility = View.VISIBLE
        }

        botonCerrar.setOnClickListener {
            popup.visibility = View.GONE
        }

        // Bottom Navigation acciones
        bottomNav.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.TarjetaBoton -> {
                    Toast.makeText(this, "Vista de Tarjeta", Toast.LENGTH_SHORT).show()
                    popup.visibility = View.GONE
                    true
                }
                R.id.GestionarBoton -> {
                    popup.visibility = View.VISIBLE
                    true
                }
                else -> false
            }
        }
    }

    private fun generarTextoQR(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val fechaHora = dateFormat.format(Date())
        return "Generado el: $fechaHora\nMensaje de prueba"
    }

    private fun generarQRPersonalizado(
        texto: String,
        tamaño: Int = 350,
        color1Hex: String,
        color2Hex: String,
        colorFondo: Int,
        logo: Bitmap? = null
    ): Bitmap {
        val color1 = Color.parseColor(color1Hex)
        val color2 = Color.parseColor(color2Hex)

        val bitMatrix: BitMatrix = MultiFormatWriter().encode(texto, BarcodeFormat.QR_CODE, tamaño, tamaño)
        val bmp = Bitmap.createBitmap(tamaño, tamaño, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(colorFondo)

        val gradient = LinearGradient(
            0f, 0f, 0f, tamaño.toFloat(),
            color1, color2, Shader.TileMode.CLAMP
        )

        val paint = Paint().apply {
            shader = gradient
            style = Paint.Style.FILL
            isAntiAlias = false
        }

        for (x in 0 until bitMatrix.width) {
            for (y in 0 until bitMatrix.height) {
                if (bitMatrix[x, y]) {
                    canvas.drawRect(
                        x.toFloat(), y.toFloat(),
                        (x + 1).toFloat(), (y + 1).toFloat(),
                        paint
                    )
                }
            }
        }

        logo?.let {
            val scaledLogo = Bitmap.createScaledBitmap(it, tamaño / 5, tamaño / 5, false)
            val centerX = (bmp.width - scaledLogo.width) / 2
            val centerY = (bmp.height - scaledLogo.height) / 2
            canvas.drawBitmap(scaledLogo, centerX.toFloat(), centerY.toFloat(), null)
        }

        val rounded = Bitmap.createBitmap(tamaño, tamaño, Bitmap.Config.ARGB_8888)
        val roundedCanvas = Canvas(rounded)
        val roundedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rectF = RectF(0f, 0f, tamaño.toFloat(), tamaño.toFloat())
        roundedCanvas.drawRoundRect(rectF, 60f, 60f, roundedPaint)
        roundedPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        roundedCanvas.drawBitmap(bmp, 0f, 0f, roundedPaint)

        return rounded
    }
}
