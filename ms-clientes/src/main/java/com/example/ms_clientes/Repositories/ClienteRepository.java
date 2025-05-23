    package com.example.ms_clientes.Repositories;

    import com.example.ms_clientes.Entities.Cliente;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import org.springframework.stereotype.Repository;

    import java.util.Optional;

    @Repository
    public interface ClienteRepository extends JpaRepository<Cliente, Long> {


        Optional<Cliente> findByRut(String rut);
    }

