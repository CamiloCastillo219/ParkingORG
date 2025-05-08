package com.example.parkingorg

import HistorialItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class HistorialAdapter(
    private val historialList: MutableList<HistorialItem>,
    private val usuario: String,
    private val matricula: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_COMPACTO = 0
    private val VIEW_TYPE_EXPANDIDO = 1

    override fun getItemViewType(position: Int): Int {
        return if (historialList[position].isExpanded) VIEW_TYPE_EXPANDIDO else VIEW_TYPE_COMPACTO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_COMPACTO) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_historial, parent, false)
            CompactoViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detalle_historial, parent, false)
            ExpandidoViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = historialList[position]
        if (holder is CompactoViewHolder) {
            holder.bind(item)
        } else if (holder is ExpandidoViewHolder) {
            holder.bind(item)
        }
    }

    override fun getItemCount(): Int = historialList.size

    inner class CompactoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fechaText: TextView = itemView.findViewById(R.id.text_fecha)

        fun bind(item: HistorialItem) {
            fechaText.text = item.fecha

            itemView.setOnClickListener {
                item.isExpanded = true
                notifyItemChanged(adapterPosition)
            }
        }
    }

    inner class ExpandidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hora: TextView = itemView.findViewById(R.id.text_hora)
        private val dia: TextView = itemView.findViewById(R.id.text_dia)
        private val anio: TextView = itemView.findViewById(R.id.text_anio)
        private val direccion: TextView = itemView.findViewById(R.id.text_direccion)
        private val ticket: TextView = itemView.findViewById(R.id.text_ticket)
        private val cerrar: ImageView = itemView.findViewById(R.id.btn_cerrar)
        private val eliminar: TextView = itemView.findViewById(R.id.btn_eliminar)

        fun bind(item: HistorialItem) {
            hora.text = item.hora
            dia.text = item.dia
            anio.text = item.anio
            direccion.text = item.direccion
            ticket.text = item.ticket

            cerrar.setOnClickListener {
                item.isExpanded = false
                notifyItemChanged(adapterPosition)
            }

            eliminar.setOnClickListener {
                val ref = com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("usuarios")
                    .child(usuario)
                    .child("managed_cars")
                    .child(matricula)
                    .child("Historial")
                    .child(item.id) // Asegúrate de que tu modelo tenga el campo id

                ref.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val pos = adapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            historialList.removeAt(pos)
                            notifyItemRemoved(pos)
                        }
                    } else {
                        // Maneja el error si lo deseas
                    }
                }
            }


        }

    }
}
