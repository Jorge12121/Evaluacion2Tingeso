    package com.example.ms_clientes.Repositories;

    import com.example.ms_clientes.Entities.Cliente;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import org.springframework.stereotype.Repository;

    import java.util.Optional;

    @Repository
    public interface ClienteRepository extends JpaRepository<Cliente, Long> {
        @Query(value = "SELECT COUNT(r) FROM Reservas r WHERE r.id_cliente = :clienteId " +
                "AND EXTRACT(MONTH FROM r.fecha) = EXTRACT(MONTH FROM CURRENT_DATE) " +
                "AND EXTRACT(YEAR FROM r.fecha) = EXTRACT(YEAR FROM CURRENT_DATE)", nativeQuery = true)
        int contarReservasDelMes(@Param("clienteId") Long clienteId);

        Optional<Cliente> findByRut(String rut);
    }

