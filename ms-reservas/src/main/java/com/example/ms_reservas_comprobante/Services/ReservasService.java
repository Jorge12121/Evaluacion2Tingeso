package com.example.ms_reservas_comprobante.Services;

import com.example.ms_clientes.Entities.Cliente;
import com.example.ms_reservas_comprobante.Entities.Reservas;
import com.example.ms_reservas_comprobante.Repositories.ReservasRepository;
import com.example.ms_tarifas_duracion.Entities.Tarifas;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;



import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import jakarta.mail.MessagingException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


@Service
public class ReservasService {

    @Autowired
    private ReservasRepository reservasRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JavaMailSender mailSender;

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

    private Cliente obtenerClientePorId(long id) {
        String url = "http://ms-clientes/cliente/" + id;
        return getFromService(url, Cliente.class, "Error al obtener cliente por ID");
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
        double descuentoCumpleanos = aplicarDescuentoCumpleanos(idCliente, hayOtroCumpleanero, tarifaBase, cantidadPersonas, fecha);

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

    public double aplicarDescuentoCumpleanos(long idCliente, boolean hayOtroCumpleanero,
                                             double precioBase, int tamanoGrupo, LocalDate fecha) {
        String url = "http://ms-tarifas-especiales/tarifas-especiales/cumpleanos?idCliente={idCliente}&hayOtroCumpleanero={hayOtroCumpleanero}&precioBase={precioBase}&tamanoGrupo={tamanoGrupo}&fecha={fecha}";

        Map<String, Object> params = new HashMap<>();
        params.put("idCliente", idCliente);
        params.put("hayOtroCumpleanero", hayOtroCumpleanero);
        params.put("precioBase", precioBase);
        params.put("tamanoGrupo", tamanoGrupo);
        params.put("fecha", fecha.toString());

        return restTemplate.getForObject(url, Double.class, params);

    }


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


    public List<Reservas> obtenerReservasPagadasPorFecha(LocalDate fechaInicio, LocalDate fechaFin) {
        return reservasRepository.findReservasByFechaAndEstado(fechaInicio, fechaFin);
    }





    public byte[] generarComprobanteExcel(int idReserva) throws IOException {
        Reservas reserva = reservasRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));


        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Comprobante de Pago");

        // Cabecera, eliminamos las columnas no necesarias
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "Nombre Cliente", "Tarifa Base", "Descuento Grupo", "Descuento Frecuencia",
                "Descuento Cumpleaños", "Monto Final", "IVA", "Monto Total"
        };

        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(getHeaderStyle(workbook));
        }

// Obtener valores divididos por persona
        int numPersonas = reserva.getCantidad_personas();
        double precioBasePorPersona = reserva.getPrecio_base() / numPersonas;
        double descuentoGrupoPorPersona = reserva.getDescuento_persona() / numPersonas;
        double descuentoFrecuenciaPorPersona = reserva.getDescuento_frecuencia() / numPersonas;
        double descuentoCumplePorPersona = reserva.getDescuento_cumpleaños() / numPersonas;
        double montoFinalPorPersona = reserva.getPrecio_total_sinIVA() / numPersonas;
        double ivaPorPersona = reserva.getIVA() / numPersonas;
        double totalPorPersona = reserva.getPrecio_total() / numPersonas;

        for (int i = 0; i < numPersonas; i++) {
            Row row = sheet.createRow(i + 1); // Cada persona tiene su fila
            row.createCell(0).setCellValue("Participante " + (i + 1)); // Opcional
            row.createCell(1).setCellValue(precioBasePorPersona);
            row.createCell(2).setCellValue(descuentoGrupoPorPersona);
            row.createCell(3).setCellValue(descuentoFrecuenciaPorPersona);
            row.createCell(4).setCellValue(descuentoCumplePorPersona);
            row.createCell(5).setCellValue(montoFinalPorPersona);
            row.createCell(6).setCellValue(ivaPorPersona);
            row.createCell(7).setCellValue(totalPorPersona);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    private CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    public byte[] generarComprobantePDFDesdeExcel(int idReserva) throws IOException {
        byte[] excelData = generarComprobanteExcel(idReserva);
        Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData));
        Sheet sheet = workbook.getSheetAt(0);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        Reservas reserva = reservasRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        Cliente clienteOpt = obtenerClientePorId(reserva.getIdCliente());
        String nombreCliente = clienteOpt.getNombre();
        document.add(new Paragraph("Comprobante de Pago - Kartódromo")
                .setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Código de la reserva: " + idReserva)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));
        document.add(new Paragraph("Generador de reserva: " + nombreCliente)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));
        document.add(new Paragraph("Fecha y hora : " + reserva.getFecha() + "  " + reserva.getHoraInicio() )
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));
        document.add(new Paragraph("Vueltas/Tiempo : " + reserva.getNumero_vueltas() )
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));
        document.add(new Paragraph("Cantidad de personas : " + reserva.getCantidad_personas() )
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

        Row headerRow = sheet.getRow(0);
        if (headerRow == null) throw new IllegalStateException("Excel sin cabeceras");

        int numCols = headerRow.getLastCellNum();
        Table table = new Table(numCols).useAllAvailableWidth();

        for (int i = 0; i < numCols; i++) {
            String header = headerRow.getCell(i).getStringCellValue();
            table.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(header).setBold()));
        }

        // Agregar los datos por persona
        for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
            Row dataRow = sheet.getRow(rowIdx);
            if (dataRow != null) {
                for (int colIdx = 0; colIdx < numCols; colIdx++) {
                    org.apache.poi.ss.usermodel.Cell cell = dataRow.getCell(colIdx);
                    String value = "";
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING -> value = cell.getStringCellValue();
                            case NUMERIC -> value = String.format("%.2f", cell.getNumericCellValue());
                            case BOOLEAN -> value = Boolean.toString(cell.getBooleanCellValue());
                            default -> value = "";
                        }
                    }
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(value)));
                }
            }
        }

        document.add(table);
        document.add(new Paragraph("\nGracias por su preferencia.")
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(20));

        document.close();
        return baos.toByteArray();
    }


    public void enviarComprobanteCorreoCliente(int idReserva, byte[] archivoPDF) throws MessagingException {
        Reservas reserva = reservasRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        Cliente cliente = obtenerClientePorId(reserva.getIdCliente());
        String correo = cliente.getCorreo();

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(correo);
        helper.setSubject("Comprobante de Pago - Karting");
        helper.setText("Adjunto se encuentra su comprobante de pago.");

        InputStreamSource attachment = new ByteArrayResource(archivoPDF);
        helper.addAttachment("ComprobantePago.pdf", attachment);

        mailSender.send(message);
    }

}
