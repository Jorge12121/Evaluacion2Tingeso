package com.example.ms_reservas_comprobante.Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    private Long id;

    @JsonProperty("nombre")
    private String nombre;

    @JsonProperty("correo")
    private String correo;

    @JsonProperty("numero_telefonico")
    private Integer numeroTelefonico;

    @JsonProperty("direccion")
    private String direccion;

    @JsonProperty("rut")
    private String rut;

    @Getter
    @JsonProperty("fechaNacimiento")
    private LocalDate fechaNacimiento;


}
