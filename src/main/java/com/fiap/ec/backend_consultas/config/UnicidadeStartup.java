package com.fiap.ec.backend_consultas.config;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.fiap.ec.backend_consultas.model.Consulta;
import com.fiap.ec.backend_consultas.model.Especialidade;
import com.fiap.ec.backend_consultas.model.Medico;
import com.fiap.ec.backend_consultas.model.Paciente;
import com.fiap.ec.backend_consultas.repository.ConsultaRepository;
import com.fiap.ec.backend_consultas.repository.EspecialidadeRepository;
import com.fiap.ec.backend_consultas.repository.MedicoRepository;
import com.fiap.ec.backend_consultas.repository.PacienteRepository;

/**
 * 1) Consolida cadastros duplicados já gravados no H2 (ex.: mesmo CRM várias vezes).
 * 2) Cria índices UNIQUE depois da limpeza - o Hibernate não pode criar a constraint
 *    na subida se ainda existirem duplicatas, senão o servidor nem inicia.
 */
@Component
@Order(1)
public class UnicidadeStartup implements CommandLineRunner {

    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final ConsultaRepository consultaRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PlatformTransactionManager transactionManager;

    public UnicidadeStartup(MedicoRepository medicoRepository,
                            PacienteRepository pacienteRepository,
                            EspecialidadeRepository especialidadeRepository,
                            ConsultaRepository consultaRepository,
                            JdbcTemplate jdbcTemplate,
                            PlatformTransactionManager transactionManager) {
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.especialidadeRepository = especialidadeRepository;
        this.consultaRepository = consultaRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionManager = transactionManager;
    }

    @Override
    public void run(String... args) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            consolidarMedicosPorCrm();
            consolidarPacientesPorCpf();
            consolidarPacientesPorEmail();
            consolidarEspecialidadesPorNome();
        });
        criarIndicesUnicos();
    }

    private void consolidarMedicosPorCrm() {
        Map<String, List<Medico>> porCrm = medicoRepository.findAll().stream()
                .filter(m -> m.getCrm() != null && !m.getCrm().isBlank())
                .collect(Collectors.groupingBy(m -> m.getCrm().trim()));

        porCrm.values().forEach(grupo -> {
            if (grupo.size() <= 1) {
                return;
            }
            grupo.sort(Comparator.comparing(Medico::getId));
            Medico mantido = grupo.get(0);
            for (int i = 1; i < grupo.size(); i++) {
                Medico duplicado = grupo.get(i);
                List<Consulta> consultas = consultaRepository.findByMedicoId(duplicado.getId());
                for (Consulta consulta : consultas) {
                    consulta.setMedico(mantido);
                    consultaRepository.save(consulta);
                }
                medicoRepository.delete(duplicado);
                System.out.println("UnicidadeStartup: médico duplicado removido (CRM "
                        + mantido.getCrm() + ", id " + duplicado.getId()
                        + "). Mantido id " + mantido.getId() + ".");
            }
        });
    }

    private void consolidarPacientesPorCpf() {
        Map<String, List<Paciente>> porCpf = pacienteRepository.findAll().stream()
                .filter(p -> p.getCpf() != null && !p.getCpf().isBlank())
                .collect(Collectors.groupingBy(p -> p.getCpf().trim()));

        porCpf.values().forEach(grupo -> fundirPacientes(grupo));
    }

    private void consolidarPacientesPorEmail() {
        Map<String, List<Paciente>> porEmail = pacienteRepository.findAll().stream()
                .filter(p -> p.getEmail() != null && !p.getEmail().isBlank())
                .collect(Collectors.groupingBy(p -> p.getEmail().trim().toLowerCase()));

        porEmail.values().forEach(grupo -> fundirPacientes(grupo));
    }

    private void fundirPacientes(List<Paciente> grupo) {
        if (grupo.size() <= 1) {
            return;
        }
        grupo.sort(Comparator.comparing(Paciente::getId));
        Paciente mantido = grupo.get(0);
        for (int i = 1; i < grupo.size(); i++) {
            Paciente duplicado = grupo.get(i);
            List<Consulta> consultas = consultaRepository.findByPacienteId(duplicado.getId());
            for (Consulta consulta : consultas) {
                consulta.setPaciente(mantido);
                consultaRepository.save(consulta);
            }
            pacienteRepository.delete(duplicado);
            System.out.println("UnicidadeStartup: paciente duplicado removido (id "
                    + duplicado.getId() + "). Mantido id " + mantido.getId() + ".");
        }
    }

    private void consolidarEspecialidadesPorNome() {
        Map<String, List<Especialidade>> porNome = especialidadeRepository.findAll().stream()
                .filter(e -> e.getNome() != null && !e.getNome().isBlank())
                .collect(Collectors.groupingBy(e -> e.getNome().trim().toLowerCase()));

        porNome.values().forEach(grupo -> {
            if (grupo.size() <= 1) {
                return;
            }
            grupo.sort(Comparator.comparing(Especialidade::getId));
            Especialidade mantida = grupo.get(0);
            for (int i = 1; i < grupo.size(); i++) {
                Especialidade duplicada = grupo.get(i);
                List<Medico> medicos = medicoRepository.findByEspecialidadeId(duplicada.getId());
                for (Medico medico : medicos) {
                    medico.setEspecialidade(mantida);
                    medicoRepository.save(medico);
                }
                especialidadeRepository.delete(duplicada);
                System.out.println("UnicidadeStartup: especialidade duplicada removida. Mantida id "
                        + mantida.getId() + ".");
            }
        });
    }

    private void criarIndicesUnicos() {
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_medico_crm ON medicos(crm)");
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_paciente_cpf ON pacientes(cpf)");
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_paciente_email ON pacientes(email)");
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_especialidade_nome ON especialidades(nome)");
        System.out.println("UnicidadeStartup: índices UNIQUE conferidos (CRM, CPF, e-mail, especialidade).");
    }
}
