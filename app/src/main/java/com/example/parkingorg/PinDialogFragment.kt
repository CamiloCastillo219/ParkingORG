package com.example.parkingorg

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.*
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class PinDialogFragment : DialogFragment() {

    private lateinit var pinEditText: EditText
    private lateinit var qrImageView: ImageView
    private lateinit var database: DatabaseReference
    private lateinit var pinleyendText: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.pin_dialog_layout)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        pinEditText = dialog.findViewById(R.id.pin_edit_text)
        pinleyendText = dialog.findViewById(R.id.leyenda_leer)
        qrImageView = dialog.findViewById(R.id.QRgen)

        qrImageView.visibility = View.GONE

        database = FirebaseDatabase.getInstance().reference

        pinEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pin = s.toString()
                if (pin.length == 6) {
                    val ref = database.child("Codigos_inv").child(pin)
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(context, "Código de invitación correcto", Toast.LENGTH_SHORT).show()

                                pinEditText.visibility = View.GONE
                                pinleyendText.visibility = View.GONE
                                qrImageView.visibility = View.VISIBLE
                                qrImageView.alpha = 1f

                                val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                                val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                val tipoAcceso = "visitante"
                                val contenidoQR = "${snapshot.value},$fecha,$hora,$tipoAcceso"

                                try {
                                    val barcodeEncoder = BarcodeEncoder()
                                    val bitmap: Bitmap = barcodeEncoder.encodeBitmap(contenidoQR, BarcodeFormat.QR_CODE, 400, 400)
                                    qrImageView.setImageBitmap(bitmap)
                                    Log.d("QR_GENERATION", "QR generado con éxito.")


                                    Handler().postDelayed({
                                        ref.removeValue()
                                            .addOnSuccessListener {
                                                Log.d("PIN_DELETE", "Código eliminado exitosamente después de 1 minuto.")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("PIN_DELETE_ERROR", "Error al eliminar el código: ${e.message}")
                                            }
                                    }, 60_000)

                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error al generar QR", Toast.LENGTH_SHORT).show()
                                    Log.e("QR_ERROR", "Error generando QR", e)
                                }

                            } else {
                                Toast.makeText(context, "Código de invitación incorrecto", Toast.LENGTH_SHORT).show()
                                Log.d("PIN_CHECK", "PIN no encontrado en Firebase.")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Error en la base de datos", Toast.LENGTH_SHORT).show()
                            Log.e("FIREBASE_ERROR", "Error: ${error.message}")
                        }
                    })
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        dialog.findViewById<TextView>(R.id.back_button).setOnClickListener {
            dismiss()
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.7).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
