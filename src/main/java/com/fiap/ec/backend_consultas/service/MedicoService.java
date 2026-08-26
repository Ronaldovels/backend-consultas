package com.fiap.ec.backend_consultas.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fiap.ec.backend_consultas.exception.DadosInvalidosException;
import com.fiap.ec.backend_consultas.exception.RecursoDuplicadoException;
import com.fiap.ec.backend_consultas.exception.RecursoNaoEncontradoException;
import com.fiap.ec.backend_consultas.model.Medico;
import com.fiap.ec.backend_consultas.repository.MedicoRepository;

@Service
public class MedicoService {

    private final MedicoRepository repository;

    public MedicoService(MedicoRepository repository) {
        this.repository = repository;
    }

    public List<Medico> listar() {
        return repository.findAll();
    }

    public Medico buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico não encontrado"));
    }

    public Medico salvar(Medico medico) {
        normalizar(medico);
        validarObrigatorios(medico);
        validarCrmUnico(medico.getCrm(), null);
        if (medico.getAtivo() == null) {
            medico.setAtivo(true);
        }
        return repository.save(medico);
    }

    public Medico atualizar(Long id, Medico medicoAtualizado) {
        Medico medicoExistente = buscarPorId(id);
        normalizar(medicoAtualizado);
        validarObrigatorios(medicoAtualizado);
        validarCrmUnico(medicoAtualizado.getCrm(), id);
        medicoExistente.setNome(medicoAtualizado.getNome());
        medicoExistente.setCrm(medicoAtualizado.getCrm());
        medicoExistente.setEspecialidade(medicoAtualizado.getEspecialidade());
        medicoExistente.setAtivo(medicoAtualizado.getAtivo());
        medicoExistente.setValorConsulta(medicoAtualizado.getValorConsulta());
        return repository.save(medicoExistente);
    }

    public void deletar(Long id) {
        Medico medico = buscarPorId(id);
        repository.delete(medico);
    }

    public Optional<Medico> buscarPorCrm(String crm) {
        if (crm == null || crm.isBlank()) {
            return Optional.empty();
        }
        return repository.findByCrm(crm.trim());
    }

    public List<Medico> listarPorEspecialidade(Long especialidadeId) {
        return repository.findByEspecialidadeId(especialidadeId);
    }

    private void normalizar(Medico medico) {
        if (medico.getNome() != null) {
            medico.setNome(medico.getNome().trim());
        }
        if (medico.getCrm() != null) {
            medico.setCrm(medico.getCrm().trim());
        }
    }

    private void validarObrigatorios(Medico medico) {
        if (medico.getNome() == null || medico.getNome().isBlank()) {
            throw new DadosInvalidosException("Nome do médico é obrigatório.");
        }
        if (medico.getCrm() == null || medico.getCrm().isBlank()) {
            throw new DadosInvalidosException("CRM é obrigatório.");
        }
        if (medico.getEspecialidade() == null || medico.getEspecialidade().getId() == null) {
            throw new DadosInvalidosException("Especialidade é obrigatória.");
        }
    }

    private void validarCrmUnico(String crm, Long idAtual) {
        boolean existe = idAtual == null
                ? repository.existsByCrm(crm)
                : repository.existsByCrmAndIdNot(crm, idAtual);
        if (existe) {
            throw new RecursoDuplicadoException("CRM já cadastrado.");
        }
    }
}
