package com.example.parkingorg

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*

class HistorialActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historialAdapter: HistorialAdapter
    private lateinit var database: DatabaseReference
    private lateinit var historialList: MutableList<HistorialItem>
    private lateinit var email: String
    private lateinit var matricula: String
    private lateinit var usuario: String
    private lateinit var tarjetaImagen: ImageView
    private lateinit var emergenteContenedor: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_viewer)

        email = intent.getStringExtra("email") ?: ""
        matricula = intent.getStringExtra("matricula") ?: ""
        usuario = email.substringBefore("@")

        tarjetaImagen = findViewById(R.id.header_image)
        emergenteContenedor = findViewById(R.id.emergente_contenedor)

        recyclerView = findViewById(R.id.recycler_historial)
        recyclerView.layoutManager = LinearLayoutManager(this)

        historialList = mutableListOf()
        historialAdapter = HistorialAdapter(historialList, usuario, matricula)
        recyclerView.adapter = historialAdapter

        database = FirebaseDatabase.getInstance().reference
        obtenerHistorialDesdeFirebase()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.TarjetaBoton -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("email", email)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    true
                }
                R.id.GestionarBoton -> {
                    mostrarPopup()
                    true
                }
                else -> false
            }
        }

        val backArrow = findViewById<ImageView>(R.id.backarrow)
        backArrow.setOnClickListener {
            finish()
        }
    }

    private fun obtenerHistorialDesdeFirebase() {
        val historialRef = database
            .child("usuarios")
            .child(usuario)
            .child("managed_cars")
            .child(matricula)
            .child("Historial")

        historialRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                historialList.clear()
                for (registroSnapshot in snapshot.children) {
                    val fecha = registroSnapshot.child("fecha").getValue(String::class.java) ?: ""
                    val hora = registroSnapshot.child("hora").getValue(String::class.java) ?: ""
                    val tipo = registroSnapshot.child("tipo").getValue(String::class.java) ?: ""
                    val Access = registroSnapshot.child("tipoAcceso").getValue(String::class.java) ?: ""
                    val ticket = registroSnapshot.key ?: ""

                    val partesFecha = fecha.split("-")
                    val anio = partesFecha.getOrNull(0) ?: ""
                    val dia = partesFecha.getOrNull(2) ?: ""

                    val item = HistorialItem(
                        id = ticket,
                        fecha = fecha,
                        hora = hora,
                        tipo = tipo,
                        dia = dia,
                        anio = anio,
                        direccion = tipo,
                        ticket = ticket,
                        typeaccess = Access
                    )

                    historialList.add(item)
                }
                historialAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al leer historial: ${error.message}")
                Toast.makeText(this@HistorialActivity, "Error al leer historial", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        cargarDatosDelVehiculoElegido()
    }

    private fun cargarDatosDelVehiculoElegido() {
        val chosenPlateRef = database.child("usuarios").child(usuario).child("Chosen_plate")

        chosenPlateRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val placaElegida = snapshot.getValue(String::class.java)

                if (!placaElegida.isNullOrBlank() && placaElegida != "--__") {
                    val vehiculoRef = database.child("usuarios").child(usuario)
                        .child("managed_cars").child(placaElegida)

                    vehiculoRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        @SuppressLint("DiscouragedApi")
                        override fun onDataChange(carSnapshot: DataSnapshot) {
                            val theme = carSnapshot.child("theme").getValue(String::class.java)
                            tarjetaImagen.setImageResource(resources.getIdentifier(theme, "drawable", packageName))
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@HistorialActivity, "Error al cargar datos del vehículo: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistorialActivity, "Error al leer Chosen_plate: ${error.message}", Toast.LENGTH_SHORT).show()
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
        val managedCarsRef = database.child("usuarios").child(usuario).child("managed_cars")

        contenedorBotones.removeAllViews()

        managedCarsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (carSnapshot in snapshot.children) {
                    val matricula = carSnapshot.key ?: continue

                    val fila = LinearLayout(this@HistorialActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { topMargin = 8 }
                    }

                    val botonMatricula = Button(this@HistorialActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        text = matricula
                        setBackgroundResource(android.R.drawable.btn_default)
                        setTextColor(resources.getColor(R.color.Negro, theme))
                        typeface = ResourcesCompat.getFont(this@HistorialActivity, R.font.alata)
                        setOnClickListener {
                            val chosenPlateRef = database.child("usuarios").child(usuario).child("Chosen_plate")
                            chosenPlateRef.setValue(matricula).addOnSuccessListener {
                                Toast.makeText(this@HistorialActivity, "Vehículo '$matricula' seleccionado", Toast.LENGTH_SHORT).show()
                                cargarDatosDelVehiculoElegido()
                                ocultarPopup()
                            }.addOnFailureListener {
                                Toast.makeText(this@HistorialActivity, "Error al seleccionar vehículo", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    val botonCerrar = findViewById<Button>(R.id.boton_cerrar)
                    botonCerrar.setOnClickListener {
                        ocultarPopup()
                    }


                    val botonEliminar = Button(this@HistorialActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        text = "❌"
                        setOnClickListener {
                            val vehiculoRef = database.child("usuarios").child(usuario).child("managed_cars").child(matricula)
                            vehiculoRef.removeValue().addOnSuccessListener {
                                Toast.makeText(this@HistorialActivity, "Vehículo '$matricula' eliminado", Toast.LENGTH_SHORT).show()
                                mostrarPopup()
                            }.addOnFailureListener {
                                Toast.makeText(this@HistorialActivity, "Error al eliminar vehículo", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    fila.addView(botonMatricula)
                    fila.addView(botonEliminar)
                    contenedorBotones.addView(fila)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistorialActivity, "Error al cargar matrículas", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun ocultarPopup() {
        emergenteContenedor.animate().alpha(0f).setDuration(80).withEndAction {
            emergenteContenedor.visibility = View.GONE
        }.start()
    }
}
