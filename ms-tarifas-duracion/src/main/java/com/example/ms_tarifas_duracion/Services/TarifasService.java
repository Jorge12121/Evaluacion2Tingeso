package com.example.ms_tarifas_duracion.Services;


import com.example.ms_tarifas_duracion.Entities.Tarifas;
import com.example.ms_tarifas_duracion.Repositories.TarifasRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TarifasService {

    @Autowired
    TarifasRepository tarifasRepository;

    public double obtenerTarifaBase(int idTarifa, int numeroVueltas) {
        Tarifas tarifa = tarifasRepository.findById(idTarifa)
                .orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada con el ID: " + idTarifa));

        switch (numeroVueltas) {
            case 10:
                return tarifa.getDiez();
            case 15:
                return tarifa.getQuince();
            case 20:
                return tarifa.getVeinte();
            default:
                throw new IllegalArgumentException("Número de vueltas no válido");
        }
    }

    public Optional<Tarifas> obtenerTarifaActiva(){
        return tarifasRepository.buscarTarifaActiva();
    }

    public List<Tarifas> obtenerTarifas(){
        return tarifasRepository.findAll();
    }

    @Transactional
    public void activarTarifa(int idTarifa) {
        // Desactiva todas las tarifas
        List<Tarifas> todas = tarifasRepository.findAll();
        for (Tarifas tarifa : todas) {
            tarifa.setHabilitada(false);
        }

        // Activa la tarifa seleccionada
        Tarifas activar = tarifasRepository.findById(idTarifa)
                .orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada con el ID: " + idTarifa));
        activar.setHabilitada(true);

        // Guarda los cambios
        tarifasRepository.saveAll(todas); // guarda la desactivación
        tarifasRepository.save(activar);  // guarda la activación
    }

    public int obtenerDuracion(int numerodevueltas) {
        if (numerodevueltas == 10) return 30;
        if (numerodevueltas == 15) return 35;
        if (numerodevueltas == 20) return 40;
        return 0;
    }

}
