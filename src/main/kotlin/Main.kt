package ar.edu.unsam.algo2.sobredosistv

import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.random.Random


/***************************************************************************************************************/
class Programa {
    var titulo = ""
    var presentadores = mutableListOf<Presentador>()
    var presupuesto = 10000
    var sponsors = mutableListOf<String>()
    var dias = mutableListOf<DayOfWeek>()
    var duracion: Int = 30
    val ratings = mutableListOf<Rating>()

    val restricciones = mutableListOf<RestriccionPrograma>()

    fun revisar(grilla: Grilla) {
        val primeraRestriccion = restricciones.find { restriccion -> !restriccion.seCumple(this) }

        primeraRestriccion?.ejecutarAcciones(this, grilla)
    }

    fun promedioDeRatings() = ratings.sortedBy { it.fecha }
        .takeLast(5)
        .map { it.valor }
        .average()

    fun conducidoPor(nombrePresentador: String) =
        presentadores.any { presentador -> presentador.nombre == nombrePresentador }

    fun mitadPresentadores() = presentadores.take(presentadores.size / 2)

    fun segundaMitadPresentadores() =
        presentadores.minus(mitadPresentadores().toSet())

    fun mitadPresupuesto() = presupuesto / 2

    fun tituloEnPalabras() = titulo.split(" ")

    fun presentadorPrincipal(): Presentador = presentadores[0]

    fun mailsConductores() = presentadores.map { it.email }

    fun cantidadConductores() = presentadores.size
}

/************************/
data class Rating(val valor: Double, val fecha: LocalDate)

data class Presentador(val nombre: String, val email: String)

/***************************************************************************************************************/
abstract class RestriccionPrograma {
    val acciones = mutableListOf<AccionRevisionPrograma>()

    abstract fun seCumple(programa: Programa): Boolean

    fun ejecutarAcciones(programa: Programa, grilla: Grilla) {
        acciones.forEach { accion ->
            accion.ejecutar(programa, grilla) }
    }
}

class MinimoDeRating(var promedioMinimo: Double) : RestriccionPrograma() {
    override fun seCumple(programa: Programa) =
        programa.promedioDeRatings() > promedioMinimo
}

class MaximoDeConductoresPrincipales(val cantidadMaxima: Int) : RestriccionPrograma(){
    override fun seCumple(programa: Programa) =
        programa.cantidadConductores() <= cantidadMaxima
}

class PresentadorEspecifico(val nombrePresentador: String) : RestriccionPrograma() {
    override fun seCumple(programa: Programa) =
        programa.conducidoPor(nombrePresentador)

}

class NoExcederPresupuesto(val presupuestoDeseado: Double): RestriccionPrograma(){
    override fun seCumple(programa: Programa) =
        programa.presupuesto <= presupuestoDeseado
}

class RestriccionOrCompuesta(val restricciones: List<RestriccionPrograma>) : RestriccionPrograma() {
    override fun seCumple(programa: Programa) =
        restricciones.any { it.seCumple(programa) }
}

class RestriccionAndCompuesta(val restricciones: List<RestriccionPrograma>) : RestriccionPrograma() {
    override fun seCumple(programa: Programa) =
        restricciones.all { it.seCumple(programa) }
}

class RestriccionVacia(): RestriccionPrograma(){
    override fun seCumple(programa: Programa) =
        true//Null Object Pattern
}

/***************************************************************************************************************/
interface AccionRevisionPrograma {
    fun ejecutar(programa: Programa, grilla: Grilla)
}

/************************/
class PartirProgramaEn2 : AccionRevisionPrograma {
    override fun ejecutar(programa: Programa, grilla: Grilla) {
        val mitadPresentadores = programa.mitadPresentadores()

        val programa1 = Programa().apply {
            presentadores = mitadPresentadores.toMutableList()
            presupuesto = programa.mitadPresupuesto()
            sponsors = programa.sponsors
            titulo = "${programa.tituloEnPalabras()[0]} en el aire!"
            dias = programa.dias
        }

        val otraMitadPresentadores = programa.segundaMitadPresentadores()

        val programa2 = Programa().apply {
            presentadores = otraMitadPresentadores.toMutableList()
            presupuesto = programa.mitadPresupuesto()
            sponsors = programa.sponsors
            titulo = programa.tituloEnPalabras().getOrNull(1) ?: "Programa sin nombre"
            dias = programa.dias
        }

        grilla.eliminarPrograma(programa)
        grilla.agregarPrograma(programa1)
        grilla.agregarPrograma(programa2)
    }
}

/************************/
class CambioPorLosSimpson:AccionRevisionPrograma{
    override fun ejecutar(programa: Programa, grilla: Grilla) {

        val programaReemplazador = Programa().apply {
            titulo = "Los Simpsons"
            dias = programa.dias
            duracion = programa.duracion
        }

        grilla.eliminarPrograma(programa)
        grilla.agregarPrograma(programaReemplazador)
    }
}

/************************/
class FusionarPrograma : AccionRevisionPrograma {
    override fun ejecutar(programa: Programa, grilla: Grilla) {
        val siguientePrograma = grilla.siguientePrograma(programa)

        val nuevoPrograma = Programa().apply {

            presentadores = mutableListOf(programa.presentadorPrincipal(), siguientePrograma.presentadorPrincipal())
            presupuesto = Math.min(programa.presupuesto, siguientePrograma.presupuesto)
            sponsors = elegirPrograma(programa, siguientePrograma).sponsors
            duracion = programa.duracion + siguientePrograma.duracion
            titulo = elegirTitulo()
            dias = programa.dias
        }

        grilla.eliminarPrograma(programa)
        grilla.eliminarPrograma(siguientePrograma)
        grilla.agregarPrograma(nuevoPrograma)
    }

    fun elegirPrograma(programa: Programa, otroPrograma: Programa) =
        if (caraOCruz()) programa else otroPrograma

    fun elegirTitulo() =
        if (caraOCruz()) "Impacto total" else "Un buen día"

    private fun caraOCruz() =
        Random.nextBoolean()

}

/***************************************************************************************************************/
class Grilla {
    val programas = mutableListOf<Programa>()
    val programasEnRevision = mutableListOf<Programa>()
    val observersNuevoPrograma = mutableListOf<ObserverNuevoPrograma>()

    fun procesoDeRevision() {
        programasEnRevision.forEach { programa -> programa.revisar(this) }
    }

    fun agregarPrograma(programa: Programa) {
        programas.add(programa)
        observersNuevoPrograma.forEach { it.notificarNuevoPrograma(programa, this) }
    }

    fun eliminarPrograma(programa: Programa) {
        programas.remove(programa) }

    fun agregarProgramaEnRevision(programa: Programa) {
        programasEnRevision.add(programa) }

    fun siguientePrograma(programa: Programa): Programa {
        val indicePrograma = programas.indexOf(programa)

        return if ((programas.size - 1) > indicePrograma)
            programas[indicePrograma + 1] else programas[0]
    }

    fun sincronizarProgramasEnRevision() {
        programasEnRevision.removeAll { programaEnRevision ->
            !programas.contains(programaEnRevision) }
    }
}
/***************************************************************************************************************/
interface ObserverNuevoPrograma {
    fun notificarNuevoPrograma(programa: Programa, grilla: Grilla)
}

/************************/
class NotificarConductoresNuevoPrograma : ObserverNuevoPrograma {
    lateinit var mailSender: MailSender

    override fun notificarNuevoPrograma(programa: Programa, grilla: Grilla) {
        programa.mailsConductores().forEach {
            mailSender.sendMail(Mail(
                from = "programacion@canal.tv",
                to = it,
                subject = "Oportunidad!",
                content = "Fuiste seleccionado para conducir ${programa.titulo}! Ponete en contacto con la gerencia."))
        }
    }
}
/************************/
interface MailSender {
    fun sendMail(mail: Mail)
}

data class Mail(val from: String, val to: String, val subject: String, val content: String)

/************************/
class EnviarSMSACliowinPorPresupuestoAlto(val smsSender: SMSSender) : ObserverNuevoPrograma {
    val presupuestoEstipulado: Int = 100_000
    val telefonoCliowin: String = "0987654321"
    val telefonoEmisor: String = "1234567890"

    override fun notificarNuevoPrograma(programa: Programa, grilla: Grilla) {

        if (programa.presupuesto > presupuestoEstipulado){
            smsSender.sendSMS(SMS(
                emisor = telefonoEmisor,
                receptor = telefonoCliowin,
                mensaje = "{${programa.presupuesto}} - {${programa.titulo}} - CONSEGUIR SPONSOR URGENTE"
            )
            )
        }
    }
}

/************************/
interface SMSSender {
    fun sendSMS(sms: SMS)
}

data class SMS(val emisor: String, val receptor: String, val mensaje: String)

/************************/
class QuitarProgramasEliminadosEnRevision : ObserverNuevoPrograma {
    override fun notificarNuevoPrograma(programa: Programa, grilla: Grilla) {
        grilla.sincronizarProgramasEnRevision()
    }
}

