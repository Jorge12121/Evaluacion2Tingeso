package com.example.ms_descuentos_frecuentes.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity

public class DescuentoFrecuentes {
    @Id
    int idReserva;
    int descuento;
}
