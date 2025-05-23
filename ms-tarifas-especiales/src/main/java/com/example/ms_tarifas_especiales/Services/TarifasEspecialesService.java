package com.example.ms_tarifas_especiales.Services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;

@Service
public class TarifasEspecialesService {
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

    public boolean esCumpleanosEnFecha(long idCliente, LocalDate fecha) {
        String url = "http://ms-clientes/cliente/" + idCliente + "/es-cumpleanos?fecha=" + fecha.toString();
        return getFromService(url, Boolean.class, "Error al obtener si es cumpleañero");
    }

    public double aplicarDescuentoCumpleanos(long idCliente, boolean hayOtroCumpleanero, double precioBase, int tamanoGrupo, LocalDate fecha) {

        boolean esCumpleanero = esCumpleanosEnFecha(idCliente, fecha);
        double descuentoTotal = 0;

        if (esCumpleanero && (tamanoGrupo > 2)) {
            descuentoTotal += precioBase * 0.5;

            if (hayOtroCumpleanero && tamanoGrupo >= 6) {
                descuentoTotal += precioBase * 0.5;
            }
        }

        return descuentoTotal;
    }
}
