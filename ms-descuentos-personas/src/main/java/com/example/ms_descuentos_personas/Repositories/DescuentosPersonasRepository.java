package com.example.ms_descuentos_personas.Repositories;




import com.example.ms_descuentos_personas.Entities.DescuentosPersonas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DescuentosPersonasRepository extends JpaRepository<DescuentosPersonas, Integer> {

}
