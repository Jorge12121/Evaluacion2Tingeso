package com.example.ms_descuentos_personas.Services;


import org.springframework.stereotype.Service;

@Service
public class DescuentosPersonasService {

    public double AplicarDescuentoPersonas(double precioBase, int numeroPersonas) {
        if (numeroPersonas >= 3 && numeroPersonas <= 5) {
            return precioBase * 0.1;
        } else if (numeroPersonas >= 6 && numeroPersonas <= 10) {
            return precioBase * 0.2;
        } else if (numeroPersonas >= 11 && numeroPersonas <= 15) {
            return precioBase * 0.3;
        }
        return 0;
    }
}
