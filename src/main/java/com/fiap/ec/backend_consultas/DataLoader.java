package com.fiap.ec.backend_consultas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fiap.ec.backend_consultas.model.Consulta;
import com.fiap.ec.backend_consultas.model.Especialidade;
import com.fiap.ec.backend_consultas.model.Medico;
import com.fiap.ec.backend_consultas.model.Paciente;
import com.fiap.ec.backend_consultas.repository.ConsultaRepository;
import com.fiap.ec.backend_consultas.repository.EspecialidadeRepository;
import com.fiap.ec.backend_consultas.repository.MedicoRepository;
import com.fiap.ec.backend_consultas.repository.PacienteRepository;

/**
 * DataLoader: executado automaticamente ao iniciar o backend.
 *
 * Semeia todos os dados caso as tabelas estejam vazias, na ordem de
 * dependência: Especialidades -> Médicos -> Pacientes -> Consultas.
 *
 * Cada seção tem a própria guarda count() == 0, então é seguro reiniciar
 * o servidor sem duplicar dados. Isso garante que o app funcione tanto
 * localmente (H2 em arquivo) quanto na nuvem do Render, onde o sistema de
 * arquivos é efêmero e o H2 começa do zero a cada reinicialização.
 */
@Component
@Order(10)
public class DataLoader implements CommandLineRunner {

    private final EspecialidadeRepository especialidadeRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;

    public DataLoader(EspecialidadeRepository especialidadeRepository,
                      MedicoRepository medicoRepository,
                      PacienteRepository pacienteRepository,
                      ConsultaRepository consultaRepository) {
        this.especialidadeRepository = especialidadeRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.consultaRepository = consultaRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        // 1. Especialidades
        if (especialidadeRepository.count() == 0) {
            especialidadeRepository.saveAll(List.of(
                    new Especialidade("Cardiologia", "Especialidade do coração"),
                    new Especialidade("Dermatologia", "Tratamento de doenças da pele"),
                    new Especialidade("Ortopedia", "Sistema músculo-esquelético"),
                    new Especialidade("Pediatria", "Saúde de crianças e adolescentes"),
                    new Especialidade("Neurologia", "Sistema nervoso central e periférico"),
                    new Especialidade("Ginecologia", "Saúde da mulher"),
                    new Especialidade("Oftalmologia", "Saúde dos olhos")
            ));
            System.out.println("DataLoader: 7 especialidades criadas.");
        }

        // 2. Médicos - associados pelo nome da especialidade, não pela posição na lista,
        //    para não trocar a especialidade caso o banco já tenha outra ordenação.
        if (medicoRepository.count() == 0) {
            Map<String, Especialidade> esp = especialidadeRepository.findAll().stream()
                    .collect(Collectors.toMap(e -> e.getNome().trim().toLowerCase(),
                            Function.identity(), (a, b) -> a));

            medicoRepository.saveAll(List.of(
                    medico("Dr. Roberto Silva", "789456", esp.get("cardiologia"), 750.00),
                    medico("Dra. Ana Ferreira", "123789", esp.get("dermatologia"), 480.00),
                    medico("Dr. Carlos Mendes", "456123", esp.get("ortopedia"), 550.00),
                    medico("Dra. Patricia Lima", "321654", esp.get("pediatria"), 420.00),
                    medico("Dr. Fernando Souza", "654321", esp.get("neurologia"), 680.00)
            ));
            System.out.println("DataLoader: 5 médicos criados.");
        }

        // 3. Pacientes
        if (pacienteRepository.count() == 0) {
            pacienteRepository.saveAll(List.of(
                    paciente("Maria Silva", "12345678901", "maria@email.com", "11999991111", "1990-03-15"),
                    paciente("João Santos", "98765432100", "joao@email.com", "11988882222", "1985-07-22"),
                    paciente("Ana Costa", "11122233344", "ana@email.com", null, "1995-11-08"),
                    paciente("Pedro Oliveira", "55544433322", "pedro@email.com", "11977773333", "1978-01-30"),
                    paciente("Lucia Fernandes", "66677788899", "lucia@email.com", "11966664444", "2001-05-17")
            ));
            System.out.println("DataLoader: 5 pacientes criados.");
        }

        // 4. Consultas
        if (consultaRepository.count() == 0) {
            List<Medico> ms = medicoRepository.findAll();
            List<Paciente> ps = pacienteRepository.findAll();

            if (ms.isEmpty() || ps.isEmpty()) {
                System.out.println("DataLoader: sem médicos ou pacientes para associar consultas.");
            } else {
                consultaRepository.saveAll(List.of(
                        new Consulta(ms.get(0), ps.get(0), LocalDateTime.of(2026, 10, 5, 9, 0),
                                "agendada", 750.0, "Consulta de rotina"),
                        new Consulta(ms.get(1), ps.get(1), LocalDateTime.of(2026, 10, 6, 14, 30),
                                "confirmada", 480.0, "Retorno pós-exame"),
                        new Consulta(ms.get(2), ps.get(2), LocalDateTime.of(2026, 10, 7, 10, 0),
                                "agendada", 550.0, null),
                        new Consulta(ms.get(0), ps.get(1), LocalDateTime.of(2026, 9, 20, 11, 0),
                                "realizada", 750.0, "Exame em dia"),
                        new Consulta(ms.get(1), ps.get(2), LocalDateTime.of(2026, 9, 18, 16, 0),
                                "cancelada", 480.0, "Paciente desmarcou"),
                        new Consulta(ms.get(2), ps.get(0), LocalDateTime.of(2026, 10, 12, 8, 30),
                                "agendada", 550.0, "Primeira consulta")
                ));
                System.out.println("DataLoader: 6 consultas criadas.");
            }
        }

        System.out.println("DataLoader: banco de dados pronto.");
    }

    private Medico medico(String nome, String crm, Especialidade especialidade, double valorConsulta) {
        Medico m = new Medico();
        m.setNome(nome);
        m.setCrm(crm);
        m.setEspecialidade(especialidade);
        m.setValorConsulta(valorConsulta);
        m.setAtivo(true);
        return m;
    }

    private Paciente paciente(String nome, String cpf, String email,
                              String telefone, String dataNascimento) {
        Paciente p = new Paciente();
        p.setNome(nome);
        p.setCpf(cpf);
        p.setEmail(email);
        p.setTelefone(telefone);
        p.setDataNascimento(LocalDate.parse(dataNascimento));
        p.setAtivo(true);
        return p;
    }
}
