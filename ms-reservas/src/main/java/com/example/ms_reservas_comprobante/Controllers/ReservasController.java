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
        public ResponseEntity<Map<String, Object>> generarReserva(
                @PathVariable String rut,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
                @RequestParam int cantidadPersonas,
                @RequestParam int numeroVueltas,
                @RequestParam boolean hayOtroCumpleanero
        ) {
            try {
                // Generar la reserva
                Reservas nuevaReserva = reservasService.generarReserva(
                        rut, fecha, horaInicio, cantidadPersonas,
                        numeroVueltas, hayOtroCumpleanero
                );

                // Crear un mapa con los datos que quieres devolver
                Map<String, Object> response = new HashMap<>();
                response.put("id", nuevaReserva.getId());
                response.put("precioTotal", nuevaReserva.getPrecio_total());
                response.put("fecha", nuevaReserva.getFecha());
                response.put("horaInicio", nuevaReserva.getHoraInicio());
                response.put("cantidadPersonas", nuevaReserva.getCantidad_personas());
                response.put("numeroVueltas", nuevaReserva.getNumero_vueltas());

                // Devolver el mapa como respuesta
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }

    }
