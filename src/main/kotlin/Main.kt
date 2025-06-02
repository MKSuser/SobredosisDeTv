package ar.edu.algo2

import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.random.Random

class GrillaDeProgramas() {
    val programas: MutableList<Programa> = mutableListOf()

    fun agregarPrograma(programa: Programa) {
        programas.add(programa)
    }

    fun removerPrograma(programa: Programa) {
        programas.remove(programa)
    }

    /***********************************************************************************/
    // Strategy

    lateinit var restriccion: Restriccion

    fun mantenerPrograma(programa: Programa): Boolean {
         return restriccion.condicion(programa)
    }

    /***********************************************************************************/
    //Commands

    val accionesATomar: MutableList<Accion> = mutableListOf()

    fun correrAccionesSobre(programa: Programa){
        accionesATomar.forEach { it.ejecutar(programa, this) }
    }

    /***********************************************************************************/
    //Observers

    // HASTA ACAAAA

    fun accionesSiNoSeCumplenRestricciones(){
    }


}

/***********************************************************************************/
//Commands

interface Accion {
    fun ejecutar(programa: Programa, grilla: GrillaDeProgramas)
}
/**************************/
class PartirProgramaEnDos: Accion{
    override fun ejecutar(programa: Programa, grilla: GrillaDeProgramas) {
        /*Mitad de los conductores*/
        val conductores = programa.conductoresPrincipales
        val medioDeConductores = conductores.size / 2

        val conductoresPrimerPrograma = conductores.take(medioDeConductores)
        val conductoresSegundoPrograma = conductores.drop(medioDeConductores)

        /*mitad de presupuesto*/
        val presupuesto = programa.presupuestoBase
        val presupuestoParaProgramas = presupuesto / 2

        /*Mismos Sponsors*/
        val sponsors = programa.sponsorsDePublicidad

        /*mitad de duracion*/
        val mitadDeDuracion = programa.duracionMinutos / 2

        /*mitad del nombre --> si hay una palabra, el segundo sin nombre*/
        val palabras = programa.titulo.split(" ").filter { it.isNotEmpty() }

        val titulo1 = "${palabras[0]} en el aire!"
        val titulo2 = if (palabras.size > 1) palabras[1].replaceFirstChar { it.uppercase() } else "Programa sin nombre"

        /*mismos días*/
        val diaDeTransmision = programa.diaTransmision

        /**Borramos y Creamos los nuevos programas**/
        grilla.removerPrograma(programa)

        grilla.agregarPrograma(ProgramFactory.crear(
            conductoresSegundoPrograma.toMutableSet(),
            presupuestoParaProgramas,
            sponsors.toMutableSet(),
            mitadDeDuracion,
            titulo2
            ,diaDeTransmision)
        )

        grilla.agregarPrograma(ProgramFactory.crear(
            conductoresPrimerPrograma.toMutableSet(),
            presupuestoParaProgramas,
            sponsors.toMutableSet(),
            mitadDeDuracion,
            titulo1
            ,diaDeTransmision)
        )


    }
}

object ProgramFactory{
    fun crear(conductores: MutableSet<Conductor>,
              presupuesto: Double,
              sponsors: MutableSet<Sponsor>,
              duracion: Double,
              tituloNuevo: String,
              diaDeTransmision: DayOfWeek): Programa{
        return Programa().apply{
            titulo = tituloNuevo
            conductoresPrincipales = conductores
            presupuestoBase = presupuesto
            sponsorsDePublicidad = sponsors
            duracionMinutos = duracion
            diaTransmision = diaDeTransmision
        }
    }
}
/**************************/
class CambioPorLosSimpson:Accion{
    override fun ejecutar(programa: Programa, grilla: GrillaDeProgramas) {
        /*Crear un programa de los simpson que herede dias y duracion en minutos*/
        val programaReemplazador = SimpsoncFactory.crear(programa)

        grilla.removerPrograma(programa)
        grilla.agregarPrograma(programaReemplazador)
    }
}

object SimpsoncFactory{
    fun crear(programa: Programa): Programa{
        return Programa().apply{
            titulo = "Los Simpson"
            diaTransmision = programa.diaTransmision
            duracionMinutos = programa.duracionMinutos
        }
    }
}
/**************************/
class FusionDeProgramas:Accion{
    override fun ejecutar(programa: Programa, grilla: GrillaDeProgramas) {
        val programas = grilla.programas

        /*Fusión con el siguiente o el primero*/
        val indiceDePrograma1 = programas.indexOf(programa)

        val programa1 = programa
        val programa2 = if (indiceDePrograma1 != programas.lastIndex) {
            programas[indiceDePrograma1 + 1]
        } else {
            programas[0]
        }

        /*Conduce el primero de la lista de cada uno, borrar el resto*/
        val conductores = mutableSetOf(
            programa1.conductoresPrincipales.first(),
            programa2.conductoresPrincipales.first()
        )

        /*presupuesto como el menor de los 2*/
        val presupuesto = minOf(programa1.presupuestoBase, programa2.presupuestoBase)

        /*Mismo sponsor al azar*/
        val sponsors = if (Random.nextBoolean()) programa1.sponsorsDePublicidad else programa2.sponsorsDePublicidad

        /*Duracion: suma de ambos*/
        val duracion = programa1.duracionMinutos + programa2.duracionMinutos

        /* Los días serán los del primer programa*/
        val diaDeTransmision = programa1.diaTransmision

        /* titulo al azar, entre "Impacto total" o "Un buen día"*/
        val titulo = if (Random.nextBoolean()) "Impacto total" else "Un buen día"

        grilla.removerPrograma(programa1)
        grilla.removerPrograma(programa2)

        grilla.agregarPrograma(ProgramFactory.crear(
            conductores.toMutableSet(),
            presupuesto,
            sponsors.toMutableSet(),
            duracion,
            titulo
            ,diaDeTransmision)
        )
    }
}

/**************************/
class CambioDeDia(val nuevoDia: DayOfWeek):Accion{
    override fun ejecutar(programa: Programa, grilla: GrillaDeProgramas) {
        /*Se pasa a otro día*/
        programa.diaTransmision = nuevoDia
    }
}

/***********************************************************************************/
//Strategy

interface Restriccion {
    fun condicion(programa: Programa):Boolean
}

class PromedioRating (val ratingASuperar: Double): Restriccion{
    override fun condicion(programa: Programa) =
        programa.ratingDeUltimas5Emisiones().average() >= ratingASuperar
}

class MaximoDeConductoresPrincipales(val cantidadMaxima: Int): Restriccion{
    override fun condicion(programa: Programa) =
        programa.conductoresPrincipales.size <= cantidadMaxima
}

class ConductorPrincipalDeseado(val conductorDeseado: Conductor): Restriccion{
    override fun condicion(programa: Programa) =
        programa.conductoresPrincipales.contains(conductorDeseado)
}

class NoExcederPresupuesto(val presupuestoDeseado: Double): Restriccion{
    override fun condicion(programa: Programa) =
       programa.presupuestoBase <= presupuestoDeseado
}

class RestriccionAnd(val restricciones: List<Restriccion>): Restriccion{
    override fun condicion(programa:Programa) =
       restricciones.all { it.condicion(programa) }
}

class RestriccionOr(val restricciones: List<Restriccion>): Restriccion{
    override fun condicion(programa:Programa) =
        restricciones.any { it.condicion(programa) }
}

class RestriccionVacia(): Restriccion{
    override fun condicion(programa:Programa) =
        true//Null Object Pattern
}

class RestriccionNoCumplidaException: Exception(){}

/***********************************************************************************/
//Observers




/***********************************************************************************/


class Programa {
    var titulo: String = ""
    lateinit var diaTransmision: DayOfWeek
    var duracionMinutos: Double = 0.0
    var conductoresPrincipales: MutableSet<Conductor> = mutableSetOf()
    var presupuestoBase: Double = 0.0
    var sponsorsDePublicidad: MutableSet<Sponsor> = mutableSetOf()
    val emisiones: MutableList<Emision> = mutableListOf()

    fun ratingDeUltimas5Emisiones(): List<Double> {
        return emisiones.map { it.rating }.takeLast(5)
    }

}

data class Emision(
    val fecha: LocalDate,
    val rating: Double
)

class Sponsor (val nombre: String){
}

class Conductor (val nombre: String) {
}