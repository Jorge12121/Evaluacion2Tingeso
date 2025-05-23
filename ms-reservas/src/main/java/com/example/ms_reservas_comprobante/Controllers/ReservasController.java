    package com.example.ms_reservas_comprobante.Controllers;

    import com.example.ms_reservas_comprobante.Entities.Reservas;
    import com.example.ms_reservas_comprobante.Services.ReservasService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.time.LocalDate;
    import java.time.LocalTime;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    @RestController
    @RequestMapping("/reservas")
    @CrossOrigin
    public class ReservasController {

        @Autowired
        ReservasService reservasService;


        @GetMapping()
        public ResponseEntity<List<Reservas>> obtenerReservas(
        ) {
            List<Reservas> reservas = reservasService.getAllReservas();
            return ResponseEntity.ok(reservas);
        }


        @PostMapping("/{rut}")
        public ResponseEntity<?> generarReserva(
                @PathVariable String rut,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
                @RequestParam int cantidadPersonas,
                @RequestParam int numeroVueltas,
                @RequestParam boolean hayOtroCumpleanero
        ) {
            try {
                Reservas reserva = reservasService.generarReserva(rut, fecha, horaInicio, cantidadPersonas, numeroVueltas, hayOtroCumpleanero);
                Map<String, Object> response = new HashMap<>();
                response.put("mensaje", "Reserva generada con éxito");
                response.put("reserva", reserva);
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
            } catch (Exception e) {
                e.printStackTrace(); // Esto muestra la traza completa en consola
                return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
            }

        }


        @GetMapping("/visitas-del-mes/{idCliente}")
        public ResponseEntity<Integer> obtenerVisitasDelMes(@PathVariable Long idCliente) {
            try {
                int visitas = reservasService.obtenerVisitasDelMes(idCliente);
                return ResponseEntity.ok(visitas);
            } catch (Exception e) {
                return ResponseEntity.ok(-1);
            }
        }

        @GetMapping("/pagadas")
        public ResponseEntity<List<Reservas>> obtenerReservasPagadas(
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
            List<Reservas> reservas = reservasService.obtenerReservasPagadasPorFecha(fechaInicio, fechaFin);
            return ResponseEntity.ok(reservas);
        }

    }
