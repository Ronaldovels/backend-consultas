package com.fiap.ec.backend_consultas.service;
import com.fiap.ec.backend_consultas.model.Especialidade;
import com.fiap.ec.backend_consultas.repository.EspecialidadeRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service

public class EspecialidadeService {

    private final EspecialidadeRepository repository;

    public EspecialidadeService(EspecialidadeRepository repository) {
        this.repository = repository;
    }

    public Especialidade salvar(Especialidade especialidade) {
        return repository.save(especialidade);
    }
    
    public List<Especialidade> listar() {
        return repository.findAll();
    }
}