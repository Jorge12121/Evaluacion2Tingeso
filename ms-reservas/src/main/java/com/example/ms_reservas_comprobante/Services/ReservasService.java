package com.example.ms_reservas_comprobante.Services;

import com.example.ms_clientes.Entities.Cliente;
import com.example.ms_reservas_comprobante.Entities.Reservas;
import com.example.ms_reservas_comprobante.Config.RestTemplateConfig;
import com.example.ms_reservas_comprobante.Repositories.ReservasRepository;
import com.example.ms_tarifas_duracion.Entities.Tarifas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservasService {

    @Autowired
    private ReservasRepository reservasRepository;

    @Autowired
    private RestTemplateConfig restTemplateConfig;

    private RestTemplate restTemplate() {
        return restTemplateConfig.restTemplate();
    }

    // Obtener cliente desde ms-clientes por RUT
    private Cliente obtenerClientePorRut(String rut) {
        String url = "http://ms-clientes/cliente/rut/" + rut;
        try {
            return restTemplate().getForObject(url, Cliente.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cliente no encontrado.");
        }
    }

    // Verifica si el cliente está de cumpleaños en una fecha específica
    private boolean esCumpleanero(Long idCliente, LocalDate fecha) {
        String url = "http://ms-clientes/cliente/esCumpleanero?id=" + idCliente + "&fecha=" + fecha;
        try {
            return restTemplate().getForObject(url, Boolean.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al verificar cumpleaños del cliente.");
        }
    }

    // Obtiene el número de visitas del cliente en el mes actual
    private int obtenerVisitasDelMes(Long idCliente) {
        String url = "http://ms-clientes/cliente/visitas-del-mes/" + idCliente;
        try {
            return restTemplate().getForObject(url, Integer.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener visitas del cliente.");
        }
    }


    public Reservas generarReserva(String rut, LocalDate fecha, LocalTime horaInicio, int cantidadPersonas, int numeroVueltas, boolean hayOtroCumpleanero) {
        // 1. Validaciones de fecha
        LocalDate hoy = LocalDate.now();
        if (fecha.isBefore(hoy)) {
            throw new IllegalArgumentException("No se pueden generar reservas en fechas anteriores a hoy.");
        }

        if (fecha.isAfter(hoy.plusDays(30))) {
            throw new IllegalArgumentException("Solo se permiten reservas hasta 30 días desde hoy.");
        }

        // 2. Validación de horario permitido
        LocalTime apertura;
        LocalTime cierre = LocalTime.of(22, 0);

        switch (fecha.getDayOfWeek()) {
            case SATURDAY:
            case SUNDAY:
                apertura = LocalTime.of(10, 0);
                break;
            default:
                apertura = LocalTime.of(14, 0);
                break;
        }

        if (horaInicio.isBefore(apertura) || horaInicio.isAfter(cierre.minusMinutes(obtenerDuracion(numeroVueltas)))) {
            throw new IllegalArgumentException("La hora de inicio debe estar dentro del horario permitido.");
        }

        // 3. Obtener cliente
        Cliente clienteReserva = obtenerClientePorRut(rut);
        if (clienteReserva == null) {
            throw new IllegalArgumentException("Cliente no encontrado.");
        }
        Long idCliente = clienteReserva.getId();

        // 4. Obtener tarifa activa
        Tarifas tarifa = obtenerTarifaActiva();
        if (tarifa == null) {
            throw new IllegalArgumentException("No hay tarifa activa en este momento.");
        }
        int idTarifa = tarifa.getId();

        // 5. Cálculo de precios y descuentos
        double tarifaBase = obtenerTarifaBase(idTarifa,numeroVueltas);
        double precioBase = cantidadPersonas * tarifaBase;

        double descuentoPersonas =0; // AplicarDescuentoPersonas(precioBase, cantidadPersonas);
        double descuentoFrecuencia = calcularDescuentoFrecuencia(idCliente, tarifaBase);
        double descuentoCumpleanos = aplicarDescuentoCumpleanos(clienteReserva, hayOtroCumpleanero, tarifaBase, cantidadPersonas, fecha);

        double precioTotalSinIVA = precioBase - descuentoPersonas - descuentoFrecuencia - descuentoCumpleanos;
        double IVA = precioTotalSinIVA * 0.19;
        double precioTotal = precioTotalSinIVA + IVA;

        // 6. Cálculo de hora fin
        int duracion = obtenerDuracion(numeroVueltas);
        LocalTime horaFin = horaInicio.plusMinutes(duracion);


        // 7. Verificar cruces con otras reservas
        List<Reservas> reservasDelDia = reservasRepository.findByFecha(fecha);
        for (Reservas reservaExistente : reservasDelDia) {
            LocalTime inicioExistente = reservaExistente.getHoraInicio();
            LocalTime finExistente = reservaExistente.getHoraFin();

            boolean seCruzan = horaInicio.isBefore(finExistente) && inicioExistente.isBefore(horaFin);
            if (seCruzan) {
                throw new IllegalArgumentException("Ya existe una reserva en ese horario.");
            }
        }

        // 8. Crear y guardar reserva
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
        int visitas = obtenerVisitasDelMes(idCliente);

        if (visitas >= 7) return precioBase * 0.3;
        if (visitas >= 5) return precioBase * 0.2;
        if (visitas >= 2) return precioBase * 0.1;
        return 0;
    }

    public double aplicarDescuentoCumpleanos(Cliente clienteReserva, boolean hayOtroCumpleanero, double precioBase, int tamanoGrupo, LocalDate fecha) {
        boolean esCumpleanero = esCumpleanero(clienteReserva.getId(), fecha);
        double descuentoTotal = 0;

        if (esCumpleanero && (tamanoGrupo > 2)) {
            descuentoTotal += precioBase * 0.5;

            if (hayOtroCumpleanero && tamanoGrupo >= 6) {
                descuentoTotal += precioBase * 0.5;
            }
        }

        return descuentoTotal;
    }

    public List<Reservas> obtenerTodas() {
        return reservasRepository.findAll();
    }

    public List<Reservas> obtenerPorClienteId(Long clienteId) {
        return reservasRepository.findByClienteId(clienteId);
    }

    public Reservas obtenerPorId(Integer id) {
        return reservasRepository.findById(id).orElse(null);
    }

    public void eliminarReserva(Integer id) {
        reservasRepository.deleteById(id);
    }

    public Reservas actualizarEstado(Integer id, String nuevoEstado) {
        Reservas reserva = reservasRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        reserva.setEstado(nuevoEstado);
        return reservasRepository.save(reserva);
    }

    public int obtenerDuracion(int numerovueltas){
        String url = "http://ms-tarifas-duracion/duracion/" + numerovueltas;
        try {
            return restTemplate().getForObject(url, int.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cliente no encontrado.");
        }

    }

    public Tarifas obtenerTarifaActiva(){
        String url = "http://ms-tarifas-duracion/activa/";
        try {
            return restTemplate().getForObject(url, Tarifas.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cliente no encontrado.");
        }
    }

    public double obtenerTarifaBase(int idTarifa, int cantidadVueltas){
        String url = "http://ms-tarifas-duracion/base/"+idTarifa+"/"+cantidadVueltas;
        try {
            return restTemplate().getForObject(url, double.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cliente no encontrado.");
        }
    }
}
