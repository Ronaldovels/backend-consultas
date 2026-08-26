package com.fiap.ec.backend_consultas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fiap.ec.backend_consultas.model.Medico;

public interface MedicoRepository extends JpaRepository<Medico, Long> {
    Optional<Medico> findByCrm(String crm);
    List<Medico> findByEspecialidadeId(Long especialidadeId);
    boolean existsByCrm(String crm);
    boolean existsByCrmAndIdNot(String crm, Long id);
}
