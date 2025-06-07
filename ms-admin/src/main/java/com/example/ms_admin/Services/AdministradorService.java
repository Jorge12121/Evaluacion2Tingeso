package com.example.ms_admin.Services;


import com.example.ms_admin.Entities.Administrador;
import com.example.ms_admin.Repositories.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AdministradorService {
    @Autowired
    AdministradorRepository administradorRepository;

    public boolean verificarAdminExistente(int rut) {
        Optional<Administrador> administrador = administradorRepository.findByRut(rut);
        return administrador.isPresent();
    }
    public Administrador crearAdministrador(Administrador administrador) {
        return administradorRepository.save(administrador);
    }


}
