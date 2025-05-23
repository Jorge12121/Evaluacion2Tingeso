package com.example.ms_descuentos_frecuentes.Repositories;


import com.example.ms_descuentos_frecuentes.Entities.DescuentoFrecuentes;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DescuentoFrecuentesRepository extends CrudRepository<DescuentoFrecuentes,Integer> {
}
