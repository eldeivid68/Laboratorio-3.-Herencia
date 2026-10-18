import java.math.BigDecimal;
import java.math.RoundingMode;
public class Instalacion extends OrdenServicio {
    private final int puntos;
    public Instalacion(String codigo, String cliente, String direccion, BigDecimal horas, Zona zona, int puntos) {
        super(codigo, cliente, direccion, horas, zona);
        Validacion.exigir(puntos > 0, "Los puntos deben ser un entero positivo.");
        this.puntos = puntos;
    }
    public int getPuntos() { return puntos; }
    public BigDecimal horasFacturables() {
        return getHoras().max(BigDecimal.valueOf(puntos).multiply(new BigDecimal("0.75"))).setScale(0, RoundingMode.CEILING);
    }
    @Override public String categoria() { return "Instalacion"; }
    @Override public BigDecimal costo() { return dinero(tarifaBase().add(horasFacturables().multiply(new BigDecimal("90")))); }
    @Override public int diasAtencion() { return getZona() == Zona.METROPOLITANA ? 3 : 5; }
    @Override public int maxVisitas() { return 3; }
}
