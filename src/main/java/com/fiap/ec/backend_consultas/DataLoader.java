package com.fiap.ec.backend_consultas;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.fiap.ec.backend_consultas.model.Consulta;
import com.fiap.ec.backend_consultas.model.Medico;
import com.fiap.ec.backend_consultas.model.Paciente;
import com.fiap.ec.backend_consultas.repository.ConsultaRepository;
import com.fiap.ec.backend_consultas.repository.MedicoRepository;
import com.fiap.ec.backend_consultas.repository.PacienteRepository;

/**
 * DataLoader: executado automaticamente ao iniciar o backend.
 *
 * O método run() só insere dados de exemplo se a tabela de consultas
 * estiver vazia (count == 0), então é seguro reiniciar o servidor
 * sem duplicar dados.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final ConsultaRepository consultaRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;

    public DataLoader(ConsultaRepository consultaRepository,
                      MedicoRepository medicoRepository,
                      PacienteRepository pacienteRepository) {
        this.consultaRepository = consultaRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Só popula se ainda não houver consultas cadastradas
        if (consultaRepository.count() > 0) {
            System.out.println("DataLoader: consultas já existem, pulando seed.");
            return;
        }

        List<Medico> medicos = medicoRepository.findAll();
        List<Paciente> pacientes = pacienteRepository.findAll();

        if (medicos.isEmpty() || pacientes.isEmpty()) {
            System.out.println("DataLoader: sem médicos ou pacientes para associar consultas.");
            return;
        }

        Medico medico1 = medicos.get(0);
        Medico medico2 = medicos.size() > 1 ? medicos.get(1) : medico1;
        Paciente paciente1 = pacientes.get(0);
        Paciente paciente2 = pacientes.size() > 1 ? pacientes.get(1) : paciente1;

        consultaRepository.saveAll(List.of(
                new Consulta(medico1, paciente1,
                        LocalDateTime.of(2026, 5, 20, 9, 0), "agendada", 250.00,
                        "Consulta de rotina"),
                new Consulta(medico2, paciente2,
                        LocalDateTime.of(2026, 5, 21, 14, 30), "confirmada", 350.00,
                        "Retorno pós-exame"),
                new Consulta(medico1, paciente2,
                        LocalDateTime.of(2026, 5, 15, 10, 0), "realizada", 200.00,
                        null),
                new Consulta(medico2, paciente1,
                        LocalDateTime.of(2026, 5, 18, 11, 0), "cancelada", 300.00,
                        "Paciente desmarcou")
        ));

        System.out.println("DataLoader: 4 consultas de exemplo criadas com sucesso!");
    }
}
