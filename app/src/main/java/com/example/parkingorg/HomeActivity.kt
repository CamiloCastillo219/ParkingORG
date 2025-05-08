package com.example.parkingorg

import PinHostDialogFragment
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.journeyapps.barcodescanner.BarcodeEncoder
import android.graphics.Bitmap
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.zxing.BarcodeFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class HomeActivity : AppCompatActivity() {

    private lateinit var emergenteContenedor: View
    private lateinit var cerrarPopupButton: Button
    private lateinit var email: String
    private lateinit var currentUsername: String
    private lateinit var database: DatabaseReference
    private lateinit var matriculaHome: TextView
    private lateinit var tarjetaImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        emergenteContenedor = findViewById(R.id.emergente_contenedor)
        cerrarPopupButton = findViewById(R.id.boton_cerrar)
        val botonAñadirVehiculo = findViewById<Button>(R.id.boton_añadir_vehiculo)

        matriculaHome = findViewById(R.id.Matricula_home)

        email = intent.getStringExtra("email") ?: ""
        currentUsername = email.substringBefore("@")

        database = FirebaseDatabase.getInstance().reference

        emergenteContenedor.visibility = View.GONE
        tarjetaImage = findViewById(R.id.TarjetaBase)

        cerrarPopupButton.setOnClickListener {
            emergenteContenedor.visibility = View.GONE
        }

        botonAñadirVehiculo.setOnClickListener {
            val intent = Intent(this@HomeActivity, RegisterCarActivity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val menuButton = findViewById<ImageView>(R.id.lateralmenu)

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        val navigationView = findViewById<NavigationView>(R.id.navigation_view)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.Personalizar_tarjeta -> {
                    val intent = Intent(this@HomeActivity, ChangeThemeActivity::class.java)
                    intent.putExtra("email", email)
                    intent.putExtra("matricula", matriculaHome.text.toString())
                    startActivity(intent)
                }
                R.id.Codigo_invitado -> {
                    val dialog = PinHostDialogFragment()
                    val args = Bundle()
                    args.putString("usuario", currentUsername) //
                    args.putString("matricula", matriculaHome.text.toString())
                    dialog.arguments = args
                    dialog.show(supportFragmentManager, "PinHostDialog")
                }
                R.id.Cerrar_cuenta -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.Modo_escaner -> {
                    val intent = Intent(this, QrScannerActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                }
                R.id.Configuracion -> {
                    // Otra acción
                }
            }
            drawerLayout.closeDrawer(GravityCompat.END)
            true
        }

        val historialdirection = findViewById<ImageView>(R.id.Historial)

        historialdirection.setOnClickListener {
            val intent = Intent(this, HistorialActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("matricula", matriculaHome.text.toString())
            startActivity(intent)

        }



        val qrImageView = findViewById<ImageView>(R.id.QRgen)

        qrImageView.alpha = 0f

        qrImageView.setOnClickListener {
            val placa = matriculaHome.text.toString()

            if (placa == "Sin vehículo asignado") {
                Toast.makeText(this, "No hay vehículo asignado para generar el QR", Toast.LENGTH_SHORT).show()
                qrImageView.alpha = 0f
                return@setOnClickListener
            }

            if (qrImageView.alpha == 0f) {
                val horaActual = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val contenidoQR = "$currentUsername,$placa,$fechaActual,$horaActual"

                try {
                    val barcodeEncoder = BarcodeEncoder()
                    val bitmap: Bitmap = barcodeEncoder.encodeBitmap(contenidoQR, BarcodeFormat.QR_CODE, 400, 400)
                    qrImageView.setImageBitmap(bitmap)

                    // Hacemos fade-in animado
                    qrImageView.animate().alpha(1f).setDuration(500).start()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al generar QR", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            } else {
                qrImageView.animate().alpha(0f).setDuration(500).start()
            }
        }


        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.TarjetaBoton -> {
                    ocultarPopup()
                    true
                }
                R.id.GestionarBoton -> {
                    mostrarPopup()
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatosDelVehiculoElegido()
    }

    private fun cargarDatosDelVehiculoElegido() {
        val chosenPlateRef = database.child("usuarios").child(currentUsername).child("Chosen_plate")

        chosenPlateRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val placaElegida = snapshot.getValue(String::class.java)

                if (!placaElegida.isNullOrBlank() && placaElegida != "--__") {
                    val vehiculoRef = database.child("usuarios").child(currentUsername)
                        .child("managed_cars").child(placaElegida)

                    vehiculoRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(carSnapshot: DataSnapshot) {
                            val theme = carSnapshot.child("theme").getValue(String::class.java)
                            matriculaHome.text = placaElegida
                            tarjetaImage.setImageResource(resources.getIdentifier(theme, "drawable", packageName))
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@HomeActivity, "Error al cargar datos del vehículo: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    matriculaHome.text = "Sin vehículo asignado"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Error al leer Chosen_plate: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarPopup() {
        emergenteContenedor.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(100).start()
        }

        val contenedorBotones = findViewById<LinearLayout>(R.id.contenedor_botones_matriculas)
        val managedCarsRef = database.child("usuarios").child(currentUsername).child("managed_cars")

        contenedorBotones.removeAllViews()

        managedCarsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (carSnapshot in snapshot.children) {
                    val matricula = carSnapshot.key ?: continue

                    val fila = LinearLayout(this@HomeActivity)
                    fila.orientation = LinearLayout.HORIZONTAL
                    fila.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 8
                    }

                    val botonMatricula = Button(this@HomeActivity)
                    botonMatricula.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    botonMatricula.text = matricula
                    botonMatricula.setBackgroundResource(android.R.drawable.btn_default)
                    botonMatricula.setTextColor(resources.getColor(R.color.Negro, theme))
                    botonMatricula.typeface = ResourcesCompat.getFont(this@HomeActivity, R.font.alata)

                    botonMatricula.setOnClickListener {
                        val chosenPlateRef = database.child("usuarios").child(currentUsername).child("Chosen_plate")
                        chosenPlateRef.setValue(matricula).addOnSuccessListener {
                            Toast.makeText(this@HomeActivity, "Vehículo '$matricula' seleccionado", Toast.LENGTH_SHORT).show()
                            cargarDatosDelVehiculoElegido()
                            ocultarPopup()
                        }.addOnFailureListener {
                            Toast.makeText(this@HomeActivity, "Error al seleccionar vehículo", Toast.LENGTH_SHORT).show()
                        }
                    }

                    val botonEliminar = Button(this@HomeActivity)
                    botonEliminar.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    botonEliminar.text = "❌"
                    botonEliminar.setOnClickListener {
                        val vehiculoRef = database.child("usuarios").child(currentUsername).child("managed_cars").child(matricula)
                        vehiculoRef.removeValue().addOnSuccessListener {
                            Toast.makeText(this@HomeActivity, "Vehículo '$matricula' eliminado", Toast.LENGTH_SHORT).show()
                            mostrarPopup() // recargar lista
                        }.addOnFailureListener {
                            Toast.makeText(this@HomeActivity, "Error al eliminar vehículo", Toast.LENGTH_SHORT).show()
                        }
                    }

                    fila.addView(botonMatricula)
                    fila.addView(botonEliminar)

                    contenedorBotones.addView(fila)
                }
            }


            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Error al cargar matrículas", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun ocultarPopup() {
        emergenteContenedor.animate().alpha(0f).setDuration(80).withEndAction {
            emergenteContenedor.visibility = View.GONE
        }.start()
    }
}
