package com.example.parkingorg

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextClock
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PinHostDialogFragment : DialogFragment() {

    private lateinit var pinEditText: EditText
    private lateinit var countdownTextClock: TextClock
    private lateinit var backButton: TextView
    private lateinit var timer: CountDownTimer
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.invitation_code_layout, container, false)

        pinEditText = view.findViewById(R.id.Invitation_pin)
        countdownTextClock = view.findViewById(R.id.textClock)
        backButton = view.findViewById(R.id.back_button)

        pinEditText.isFocusable = false
        pinEditText.isClickable = false

        database = FirebaseDatabase.getInstance().reference

        val usuario = arguments?.getString("usuario") ?: ""
        val matricula = arguments?.getString("matricula") ?: ""

        val pin = (100000..999999).random().toString()
        pinEditText.setText(pin)

        if (usuario.isNotEmpty() && matricula.isNotEmpty()) {
            val ref = database.child("Codigos_inv")
                .child(pin)
            ref.setValue("$usuario,$matricula")
        }

        iniciarCuentaRegresiva()

        backButton.setOnClickListener {
            timer.cancel()
            dismiss()
        }

        return view
    }

    private fun iniciarCuentaRegresiva() {
        timer = object : CountDownTimer(30_000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                countdownTextClock.text = "Expira en: $seconds s"
            }

            override fun onFinish() {
                dismiss()
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer.cancel()
    }
}
