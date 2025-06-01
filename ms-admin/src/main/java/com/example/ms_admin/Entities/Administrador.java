package com.example.ms_admin.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Administrador {

    @Id
    private int id;

    private String nombre;
    private int edad;
    private int rut;
    private int telefono;
    private String correo;
    private String password;

}
