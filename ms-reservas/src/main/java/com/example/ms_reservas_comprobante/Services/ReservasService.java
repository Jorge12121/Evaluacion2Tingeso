package com.example.ms_reservas_comprobante.Services;

import com.example.ms_clientes.Entities.Cliente;
import com.example.ms_reservas_comprobante.Entities.Reservas;
import com.example.ms_reservas_comprobante.Repositories.ReservasRepository;
import com.example.ms_tarifas_duracion.Entities.Tarifas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservasService {

    @Autowired
    private ReservasRepository reservasRepository;

    @Autowired
    private RestTemplate restTemplate;

    // Método genérico para consumir otros servicios REST
    private <T> T getFromService(String url, Class<T> responseType, String errorMsg) {
        try {
            return restTemplate.getForObject(url, responseType);
        } catch (Exception e) {
            throw new RuntimeException(errorMsg + ": " + e.getMessage(), e);
        }
    }

    private Cliente obtenerClientePorRut(String rut) {
        String url = "http://ms-clientes/cliente/rut/" + rut;
        return getFromService(url, Cliente.class, "Error al obtener cliente por RUT");
    }



    public int obtenerVisitasDelMes(Long idCliente) {
        int visitas = reservasRepository.contarReservasDelMes(idCliente);
        return visitas;
    }

    public Reservas generarReserva(String rut, LocalDate fecha, LocalTime horaInicio,
                                   int cantidadPersonas, int numeroVueltas, boolean hayOtroCumpleanero) {
        LocalDate hoy = LocalDate.now();

        if (fecha.isBefore(hoy)) {
            throw new IllegalArgumentException("No se pueden generar reservas en fechas anteriores a hoy.");
        }
        if (fecha.isAfter(hoy.plusDays(30))) {
            throw new IllegalArgumentException("Solo se permiten reservas hasta 30 días desde hoy.");
        }

        LocalTime apertura;
        LocalTime cierre = LocalTime.of(22, 0);

        switch (fecha.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> apertura = LocalTime.of(10, 0);
            default -> apertura = LocalTime.of(14, 0);
        }

        int duracion = obtenerDuracion(numeroVueltas);

        if (horaInicio.isBefore(apertura) || horaInicio.isAfter(cierre.minusMinutes(duracion))) {
            throw new IllegalArgumentException("La hora de inicio debe estar dentro del horario permitido.");
        }

        Cliente clienteReserva = obtenerClientePorRut(rut);
        if (clienteReserva == null) {
            throw new IllegalArgumentException("Cliente no encontrado.");
        }
        Long idCliente = clienteReserva.getId();

        Tarifas tarifa = obtenerTarifaActiva();
        if (tarifa == null) {
            throw new IllegalArgumentException("No hay tarifa activa en este momento.");
        }
        int idTarifa = tarifa.getId();

        double tarifaBase = obtenerTarifaBase(idTarifa, numeroVueltas);
        double precioBase = cantidadPersonas * tarifaBase;

        double descuentoPersonas = obtenerDescuentoPersona(precioBase, cantidadPersonas);
        double descuentoFrecuencia = calcularDescuentoFrecuencia(idCliente, tarifaBase);
        double descuentoCumpleanos =0; // aplicarDescuentoCumpleanos(clienteReserva, hayOtroCumpleanero, tarifaBase, cantidadPersonas, fecha);

        double precioTotalSinIVA = precioBase - descuentoPersonas - descuentoFrecuencia - descuentoCumpleanos;
        double IVA = precioTotalSinIVA * 0.19;
        double precioTotal = precioTotalSinIVA + IVA;

        LocalTime horaFin = horaInicio.plusMinutes(duracion);

        List<Reservas> reservasDelDia = reservasRepository.findByFecha(fecha);
        for (Reservas reservaExistente : reservasDelDia) {
            LocalTime inicioExistente = reservaExistente.getHoraInicio();
            LocalTime finExistente = reservaExistente.getHoraFin();

            boolean seCruzan = horaInicio.isBefore(finExistente) && inicioExistente.isBefore(horaFin);
            if (seCruzan) {
                throw new IllegalArgumentException("Ya existe una reserva en ese horario.");
            }
        }

        Reservas nuevaReserva = new Reservas();
        nuevaReserva.setIdCliente(idCliente);
        nuevaReserva.setFecha(fecha);
        nuevaReserva.setHoraInicio(horaInicio);
        nuevaReserva.setHoraFin(horaFin);
        nuevaReserva.setCantidad_personas(cantidadPersonas);
        nuevaReserva.setNumero_vueltas(numeroVueltas);
        nuevaReserva.setDuracion(duracion);
        nuevaReserva.setPrecio_base((int) Math.round(precioBase));
        nuevaReserva.setDescuento_persona((int) Math.round(descuentoPersonas));
        nuevaReserva.setDescuento_frecuencia((int) Math.round(descuentoFrecuencia));
        nuevaReserva.setDescuento_cumpleaños((int) Math.round(descuentoCumpleanos));
        nuevaReserva.setPrecio_total_sinIVA((int) Math.round(precioTotalSinIVA));
        nuevaReserva.setIVA((int) Math.round(IVA));
        nuevaReserva.setPrecio_total((int) Math.round(precioTotal));
        nuevaReserva.setIdTarifa(idTarifa);
        nuevaReserva.setEstado("Pendiente");

        return reservasRepository.save(nuevaReserva);
    }

    public double calcularDescuentoFrecuencia(Long idCliente, double precioBase) {
        String url = "http://ms-descuentos-frecuentes/descuentos-frecuencia?idCliente=" + idCliente + "&precioBase=" + precioBase;
        return getFromService(url, Double.class, "Error al obtener descuento por frecuencia");
    }

//    public double aplicarDescuentoCumpleanos(Cliente clienteReserva, boolean hayOtroCumpleanero,
//                                             double precioBase, int tamanoGrupo, LocalDate fecha) {
//        boolean esCumpleanero = esCumpleanero(clienteReserva.getId(), fecha);
//        double descuentoTotal = 0;
//
//        if (esCumpleanero && tamanoGrupo > 2) {
//            descuentoTotal += precioBase * 0.5;
//            if (hayOtroCumpleanero && tamanoGrupo >= 6) {
//                descuentoTotal += precioBase * 0.5;
//            }
//        }
//
//        return descuentoTotal;
//    }

    public List<Reservas> obtenerTodas() {
        return reservasRepository.findAll();
    }

    public List<Reservas> obtenerPorClienteId(Long clienteId) {
        return reservasRepository.findByIdCliente(clienteId);
    }

    public Reservas obtenerPorId(Integer id) {
        return reservasRepository.findById(id).orElse(null);
    }

    public void eliminarReserva(Integer id) {
        reservasRepository.deleteById(id);
    }

    public Reservas actualizarEstado(Integer id, String nuevoEstado) {
        Reservas reserva = reservasRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        reserva.setEstado(nuevoEstado);
        return reservasRepository.save(reserva);
    }

    public int obtenerDuracion(int numeroVueltas) {
        String url = "http://ms-tarifas-duracion/tarifas/duracion?numerodevueltas=" + numeroVueltas;
        return getFromService(url, Integer.class, "Error al obtener duración");
    }

    public Tarifas obtenerTarifaActiva() {
        String url = "http://ms-tarifas-duracion/tarifas/activa";
        return getFromService(url, Tarifas.class, "Error al obtener tarifa activa");
    }

    public double obtenerTarifaBase(int idTarifa, int cantidadVueltas) {
        String url = "http://ms-tarifas-duracion/tarifas/base?idTarifa=" + idTarifa + "&numeroVueltas=" + cantidadVueltas;
        return getFromService(url, Double.class, "Error al obtener tarifa base");
    }

    public double obtenerDescuentoPersona(double precioBase, int numeroPersonas) {
        String url = "http://ms-descuentos-personas/descuentos-persona?precioBase=" + precioBase + "&numeroPersonas=" + numeroPersonas;
        return getFromService(url, Double.class, "Error al obtener descuento por persona");
    }

    public List<Reservas> getAllReservas() {
        return reservasRepository.findAll();
    }
}
