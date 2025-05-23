package com.example.ms_reportes.Controllers;


import com.example.ms_reportes.Services.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.Map;


@RestController
@RequestMapping("/reportes")
@CrossOrigin
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @GetMapping("/mensual/vueltas")
    public Map<String, Object> reporteMensualPorVueltas(
            @RequestParam String mesInicio,
            @RequestParam String mesFin) {

        YearMonth inicio = YearMonth.parse(mesInicio.trim());
        YearMonth fin = YearMonth.parse(mesFin.trim());

        return reporteService.generarReporteMensualPorVueltas(inicio, fin);
    }

    @GetMapping("/mensual/personas")
    public Map<String, Object> reporteMensualPorPersonas(
            @RequestParam String mesInicio,
            @RequestParam String mesFin) {

        YearMonth inicio = YearMonth.parse(mesInicio.trim());
        YearMonth fin = YearMonth.parse(mesFin.trim());

        return reporteService.generarReporteMensualPorPersonas(inicio, fin);
    }


}
