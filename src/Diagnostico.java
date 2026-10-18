import java.math.BigDecimal;
public class Diagnostico extends OrdenServicio {
    public Diagnostico(String codigo, String cliente, String direccion, BigDecimal horas, Zona zona) {
        super(codigo, cliente, direccion, horas, zona);
        Validacion.exigir(horas.compareTo(new BigDecimal("1.5")) <= 0, "El diagnostico no puede superar 1.5 horas.");
    }
    @Override public String categoria() { return "Diagnostico"; }
    @Override public BigDecimal costo() { return new BigDecimal(getZona() == Zona.METROPOLITANA ? "150.00" : "225.00"); }
    @Override public int diasAtencion() { return getZona() == Zona.METROPOLITANA ? 1 : 2; }
    @Override public int maxVisitas() { return 3; }
}
