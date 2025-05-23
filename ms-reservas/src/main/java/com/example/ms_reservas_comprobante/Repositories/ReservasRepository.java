package com.example.ms_reservas_comprobante.Repositories;



import com.example.ms_reservas_comprobante.Entities.Reservas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservasRepository extends JpaRepository<Reservas, Integer> {
    List<Reservas> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    // Consulta para obtener las reservas dentro de un rango de fechas y que tengan estado "pagado"
    @Query("SELECT r FROM Reservas r WHERE r.fecha BETWEEN :fechaInicio AND :fechaFin AND r.estado = 'pagado'")
    List<Reservas> findReservasByFechaAndEstado(LocalDate fechaInicio, LocalDate fechaFin);

    List<Reservas> findByFecha(LocalDate fecha);

    List<Reservas> findByIdCliente(Long idCliente);

    @Query(value = "SELECT COUNT(r) FROM Reservas r WHERE r.id_cliente = :clienteId " +
            "AND EXTRACT(MONTH FROM r.fecha) = EXTRACT(MONTH FROM CURRENT_DATE) " +
            "AND EXTRACT(YEAR FROM r.fecha) = EXTRACT(YEAR FROM CURRENT_DATE)", nativeQuery = true)
    int contarReservasDelMes(@Param("clienteId") Long clienteId);

}
