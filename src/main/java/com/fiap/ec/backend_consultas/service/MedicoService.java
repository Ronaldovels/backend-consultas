package com.fiap.ec.backend_consultas.service;
import com.fiap.ec.backend_consultas.model.Medico;
import com.fiap.ec.backend_consultas.repository.MedicoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
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
                .orElseThrow(() -> new RuntimeException("Médico não encontrado"));
    }
    public Medico salvar(Medico medico) {
        return repository.save(medico);
    }
    public Medico atualizar(Long id, Medico medicoAtualizado) {
        Medico medicoExistente = buscarPorId(id);
        medicoExistente.setNome(medicoAtualizado.getNome());
        medicoExistente.setCrm(medicoAtualizado.getCrm());
        medicoExistente.setEspecialidade(medicoAtualizado.getEspecialidade());
        medicoExistente.setAtivo(medicoAtualizado.getAtivo());
        return repository.save(medicoExistente);
    }
    public void deletar(Long id) {
        Medico medico = buscarPorId(id);
        repository.delete(medico);
    }
}