package ar.edu.algo2

import java.time.LocalDate

abstract class Lugar(
    val codigo:String,
    val nombre:String
){
    fun esDivertido(): Boolean {
        return (nombre.count { it.isLetter() } % 2 == 0) && esDivertidoParticular()
    }
    abstract fun esDivertidoParticular(): Boolean

    open fun esTranquilo(): Boolean{
        return false
    }
}

class Ciudad(
    codigo:String,
    nombre:String,
    var cantidadHabitantes:Int,
    var decibeles:Int
): Lugar(codigo, nombre){
    val atracciones = mutableListOf<String>()
    val cantidadHabitantesASuperar: Int = 100000

    override fun esDivertidoParticular(): Boolean {
        return (atracciones.size > 3) && (cantidadHabitantes > cantidadHabitantesASuperar)
    }

    override fun esTranquilo(): Boolean {
        return decibeles < 20
    }
}

class Pueblo(
    codigo:String,
    nombre:String,
    val estensionEnKm:Int,
    val fechaFundacion: LocalDate,
    val provincia: String,
): Lugar(codigo, nombre) {
    val provinciasLitoral = listOf("Entre Rios", "Corrientes", "Misiones")
    val anioComparacion: Int = 1800

    override fun esDivertidoParticular(): Boolean {
        return (fechaFundacion.year < anioComparacion) || (provinciasLitoral.contains(provincia))
    }

    override fun esTranquilo(): Boolean {
        return provincia == "La Pampa"
    }
}

class Balneario(
    codigo:String,
    nombre: String,
    var metrosPlaya:Int,
    var marPeligroso: Boolean,
    var peatonal: Boolean
):Lugar(codigo, nombre){
    val metrosDePlayaAComparar: Int = 300
    override fun esDivertidoParticular(): Boolean {
        return ((metrosPlaya > metrosDePlayaAComparar) && marPeligroso)
}

    override fun esTranquilo(): Boolean {
        return (!peatonal)
    }
}

/***************************************************************************************************/
class Persona(
    var presupuestoMaximo:Int,
    var mail:String,
    var dni: String
){
    lateinit var preferenciaParaVacaciones : Preferencia

    fun leGusta(lugar:Lugar): Boolean{
        return preferenciaParaVacaciones.leGusta(lugar)
    }
}
/*************************************************************************************************/
interface Preferencia{
    fun leGusta(lugar:Lugar): Boolean
}

object Tranquilo: Preferencia{
    override fun leGusta(lugar: Lugar): Boolean {
        return lugar.esTranquilo()
    }
}

object Divertido: Preferencia{
    override fun leGusta(lugar: Lugar): Boolean {
        return lugar.esDivertido()
    }
}

class Bipolar: Preferencia{
    val preferenciasARotar = mutableListOf<Preferencia>(Tranquilo,Divertido)

    override fun leGusta(lugar: Lugar): Boolean {
        val primero = preferenciasARotar.first()
        val resultado = primero.leGusta(lugar)

        if (resultado) {rotarPrefencias()}

        return resultado
    }

    fun rotarPrefencias(){
        val primero = preferenciasARotar.removeAt(0)
        preferenciasARotar.add(primero)
    }
}

class Combineta: Preferencia{
    val preferencias = mutableListOf<Preferencia>()

    override fun leGusta(lugar: Lugar): Boolean {
        return preferencias.any { it.leGusta(lugar) }
    }
}

/*************************************************************************************************/

class Tour(
    val fechaSalida: LocalDate,
    var cantidadTotalDePersonas: Int = 0,
    var montoPorPersona: Int
){
    val lugaresAVisitar = mutableListOf<Lugar>()
    val personasAnotadas = mutableListOf<Persona>()
    var confirmado: Boolean = false

    fun estaLleno(): Boolean{
        return personasAnotadas.size >= cantidadTotalDePersonas
    }

    fun confirmarTour(){
        confirmado = true
    }

    fun agregarPersonaATour(persona:Persona){
        if (!confirmado){ personasAnotadas.add(persona) }
        if (estaLleno()){ confirmarTour() }
    }

    fun agregarPersonaSinCondiciones(persona: Persona){
        personasAnotadas.add(persona)
    }

    fun quitarPersonaSinCondiciones(persona: Persona){
        personasAnotadas.remove(persona)

    }

}

/*************************************************************************************************/
class AministradorDeTours(){
    val toursADesignar = mutableListOf<Tour>()
    val personasParaAsignarTour = mutableListOf<Persona>()
    val personasPendientesDeAsignarTour = mutableListOf<Persona>()
    val observers = mutableListOf<PostConfirmacionObservers>()

    fun otorgarTourAPersonas() {
        personasParaAsignarTour.forEach { persona ->
            toursADesignar
                .filter { !it.confirmado }
                .filter { it.montoPorPersona <= persona.presupuestoMaximo }
                .find { it.lugaresAVisitar.all { lugar -> persona.leGusta(lugar) } }
                ?.also { agregarPersonaATour(it, persona) }
                ?: run { agregarPersonaAPendientesDeAsignar(persona) }
        }
    }

    fun agregarPersonaAPendientesDeAsignar(persona: Persona){
        personasPendientesDeAsignarTour.add(persona)
    }

    fun agregarPersonaATour(tour:Tour, persona:Persona){
        tour.agregarPersonaATour(persona)
        personasParaAsignarTour.remove(persona)
    }

    fun quitarPersonaDeTour(tour:Tour, persona:Persona){
        tour.quitarPersonaSinCondiciones(persona)
    }

    fun confirmarTour(tour: Tour){
        tour.confirmarTour()
        observers.forEach { it.ejecutar(tour)}
    }
}

/*************************************************************************************************/
interface PostConfirmacionObservers{
    fun ejecutar(tour: Tour)
}

class EnviarMail: PostConfirmacionObservers {
    lateinit var mailSender: MailSender

    override fun ejecutar(tour: Tour) {
        val fechaLimite = tour.fechaSalida.minusDays(30)

        mailSender.sendMail(
            Mail(
                from = "admin@admin.com",
                to = mailsDestino(tour),
                subject = "el tour ha sido confirmado!",
                content = "La fecha de salida será el día ${tour.fechaSalida}" +
                        "La fecha límite de pago será el día $fechaLimite" +
                        "Visitaremos los siguientes lugares:" +
                        lugaresAVisitarEnMail(tour)
            )
        )
    }

    fun mailsDestino(tour: Tour): String{
        return tour.personasAnotadas.joinToString(", ") {it.mail}
    }

    fun lugaresAVisitarEnMail(tour: Tour): String{
        return tour.lugaresAVisitar.joinToString(", ") {it.nombre}
    }

}
/***************************/
interface MailSender {
    fun sendMail(mail: Mail)
}

data class Mail(
    val from: String,
    val to: String,
    val subject: String,
    val content: String)

/***************************/
class MontoSuperadoInformarAFIP(val notificador: AFIPSender): PostConfirmacionObservers { //Constructor Injection
    val montoExceso: Int = 10_000_000

    override fun ejecutar(tour: Tour) {
        if (tour.montoPorPersona >= montoExceso){
            notificar(tour)
        }
    }

    fun notificar(tour:Tour){

        notificador.notificarAFIP(
            InterfazAFIP(
                codigosDeLugares(tour),
                dnisDePersonas(tour)
            )
        )
    }

    fun codigosDeLugares(tour:Tour): String{
        return tour.lugaresAVisitar.joinToString(", ") {it.codigo}
    }

    fun dnisDePersonas(tour:Tour): String{
        return tour.personasAnotadas.joinToString(", ") {it.dni}
    }
}
/***************************/
interface AFIPSender { // Como no te dice con qué manda simulamos otro tipo de sender
    fun notificarAFIP(data: InterfazAFIP)
}

data class InterfazAFIP(val codigos: String, val dnis: String)
/***************************/
class RotadorDePreferenciaBipolar: PostConfirmacionObservers{

    override fun ejecutar(tour: Tour) {
        val personas = tour.personasAnotadas

        personas.forEach { persona ->
            val preferencia = persona.preferenciaParaVacaciones

            if (preferencia is Bipolar) {

                preferencia.rotarPrefencias()
            }
        }
    }
}