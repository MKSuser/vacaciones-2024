
---

## 🧠 **1. Enfoque general del diseño**

Este sistema modela un dominio turístico donde personas son asignadas a tours de acuerdo a sus preferencias vacacionales, considerando el presupuesto y las características de los lugares. Además, permite ejecutar lógicas de negocio posteriores a la confirmación de un tour (como enviar mails o notificar a la AFIP).

Diseñé el sistema aplicando fuertemente principios de diseño orientado a objetos como:

* **Encapsulamiento** de responsabilidades,
* **Desacoplamiento** entre actores,
* **Abstracción** mediante interfaces,
* Y uso de **patrones de diseño** como Strategy, Observer y Value Object.

---

## 🧭 **2. Patrones de diseño utilizados**

### ✅ **Strategy**: Preferencias vacacionales

La interfaz `Preferencia` y sus implementaciones (`Tranquilo`, `Divertido`, `Combineta`, `Bipolar`) representan un uso clásico del **patrón Strategy**.

* Esto permite que cada persona tenga una estrategia dinámica y modificable de evaluación (`leGusta(lugar)`).
* Desacopla la responsabilidad de que las personas contesten si les gusta el lugar (recae sobre las preferencias)
* El caso de `Combineta` permite una composición de estrategias.
* `Bipolar` extiende la idea: cambia internamente la estrategia en cada evaluación exitosa, incorporando **comportamiento mutable y dinámico**, sin afectar al cliente (`Persona`).

📌 Este diseño sigue el **principio de abierto/cerrado (OCP)**: se pueden agregar nuevas preferencias sin modificar código existente.

---

### ✅ **Template Method**: Diversión de los lugares

En `Lugar`, el método `esDivertido()` aplica una lógica común (paridad de letras del nombre) y delega en un método abstracto `esDivertidoParticular()` para que cada subtipo (`Ciudad`, `Pueblo`, `Balneario`) aporte su criterio propio.

* Esto es un claro ejemplo del **patrón Template Method**, que encapsula la lógica general en la clase base y permite especializaciones en las subclases.
* Permite reutilizar y extender comportamiento común sin duplicación.

---

### ✅ **Observer**: Acciones al confirmar un tour

El sistema permite registrar múltiples acciones automáticas a ejecutar cuando se confirma un tour. Esto se implementa mediante una lista de `PostConfirmacionObservers`, que se recorren en `AministradorDeTours.confirmarTour`.

* Implementaciones como `EnviarMail`, `RegaloRecibidoInformarFlete` y `RotadorDePreferenciaBipolar` encapsulan comportamientos disjuntos.
* Esto reduce el acoplamiento y sigue el principio **Open/Closed**, permitiendo nuevas acciones sin modificar el código del administrador.

Este es un uso canónico del **patrón Observer**, aunque en una versión *pull* simplificada (el `Administrador` ejecuta directamente).

---

### ✅ **Value Object**: Mail y Datos para AFIP

Las clases `Mail` e `InterfazAFIP` son objetos inmutables que encapsulan múltiples valores como una unidad semántica.

* Son típicos **Value Objects**, que se identifican por su valor y no por su identidad.
* Por ejemplo, un `Mail` contiene `from`, `to`, `subject` y `content`, que juntos representan un mensaje pero no tienen entidad propia.
* Esto mejora la claridad del diseño, evita errores y permite pasar estructuras de datos complejas como un único objeto.

---

### ✅ **Inyección de dependencias**: Desacople de servicios externos

* `MailSender` (setter inyection) y `AFIPSender`(constructor inyection) son interfaces que abstraen mecanismos externos.
* `EnviarMail` y `RegaloRecibidoInformarFlete` reciben esas dependencias desde afuera (constructor o atributo), permitiendo cambiar implementaciones (por ejemplo, para tests, logs, mocks).

Esto respeta el **principio de inversión de dependencias (DIP)**: los módulos de alto nivel no dependen de detalles, sino de abstracciones.

---

## 🛠️ **3. Otras herramientas y principios aplicados**

### ✅ **Long Parameter Method evitado**

En métodos como `sendMail()` o `notificarAFIP()`, en vez de pasar muchos parámetros sueltos, se agrupan en `Mail` e `InterfazAFIP`. Esto evita el *code smell* conocido como **Long Parameter List**.

* Facilita el mantenimiento,
* Hace el código más legible,
* Y evita errores por desorden de argumentos.

---

### ✅ **SRP (Single Responsibility Principle)**

Cada clase y cada método tiene una única responsabilidad clara:

* `Lugar` y sus subtipos encapsulan lógica de lugares.
* `Persona` conoce sus preferencias.
* `AdministradorDeTours` gestiona la asignación y confirmación de tours.
* Los observers encapsulan cada uno una lógica puntual post-confirmación.

Esto mejora la cohesión y reduce el impacto de cambios.

---

### ✅ **DRY (Don't Repeat Yourself)**

* El método `esDivertido()` generaliza parte de la lógica, evitando duplicación.
* Métodos como `mailsDestino()` y `codigosDeLugares()` centralizan la construcción de strings, reutilizados luego en distintos observers.

---

### ✅ **Nombrado claro y semántico**

Las clases y métodos están nombrados de forma coherente y expresiva:

* `preferenciaParaVacaciones`, `montoPorPersona`, `agregarPersonaATour`, `rotarPrefencias()` comunican bien su intención.

---


## 🧩 **5. Conclusión**

Este diseño busca:

* Representar el dominio turístico de forma expresiva y modular.
* Aislar responsabilidades,
* Aplicar patrones de forma estratégica para mejorar la extensión futura del sistema,
* Evitar code smells comunes (como long parameter list, lógica duplicada o acoplamiento fuerte).

Esto da lugar a un sistema robusto, flexible y preparado para escalar.

---
## **Surgio la duda de si debería haber hecho los Observers como Commands..**
---
## 🧠 **¿Observer o Command? ¿Cuál conviene?**

### ✅ **Usar Observer (como hiciste)**

Tu uso del **patrón Observer** para las acciones post-confirmación **es completamente válido y adecuado**. De hecho, el problema encaja naturalmente con Observer:

* El `AdministradorDeTours` **notifica** a una serie de "observadores" cuando un tour es confirmado.
* Cada observador (como `EnviarMail`, `RegaloRecibidoInformarFlete`, etc.) **reacciona** al evento realizando su acción.
* **Desacopla** quién confirma el tour de lo que ocurre después.



---

## 🌀 ¿Entonces por qué mencioné Command?

Porque hay una **zona gris muy interesante**:
**Lo que estás modelando (acciones post-confirmación) pueden ser vistas como eventos observables... o como comandos a ejecutar.**

Ambos patrones **comparten objetivos similares**:

| Aspecto              | Observer                         | Command                              |
| -------------------- | -------------------------------- | ------------------------------------ |
| Se usa para          | Reaccionar a eventos             | Encapsular acciones                  |
| Acoplamiento         | Bajo entre sujeto y observadores | Bajo entre invocador y ejecutor      |
| Extensibilidad       | Alta (nuevos observers)          | Alta (nuevos comandos)               |
| Ejecución diferida   | Difícil de controlar             | Muy fácil (ejecutás cuando querés)   |
| Orientado a          | Eventos                          | Acciones (a veces asincrónicas o no) |
| Ejemplo en tu código | PostConfirmacionObservers        | Posibles comandos post-confirmación  |

---

## 🔄 ¿Cuándo usaría Command en lugar de Observer?

Cuando:

1. **Quiero controlar el momento de ejecución exacto** (ej.: ejecutar más tarde, encolar, guardar en disco, repetir, etc).
2. **Tengo acciones que quiero parametrizar, loguear, testear, o cancelar.**
3. Estoy en contextos donde **asincronía o ejecución distribuida** puede aparecer (como se te mencionó).

---

## 💡 ¿Y por qué te lo mencionaron como "para cosas asincrónicas"?

Porque **Command brilla cuando necesitás ejecución diferida o asincrónica**. Ejemplos típicos:

* Encolar tareas para ejecutar después.
* Mandar trabajos a un worker o thread pool.
* Implementar *jobs* o *batch tasks*.
* Permitir *undo* o *redo* en una interfaz.

Entonces, sí: **Command no es solo para asincronía**, pero **es ideal cuando eso aparece**.
