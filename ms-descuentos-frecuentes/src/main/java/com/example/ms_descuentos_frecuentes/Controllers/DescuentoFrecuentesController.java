package com.example.ms_descuentos_frecuentes.Controllers;

import com.example.ms_descuentos_frecuentes.Services.DescuentoFrecuentesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/descuentos-frecuencia")
@CrossOrigin
public class DescuentoFrecuentesController {

    @Autowired
    private DescuentoFrecuentesService descuentoFrecuentesService;

    @GetMapping()
    public ResponseEntity<Double> getDescuentoPorPersonas(
            @RequestParam long idCliente,
            @RequestParam double precioBase) {

            double descuento = descuentoFrecuentesService.calcularDescuentoFrecuencia(idCliente, precioBase);

            return ResponseEntity.ok(descuento);
    }


}