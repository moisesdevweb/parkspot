package com.parking.parkspot.repository;

import com.parking.parkspot.model.entity.ImagenReporte;
import com.parking.parkspot.model.entity.ReporteIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagenReporteRepository extends JpaRepository<ImagenReporte, Long> {
    
    // Encontrar imágenes por reporte
    List<ImagenReporte> findByReporte(ReporteIncidencia reporte);
    
    // Encontrar por nombre de archivo
    ImagenReporte findByNombreArchivo(String nombreArchivo);
    
    // Eliminar por reporte
    void deleteByReporte(ReporteIncidencia reporte);
}
