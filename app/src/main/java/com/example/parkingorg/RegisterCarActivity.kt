package com.example.parkingorg

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class RegisterCarActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_register)

        val matriculaEditText = findViewById<EditText>(R.id.Matricula_vehiculo)
        val marcaEditText = findViewById<EditText>(R.id.Marca_vehiculo)
        val modeloEditText = findViewById<EditText>(R.id.Modelo_vehiculo)
        val colorEditText = findViewById<EditText>(R.id.Color_vehiculo)
        val spinnerTipo = findViewById<Spinner>(R.id.spinner_tipo_vehiculo)
        val botonGuardar = findViewById<Button>(R.id.confirmarRegistroButton)
        email = intent.getStringExtra("email")
        val user = email?.substringBefore("@") ?: return

        val opciones = arrayOf("Carro", "Moto", "Otro")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipo.adapter = adapter

        database = FirebaseDatabase.getInstance().reference

        botonGuardar.setOnClickListener {
            val matricula = matriculaEditText.text.toString().trim()
            val marca = marcaEditText.text.toString().trim()
            val modelo = modeloEditText.text.toString().trim()
            val color = colorEditText.text.toString().trim()
            val tipo = spinnerTipo.selectedItem.toString()

            if (matricula.isEmpty() || marca.isEmpty() || modelo.isEmpty() || color.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val managedCarsRef = database.child("usuarios").child(user).child("managed_cars")

            managedCarsRef.child("--- ---").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.child("FT").getValue(Boolean::class.java) == true) {
                        val data = snapshot.value

                        managedCarsRef.child(matricula).setValue(data).addOnSuccessListener {
                            managedCarsRef.child(matricula).child("FT").setValue(false)
                            managedCarsRef.child("--- ---").removeValue()
                        }
                    }

                    val detalles = mapOf(
                        "matricula" to matricula,
                        "marca" to marca,
                        "modelo" to modelo,
                        "color" to color,
                        "tipo" to tipo
                    )

                    val carRef = managedCarsRef.child(matricula)
                    carRef.child("detalles_vehiculo").setValue(detalles)
                        .addOnSuccessListener {
                            val chosenPlateRef = database.child("usuarios").child(user).child("Chosen_plate")
                            chosenPlateRef.setValue(matricula)

                            carRef.child("theme").addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (!snapshot.exists()) {
                                        // Redirigir a ThemeActivity para seleccionar un tema
                                        val intent = Intent(this@RegisterCarActivity, ThemeActivity::class.java).apply {
                                            putExtra("fromRegister", true)
                                            putExtra("email", email)
                                            putExtra("matricula", matricula)
                                            putExtra("FT", true)
                                        }
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(applicationContext, "Vehículo guardado correctamente", Toast.LENGTH_SHORT).show()
                                    }
                                    finish()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(applicationContext, "Error al verificar el tema", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            })

                        }

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        val backArrow = findViewById<ImageView>(R.id.backarrow)
        backArrow.setOnClickListener {
            finish()
        }
    }
}
