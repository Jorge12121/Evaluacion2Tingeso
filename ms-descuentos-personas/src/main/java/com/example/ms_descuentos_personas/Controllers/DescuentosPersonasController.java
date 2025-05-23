    package com.example.ms_descuentos_personas.Controllers;


    import com.example.ms_descuentos_personas.Services.DescuentosPersonasService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;


    @RestController
    @RequestMapping("/descuentos-persona")
    @CrossOrigin
    public class DescuentosPersonasController {

        @Autowired
        private DescuentosPersonasService reservasService;

        @GetMapping()
        public ResponseEntity<Double> getDescuentoPorPersonas(
                @RequestParam double precioBase,
                @RequestParam int numeroPersonas) {

            double descuento = reservasService.AplicarDescuentoPersonas(precioBase, numeroPersonas);

            if (descuento > 0 || (descuento == 0 && (numeroPersonas < 3 || numeroPersonas > 15))) {
                // Devuelve el descuento calculado (puede ser 0 si no aplica pero es un caso válido)
                return ResponseEntity.ok(descuento);
            } else {
                // Este caso sería más para un error si las entradas no son válidas o si la lógica de negocio dice que 0 es un error
                // Basado en tu lógica, 0 significa "no aplica descuento" o "fuera de rango".
                // Podríamos devolver 0 con 200 OK, o un 400 Bad Request si los parámetros estuvieran fuera de un rango esperado antes de calcular.
                // Para este caso, si numeroPersonas no está en ningún rango, simplemente devuelve 0.0 con 200 OK.
                return ResponseEntity.ok(0.0);
            }
        }


    }
