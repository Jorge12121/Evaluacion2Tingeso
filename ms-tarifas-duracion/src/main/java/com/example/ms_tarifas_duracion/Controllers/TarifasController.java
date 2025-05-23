package com.example.ms_tarifas_duracion.Controllers;


import com.example.ms_tarifas_duracion.Entities.Tarifas;
import com.example.ms_tarifas_duracion.Repositories.TarifasRepository;
import com.example.ms_tarifas_duracion.Services.TarifasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tarifas")
@CrossOrigin

public class TarifasController {

    @Autowired
    private TarifasService tarifasService;

    @Autowired
    private TarifasRepository tarifasRepository;

    @GetMapping
    public List<Tarifas> findAll() {
        return tarifasService.obtenerTarifas();
    }

    @GetMapping("/activa")
    public ResponseEntity<Tarifas> obtenerTarifaActiva() {
        Optional<Tarifas> activa = tarifasService.obtenerTarifaActiva();
        return activa.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/activar/{id}")
    public ResponseEntity<String> activarTarifa(@PathVariable int id) {
        tarifasService.activarTarifa(id);
        return ResponseEntity.ok("Tarifa activada con éxito.");
    }

    @PostMapping
    public Tarifas crearTarifa(@RequestBody Tarifas tarifa) {
        return tarifasRepository.save(tarifa);
    }

    @GetMapping("/base")
    public ResponseEntity<Double> obtenerTarifaBase(
            @RequestParam int idTarifa,
            @RequestParam int numeroVueltas) {
        return ResponseEntity.ok(tarifasService.obtenerTarifaBase(idTarifa, numeroVueltas));
    }

    @GetMapping("/duracion")
    public ResponseEntity<Integer> getDuracionPorVueltas(@RequestParam int numerodevueltas) {
        int duracion = tarifasService.obtenerDuracion(numerodevueltas);
        if (duracion > 0) {
            return ResponseEntity.ok(duracion);
        } else {
            // Si el número de vueltas no coincide con las predefinidas
            return ResponseEntity.badRequest().body(0); // O HttpStatus.NOT_FOUND o un mensaje más específico
        }
    }


}
