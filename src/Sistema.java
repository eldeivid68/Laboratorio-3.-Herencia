import java.math.BigDecimal;
import java.util.*;
/** Coordina operaciones y conserva los ingresos; el menu no modifica entidades. */
public class Sistema {
    private final Map<String, OrdenServicio> ordenes = new LinkedHashMap<>();
    private final Map<String, Cuadrilla> cuadrillas = new LinkedHashMap<>();
    private final Map<String, Jornada> jornadas = new LinkedHashMap<>();
    private BigDecimal ingresoRegistro = new BigDecimal("0.00");
    private BigDecimal ingresoVisitas = new BigDecimal("0.00");
    private final Map<String, BigDecimal> ingresosCategoria = new LinkedHashMap<>();
    public Sistema() { }
    public String cotizar(OrdenServicio orden) { return "Costo: Q" + orden.costo() + " | atencion: " + orden.diasAtencion() + " dias habiles"; }
    public void registrar(OrdenServicio orden) { agregar(orden, true); }
    private void agregar(OrdenServicio orden, boolean cobrar) {
        Validacion.exigir(orden != null, "La orden es obligatoria.");
        Validacion.exigir(!ordenes.containsKey(orden.getCodigo()), "El codigo de orden ya existe.");
        Validacion.exigir(orden.getEstado() == EstadoOrden.PENDIENTE && orden.getVisitas() == 0, "La orden debe ser nueva.");
        BigDecimal monto = cobrar ? orden.costo() : new BigDecimal("0.00");
        ordenes.put(orden.getCodigo(), orden);
        ingresoRegistro = ingresoRegistro.add(monto);
        ingresosCategoria.merge(orden.categoria(), monto, BigDecimal::add);
    }
    public void registrarCuadrilla(Cuadrilla cuadrilla) {
        Validacion.exigir(cuadrilla != null, "La cuadrilla es obligatoria.");
        Validacion.exigir(!cuadrillas.containsKey(cuadrilla.getCodigo()), "El codigo de cuadrilla ya existe.");
        cuadrillas.put(cuadrilla.getCodigo(), cuadrilla);
    }
    public void crearJornada(String codigo, String codigoCuadrilla) {
        codigo = Validacion.texto(codigo);
        Validacion.exigir(!jornadas.containsKey(codigo), "El codigo de jornada ya existe.");
        Cuadrilla c = cuadrilla(codigoCuadrilla);
        for (Jornada j : jornadas.values()) Validacion.exigir(j.getCuadrilla() != c || j.getEstado() == EstadoJornada.CERRADA, "La cuadrilla ya tiene una jornada sin cerrar.");
        jornadas.put(codigo, new Jornada(codigo, c));
    }
    public OrdenServicio orden(String codigo) {
        OrdenServicio o = ordenes.get(Validacion.texto(codigo));
        Validacion.exigir(o != null, "No existe esa orden."); return o;
    }
    public Cuadrilla cuadrilla(String codigo) {
        Cuadrilla c = cuadrillas.get(Validacion.texto(codigo));
        Validacion.exigir(c != null, "No existe esa cuadrilla."); return c;
    }
    public Jornada jornada(String codigo) {
        Jornada j = jornadas.get(Validacion.texto(codigo));
        Validacion.exigir(j != null, "No existe esa jornada."); return j;
    }
    public List<OrdenServicio> getOrdenes() { return Collections.unmodifiableList(new ArrayList<>(ordenes.values())); }
    public List<Cuadrilla> getCuadrillas() { return Collections.unmodifiableList(new ArrayList<>(cuadrillas.values())); }
    public void asignar(String jornada, String orden) {
        Jornada j = jornada(jornada); OrdenServicio o = orden(orden);
        // Una jornada anterior puede conservar la referencia como historial.
        for (Jornada otra : jornadas.values())
            Validacion.exigir(otra.getEstado() == EstadoJornada.CERRADA || !otra.getOrdenes().contains(o), "La orden pertenece a otra jornada sin cerrar.");
        j.asignar(o);
    }
    public void retirar(String jornada, String orden) { jornada(jornada).retirar(orden(orden)); }
    public void iniciar(String codigo) {
        Map<String, BigDecimal> cargos = jornada(codigo).iniciar();
        for (Map.Entry<String, BigDecimal> cargo : cargos.entrySet()) {
            ingresoVisitas = ingresoVisitas.add(cargo.getValue());
            ingresosCategoria.merge(cargo.getKey(), cargo.getValue(), BigDecimal::add);
        }
    }
    public void resultado(String jornada, String orden, boolean completada) { jornada(jornada).registrarResultado(orden(orden), completada); }
    public BigDecimal getIngresoRegistro() { return ingresoRegistro; }
    public BigDecimal getIngresoVisitas() { return ingresoVisitas; }
    public BigDecimal ingresoTotal() { return ingresoRegistro.add(ingresoVisitas); }
    public String reportes() {
        StringBuilder r = new StringBuilder("ORDENES POR CATEGORIA Y ESTADO\n");
        Map<String, EnumMap<EstadoOrden, Integer>> conteo = new LinkedHashMap<>();
        for (OrdenServicio o : ordenes.values()) conteo.computeIfAbsent(o.categoria(), k -> new EnumMap<>(EstadoOrden.class)).merge(o.getEstado(), 1, Integer::sum);
        for (String categoria : conteo.keySet()) {
            r.append(categoria).append(": ");
            for (EstadoOrden estado : EstadoOrden.values()) r.append(estado).append("=").append(conteo.get(categoria).getOrDefault(estado, 0)).append(" ");
            r.append("\n");
        }
        r.append("Registro: Q").append(ingresoRegistro).append(" | Revisitas: Q").append(ingresoVisitas).append(" | Total: Q").append(ingresoTotal()).append("\n");
        ingresosCategoria.forEach((categoria, monto) -> r.append(categoria).append(": Q").append(monto).append("\n"));
        r.append("JORNADAS SIN CERRAR\n");
        for (Jornada j : jornadas.values()) if (j.getEstado() != EstadoJornada.CERRADA) r.append(j).append("\n");
        return r.toString();
    }
    public static Sistema conDatosIniciales() {
        Sistema s = new Sistema();
        s.registrarCuadrilla(new Cuadrilla("C1", new BigDecimal("8"), true));
        s.registrarCuadrilla(new Cuadrilla("C2", new BigDecimal("6"), false));
        s.registrarCuadrilla(new Cuadrilla("C3", new BigDecimal("4"), false));
        s.agregar(new Diagnostico("D1", "Ana", "Zona 1", new BigDecimal("1"), Zona.METROPOLITANA), false);
        s.agregar(new Diagnostico("D2", "Luis", "Mixco", new BigDecimal("1.5"), Zona.PERIFERICA), false);
        s.agregar(new Instalacion("I1", "Maria", "Zona 2", new BigDecimal("2.5"), Zona.METROPOLITANA, 5), false);
        s.agregar(new Instalacion("I2", "Jose", "Villa Nueva", new BigDecimal("2.1"), Zona.PERIFERICA, 1), false);
        s.agregar(new InstalacionCertificada("E1", "Pedro", "Zona 3", new BigDecimal("2.5"), Zona.METROPOLITANA, 5, new BigDecimal("2000")), false);
        s.agregar(new InstalacionCertificada("E2", "Sofia", "Amatitlan", new BigDecimal("1.2"), Zona.PERIFERICA, 1, new BigDecimal("1000")), false);
        s.agregar(new MantenimientoAire("A1", "Diego", "Zona 4", new BigDecimal("1.5"), Zona.METROPOLITANA, 9000), false);
        s.agregar(new MantenimientoAire("A2", "Lucia", "Zona 5", new BigDecimal("2"), Zona.METROPOLITANA, 60000), false);
        return s;
    }
}
