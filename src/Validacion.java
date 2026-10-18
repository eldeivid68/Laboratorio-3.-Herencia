import java.math.BigDecimal;
/** Validaciones comunes; no decide reglas de categorias. */
public final class Validacion {
    private Validacion() { }
    public static String texto(String valor) {
        if (valor == null || valor.trim().isEmpty()) throw new IllegalArgumentException("El texto o codigo no puede estar vacio.");
        return valor.trim();
    }
    public static BigDecimal positivo(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) throw new IllegalArgumentException("El numero debe ser positivo.");
        return valor;
    }
    public static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new IllegalArgumentException(mensaje);
    }
}
