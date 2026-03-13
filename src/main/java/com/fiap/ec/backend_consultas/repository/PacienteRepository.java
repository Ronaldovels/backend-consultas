package com.fiap.ec.backend_consultas.repository;

import com.fiap.ec.backend_consultas.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
}