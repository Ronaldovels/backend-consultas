package com.fiap.ec.backend_consultas.controller;
import com.fiap.ec.backend_consultas.model.Paciente;
import com.fiap.ec.backend_consultas.service.PacienteService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/pacientes")
@CrossOrigin
public class PacienteController {
    private final PacienteService service;
    public PacienteController(PacienteService service) {
        this.service = service;
    }
    @PostMapping
    public Paciente criar(@RequestBody Paciente paciente) {
        return service.salvar(paciente);
    }
    @GetMapping
    public List<Paciente> listar() {
        return service.listar();
    }
    @GetMapping("/{id}")
    public Paciente buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }
    @PutMapping("/{id}")
    public Paciente atualizar(@PathVariable Long id, @RequestBody Paciente paciente) {
        return service.atualizar(id, paciente);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}