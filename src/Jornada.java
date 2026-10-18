import java.math.BigDecimal;
import java.util.*;
public class Jornada {
    private final String codigo;
    private final Cuadrilla cuadrilla;
    private final List<OrdenServicio> ordenes = new ArrayList<>();
    private final Set<String> resultados = new HashSet<>();
    private EstadoJornada estado = EstadoJornada.PREPARACION;
    public Jornada(String codigo, Cuadrilla cuadrilla) {
        this.codigo = Validacion.texto(codigo);
        Validacion.exigir(cuadrilla != null, "La cuadrilla es obligatoria.");
        this.cuadrilla = cuadrilla;
    }
    public String getCodigo() { return codigo; }
    public Cuadrilla getCuadrilla() { return cuadrilla; }
    public EstadoJornada getEstado() { return estado; }
    public List<OrdenServicio> getOrdenes() { return Collections.unmodifiableList(ordenes); }
    public BigDecimal horasAsignadas() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrdenServicio o : ordenes) total = total.add(o.getHoras());
        return total;
    }
    public BigDecimal capacidadDisponible() { return cuadrilla.getCapacidad().subtract(horasAsignadas()); }
    void asignar(OrdenServicio orden) {
        Validacion.exigir(estado == EstadoJornada.PREPARACION, "Solo puede asignar durante la preparacion.");
        Validacion.exigir(orden.getEstado() == EstadoOrden.PENDIENTE && orden.getVisitas() < orden.maxVisitas(), "La orden no esta pendiente o agoto sus visitas.");
        Validacion.exigir(orden.compatible(cuadrilla), "La cuadrilla no es compatible con este servicio.");
        Validacion.exigir(orden.getHoras().compareTo(capacidadDisponible()) <= 0, "La orden excede la capacidad disponible.");
        orden.asignar();
        ordenes.add(orden);
    }
    void retirar(OrdenServicio orden) {
        Validacion.exigir(estado == EstadoJornada.PREPARACION && ordenes.contains(orden), "No se puede retirar esta orden de la jornada.");
        orden.retirar();
        ordenes.remove(orden);
    }
    Map<String, BigDecimal> iniciar() {
        Validacion.exigir(estado == EstadoJornada.PREPARACION && !ordenes.isEmpty(), "La jornada debe estar en preparacion y tener ordenes.");
        // Primero se revisan todas. Asi un rechazo no deja cambios a medias.
        for (OrdenServicio o : ordenes) Validacion.exigir(o.getEstado() == EstadoOrden.ASIGNADA, "Una orden no esta asignada.");
        Map<String, BigDecimal> cargos = new LinkedHashMap<>();
        for (OrdenServicio o : ordenes) cargos.merge(o.categoria(), o.iniciarVisita(), BigDecimal::add);
        estado = EstadoJornada.INICIADA;
        return cargos;
    }
    void registrarResultado(OrdenServicio orden, boolean completada) {
        Validacion.exigir(estado == EstadoJornada.INICIADA && ordenes.contains(orden) && !resultados.contains(orden.getCodigo()), "Resultado invalido o ya registrado.");
        orden.resultado(completada);
        resultados.add(orden.getCodigo());
        if (resultados.size() == ordenes.size()) estado = EstadoJornada.CERRADA;
    }
    @Override public String toString() {
        return codigo + " | cuadrilla: " + cuadrilla.getCodigo() + " | " + estado + " | horas: " + horasAsignadas() + " | disponibles: " + capacidadDisponible();
    }
}
