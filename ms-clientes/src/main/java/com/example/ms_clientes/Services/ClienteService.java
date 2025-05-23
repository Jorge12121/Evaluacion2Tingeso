package com.example.ms_clientes.Services;


import com.example.ms_clientes.Entities.Cliente;
import com.example.ms_clientes.Repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    ClienteRepository clienteRepository;


    public Cliente crearCliente(Cliente cliente) {
        boolean existe = verificarClienteExistente(cliente.getRut());
        if (!existe) {
            return clienteRepository.save(cliente);
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ya existe un cliente registrado con ese RUT."
            );

        }
    }

    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    public boolean esCumpleanosEnFecha(Cliente cliente, LocalDate fecha) {
        if (cliente == null || cliente.getFechaNacimiento() == null || fecha == null) {
            return false;
        }

        return cliente.getFechaNacimiento().getDayOfMonth() == fecha.getDayOfMonth() &&
                cliente.getFechaNacimiento().getMonth() == fecha.getMonth();
    }


    public Cliente findByRut(String rut) {
        if (rut == "") {
            throw new IllegalArgumentException("El ID del cliente no puede ser nulo");
        }

        return clienteRepository.findByRut(rut)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con el ID: " + rut));
    }

    public boolean verificarClienteExistente(String rut) {
        Optional<Cliente> cliente = clienteRepository.findByRut(rut);
        return cliente.isPresent();
    }

    public Cliente findById(Long idCliente) {
        if (idCliente == 0) {
            throw new IllegalArgumentException("El ID del cliente no puede ser nulo");
        }

        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con el ID: " + idCliente));
    }

    public void eliminarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));

        clienteRepository.delete(cliente);
    }



}
