import java.util.ArrayList;
import java.util.List;

public class Parser {

    //ATRIBUTOS
    private final List<Token> tokens;
    private final List<String> errores;
    private int pos;

    //CONSTRUCTOR
    public Parser(List<Token> tokens) {
        this.tokens = tokens == null ? new ArrayList<>() : tokens;
        this.errores = new ArrayList<>();
        this.pos = 0;
    }

    //METODOS
    //Parsea el programa completo
    public GeneradorCodigo.NodoPrograma parsear() {
        List<GeneradorCodigo.NodoSentencia> sentencias = new ArrayList<>();

        while (!ver(Token.TipoToken.EOF)) {
            GeneradorCodigo.NodoSentencia sentencia = parsearSentencia();

            if (sentencia != null) {
                sentencias.add(sentencia);
            } else {
                avanzarSeguro();
            }
        }

        return new GeneradorCodigo.NodoPrograma(sentencias);
    }

    //Parsea una sentencia
    private GeneradorCodigo.NodoSentencia parsearSentencia() {
        if (ver(Token.TipoToken.ID)) {
            return parsearAsignacion(true);
        }

        if (ver(Token.TipoToken.IF)) {
            return parsearIf();
        }

        if (ver(Token.TipoToken.WHILE)) {
            return parsearWhile();
        }

        if (ver(Token.TipoToken.FOR)) {
            return parsearFor();
        }

        Token actual = actual();
        error(actual, "Sentencia invalida");
        return null;
    }

    //Parsea asignacion
    private GeneradorCodigo.NodoAsignacion parsearAsignacion(boolean requierePyc) {
        Token id = consumir(Token.TipoToken.ID, "Se esperaba identificador");

        consumir(Token.TipoToken.IGUAL, "Se esperaba '=' en asignacion");

        GeneradorCodigo.NodoExpresion expresion = parsearExpresion();

        if (requierePyc) {
            consumir(Token.TipoToken.PYC, "Se esperaba ';' al final de la asignacion");
        }

        return new GeneradorCodigo.NodoAsignacion(id.getLexema(), expresion);
    }

    //Parsea if o if else
    private GeneradorCodigo.NodoSentencia parsearIf() {
        consumir(Token.TipoToken.IF, "Se esperaba if");
        consumir(Token.TipoToken.PARENIZQ, "Se esperaba '(' despues de if");

        GeneradorCodigo.NodoBooleano condicion = parsearBooleano();

        consumir(Token.TipoToken.PARENDER, "Se esperaba ')' despues de condicion");

        GeneradorCodigo.NodoSentencia thenStmt = parsearSentencia();

        if (ver(Token.TipoToken.ELSE)) {
            consumir(Token.TipoToken.ELSE, "Se esperaba else");
            GeneradorCodigo.NodoSentencia elseStmt = parsearSentencia();
            return new GeneradorCodigo.NodoIfElse(condicion, thenStmt, elseStmt);
        }

        return new GeneradorCodigo.NodoIf(condicion, thenStmt);
    }

    //Parsea while
    private GeneradorCodigo.NodoSentencia parsearWhile() {
        consumir(Token.TipoToken.WHILE, "Se esperaba while");
        consumir(Token.TipoToken.PARENIZQ, "Se esperaba '(' despues de while");

        GeneradorCodigo.NodoBooleano condicion = parsearBooleano();

        consumir(Token.TipoToken.PARENDER, "Se esperaba ')' despues de condicion");

        GeneradorCodigo.NodoSentencia cuerpo = parsearSentencia();

        return new GeneradorCodigo.NodoWhile(condicion, cuerpo);
    }

    //Parsea for
    private GeneradorCodigo.NodoSentencia parsearFor() {
        consumir(Token.TipoToken.FOR, "Se esperaba for");
        consumir(Token.TipoToken.PARENIZQ, "Se esperaba '(' despues de for");

        GeneradorCodigo.NodoAsignacion inicial = parsearAsignacion(false);

        consumir(Token.TipoToken.PYC, "Se esperaba ';' despues de inicializacion del for");

        GeneradorCodigo.NodoBooleano condicion = parsearBooleano();

        consumir(Token.TipoToken.PYC, "Se esperaba ';' despues de condicion del for");

        GeneradorCodigo.NodoAsignacion actualizacion = parsearAsignacion(false);

        consumir(Token.TipoToken.PARENDER, "Se esperaba ')' despues del for");

        GeneradorCodigo.NodoSentencia cuerpo = parsearSentencia();

        return new GeneradorCodigo.NodoFor(inicial, condicion, actualizacion, cuerpo);
    }

    //Parsea expresiones aritmeticas
    private GeneradorCodigo.NodoExpresion parsearExpresion() {
        return parsearSumaResta();
    }

    //Parsea suma y resta
    private GeneradorCodigo.NodoExpresion parsearSumaResta() {
        GeneradorCodigo.NodoExpresion izquierda = parsearMultiplicacionDivision();

        while (ver(Token.TipoToken.SUM) || ver(Token.TipoToken.REST)) {
            Token op = actual();
            avanzar();
            GeneradorCodigo.NodoExpresion derecha = parsearMultiplicacionDivision();
            izquierda = new GeneradorCodigo.NodoBinario(izquierda, op.getLexema(), derecha);
        }

        return izquierda;
    }

    //Parsea multiplicacion y division
    private GeneradorCodigo.NodoExpresion parsearMultiplicacionDivision() {
        GeneradorCodigo.NodoExpresion izquierda = parsearUnario();

        while (ver(Token.TipoToken.MULT) || ver(Token.TipoToken.DIV)) {
            Token op = actual();
            avanzar();
            GeneradorCodigo.NodoExpresion derecha = parsearUnario();
            izquierda = new GeneradorCodigo.NodoBinario(izquierda, op.getLexema(), derecha);
        }

        return izquierda;
    }

    //Parsea menos unario
    private GeneradorCodigo.NodoExpresion parsearUnario() {
        if (ver(Token.TipoToken.REST)) {
            Token op = actual();
            avanzar();
            GeneradorCodigo.NodoExpresion expr = parsearUnario();
            return new GeneradorCodigo.NodoUnario(op.getLexema(), expr);
        }

        return parsearPrimario();
    }

    //Parsea atomos de expresion
    private GeneradorCodigo.NodoExpresion parsearPrimario() {
        if (ver(Token.TipoToken.ID)) {
            Token id = actual();
            avanzar();
            return new GeneradorCodigo.NodoValor(id.getLexema());
        }

        if (ver(Token.TipoToken.NUM)) {
            Token num = actual();
            avanzar();
            return new GeneradorCodigo.NodoValor(num.getLexema());
        }

        if (ver(Token.TipoToken.PARENIZQ)) {
            avanzar();
            GeneradorCodigo.NodoExpresion expr = parsearExpresion();
            consumir(Token.TipoToken.PARENDER, "Se esperaba ')' en expresion");
            return expr;
        }

        Token t = actual();
        error(t, "Expresion invalida");
        avanzarSeguro();
        return new GeneradorCodigo.NodoValor("0");
    }

    //Parsea expresiones booleanas
    private GeneradorCodigo.NodoBooleano parsearBooleano() {
        return parsearOr();
    }

    //Parsea OR
    private GeneradorCodigo.NodoBooleano parsearOr() {
        GeneradorCodigo.NodoBooleano izquierda = parsearAnd();

        while (ver(Token.TipoToken.OR)) {
            avanzar();
            GeneradorCodigo.NodoBooleano derecha = parsearAnd();
            izquierda = new GeneradorCodigo.NodoBoolBinario(izquierda, "||", derecha);
        }

        return izquierda;
    }

    //Parsea AND
    private GeneradorCodigo.NodoBooleano parsearAnd() {
        GeneradorCodigo.NodoBooleano izquierda = parsearNot();

        while (ver(Token.TipoToken.AND)) {
            avanzar();
            GeneradorCodigo.NodoBooleano derecha = parsearNot();
            izquierda = new GeneradorCodigo.NodoBoolBinario(izquierda, "&&", derecha);
        }

        return izquierda;
    }

    //Parsea NOT
    private GeneradorCodigo.NodoBooleano parsearNot() {
        if (ver(Token.TipoToken.NOT)) {
            avanzar();
            return new GeneradorCodigo.NodoNot(parsearNot());
        }

        return parsearBooleanoPrimario();
    }

    //Parsea booleano primario
    private GeneradorCodigo.NodoBooleano parsearBooleanoPrimario() {
        if (ver(Token.TipoToken.TRUE)) {
            avanzar();
            return new GeneradorCodigo.NodoBoolConstante(true);
        }

        if (ver(Token.TipoToken.FALSE)) {
            avanzar();
            return new GeneradorCodigo.NodoBoolConstante(false);
        }

        if (ver(Token.TipoToken.PARENIZQ)) {
            avanzar();
            GeneradorCodigo.NodoBooleano b = parsearBooleano();
            consumir(Token.TipoToken.PARENDER, "Se esperaba ')' en booleano");
            return b;
        }

        GeneradorCodigo.NodoExpresion izquierda = parsearExpresion();

        if (esRelacional(actual().getTipo())) {
            Token op = actual();
            avanzar();
            GeneradorCodigo.NodoExpresion derecha = parsearExpresion();
            return new GeneradorCodigo.NodoRelacional(izquierda, op.getLexema(), derecha);
        }

        error(actual(), "Se esperaba operador relacional");
        return new GeneradorCodigo.NodoBoolConstante(false);
    }

    //Valida si es operador relacional
    private boolean esRelacional(Token.TipoToken tipo) {
        return tipo == Token.TipoToken.MENOR
                || tipo == Token.TipoToken.MAYOR
                || tipo == Token.TipoToken.MEIGUAL
                || tipo == Token.TipoToken.MAIGUAL
                || tipo == Token.TipoToken.ESIGUAL
                || tipo == Token.TipoToken.NOIGUAL;
    }

    //Consume token esperado
    private Token consumir(Token.TipoToken tipo, String mensaje) {
        if (ver(tipo)) {
            Token token = actual();
            avanzar();
            return token;
        }

        Token token = actual();
        error(token, mensaje);
        return token;
    }

    //Valida tipo actual
    private boolean ver(Token.TipoToken tipo) {
        return actual().getTipo() == tipo;
    }

    //Retorna token actual
    private Token actual() {
        if (pos >= tokens.size()) {
            return tokens.get(tokens.size() - 1);
        }

        return tokens.get(pos);
    }

    //Avanza un token
    private void avanzar() {
        if (pos < tokens.size()) {
            pos++;
        }
    }

    //Avanza seguro para recuperar errores
    private void avanzarSeguro() {
        if (!ver(Token.TipoToken.EOF)) {
            avanzar();
        }
    }

    //Registra error sintactico
    private void error(Token token, String mensaje) {
        errores.add(String.format("line %d, col %d: ERROR Sintactico. %s",
                token.getLinea(), token.getColumna(), mensaje));
    }

    //GETTERS
    public List<String> getErrores() {
        return errores;
    }
}