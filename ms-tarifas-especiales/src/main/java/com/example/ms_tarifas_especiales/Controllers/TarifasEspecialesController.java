    package com.example.ms_tarifas_especiales.Controllers;
    import com.example.ms_tarifas_especiales.Services.TarifasEspecialesService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import java.time.LocalDate;


    @RestController
    @RequestMapping("/tarifas-especiales")
    @CrossOrigin
    public class TarifasEspecialesController {

        @Autowired
        private TarifasEspecialesService tarifasEspecialesService;

        @GetMapping("/cumpleanos")
        public ResponseEntity<Double> obtenerDescuentoCumpleanos(
                @RequestParam long idCliente,
                @RequestParam boolean hayOtroCumpleanero,
                @RequestParam double precioBase,
                @RequestParam int tamanoGrupo,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
        ) {
            double descuento = tarifasEspecialesService.aplicarDescuentoCumpleanos(idCliente, hayOtroCumpleanero, precioBase, tamanoGrupo, fecha);
            return ResponseEntity.ok(descuento);
        }

    }
