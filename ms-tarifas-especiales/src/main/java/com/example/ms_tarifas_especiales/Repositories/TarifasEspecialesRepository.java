package com.example.ms_tarifas_especiales.Repositories;


import com.example.ms_tarifas_especiales.Entities.TarifasEspeciales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TarifasEspecialesRepository extends JpaRepository<TarifasEspeciales, Integer> {

}
