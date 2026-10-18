import java.math.BigDecimal;
import java.math.RoundingMode;
/** Contiene los datos y el ciclo de vida compartidos por todas las ordenes. */
public abstract class OrdenServicio {
    private final String codigo;
    private final String cliente;
    private final String direccion;
    private final BigDecimal horas;
    private final Zona zona;
    private EstadoOrden estado = EstadoOrden.PENDIENTE;
    private int visitas = 0;
    protected OrdenServicio(String codigo, String cliente, String direccion, BigDecimal horas, Zona zona) {
        this.codigo = Validacion.texto(codigo);
        this.cliente = Validacion.texto(cliente);
        this.direccion = Validacion.texto(direccion);
        this.horas = Validacion.positivo(horas);
        Validacion.exigir(zona != null, "Seleccione una zona valida.");
        this.zona = zona;
    }
    public abstract String categoria();
    public abstract BigDecimal costo();
    public abstract int diasAtencion();
    public abstract int maxVisitas();
    public boolean compatible(Cuadrilla cuadrilla) { return cuadrilla != null; }
    protected BigDecimal tarifaBase() { return new BigDecimal(zona == Zona.METROPOLITANA ? "100" : "175"); }
    protected BigDecimal dinero(BigDecimal valor) { return valor.setScale(2, RoundingMode.HALF_UP); }
    public String getCodigo() { return codigo; }
    public String getCliente() { return cliente; }
    public String getDireccion() { return direccion; }
    public BigDecimal getHoras() { return horas; }
    public Zona getZona() { return zona; }
    public EstadoOrden getEstado() { return estado; }
    public int getVisitas() { return visitas; }
    void asignar() {
        Validacion.exigir(estado == EstadoOrden.PENDIENTE && visitas < maxVisitas(), "La orden no esta disponible.");
        estado = EstadoOrden.ASIGNADA;
    }
    void retirar() {
        Validacion.exigir(estado == EstadoOrden.ASIGNADA, "La orden no esta asignada.");
        estado = EstadoOrden.PENDIENTE;
    }
    BigDecimal iniciarVisita() {
        Validacion.exigir(estado == EstadoOrden.ASIGNADA, "La orden no esta asignada.");
        visitas++;
        estado = EstadoOrden.EN_ATENCION;
        return visitas > 1 ? new BigDecimal("40.00") : new BigDecimal("0.00");
    }
    void resultado(boolean completada) {
        Validacion.exigir(estado == EstadoOrden.EN_ATENCION, "La orden no esta en atencion.");
        estado = completada ? EstadoOrden.COMPLETADA :
                (visitas >= maxVisitas() ? EstadoOrden.CANCELADA : EstadoOrden.PENDIENTE);
    }
    @Override public String toString() {
        return codigo + " | " + categoria() + " | " + cliente + " | " + direccion + " | " + zona
                + " | horas: " + horas + " | " + estado + " | visitas: " + visitas;
    }
}
