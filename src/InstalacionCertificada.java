import java.math.BigDecimal;
public class InstalacionCertificada extends Instalacion {
    private final BigDecimal materiales;
    public InstalacionCertificada(String codigo, String cliente, String direccion, BigDecimal horas, Zona zona, int puntos, BigDecimal materiales) {
        super(codigo, cliente, direccion, horas, zona, puntos);
        this.materiales = Validacion.positivo(materiales);
    }
    public BigDecimal getMateriales() { return materiales; }
    @Override public String categoria() { return "Instalacion electrica certificada"; }
    @Override public BigDecimal costo() { return dinero(super.costo().add(new BigDecimal("120")).add(materiales.multiply(new BigDecimal("0.05")))); }
    @Override public int diasAtencion() { return super.diasAtencion() + 2; }
    @Override public int maxVisitas() { return 2; }
}
