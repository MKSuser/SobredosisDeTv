
---

### 🔷 **1. Patrones de Diseño Utilizados**

#### ✅ **Strategy (Restricciones y Acciones)**

> **Dónde se ve:**

* `RestriccionPrograma` y sus subclases (`MinimoRating`, `NoExcederPresupuesto`, etc.)
* `AccionRevisionPrograma` y sus implementaciones (`PartirProgramaEn2`, `FusionarPrograma`, etc.)

> **Por qué lo usamos:**
> Nos permite encapsular distintas restricciones y acciones intercambiables sin modificar el código del `Programa` o de la `Grilla`.
> Esto es fundamental para cumplir con el **principio de abierto/cerrado (OCP)**: podemos agregar nuevas restricciones o acciones sin tocar el resto del sistema.

---

#### ✅ **Composite (Restricciones compuestas)**

> **Dónde se ve:**

* `RestriccionOrCompuesta` y `RestriccionAndCompuesta`

> **Por qué lo usamos:**
> Permite tratar una combinación de restricciones como si fuera una sola. Gracias a este patrón, el cliente puede componer condiciones complejas (`"rating > 5 o máx 3 conductores"`) de forma transparente.
> Favorece reutilización y composición flexible.

---

#### ✅ **Null Object (Restricción vacía)**

> **Dónde se ve:**

* `RestriccionVacia`

> **Por qué lo usamos:**
> Evita tener que hacer chequeos nulos o condicionales si un programa no tiene restricciones. Provee un comportamiento por defecto seguro.

---

#### ✅ **Observer (acciones al agregar un programa)**

> **Dónde se ve:**

* `ObserverNuevoPrograma` y sus implementaciones (`NotificarConductoresNuevoPrograma`, `EnviarSMSACliowinPorPresupuestoAlto`, `QuitarProgramasEliminadosEnRevision`)

> **Por qué lo usamos:**
> El sistema puede reaccionar a eventos sin acoplarse a detalles concretos de qué hacer: enviar mails, SMS o actualizar la grilla.
> Esto hace que el sistema sea **extensible** y que se puedan agregar nuevas reacciones a la creación de un programa sin modificar la `Grilla`.

---

#### ✅ **Command (implícito en AccionRevisionPrograma)**

> **Dónde se ve:**

* Las clases que implementan `AccionRevisionPrograma` encapsulan una acción ejecutable.

> **Por qué lo usamos:**
> Cada acción es una clase con un único propósito: puede ejecutarse, combinarse, reutilizarse o testearse de forma independiente.
> Facilita la trazabilidad de qué acciones se disparan por cada restricción.

---

### 🔷 **2. Principios SOLID y Buenas Prácticas**

#### ✅ **SRP (Single Responsibility Principle)**

Cada clase tiene una única responsabilidad clara:

* `Programa` encapsula sus datos y lógica relacionada con su estado.
* `RestriccionPrograma` encapsula las validaciones.
* `AccionRevisionPrograma` ejecuta efectos colaterales.
* `ObserverNuevoPrograma` maneja efectos secundarios al alta.

#### ✅ **OCP (Open/Closed Principle)**

Podemos agregar nuevos tipos de restricciones, acciones, observers sin modificar las clases existentes. Esto es clave en sistemas con alta variabilidad de reglas como este.

#### ✅ **DIP (Dependency Inversion Principle)**

Los observers dependen de interfaces como `MailSender` y `SMSSender`. Esto permite **inyección de dependencias** y facilita los test unitarios.

#### ✅ **Law of Demeter**

Cada objeto conoce solo lo necesario. No vemos cadenas de llamadas como `programa.getPresentadores().get(0).getNombre()`.

---

### 🔷 **3. Value Objects & Técnicas de diseño**

#### ✅ **Value Object**

> **Dónde se ve:**

* `Presentador`, `Rating`, `Mail`, `SMS`

> **Por qué lo usamos:**
> Representan datos inmutables que se comportan como unidades semánticas completas.

#### ✅ **Long Parameter List evitado con objetos compuestos**

> Por ejemplo, `Mail` encapsula varios parámetros que viajarían juntos. Evita listas largas de argumentos en métodos como `sendMail(...)`.

#### ✅ **Builder Pattern (usado informalmente con `apply`)**

> **Dónde se ve:**

* En la creación de programas nuevos: `Programa().apply { ... }`

> Provee fluidez y claridad al construir objetos con muchos atributos configurables.

---

### 🔷 **4. Extensibilidad y Mantenibilidad**

> * Podés agregar restricciones con solo crear nuevas subclases de `RestriccionPrograma`.
> * Nuevas acciones de revisión: nuevas implementaciones de `AccionRevisionPrograma`.
> * Nuevos observers: implementás `ObserverNuevoPrograma` y los agregás a la grilla.

Esto demuestra que el sistema está **preparado para el cambio**, algo esencial en entornos como el de programación de TV.

---

### 🔷 **5. Algunas posibles mejoras futuras (para mostrar proactividad)**

* **Encapsular `titulo` en un Value Object** que exponga `primeraPalabra()` y `segundaPalabra()` (evitando lógica de split suelta).
* **Agregar tests automáticos unitarios y de integración**, que serán fáciles de escribir por el bajo acoplamiento.
* **Extraer `ProgramaFactory`** para evitar duplicación de lógica al crear nuevos programas (como en `PartirProgramaEn2` y `FusionarPrograma`).
* **Agregar un `LoggerObserver`** para auditar qué acciones se ejecutaron durante una revisión.

---

### 🔷 **Conclusión para la defensa**

> "Elegí un diseño altamente modular y extensible, donde cada pieza del sistema cumple un rol bien definido. Apliqué los patrones Strategy, Composite, Observer y Command para representar restricciones, acciones y efectos secundarios con bajo acoplamiento. Usé objetos de valor para evitar estructuras de datos desestructuradas, evité long parameter lists, y me aseguré de cumplir los principios SOLID. El resultado es un sistema preparado para el cambio, fácil de mantener y ampliar, que refleja fielmente las reglas dinámicas de una programación televisiva real."

---

