// src/main/java/com/example/ms_clientes/Controllers/ClienteController.java
package com.example.ms_clientes.Controllers;

import com.example.ms_clientes.Entities.Cliente; // Asumiendo que tu entidad Cliente está aquí
import com.example.ms_clientes.Services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat; // Necesario para @DateTimeFormat
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // Necesario para ResponseStatusException

import java.time.LocalDate; // Necesario para LocalDate
import java.util.List;

@RestController
@RequestMapping("/cliente") // La URL base para todos los endpoints de clientes
@CrossOrigin // Habilita CORS si tu frontend está en un dominio diferente
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // 1. Endpoint para crear un nuevo cliente (método POST)
    // POST http://localhost:8080/cliente
    @PostMapping
    public ResponseEntity<Cliente> crearCliente(@RequestBody Cliente cliente) {
        try {
            Cliente nuevoCliente = clienteService.crearCliente(cliente);
            return new ResponseEntity<>(nuevoCliente, HttpStatus.CREATED); // Código 201 CREATED
        } catch (ResponseStatusException e) {
            // Captura la excepción de RUT existente del servicio
            return new ResponseEntity<>(null, e.getStatusCode()); // Devuelve 400 BAD_REQUEST
        } catch (Exception e) {
            // Manejo genérico para cualquier otra excepción
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR); // Código 500 INTERNAL_SERVER_ERROR
        }
    }

    // 2. Endpoint para obtener todos los clientes (método GET)
    // GET http://localhost:8080/cliente
    @GetMapping
    public ResponseEntity<List<Cliente>> obtenerClientes() {
        List<Cliente> clientes = clienteService.findAll();
        // System.out.println(clientes); // Esto es solo para depuración, puedes quitarlo en producción
        return new ResponseEntity<>(clientes, HttpStatus.OK); // Código 200 OK
    }

    // 3. Endpoint para obtener un cliente por su ID (método GET)
    // GET http://localhost:8080/cliente/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> getClienteById(@PathVariable Long id) {
        try {
            Cliente cliente = clienteService.findById(id);
            return new ResponseEntity<>(cliente, HttpStatus.OK); // Código 200 OK
        } catch (IllegalArgumentException e) {
            // Captura la excepción si el cliente no es encontrado o el ID es nulo
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Código 404 NOT_FOUND
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Código 500 INTERNAL_SERVER_ERROR
        }
    }

    // 4. Endpoint para obtener un cliente por su RUT (método GET)
    // GET http://localhost:8080/cliente/rut/{rut}
    @GetMapping("/rut/{rut}")
    public ResponseEntity<Cliente> getClienteByRut(@PathVariable String rut) {
        try {
            Cliente cliente = clienteService.findByRut(rut);
            return new ResponseEntity<>(cliente, HttpStatus.OK); // Código 200 OK
        } catch (IllegalArgumentException e) {
            // Captura la excepción si el cliente no es encontrado o el RUT es nulo
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Código 404 NOT_FOUND
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Código 500 INTERNAL_SERVER_ERROR
        }
    }

    // 5. Endpoint para verificar si un cliente existe por RUT (método GET)
    // GET http://localhost:8080/cliente/existe/{rut}
    // Tu implementación actual es buena, pero podemos devolver un boolean directamente en el cuerpo.
    @GetMapping("/existe/{rut}")
    public ResponseEntity<Boolean> verificarClienteExistente(@PathVariable String rut) {
        boolean existe = clienteService.verificarClienteExistente(rut);
        return new ResponseEntity<>(existe, HttpStatus.OK); // Código 200 OK, true/false en el cuerpo
    }

    // 6. Endpoint para eliminar un cliente por ID (método DELETE)
    // DELETE http://localhost:8080/cliente/eliminar/{id}
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminarCliente(@PathVariable Long id) {
        try {
            clienteService.eliminarCliente(id);
            // 204 No Content es apropiado para una eliminación exitosa que no devuelve contenido
            return new ResponseEntity<>("Cliente eliminado exitosamente.", HttpStatus.NO_CONTENT); // Código 204 NO_CONTENT
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); // Código 404 NOT_FOUND
        } catch (Exception e) {
            return new ResponseEntity<>("Error al eliminar el cliente: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // Código 500 INTERNAL_SERVER_ERROR
        }
    }

    // 7. Endpoint para verificar si es cumpleaños en una fecha específica
    // GET http://localhost:8080/cliente/{clienteId}/es-cumpleanos?fecha=YYYY-MM-DD
    @GetMapping("/{clienteId}/es-cumpleanos")
    public ResponseEntity<Boolean> esCumpleanosEnFecha(
            @PathVariable Long clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            Cliente cliente = clienteService.findById(clienteId); // Obtener el cliente primero
            boolean esCumple = clienteService.esCumpleanosEnFecha(cliente, fecha);
            return new ResponseEntity<>(esCumple, HttpStatus.OK); // Código 200 OK
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Código 404 NOT_FOUND si el cliente no existe
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Código 500 INTERNAL_SERVER_ERROR
        }
    }

}
