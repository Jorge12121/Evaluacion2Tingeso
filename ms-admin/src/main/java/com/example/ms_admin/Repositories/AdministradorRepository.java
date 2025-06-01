package com.example.ms_admin.Repositories;


import com.example.ms_admin.Entities.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {
    Optional<Administrador> findByRut(int rut);
}
