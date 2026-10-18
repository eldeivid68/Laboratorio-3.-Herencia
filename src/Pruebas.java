import java.math.BigDecimal;
import java.util.Objects;
/** Pruebas reproducibles sin librerias externas. */
public class Pruebas {
    private static int total = 0;
    private static BigDecimal n(String valor) { return new BigDecimal(valor); }
    private static void igual(String caso, Object esperado, Object obtenido) {
        total++;
        boolean ok = esperado instanceof BigDecimal && obtenido instanceof BigDecimal
                ? ((BigDecimal) esperado).compareTo((BigDecimal) obtenido) == 0 : Objects.equals(esperado, obtenido);
        System.out.println((ok ? "OK" : "FALLO") + " | " + caso + " | esperado=" + esperado + " | obtenido=" + obtenido);
        if (!ok) throw new AssertionError(caso);
    }
    private static void rechaza(String caso, Runnable accion) {
        try { accion.run(); } catch (IllegalArgumentException e) {
            igual(caso + " (" + e.getMessage() + ")", "rechazo", "rechazo"); return;
        }
        igual(caso, "rechazo", "aceptado");
    }
    private static Diagnostico diagnostico(String codigo, String horas, Zona zona) {
        return new Diagnostico(codigo, "Cliente", "Direccion", n(horas), zona);
    }
    private static Instalacion instalacion(String codigo, String horas, Zona zona, int puntos) {
        return new Instalacion(codigo, "Cliente", "Direccion", n(horas), zona, puntos);
    }
    private static InstalacionCertificada certificada(String codigo, Zona zona) {
        return new InstalacionCertificada(codigo, "Cliente", "Direccion", n("2.5"), zona, 5, n("2000"));
    }
    private static MantenimientoAire aire(String codigo, Zona zona, int btu) {
        return new MantenimientoAire(codigo, "Cliente", "Direccion", n("1.5"), zona, btu);
    }
    private static void politica(OrdenServicio orden, int limite) {
        Sistema s = new Sistema();
        s.registrar(orden);
        s.registrarCuadrilla(new Cuadrilla("C", n("8"), true));
        for (int i = 1; i <= limite; i++) {
            String j = "J" + i;
            s.crearJornada(j, "C"); s.asignar(j, orden.getCodigo());
            igual(orden.categoria() + " antes de iniciar visita " + i, n(String.valueOf(Math.max(0, i - 2) * 40)), s.getIngresoVisitas());
            s.iniciar(j);
            igual(orden.categoria() + " cargo visita " + i, n(String.valueOf((i - 1) * 40)), s.getIngresoVisitas());
            igual(orden.categoria() + " contador " + i, i, orden.getVisitas());
            s.resultado(j, orden.getCodigo(), false);
            igual(orden.categoria() + " falla " + i, i == limite ? EstadoOrden.CANCELADA : EstadoOrden.PENDIENTE, orden.getEstado());
            igual("Jornada termina tras resultado " + j, EstadoJornada.CERRADA, s.jornada(j).getEstado());
        }
        s.crearJornada("FINAL", "C");
        String previo = s.reportes();
        rechaza("No reasignar cancelada " + orden.categoria(), () -> s.asignar("FINAL", orden.getCodigo()));
        igual("Rechazo conserva reporte " + orden.categoria(), true, previo.equals(s.reportes()));
        igual("Registro no cambia con fallos " + orden.categoria(), orden.costo(), s.getIngresoRegistro());
    }
    public static void main(String[] args) {
        Zona m = Zona.METROPOLITANA, p = Zona.PERIFERICA;
        igual("Diagnostico M 1 h", n("150"), diagnostico("D", "1", m).costo());
        igual("Diagnostico P 1.5 h", n("225"), diagnostico("D", "1.5", p).costo());
        igual("Diagnostico M plazo", 1, diagnostico("D", "1", m).diasAtencion());
        igual("Diagnostico P plazo", 2, diagnostico("D", "1", p).diasAtencion());
        igual("Instalacion M 2.5 h 5 puntos", n("460"), instalacion("I", "2.5", m, 5).costo());
        igual("Instalacion P 2.5 h 5 puntos", n("535"), instalacion("I", "2.5", p, 5).costo());
        igual("Instalacion M 2.1 h 1 punto redondea 3 h", n("370"), instalacion("I", "2.1", m, 1).costo());
        igual("Instalacion M 3 h exactas no redondea a 4", n("370"), instalacion("I", "3", m, 1).costo());
        igual("Instalacion M plazo", 3, instalacion("I", "1", m, 1).diasAtencion());
        igual("Instalacion P plazo", 5, instalacion("I", "1", p, 1).diasAtencion());
        igual("Certificada M 2.5 h 5 puntos materiales 2000", n("680"), certificada("E", m).costo());
        igual("Certificada P 2.5 h 5 puntos materiales 2000", n("755"), certificada("E", p).costo());
        igual("Certificada M plazo", 5, certificada("E", m).diasAtencion());
        igual("Certificada P plazo", 7, certificada("E", p).diasAtencion());
        igual("Aire 1.5 h 9000 BTU", n("345"), aire("A", m, 9000).costo());
        igual("Aire 1.5 h 60000 BTU mismo costo", n("345"), aire("A", m, 60000).costo());
        igual("Aire plazo", 2, aire("A", m, 9000).diasAtencion());
        rechaza("Diagnostico 1.51 h", () -> diagnostico("D", "1.51", m));
        rechaza("Horas 0", () -> diagnostico("D", "0", m));
        rechaza("Horas negativas", () -> diagnostico("D", "-1", m));
        rechaza("Codigo vacio", () -> diagnostico(" ", "1", m));
        rechaza("Cliente vacio", () -> new Diagnostico("D", " ", "Dir", n("1"), m));
        rechaza("Direccion vacia", () -> new Diagnostico("D", "Cli", " ", n("1"), m));
        rechaza("Puntos 0", () -> instalacion("I", "1", m, 0));
        rechaza("Materiales 0", () -> new InstalacionCertificada("E", "C", "Dir", n("1"), m, 1, n("0")));
        rechaza("Aire periferico", () -> aire("A", p, 9000));
        rechaza("BTU 8999", () -> aire("A", m, 8999));
        rechaza("BTU 60001", () -> aire("A", m, 60001));
        rechaza("Capacidad 0", () -> new Cuadrilla("C", n("0"), true));
        Sistema s = Sistema.conDatosIniciales();
        igual("Ocho ordenes iniciales", 8, s.getOrdenes().size());
        igual("Tres cuadrillas iniciales", 3, s.getCuadrillas().size());
        igual("Iniciales pendientes", true, s.getOrdenes().stream().allMatch(o -> o.getEstado() == EstadoOrden.PENDIENTE));
        igual("Ingreso inicial", n("0"), s.ingresoTotal());
        String previo = s.reportes();
        s.cotizar(certificada("COT", m));
        igual("Cotizar no modifica reporte ni ingresos", true, previo.equals(s.reportes()));
        rechaza("Codigo D1 repetido", () -> s.registrar(diagnostico("D1", "1", m)));
        igual("Registro rechazado no modifica reporte", true, previo.equals(s.reportes()));
        rechaza("Cuadrilla C1 repetida", () -> s.registrarCuadrilla(new Cuadrilla("C1", n("2"), false)));
        s.crearJornada("J1", "C2");
        rechaza("Segunda jornada abierta C2", () -> s.crearJornada("JX", "C2"));
        rechaza("Jornada vacia", () -> s.iniciar("J1"));
        previo = s.reportes();
        rechaza("A1 a C2 sin refrigeracion", () -> s.asignar("J1", "A1"));
        igual("Incompatibilidad conserva reporte", true, previo.equals(s.reportes()));
        s.asignar("J1", "I1");
        igual("Capacidad usa 2.5 estimadas, no 4 facturables", n("2.5"), s.jornada("J1").horasAsignadas());
        s.crearJornada("J2", "C3");
        rechaza("Orden en dos jornadas", () -> s.asignar("J2", "I1"));
        s.asignar("J2", "E1");
        previo = s.reportes();
        rechaza("Exceder capacidad C3 con I2", () -> s.asignar("J2", "I2"));
        igual("Exceso conserva reporte", true, previo.equals(s.reportes()));
        s.asignar("J2", "D2");
        igual("Se acepta capacidad exacta 4 h", n("0"), s.jornada("J2").capacidadDisponible());
        s.retirar("J1", "I1");
        igual("Retiro devuelve pendiente", EstadoOrden.PENDIENTE, s.orden("I1").getEstado());
        igual("Retiro no cobra", n("0"), s.ingresoTotal());
        igual("Retiro no cuenta visita", 0, s.orden("I1").getVisitas());
        s.asignar("J1", "I1"); s.iniciar("J1");
        igual("Inicio pasa a atencion", EstadoOrden.EN_ATENCION, s.orden("I1").getEstado());
        igual("Primera visita sin recargo", n("0"), s.ingresoTotal());
        previo = s.reportes();
        rechaza("Iniciar por segunda vez", () -> s.iniciar("J1"));
        rechaza("Retirar tras inicio", () -> s.retirar("J1", "I1"));
        rechaza("Asignar tras inicio", () -> s.asignar("J1", "D1"));
        rechaza("Resultado de orden ajena", () -> s.resultado("J1", "D1", true));
        igual("Operaciones rechazadas conservan reporte", true, previo.equals(s.reportes()));
        s.resultado("J1", "I1", true);
        igual("Visita completada", EstadoOrden.COMPLETADA, s.orden("I1").getEstado());
        igual("Cierre automatico", EstadoJornada.CERRADA, s.jornada("J1").getEstado());
        rechaza("Resultado duplicado", () -> s.resultado("J1", "I1", false));
        s.crearJornada("J3", "C2");
        rechaza("No reasignar completada", () -> s.asignar("J3", "I1"));
        rechaza("Resultado antes de iniciar", () -> s.resultado("J2", "E1", true));
        s.iniciar("J2"); s.resultado("J2", "E1", false);
        igual("Jornada permanece abierta con resultado pendiente", EstadoJornada.INICIADA, s.jornada("J2").getEstado());
        rechaza("No reasignar antes de cerrar jornada anterior", () -> s.asignar("J3", "E1"));
        s.resultado("J2", "D2", true);
        s.asignar("J3", "E1"); s.retirar("J3", "E1");
        igual("Asignar y retirar revisita no cobra", n("0"), s.getIngresoVisitas());
        s.asignar("J3", "E1"); s.iniciar("J3");
        igual("Revisita inicial E1 cobra Q40", n("40"), s.getIngresoVisitas());
        s.resultado("J3", "E1", true);
        igual("Exito en ultima visita completa, no cancela", EstadoOrden.COMPLETADA, s.orden("E1").getEstado());
        s.registrar(diagnostico("NUEVA", "1", m));
        igual("Cobro al registrar", n("150"), s.getIngresoRegistro());
        igual("Total registro y visitas", n("190"), s.ingresoTotal());
        igual("Reporte separa categoria certificada", true, s.reportes().contains("Instalacion electrica certificada: Q40.00"));
        igual("Reporte excluye jornada cerrada J1", false, s.reportes().contains("J1 |"));
        politica(diagnostico("D", "1", m), 3);
        politica(instalacion("I", "1", m, 1), 3);
        politica(certificada("E", m), 2);
        politica(aire("A", m, 9000), 1);
        System.out.println("TOTAL: " + total + " verificaciones correctas.");
    }
}
