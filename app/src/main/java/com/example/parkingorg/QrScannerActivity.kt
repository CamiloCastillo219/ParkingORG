package com.example.parkingorg

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import android.util.Log

class QrScannerActivity : AppCompatActivity() {

    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var database: DatabaseReference
    private var tipoAcceso: String = ""
    private lateinit var currentUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        barcodeView = findViewById(R.id.barcode_scanner)
        database = FirebaseDatabase.getInstance().reference

        val email = "usuario@example.com"
        currentUsername = email.substringBefore("@")

        val emergenteContenedor = findViewById<FrameLayout>(R.id.Pregunta_de_puerta)
        val botonEntrada = findViewById<Button>(R.id.boton_entrada)
        val botonSalida = findViewById<Button>(R.id.boton_salida)
        val botonCerrar = findViewById<Button>(R.id.boton_cerrar)

        emergenteContenedor.visibility = View.VISIBLE

        botonEntrada.setOnClickListener {
            tipoAcceso = "entrada"
            emergenteContenedor.visibility = View.GONE
            checkCameraPermissionAndStartScanner()
        }

        botonSalida.setOnClickListener {
            tipoAcceso = "salida"
            emergenteContenedor.visibility = View.GONE
            checkCameraPermissionAndStartScanner()
        }

        botonCerrar.setOnClickListener {
            emergenteContenedor.visibility = View.GONE
        }
    }

    private fun checkCameraPermissionAndStartScanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1001)
        } else {
            startScanner()
        }
    }

    private fun startScanner() {
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.let {
                    val contenido = it.text
                    Toast.makeText(this@QrScannerActivity, "Contenido leído: $contenido", Toast.LENGTH_LONG).show()

                    val partes = contenido.split(",")

                    if (partes.size >= 4) {
                        val usuarios = partes[0]
                        val matricula = partes[1]
                        val fecha = partes[2]
                        val hora = partes[3]
                        val Access = partes[4]

                        enviarDatosABaseDeDatos(usuarios, matricula, fecha, hora, tipoAcceso, Access)

                        barcodeView.pause()
                    } else {
                        Toast.makeText(this@QrScannerActivity, "Formato de QR incorrecto", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {}
        })
        barcodeView.resume()
    }

    private fun enviarDatosABaseDeDatos(usuario: String, matricula: String, fecha: String, hora: String, tipo: String, Access: String) {
        val historialRef = database.child("usuarios").child(usuario).child("managed_cars").child(matricula).child("Historial")

        historialRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("DefaultLocale")
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount + 1
                val registroId = String.format("%04d", count)

                val registro = mapOf(
                    "fecha" to fecha,
                    "hora" to hora,
                    "tipo" to tipo,
                    "tipoAcceso" to Access
                )

                historialRef.child(registroId).setValue(registro)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Datos guardados exitosamente")
                        Toast.makeText(this@QrScannerActivity, "Datos guardados exitosamente", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Log.d("Firebase", "Error al guardar los datos: ${it.message}")
                        Toast.makeText(this@QrScannerActivity, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Firebase", "Error al acceder a la base de datos: ${error.message}")
                Toast.makeText(this@QrScannerActivity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            barcodeView.resume()
        }
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanner()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
