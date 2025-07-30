package com.parking.parkspot.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {
    @GetMapping("/all")
    public String allAccess() {
        return "Contenido público.";
    }

    @GetMapping("/cliente")
    @PreAuthorize("hasRole('CLIENTE') or hasRole('VIGILANTE') or hasRole('ADMIN')")
    public String userAccess() {
        return "Contenido para Cliente, Vigilante o Admin.";
    }

    @GetMapping("/vigilante")
    @PreAuthorize("hasRole('VIGILANTE') or hasRole('ADMIN')")
    public String vigilanteAccess() {
        return "Contenido para Vigilante o Admin.";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Contenido para Admin.";
    }
}
