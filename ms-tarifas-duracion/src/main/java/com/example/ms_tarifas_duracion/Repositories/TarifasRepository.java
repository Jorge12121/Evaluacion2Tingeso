package com.example.ms_tarifas_duracion.Repositories;


import com.example.ms_tarifas_duracion.Entities.Tarifas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface TarifasRepository extends JpaRepository<Tarifas, Integer> {

    @Query("SELECT t FROM Tarifas t WHERE t.habilitada = true")
    Optional<Tarifas> buscarTarifaActiva();






}
