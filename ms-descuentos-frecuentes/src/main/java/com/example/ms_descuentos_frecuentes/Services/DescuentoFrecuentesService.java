package com.example.ms_descuentos_frecuentes.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DescuentoFrecuentesService {

    @Autowired
    private RestTemplate restTemplate;

    // Método genérico para consumir otros servicios REST
    private <T> T getFromService(String url, Class<T> responseType, String errorMsg) {
        try {
            return restTemplate.getForObject(url, responseType);
        } catch (Exception e) {
            throw new RuntimeException(errorMsg + ": " + e.getMessage(), e);
        }
    }

    private int contarReservasDelMes(Long idCliente){
        String url = "http://ms-reservas/reservas/visitas-del-mes/"+ idCliente ;
        return getFromService(url, Integer.class, "Error al obtener la cantidad de reservas");
    }


    public double calcularDescuentoFrecuencia(Long idCliente, double precioBase) {


        int visitas = contarReservasDelMes(idCliente);

        if (visitas >= 7) return precioBase * 0.3;
        if (visitas >= 5) return precioBase * 0.2;
        if (visitas >= 2) return precioBase * 0.1;
        return 0;
    }
}
