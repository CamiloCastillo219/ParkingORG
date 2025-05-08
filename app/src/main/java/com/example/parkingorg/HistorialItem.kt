data class HistorialItem(
    val id: String, // <-- este será el key de Firebase
    val fecha: String,
    val hora: String,
    val tipo: String,
    val dia: String,
    val anio: String,
    val direccion: String,
    val ticket: String,
    var isExpanded: Boolean = false
)