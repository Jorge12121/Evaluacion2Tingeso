package com.example.ms_admin.Controllers;


import com.example.ms_admin.Services.AdministradorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/administrador")
@CrossOrigin
public class AdministradorController {

    @Autowired
    private AdministradorService administradorService;

    @GetMapping("/existe/{rut}")
    public ResponseEntity<String> verificarAdmin(@PathVariable int rut) {
        boolean existe = administradorService.verificarAdminExistente(rut);

        if (existe) {
            return ResponseEntity.ok("Administrador encontrado");
        } else {
            return ResponseEntity.status(404).body("Administrador no encontrado");
        }
    }
}
