package com.fiap.ec.backend_consultas.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fiap.ec.backend_consultas.exception.DadosInvalidosException;
import com.fiap.ec.backend_consultas.exception.RecursoDuplicadoException;
import com.fiap.ec.backend_consultas.exception.RecursoNaoEncontradoException;
import com.fiap.ec.backend_consultas.model.Especialidade;
import com.fiap.ec.backend_consultas.repository.EspecialidadeRepository;

@Service
public class EspecialidadeService {

    private final EspecialidadeRepository repository;

    public EspecialidadeService(EspecialidadeRepository repository) {
        this.repository = repository;
    }

    public Especialidade salvar(Especialidade especialidade) {
        normalizar(especialidade);
        validarObrigatorios(especialidade);
        validarNomeUnico(especialidade.getNome(), null);
        return repository.save(especialidade);
    }

    public List<Especialidade> listar() {
        return repository.findAll();
    }

    public Especialidade buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Especialidade não encontrada"));
    }

    public Especialidade atualizar(Long id, Especialidade especialidadeAtualizada) {
        Especialidade especialidadeExistente = buscarPorId(id);
        normalizar(especialidadeAtualizada);
        validarObrigatorios(especialidadeAtualizada);
        validarNomeUnico(especialidadeAtualizada.getNome(), id);
        especialidadeExistente.setNome(especialidadeAtualizada.getNome());
        especialidadeExistente.setDescricao(especialidadeAtualizada.getDescricao());
        return repository.save(especialidadeExistente);
    }

    public void deletar(Long id) {
        Especialidade especialidade = buscarPorId(id);
        repository.delete(especialidade);
    }

    private void normalizar(Especialidade especialidade) {
        if (especialidade.getNome() != null) {
            especialidade.setNome(especialidade.getNome().trim());
        }
    }

    private void validarObrigatorios(Especialidade especialidade) {
        if (especialidade.getNome() == null || especialidade.getNome().isBlank()) {
            throw new DadosInvalidosException("Nome da especialidade é obrigatório.");
        }
    }

    private void validarNomeUnico(String nome, Long idAtual) {
        boolean existe = idAtual == null
                ? repository.existsByNomeIgnoreCase(nome)
                : repository.existsByNomeIgnoreCaseAndIdNot(nome, idAtual);
        if (existe) {
            throw new RecursoDuplicadoException("Especialidade já cadastrada.");
        }
    }
}
