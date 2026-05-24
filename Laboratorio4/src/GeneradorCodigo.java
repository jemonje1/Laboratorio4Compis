import java.util.ArrayList;
import java.util.List;

public class GeneradorCodigo {

    //ATRIBUTOS
    private int contadorTemporales;
    private int contadorEtiquetas;

    //CONSTRUCTOR
    public GeneradorCodigo() {
        this.contadorTemporales = 1;
        this.contadorEtiquetas = 1;
    }

    //METODOS
    //Genera codigo completo del programa
    public String generar(NodoPrograma programa) {
        Contexto contexto = new Contexto();

        String siguiente = nuevaEtiqueta();
        generarSecuencia(programa.getSentencias(), siguiente, contexto);
        contexto.etiqueta(siguiente);

        return contexto.getCodigo();
    }

    //Genera una secuencia de sentencias
    private void generarSecuencia(List<NodoSentencia> sentencias, String siguiente, Contexto contexto) {
        for (int i = 0; i < sentencias.size(); i++) {
            NodoSentencia sentencia = sentencias.get(i);

            if (i == sentencias.size() - 1) {
                sentencia.generar(this, contexto, siguiente);
            } else {
                String etiquetaIntermedia = nuevaEtiqueta();
                sentencia.generar(this, contexto, etiquetaIntermedia);
                contexto.etiqueta(etiquetaIntermedia);
            }
        }
    }

    //Genera temporal nuevo
    private String nuevoTemporal() {
        String temporal = "t" + contadorTemporales;
        contadorTemporales++;
        return temporal;
    }

    //Genera etiqueta nueva
    private String nuevaEtiqueta() {
        String etiqueta = "L" + contadorEtiquetas;
        contadorEtiquetas++;
        return etiqueta;
    }

    //CLASE CONTEXTO
    public static class Contexto {

        //ATRIBUTOS
        private final List<String> lineas;

        //CONSTRUCTOR
        public Contexto() {
            this.lineas = new ArrayList<>();
        }

        //METODOS
        //Agrega una linea
        public void gen(String linea) {
            lineas.add(linea);
        }

        //Agrega etiqueta
        public void etiqueta(String etiqueta) {
            lineas.add(etiqueta + ":");
        }

        //Retorna codigo completo
        public String getCodigo() {
            return String.join(System.lineSeparator(), lineas);
        }
    }

    //RESULTADO DE EXPRESION
    public static class ResultadoExpresion {

        //ATRIBUTOS
        private final String dir;

        //CONSTRUCTOR
        public ResultadoExpresion(String dir) {
            this.dir = dir;
        }

        //GETTERS
        public String getDir() {
            return dir;
        }
    }

    //NODO PROGRAMA
    public static class NodoPrograma {

        //ATRIBUTOS
        private final List<NodoSentencia> sentencias;

        //CONSTRUCTOR
        public NodoPrograma(List<NodoSentencia> sentencias) {
            this.sentencias = sentencias;
        }

        //GETTERS
        public List<NodoSentencia> getSentencias() {
            return sentencias;
        }
    }

    //NODO SENTENCIA
    public static abstract class NodoSentencia {

        //METODOS
        //Genera codigo de sentencia
        public abstract void generar(GeneradorCodigo g, Contexto c, String siguiente);
    }

    //NODO ASIGNACION
    public static class NodoAsignacion extends NodoSentencia {

        //ATRIBUTOS
        private final String id;
        private final NodoExpresion expresion;

        //CONSTRUCTOR
        public NodoAsignacion(String id, NodoExpresion expresion) {
            this.id = id;
            this.expresion = expresion;
        }

        //METODOS
        //Genera asignacion
        @Override
        public void generar(GeneradorCodigo g, Contexto c, String siguiente) {
            ResultadoExpresion resultado = expresion.generar(g, c);
            c.gen(id + " = " + resultado.getDir());
        }
    }

    //NODO IF
    public static class NodoIf extends NodoSentencia {

        //ATRIBUTOS
        private final NodoBooleano condicion;
        private final NodoSentencia thenStmt;

        //CONSTRUCTOR
        public NodoIf(NodoBooleano condicion, NodoSentencia thenStmt) {
            this.condicion = condicion;
            this.thenStmt = thenStmt;
        }

        //METODOS
        //Genera if
        @Override
        public void generar(GeneradorCodigo g, Contexto c, String siguiente) {
            String verdadera = g.nuevaEtiqueta();

            condicion.generar(g, c, verdadera, siguiente);
            c.etiqueta(verdadera);
            thenStmt.generar(g, c, siguiente);
        }
    }

    //NODO IF ELSE
    public static class NodoIfElse extends NodoSentencia {

        //ATRIBUTOS
        private final NodoBooleano condicion;
        private final NodoSentencia thenStmt;
        private final NodoSentencia elseStmt;

        //CONSTRUCTOR
        public NodoIfElse(NodoBooleano condicion, NodoSentencia thenStmt, NodoSentencia elseStmt) {
            this.condicion = condicion;
            this.thenStmt = thenStmt;
            this.elseStmt = elseStmt;
        }

        //METODOS
        //Genera if else
        @Override
        public void generar(GeneradorCodigo g, Contexto c, String siguiente) {
            String verdadera = g.nuevaEtiqueta();
            String falsa = g.nuevaEtiqueta();

            condicion.generar(g, c, verdadera, falsa);

            c.etiqueta(verdadera);
            thenStmt.generar(g, c, siguiente);
            c.gen("goto " + siguiente);

            c.etiqueta(falsa);
            elseStmt.generar(g, c, siguiente);
        }
    }

    //NODO WHILE
    public static class NodoWhile extends NodoSentencia {

        //ATRIBUTOS
        private final NodoBooleano condicion;
        private final NodoSentencia cuerpo;

        //CONSTRUCTOR
        public NodoWhile(NodoBooleano condicion, NodoSentencia cuerpo) {
            this.condicion = condicion;
            this.cuerpo = cuerpo;
        }

        //METODOS
        //Genera while
        @Override
        public void generar(GeneradorCodigo g, Contexto c, String siguiente) {
            String inicio = g.nuevaEtiqueta();
            String verdadera = g.nuevaEtiqueta();

            c.etiqueta(inicio);
            condicion.generar(g, c, verdadera, siguiente);

            c.etiqueta(verdadera);
            cuerpo.generar(g, c, inicio);
            c.gen("goto " + inicio);
        }
    }

    //NODO FOR
    public static class NodoFor extends NodoSentencia {

        //ATRIBUTOS
        private final NodoAsignacion inicial;
        private final NodoBooleano condicion;
        private final NodoAsignacion actualizacion;
        private final NodoSentencia cuerpo;

        //CONSTRUCTOR
        public NodoFor(NodoAsignacion inicial, NodoBooleano condicion,
                       NodoAsignacion actualizacion, NodoSentencia cuerpo) {
            this.inicial = inicial;
            this.condicion = condicion;
            this.actualizacion = actualizacion;
            this.cuerpo = cuerpo;
        }

        //METODOS
        //Genera for
        @Override
        public void generar(GeneradorCodigo g, Contexto c, String siguiente) {
            String inicio = g.nuevaEtiqueta();
            String verdadera = g.nuevaEtiqueta();
            String actualiza = g.nuevaEtiqueta();

            inicial.generar(g, c, inicio);

            c.etiqueta(inicio);
            condicion.generar(g, c, verdadera, siguiente);

            c.etiqueta(verdadera);
            cuerpo.generar(g, c, actualiza);

            c.etiqueta(actualiza);
            actualizacion.generar(g, c, inicio);
            c.gen("goto " + inicio);
        }
    }

    //NODO EXPRESION
    public static abstract class NodoExpresion {

        //METODOS
        //Genera codigo de expresion
        public abstract ResultadoExpresion generar(GeneradorCodigo g, Contexto c);
    }

    //NODO VALOR
    public static class NodoValor extends NodoExpresion {

        //ATRIBUTOS
        private final String valor;

        //CONSTRUCTOR
        public NodoValor(String valor) {
            this.valor = valor;
        }

        //METODOS
        //Retorna valor directo
        @Override
        public ResultadoExpresion generar(GeneradorCodigo g, Contexto c) {
            return new ResultadoExpresion(valor);
        }
    }

    //NODO BINARIO
    public static class NodoBinario extends NodoExpresion {

        //ATRIBUTOS
        private final NodoExpresion izquierda;
        private final String operador;
        private final NodoExpresion derecha;

        //CONSTRUCTOR
        public NodoBinario(NodoExpresion izquierda, String operador, NodoExpresion derecha) {
            this.izquierda = izquierda;
            this.operador = operador;
            this.derecha = derecha;
        }

        //METODOS
        //Genera expresion binaria con temporal
        @Override
        public ResultadoExpresion generar(GeneradorCodigo g, Contexto c) {
            ResultadoExpresion izq = izquierda.generar(g, c);
            ResultadoExpresion der = derecha.generar(g, c);

            String temp = g.nuevoTemporal();
            c.gen(temp + " = " + izq.getDir() + " " + operador + " " + der.getDir());

            return new ResultadoExpresion(temp);
        }
    }

    //NODO UNARIO
    public static class NodoUnario extends NodoExpresion {

        //ATRIBUTOS
        private final String operador;
        private final NodoExpresion expresion;

        //CONSTRUCTOR
        public NodoUnario(String operador, NodoExpresion expresion) {
            this.operador = operador;
            this.expresion = expresion;
        }

        //METODOS
        //Genera expresion unaria
        @Override
        public ResultadoExpresion generar(GeneradorCodigo g, Contexto c) {
            ResultadoExpresion resultado = expresion.generar(g, c);
            String temp = g.nuevoTemporal();

            if (operador.equals("-")) {
                c.gen(temp + " = menos " + resultado.getDir());
            } else {
                c.gen(temp + " = " + operador + " " + resultado.getDir());
            }

            return new ResultadoExpresion(temp);
        }
    }

    //NODO BOOLEANO
    public static abstract class NodoBooleano {

        //METODOS
        //Genera saltos booleanos
        public abstract void generar(GeneradorCodigo g, Contexto c, String verdadera, String falsa);
    }

    //NODO RELACIONAL
    public static class NodoRelacional extends NodoBooleano {

        //ATRIBUTOS
        private final NodoExpresion izquierda;
        private final String operador;
        private final NodoExpresion derecha;

        //CONSTRUCTOR
        public NodoRelacional(NodoExpresion izquierda, String operador, NodoExpresion derecha) {
            this.izquierda = izquierda;
            this.operador = operador;
            this.derecha = derecha;
        }

        //METODOS
        //Genera comparacion relacional
        @Override
        public void generar(GeneradorCodigo g, Contexto c, String verdadera, String falsa) {
            ResultadoExpresion izq = izquierda.generar(g, c);
            ResultadoExpresion der = derecha.generar(g, c);

            c.gen("if " + izq.getDir() + " " + operador + " " + der.getDir() + " goto " + verdadera);
            c.gen("goto " + falsa);
        }
    }

    //NODO BOOLEANO BINARIO
    public static class NodoBoolBinario extends NodoBooleano {

        //ATRIBUTOS
        private final NodoBooleano izquierda;
        private final String operador;
        private final NodoBooleano derecha;

        //CONSTRUCTOR
        public NodoBoolBinario(NodoBooleano izquierda, String operador, NodoBooleano derecha) {
            this.izquierda = izquierda;
            this.operador = operador;
            this.derecha = derecha;
        }

        //METODOS
        //Genera AND u OR con cortocircuito
        @Override
        public void generar(GeneradorCodigo g, Contexto c, String verdadera, String falsa) {
            if (operador.equals("&&")) {
                String intermedia = g.nuevaEtiqueta();

                izquierda.generar(g, c, intermedia, falsa);
                c.etiqueta(intermedia);
                derecha.generar(g, c, verdadera, falsa);
            } else {
                String intermedia = g.nuevaEtiqueta();

                izquierda.generar(g, c, verdadera, intermedia);
                c.etiqueta(intermedia);
                derecha.generar(g, c, verdadera, falsa);
            }
        }
    }

    //NODO NOT
    public static class NodoNot extends NodoBooleano {

        //ATRIBUTOS
        private final NodoBooleano expresion;

        //CONSTRUCTOR
        public NodoNot(NodoBooleano expresion) {
            this.expresion = expresion;
        }

        //METODOS
        //Invierte true y false
        @Override
        public void generar(GeneradorCodigo g, Contexto c, String verdadera, String falsa) {
            expresion.generar(g, c, falsa, verdadera);
        }
    }

    //NODO BOOLEANO CONSTANTE
    public static class NodoBoolConstante extends NodoBooleano {

        //ATRIBUTOS
        private final boolean valor;

        //CONSTRUCTOR
        public NodoBoolConstante(boolean valor) {
            this.valor = valor;
        }

        //METODOS
        //Genera salto directo
        @Override
        public void generar(GeneradorCodigo g, Contexto c, String verdadera, String falsa) {
            if (valor) {
                c.gen("goto " + verdadera);
            } else {
                c.gen("goto " + falsa);
            }
        }
    }
}