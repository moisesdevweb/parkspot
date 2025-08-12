package com.parking.parkspot.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.parking.parkspot.model.enums.EstadoEspacio;
import com.parking.parkspot.model.enums.EstadoReserva;
import com.parking.parkspot.repository.EspacioEstacionamientoRepository;
import com.parking.parkspot.repository.RegistroEstacionamientoRepository;
import com.parking.parkspot.repository.ReservaRepository;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private EspacioEstacionamientoRepository espacioRepository;

    @Autowired
    private RegistroEstacionamientoRepository registroRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    // Estadísticas generales para ADMIN
    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        // Estadísticas de espacios
        long espaciosDisponibles = espacioRepository.countByEstado(EstadoEspacio.DISPONIBLE);
        long espaciosOcupados = espacioRepository.countByEstado(EstadoEspacio.OCUPADO);
        long espaciosReservados = espacioRepository.countByEstado(EstadoEspacio.RESERVADO);
        long espaciosMantenimiento = espacioRepository.countByEstado(EstadoEspacio.MANTENIMIENTO);
        long totalEspacios = espacioRepository.count();

        Map<String, Object> espacios = new HashMap<>();
        espacios.put("disponibles", espaciosDisponibles);
        espacios.put("ocupados", espaciosOcupados);
        espacios.put("reservados", espaciosReservados);
        espacios.put("mantenimiento", espaciosMantenimiento);
        espacios.put("total", totalEspacios);
        espacios.put("porcentajeOcupacion", totalEspacios > 0 ? (espaciosOcupados * 100.0 / totalEspacios) : 0);

        estadisticas.put("espacios", espacios);

        // Estadísticas de registros
        long registrosActivos = registroRepository.countRegistrosActivos();
        long totalRegistros = registroRepository.count();

        Map<String, Object> registros = new HashMap<>();
        registros.put("activos", registrosActivos);
        registros.put("total", totalRegistros);

        estadisticas.put("registros", registros);

        // Estadísticas de reservas
        long reservasPendientes = reservaRepository.countByEstado(EstadoReserva.PENDIENTE);
        long reservasConfirmadas = reservaRepository.countByEstado(EstadoReserva.CONFIRMADA);
        long reservasCanceladas = reservaRepository.countByEstado(EstadoReserva.CANCELADA);
        long reservasUtilizadas = reservaRepository.countByEstado(EstadoReserva.UTILIZADA);

        Map<String, Object> reservas = new HashMap<>();
        reservas.put("pendientes", reservasPendientes);
        reservas.put("confirmadas", reservasConfirmadas);
        reservas.put("canceladas", reservasCanceladas);
        reservas.put("utilizadas", reservasUtilizadas);

        estadisticas.put("reservas", reservas);

        return ResponseEntity.ok(estadisticas);
    }

    // Estadísticas simples para VIGILANTE
    @GetMapping("/estadisticas-vigilante")
    @PreAuthorize("hasRole('ROLE_VIGILANTE')")
    public ResponseEntity<?> obtenerEstadisticasVigilante() {
        Map<String, Object> estadisticas = new HashMap<>();

        // Solo información básica para vigilantes
        long espaciosDisponibles = espacioRepository.countByEstado(EstadoEspacio.DISPONIBLE);
        long espaciosOcupados = espacioRepository.countByEstado(EstadoEspacio.OCUPADO);
        long registrosActivos = registroRepository.countRegistrosActivos();
        long reservasPendientes = reservaRepository.countByEstado(EstadoReserva.PENDIENTE);

        estadisticas.put("espaciosDisponibles", espaciosDisponibles);
        estadisticas.put("espaciosOcupados", espaciosOcupados);
        estadisticas.put("registrosActivos", registrosActivos);
        estadisticas.put("reservasPendientes", reservasPendientes);

        return ResponseEntity.ok(estadisticas);
    }
}
