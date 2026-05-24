import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Inicio {

    //ATRIBUTOS
    private final Scanner scanner;

    //CONSTRUCTOR
    public Inicio() {
        this.scanner = new Scanner(System.in);
    }

    //METODOS
    //Inicia el programa desde consola
    public void iniciar() {
        System.out.println("----- GENERADOR DE CODIGO DE 3 DIRECCIONES -----");
        System.out.print("Ingrese la ruta del archivo de entrada .txt: ");

        String rutaEntrada = scanner.nextLine().trim();

        try {
            String contenido = Files.readString(Paths.get(rutaEntrada));

            AnalizadorLexico lexico = new AnalizadorLexico(contenido);
            List<Token> tokens = lexico.analizar();

            Parser parser = new Parser(tokens);
            GeneradorCodigo.NodoPrograma programa = parser.parsear();

            System.out.println("\n----- TOKENS -----");
            for (Token token : tokens) {
                System.out.println(token);
            }

            System.out.println("\n----- ERRORES -----");

            boolean hayErrores = false;

            for (String error : lexico.getErrores()) {
                hayErrores = true;
                System.out.println("[LEXICO] " + error);
            }

            for (String error : parser.getErrores()) {
                hayErrores = true;
                System.out.println("[SINTACTICO] " + error);
            }

            if (hayErrores) {
                System.out.println("\nNo se genero codigo porque existen errores.");
                return;
            }

            GeneradorCodigo generador = new GeneradorCodigo();
            String codigo = generador.generar(programa);

            System.out.println("\n----- CODIGO DE 3 DIRECCIONES -----");
            System.out.println(codigo);

            Path rutaSalida = obtenerRutaSalida(rutaEntrada);
            Files.writeString(rutaSalida, codigo);

            System.out.println("Archivo generado en: " + rutaSalida.toAbsolutePath());

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    //Genera ruta de salida con extension tac
    private Path obtenerRutaSalida(String rutaEntrada) {
        Path entrada = Paths.get(rutaEntrada);
        String nombre = entrada.getFileName().toString();

        int punto = nombre.lastIndexOf(".");
        String base = punto >= 0 ? nombre.substring(0, punto) : nombre;

        String salida = base + ".tac";

        if (entrada.getParent() == null) {
            return Paths.get(salida);
        }

        return entrada.getParent().resolve(salida);
    }
}