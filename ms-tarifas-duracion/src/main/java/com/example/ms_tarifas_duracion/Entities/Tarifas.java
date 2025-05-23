package com.example.ms_tarifas_duracion.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tarifas") // Asegúrate de que el nombre coincide con el de tu tabla
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tarifas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String tipo_tarifa;
    private int diez;
    private int quince;
    private int veinte;

    private boolean habilitada = false;
}
