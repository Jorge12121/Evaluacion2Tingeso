package com.example.ms_reportes.Services;


import com.example.ms_clientes.Entities.Cliente;
import com.example.ms_reservas_comprobante.Entities.Reservas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@Service
public class ReporteService {

    @Autowired
    private RestTemplate restTemplate;

    // Método genérico para consumir otros servicios REST
    private <T> T getFromService(String url, Class<T> responseType, String errorMsg) {
        try {
            return restTemplate.getForObject(url, responseType);
        } catch (Exception e) {
            throw new RuntimeException(errorMsg + ": " + e.getMessage(), e);
        }
    }

    private List<Reservas> obtenerReservasPagadas(LocalDate inicio, LocalDate fin) {
        String url = "http://ms-reservas/reservas/pagadas?inicio=" + inicio.toString() + "&fin=" + fin.toString();
        return getFromService(url, List.class, "Error al obtener cliente por RUT");
    }


    // RF8: Reporte mensual por número de vueltas o tiempo máximo
    public Map<String, Object> generarReporteMensualPorVueltas(YearMonth mesInicio, YearMonth mesFin) {
        Map<String, Object> reporte = new LinkedHashMap<>();
        YearMonth mesActual = mesInicio;

        while (!mesActual.isAfter(mesFin)) {
            LocalDate inicioMes = mesActual.atDay(1);
            LocalDate finMes = mesActual.atEndOfMonth();

            List<Reservas> reservas = obtenerReservasPagadas(inicioMes, finMes);

            Map<String, Integer> ingresosPorVueltas = new LinkedHashMap<>();
            ingresosPorVueltas.put("10 vueltas", 0);
            ingresosPorVueltas.put("15 vueltas", 0);
            ingresosPorVueltas.put("20 vueltas", 0);

            for (Reservas reserva : reservas) {
                switch (reserva.getNumero_vueltas()) {
                    case 10 -> ingresosPorVueltas.merge("10 vueltas", reserva.getPrecio_total(), Integer::sum);
                    case 15 -> ingresosPorVueltas.merge("15 vueltas", reserva.getPrecio_total(), Integer::sum);
                    case 20 -> ingresosPorVueltas.merge("20 vueltas", reserva.getPrecio_total(), Integer::sum);
                }
            }

            reporte.put(mesActual.toString(), Map.of("ingresos_por_vueltas", ingresosPorVueltas));
            mesActual = mesActual.plusMonths(1);
        }

        return reporte;
    }

    // RF9: Reporte mensual por cantidad de personas
    public Map<String, Object> generarReporteMensualPorPersonas(YearMonth mesInicio, YearMonth mesFin) {
        Map<String, Object> reporte = new LinkedHashMap<>();
        YearMonth mesActual = mesInicio;

        while (!mesActual.isAfter(mesFin)) {
            LocalDate inicioMes = mesActual.atDay(1);
            LocalDate finMes = mesActual.atEndOfMonth();

            List<Reservas> reservas = obtenerReservasPagadas(inicioMes, finMes);

            Map<String, Integer> ingresosPorPersonas = new LinkedHashMap<>();
            ingresosPorPersonas.put("1-2 personas", 0);
            ingresosPorPersonas.put("3-5 personas", 0);
            ingresosPorPersonas.put("6-10 personas", 0);
            ingresosPorPersonas.put("11-15 personas", 0);

            for (Reservas reserva : reservas) {
                int cantidad = reserva.getCantidad_personas();
                int total = reserva.getPrecio_total();

                if (cantidad >= 1 && cantidad <= 2) {
                    ingresosPorPersonas.merge("1-2 personas", total, Integer::sum);
                } else if (cantidad >= 3 && cantidad <= 5) {
                    ingresosPorPersonas.merge("3-5 personas", total, Integer::sum);
                } else if (cantidad >= 6 && cantidad <= 10) {
                    ingresosPorPersonas.merge("6-10 personas", total, Integer::sum);
                } else if (cantidad >= 11 && cantidad <= 15) {
                    ingresosPorPersonas.merge("11-15 personas", total, Integer::sum);
                }
            }

            reporte.put(mesActual.toString(), Map.of("ingresos_por_personas", ingresosPorPersonas));
            mesActual = mesActual.plusMonths(1);
        }

        return reporte;
    }
}
