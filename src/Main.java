import java.math.BigDecimal;
import java.util.*;
public class Main {
    private final Scanner entrada = new Scanner(System.in);
    private final Sistema sistema = Sistema.conDatosIniciales();
    public Main() { }
    public static void main(String[] args) { new Main().ejecutar(); }
    private String leer(String mensaje) { System.out.print(mensaje); return entrada.nextLine().trim(); }
    private BigDecimal decimal(String mensaje) { return new BigDecimal(leer(mensaje).replace(',', '.')); }
    private int entero(String mensaje) { return Integer.parseInt(leer(mensaje)); }
    private boolean siNo(String mensaje) {
        String valor = leer(mensaje + " (s/n): ");
        Validacion.exigir(valor.equalsIgnoreCase("s") || valor.equalsIgnoreCase("n"), "Ingrese s o n.");
        return valor.equalsIgnoreCase("s");
    }
    private OrdenServicio leerOrden() {
        int tipo = entero("1 Diagnostico | 2 Instalacion | 3 Electrica certificada | 4 Aire: ");
        Validacion.exigir(tipo >= 1 && tipo <= 4, "Categoria invalida.");
        String codigo = leer("Codigo (para cotizar puede usar COT): ");
        String cliente = leer("Cliente: ");
        String direccion = leer("Direccion: ");
        BigDecimal horas = decimal("Horas estimadas: ");
        int z = entero("Zona: 1 Metropolitana | 2 Periferica: ");
        Validacion.exigir(z == 1 || z == 2, "Zona invalida.");
        Zona zona = z == 1 ? Zona.METROPOLITANA : Zona.PERIFERICA;
        // El switch solo construye el objeto; las reglas viven en cada clase.
        switch (tipo) {
            case 1: return new Diagnostico(codigo, cliente, direccion, horas, zona);
            case 2: return new Instalacion(codigo, cliente, direccion, horas, zona, entero("Puntos: "));
            case 3: return new InstalacionCertificada(codigo, cliente, direccion, horas, zona, entero("Puntos: "), decimal("Valor de materiales: "));
            default: return new MantenimientoAire(codigo, cliente, direccion, horas, zona, entero("BTU: "));
        }
    }
    public void ejecutar() {
        while (true) {
            System.out.println("\nTECNIHOGAR\n1 Cotizar / registrar\n2 Registrar cuadrilla\n3 Crear jornada\n4 Asignar orden\n5 Retirar orden\n6 Iniciar jornada\n7 Registrar resultado\n8 Consultar ordenes\n9 Consultar cuadrillas\n10 Reportes\n0 Salir");
            try {
                int opcion = entero("Opcion: ");
                switch (opcion) {
                    case 0: return;
                    case 1:
                        OrdenServicio o = leerOrden();
                        System.out.println(sistema.cotizar(o));
                        if (siNo("Registrar y cobrar")) { sistema.registrar(o); System.out.println("Orden registrada y cobrada."); }
                        break;
                    case 2:
                        sistema.registrarCuadrilla(new Cuadrilla(leer("Codigo: "), decimal("Capacidad en horas: "), siNo("Certificada en refrigeracion"))); break;
                    case 3: sistema.crearJornada(leer("Codigo de jornada: "), leer("Codigo de cuadrilla: ")); break;
                    case 4: sistema.asignar(leer("Jornada: "), leer("Orden: ")); break;
                    case 5: sistema.retirar(leer("Jornada: "), leer("Orden: ")); break;
                    case 6: sistema.iniciar(leer("Jornada: ")); break;
                    case 7: sistema.resultado(leer("Jornada: "), leer("Orden: "), siNo("Visita completada")); break;
                    case 8: sistema.getOrdenes().forEach(System.out::println); break;
                    case 9: sistema.getCuadrillas().forEach(System.out::println); break;
                    case 10: System.out.print(sistema.reportes()); break;
                    default: System.out.println("Opcion invalida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: ingrese un numero valido. Operacion cancelada.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (NoSuchElementException e) {
                System.out.println("Fin de la entrada."); return;
            }
        }
    }
}
