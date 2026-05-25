# Laboratorio 4 - Generador de codigo de tres direcciones

## Descripcion

Este laboratorio implementa un generador de codigo intermedio de tres direcciones para un subconjunto de lenguaje visto en clase.

El programa lee un archivo fuente `.txt`, analiza asignaciones, expresiones aritmeticas, expresiones booleanas y estructuras de control. Como salida genera codigo de tres direcciones y lo guarda en un archivo `.tac`.

## Funcionalidades implementadas

El generador reconoce:

```txt
asignaciones
expresiones aritmeticas
expresiones booleanas
if
if else
while
for
```

## Estructura del proyecto

```txt
src/
├── App.java
├── Inicio.java
├── Token.java
├── AnalizadorLexico.java
├── Parser.java
└── GeneradorCodigo.java

pruebas/
├── caso1_aritmetica.txt
├── caso2_menos_unario.txt
├── caso3_if_else.txt
├── caso4_while.txt
├── caso5_for.txt
└── caso6_completo.txt
```

## Como ejecutar

Desde la raiz del proyecto:

```cmd
rmdir /s /q bin
mkdir bin
javac -encoding UTF-8 -d bin src\App.java src\Inicio.java src\Token.java src\AnalizadorLexico.java src\Parser.java src\GeneradorCodigo.java
java -cp bin App
```

Luego ingresar la ruta del archivo fuente.

Ejemplo:

```txt
pruebas\caso1_aritmetica.txt
```

El programa muestra el codigo generado en consola y tambien crea un archivo `.tac`.

Ejemplo:

```txt
pruebas\caso1_aritmetica.tac
```

## Tokens reconocidos

El analizador lexico reconoce:

```txt
id
num
if
else
while
for
true
false
+
-
*
/
=
<
>
<=
>=
==
!=
&&
||
!
(
)
;
,
```

## Generacion de temporales

Los temporales se generan secuencialmente usando el formato:

```txt
t1
t2
t3
```

Cada operacion aritmetica no trivial genera un temporal.

Ejemplo de entrada:

```txt
x = a + b * c;
```

Salida equivalente:

```txt
t1 = b * c
t2 = a + t1
x = t2
```

## Generacion de etiquetas

Las etiquetas se generan secuencialmente usando el formato:

```txt
L1
L2
L3
```

Se usan para controlar saltos de `if`, `if-else`, `while` y `for`.

Ejemplo:

```txt
if x < y goto L1
goto L2
L1:
x = 1
L2:
```

## Traduccion de expresiones booleanas

Las expresiones booleanas se traducen usando saltos condicionales e incondicionales.

Ejemplo:

```txt
if (x < y)
    x = 1;
```

Salida equivalente:

```txt
if x < y goto L1
goto L2
L1:
x = 1
L2:
```

## Operadores booleanos

El generador soporta:

```txt
&&
||
!
true
false
```

Para `&&`, si la primera condicion es falsa, se salta directamente a la etiqueta falsa.

Para `||`, si la primera condicion es verdadera, se salta directamente a la etiqueta verdadera.

Esto permite generar codigo con cortocircuito.

## Asignaciones

La produccion usada es:

```txt
S -> id = E ;
```

Ejemplo:

```txt
x = 5;
```

Codigo generado:

```txt
x = 5
```

Ejemplo con expresion:

```txt
x = a + b;
```

Codigo generado:

```txt
t1 = a + b
x = t1
```

## Expresiones aritmeticas

Se soporta:

```txt
+
-
*
/
()
menos unario
id
num
```

Ejemplo:

```txt
z = -(x + y);
```

Salida equivalente:

```txt
t1 = x + y
t2 = menos t1
z = t2
```

## If else

Entrada:

```txt
if (x >= y)
    max = x;
else
    max = y;
```

Salida equivalente:

```txt
if x >= y goto L1
goto L2
L1:
max = x
goto L3
L2:
max = y
L3:
```

## While

Entrada:

```txt
while (i < 10)
    i = i + 1;
```

Salida equivalente:

```txt
L1:
if i < 10 goto L2
goto L3
L2:
t1 = i + 1
i = t1
goto L1
L3:
```

## For

Entrada:

```txt
for (i = 0; i < 5; i = i + 1)
    total = total + i;
```

Salida equivalente:

```txt
i = 0
L1:
if i < 5 goto L2
goto L3
L2:
t1 = total + i
total = t1
L4:
t2 = i + 1
i = t2
goto L1
L3:
```

## Archivos de prueba

### Caso 1

```txt
caso1_aritmetica.txt
```

Prueba expresiones aritmeticas con precedencia.

### Caso 2

```txt
caso2_menos_unario.txt
```

Prueba menos unario.

### Caso 3

```txt
caso3_if_else.txt
```

Prueba estructura `if-else`.

### Caso 4

```txt
caso4_while.txt
```

Prueba ciclo `while`.

### Caso 5

```txt
caso5_for.txt
```

Prueba ciclo `for`.

### Caso 6

```txt
caso6_completo.txt
```

Prueba combinada con asignaciones, `while`, `if-else`, `&&` y `||`.

## Limitaciones conocidas

El laboratorio se enfoca en generacion de codigo intermedio, no en analisis semantico completo.

Limitaciones:

```txt
No valida tipos de variables
No maneja declaraciones de variables
No maneja funciones
No maneja arreglos
No maneja strings
No maneja comentarios
No maneja bloques con llaves
No maneja scopes
```

Las sentencias se procesan segun el subconjunto de la gramatica requerida para el laboratorio.

## Salida

La salida se muestra en consola y se guarda en un archivo `.tac`.

Ejemplo:

```txt
entrada.txt
entrada.tac
```

## Diseño general

El proyecto esta dividido en tres partes principales:

```txt
AnalizadorLexico
Parser
GeneradorCodigo
```

### AnalizadorLexico

Convierte el archivo fuente en tokens.

### Parser

Construye una estructura de nodos para representar asignaciones, expresiones, booleanos y estructuras de control.

### GeneradorCodigo

Recorre los nodos y genera codigo de tres direcciones usando temporales y etiquetas.

## Integrantes

```txt
Nombre 1
Nombre 2
```