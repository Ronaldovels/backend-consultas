package com.fiap.ec.backend_consultas.repository;
import com.fiap.ec.backend_consultas.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MedicoRepository extends JpaRepository<Medico, Long> {
}