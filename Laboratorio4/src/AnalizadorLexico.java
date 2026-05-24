import java.util.ArrayList;
import java.util.List;

public class AnalizadorLexico {

    //ATRIBUTOS
    private final String contenido;
    private final List<String> errores;
    private int pos;
    private int linea;
    private int columna;

    //CONSTRUCTOR
    public AnalizadorLexico(String contenido) {
        this.contenido = contenido == null ? "" : contenido;
        this.errores = new ArrayList<>();
        this.pos = 0;
        this.linea = 1;
        this.columna = 1;
    }

    //METODOS
    //Analiza el contenido y genera tokens
    public List<Token> analizar() {
        List<Token> tokens = new ArrayList<>();

        while (pos < contenido.length()) {
            char actual = contenido.charAt(pos);

            if (Character.isWhitespace(actual)) {
                avanzarEspacio(actual);
                continue;
            }

            if (Character.isLetter(actual)) {
                tokens.add(leerIdentificadorOPalabraReservada());
                continue;
            }

            if (Character.isDigit(actual)) {
                tokens.add(leerNumero());
                continue;
            }

            if (actual == '&' && mirarSiguiente('&')) {
                tokens.add(crearToken(Token.TipoToken.AND, "&&", 2));
                continue;
            }

            if (actual == '|' && mirarSiguiente('|')) {
                tokens.add(crearToken(Token.TipoToken.OR, "||", 2));
                continue;
            }

            if (actual == '=' && mirarSiguiente('=')) {
                tokens.add(crearToken(Token.TipoToken.ESIGUAL, "==", 2));
                continue;
            }

            if (actual == '!' && mirarSiguiente('=')) {
                tokens.add(crearToken(Token.TipoToken.NOIGUAL, "!=", 2));
                continue;
            }

            if (actual == '<' && mirarSiguiente('=')) {
                tokens.add(crearToken(Token.TipoToken.MEIGUAL, "<=", 2));
                continue;
            }

            if (actual == '>' && mirarSiguiente('=')) {
                tokens.add(crearToken(Token.TipoToken.MAIGUAL, ">=", 2));
                continue;
            }

            switch (actual) {
                case '(':
                    tokens.add(crearToken(Token.TipoToken.PARENIZQ, "(", 1));
                    break;
                case ')':
                    tokens.add(crearToken(Token.TipoToken.PARENDER, ")", 1));
                    break;
                case ';':
                    tokens.add(crearToken(Token.TipoToken.PYC, ";", 1));
                    break;
                case ',':
                    tokens.add(crearToken(Token.TipoToken.COMA, ",", 1));
                    break;
                case '+':
                    tokens.add(crearToken(Token.TipoToken.SUM, "+", 1));
                    break;
                case '-':
                    tokens.add(crearToken(Token.TipoToken.REST, "-", 1));
                    break;
                case '*':
                    tokens.add(crearToken(Token.TipoToken.MULT, "*", 1));
                    break;
                case '/':
                    tokens.add(crearToken(Token.TipoToken.DIV, "/", 1));
                    break;
                case '=':
                    tokens.add(crearToken(Token.TipoToken.IGUAL, "=", 1));
                    break;
                case '<':
                    tokens.add(crearToken(Token.TipoToken.MENOR, "<", 1));
                    break;
                case '>':
                    tokens.add(crearToken(Token.TipoToken.MAYOR, ">", 1));
                    break;
                case '!':
                    tokens.add(crearToken(Token.TipoToken.NOT, "!", 1));
                    break;
                default:
                    errores.add(String.format("line %d, col %d: ERROR Caracter inesperado '%c'",
                            linea, columna, actual));
                    tokens.add(crearToken(Token.TipoToken.DESCONOCIDO, String.valueOf(actual), 1));
                    break;
            }
        }

        tokens.add(new Token(Token.TipoToken.EOF, "$", linea, columna));
        return tokens;
    }

    //Lee identificadores y palabras reservadas
    private Token leerIdentificadorOPalabraReservada() {
        int inicio = pos;
        int col = columna;

        while (pos < contenido.length()) {
            char c = contenido.charAt(pos);

            if (!Character.isLetterOrDigit(c) && c != '_') {
                break;
            }

            avanzar();
        }

        String lexema = contenido.substring(inicio, pos);

        switch (lexema) {
            case "if":
                return new Token(Token.TipoToken.IF, lexema, linea, col);
            case "else":
                return new Token(Token.TipoToken.ELSE, lexema, linea, col);
            case "while":
                return new Token(Token.TipoToken.WHILE, lexema, linea, col);
            case "for":
                return new Token(Token.TipoToken.FOR, lexema, linea, col);
            case "true":
                return new Token(Token.TipoToken.TRUE, lexema, linea, col);
            case "false":
                return new Token(Token.TipoToken.FALSE, lexema, linea, col);
            default:
                return new Token(Token.TipoToken.ID, lexema, linea, col);
        }
    }

    //Lee numeros enteros
    private Token leerNumero() {
        int inicio = pos;
        int col = columna;

        while (pos < contenido.length() && Character.isDigit(contenido.charAt(pos))) {
            avanzar();
        }

        String lexema = contenido.substring(inicio, pos);
        return new Token(Token.TipoToken.NUM, lexema, linea, col);
    }

    //Crea un token y avanza
    private Token crearToken(Token.TipoToken tipo, String lexema, int longitud) {
        Token token = new Token(tipo, lexema, linea, columna);

        for (int i = 0; i < longitud; i++) {
            avanzar();
        }

        return token;
    }

    //Valida si el siguiente caracter coincide
    private boolean mirarSiguiente(char esperado) {
        return pos + 1 < contenido.length() && contenido.charAt(pos + 1) == esperado;
    }

    //Avanza considerando salto de linea
    private void avanzarEspacio(char c) {
        if (c == '\n') {
            linea++;
            columna = 1;
            pos++;
        } else if (c == '\r') {
            pos++;
        } else {
            avanzar();
        }
    }

    //Avanza un caracter
    private void avanzar() {
        pos++;
        columna++;
    }

    //GETTERS
    public List<String> getErrores() {
        return errores;
    }
}