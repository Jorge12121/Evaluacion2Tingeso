package com.example.ms_reportes.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor


public class Reservas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private Long idCliente;
    private LocalDate fecha;
    private int cantidad_personas;
    private int numero_vueltas;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int duracion;
    private int precio_base;
    private int descuento_persona;
    private int descuento_frecuencia;
    private int descuento_cumpleaños;
    private int precio_total_sinIVA;
    private int IVA;
    private int precio_total;
    private int idTarifa;
    private String estado; // pendiente , pagado, cancelado
}
