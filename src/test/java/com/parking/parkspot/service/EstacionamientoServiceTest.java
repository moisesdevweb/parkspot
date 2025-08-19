package com.parking.parkspot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parking.parkspot.model.entity.User;
import com.parking.parkspot.model.entity.Persona;
import com.parking.parkspot.model.entity.Vehiculo;
import com.parking.parkspot.model.entity.EspacioEstacionamiento;
import com.parking.parkspot.model.entity.RegistroEstacionamiento;
import com.parking.parkspot.model.enums.EstadoEspacio;
import com.parking.parkspot.payload.request.RegistrarEntradaRequest;
import com.parking.parkspot.repository.*;
import com.parking.parkspot.service.exception.InvalidOperationException;
import com.parking.parkspot.service.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class EstacionamientoServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private VehiculoRepository vehiculoRepository;
    
    @Mock
    private EspacioEstacionamientoRepository espacioRepository;
    
    @Mock
    private RegistroEstacionamientoRepository registroRepository;
    
    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private EstacionamientoService estacionamientoService;

    private User cliente;
    private User vigilante;
    private Vehiculo vehiculo;
    private EspacioEstacionamiento espacio;
    private RegistrarEntradaRequest entradaRequest;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        Persona personaCliente = new Persona();
        personaCliente.setId(1L);
        personaCliente.setNombreCompleto("Juan Pérez");
        personaCliente.setEstado(1);

        cliente = new User();
        cliente.setId(1L);
        cliente.setUsername("cliente1");
        cliente.setPersona(personaCliente);

        Persona personaVigilante = new Persona();
        personaVigilante.setId(2L);
        personaVigilante.setNombreCompleto("Carlos Vigilante");
        personaVigilante.setEstado(1);

        vigilante = new User();
        vigilante.setId(2L);
        vigilante.setUsername("vigilante1");
        vigilante.setPersona(personaVigilante);

        vehiculo = new Vehiculo();
        vehiculo.setId(1L);
        vehiculo.setPlaca("ABC123");
        vehiculo.setCliente(cliente);

        espacio = new EspacioEstacionamiento();
        espacio.setId(1L);
        espacio.setNumero("101"); // String, no int
        espacio.setEstado(EstadoEspacio.DISPONIBLE);

        entradaRequest = new RegistrarEntradaRequest();
        entradaRequest.setClienteId(1L);
        entradaRequest.setVehiculoId(1L);
        entradaRequest.setEspacioId(1L);
        entradaRequest.setObservaciones("Test de entrada");
    }

    @Test
    void testRegistrarEntradaExitosa() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(registroRepository.findRegistroActivoPorVehiculo(vehiculo)).thenReturn(Optional.empty());
        when(espacioRepository.save(any(EspacioEstacionamiento.class))).thenReturn(espacio);
        when(registroRepository.save(any(RegistroEstacionamiento.class))).thenReturn(new RegistroEstacionamiento());

        // Act
        String resultado = estacionamientoService.registrarEntrada(entradaRequest, vigilante);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.contains("Entrada registrada exitosamente"));
        assertTrue(resultado.contains("Juan Pérez"));
        assertTrue(resultado.contains("ABC123"));
        assertTrue(resultado.contains("101"));

        // Verificar que se llamaron los métodos necesarios
        verify(userRepository).findById(1L);
        verify(vehiculoRepository).findById(1L);
        verify(espacioRepository).findById(1L);
        verify(registroRepository).findRegistroActivoPorVehiculo(vehiculo);
        verify(espacioRepository).save(espacio);
        verify(registroRepository).save(any(RegistroEstacionamiento.class));
    }

    @Test
    void testRegistrarEntradaClienteNoEncontrado() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> estacionamientoService.registrarEntrada(entradaRequest, vigilante)
        );

        assertEquals("Cliente no encontrado", exception.getMessage());
        verify(userRepository).findById(1L);
        verifyNoInteractions(vehiculoRepository, espacioRepository, registroRepository);
    }

    @Test
    void testRegistrarEntradaVehiculoYaEstacionado() {
        // Arrange
        RegistroEstacionamiento registroActivo = new RegistroEstacionamiento();
        registroActivo.setEspacio(espacio);

        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));
        when(registroRepository.findRegistroActivoPorVehiculo(vehiculo))
            .thenReturn(Optional.of(registroActivo));

        // Act & Assert
        InvalidOperationException exception = assertThrows(
            InvalidOperationException.class,
            () -> estacionamientoService.registrarEntrada(entradaRequest, vigilante)
        );

        assertTrue(exception.getMessage().contains("ya se encuentra estacionado"));
        verify(userRepository).findById(1L);
        verify(vehiculoRepository).findById(1L);
        verify(registroRepository).findRegistroActivoPorVehiculo(vehiculo);
        verifyNoInteractions(espacioRepository);
    }

    @Test
    void testRegistrarEntradaEspacioOcupado() {
        // Arrange
        espacio.setEstado(EstadoEspacio.OCUPADO);

        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));
        when(espacioRepository.findById(1L)).thenReturn(Optional.of(espacio));
        when(registroRepository.findRegistroActivoPorVehiculo(vehiculo)).thenReturn(Optional.empty());

        // Act & Assert
        InvalidOperationException exception = assertThrows(
            InvalidOperationException.class,
            () -> estacionamientoService.registrarEntrada(entradaRequest, vigilante)
        );

        assertTrue(exception.getMessage().contains("está ocupado"));
        verify(espacioRepository).findById(1L);
    }

    // Elimina este bloque si no quieres este test
    @Test
    void testRegistrarEntradaVehiculoNoPerteneceACliente() {
        // Arrange
        User otroCliente = new User();
        otroCliente.setId(999L);
        vehiculo.setCliente(otroCliente);

        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));

        // Act & Assert
        InvalidOperationException exception = assertThrows(
            InvalidOperationException.class,
            () -> estacionamientoService.registrarEntrada(entradaRequest, vigilante)
        );

        assertEquals("El vehículo no pertenece al cliente seleccionado", exception.getMessage());
    }
}
