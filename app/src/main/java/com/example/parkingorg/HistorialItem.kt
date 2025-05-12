package com.example.parkingorg

data class HistorialItem(
    val id: String,
    val fecha: String,
    val hora: String,
    val tipo: String,
    val dia: String,
    val anio: String,
    val direccion: String,
    val ticket: String,
    val typeaccess: String = "",
    var isExpanded: Boolean = false
)