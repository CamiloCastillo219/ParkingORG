package com.example.parkingorg

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {
    private lateinit var tarjetaBase: ImageView
    private lateinit var database: DatabaseReference
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tarjetaBase = findViewById(R.id.TarjetaBase)
        email = intent.getStringExtra("email")

        // Obtener referencia a la base de datos
        val user = email?.substringBefore("@") ?: return
        database = FirebaseDatabase.getInstance().reference
        val themeRef = database.child("usuarios").child(user).child("managed cars").child("--- ---").child("theme")

        // Leer el tema desde Firebase
        themeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val theme = snapshot.getValue(String::class.java) ?: return
                val resId = resources.getIdentifier(theme.lowercase(), "drawable", packageName)

                if (resId != 0) {
                    tarjetaBase.setImageResource(resId)
                } else {
                    Toast.makeText(this@HomeActivity, "Tema no encontrado", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Error al cargar el tema", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
