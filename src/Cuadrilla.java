import java.math.BigDecimal;
public class Cuadrilla {
    private final String codigo;
    private final BigDecimal capacidad;
    private final boolean refrigeracion;
    public Cuadrilla(String codigo, BigDecimal capacidad, boolean refrigeracion) {
        this.codigo = Validacion.texto(codigo);
        this.capacidad = Validacion.positivo(capacidad);
        this.refrigeracion = refrigeracion;
    }
    public String getCodigo() { return codigo; }
    public BigDecimal getCapacidad() { return capacidad; }
    public boolean isRefrigeracion() { return refrigeracion; }
    @Override public String toString() { return codigo + " | capacidad: " + capacidad + " h | refrigeracion: " + refrigeracion; }
}
