package com.example.parkingorg

import HistorialItem
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_viewer)

        email = intent.getStringExtra("email") ?: ""
        matricula = intent.getStringExtra("matricula") ?: ""
        usuario = email.substringBefore("@")

        tarjetaImagen = findViewById(R.id.header_image)

        // Inicializar RecyclerView
        recyclerView = findViewById(R.id.recycler_historial)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Configurar lista y adaptador
        historialList = mutableListOf()
        historialAdapter = HistorialAdapter(historialList, usuario, matricula)
        recyclerView.adapter = historialAdapter

        // Referencia a Firebase
        database = FirebaseDatabase.getInstance().reference

        // Obtener los datos del historial
        obtenerHistorialDesdeFirebase()

        val backArrow = findViewById<ImageView>(R.id.backarrow)
        backArrow.setOnClickListener {
            finish() // Regresa a la actividad anterior
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
            override fun onDataChange(snapshot: DataSnapshot) {
                historialList.clear()
                for (registroSnapshot in snapshot.children) {
                    val fecha = registroSnapshot.child("fecha").getValue(String::class.java) ?: ""
                    val hora = registroSnapshot.child("hora").getValue(String::class.java) ?: ""
                    val tipo = registroSnapshot.child("tipo").getValue(String::class.java) ?: ""
                    val ticket = registroSnapshot.key ?: ""

                    // Extraer día y año
                    val partesFecha = fecha.split("-")
                    val anio = partesFecha.getOrNull(0) ?: ""
                    val dia = partesFecha.getOrNull(2) ?: ""

                    val item = HistorialItem(
                        id = ticket, // <-- Asegúrate de tener el campo id en tu modelo
                        fecha = fecha,
                        hora = hora,
                        tipo = tipo,
                        dia = dia,
                        anio = anio,
                        direccion = tipo,
                        ticket = ticket
                    )

                    historialList.add(item)
                }
                // ¡Notifica al adapter aquí!
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
}
