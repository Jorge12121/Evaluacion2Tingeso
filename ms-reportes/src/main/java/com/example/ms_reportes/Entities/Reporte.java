package com.example.ms_reportes.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Reporte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String tipo; // "Vueltas/Tiempo" o "Número de personas"
    private Date fecha;
    private int totalIngresos;
    private String detalle; // Para almacenar información adicional (Ej: "10 vueltas", "4-6 personas")
}
