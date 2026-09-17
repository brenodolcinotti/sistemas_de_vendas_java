package br.com.aweb.sistema_vendas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.aweb.sistema_vendas.model.Cliente;
import br.com.aweb.sistema_vendas.service.ClienteService;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // 1. LISTAR CLIENTES
    @GetMapping
    public String listar(Model model) {
        // Busca todos os clientes no banco e envia para a list.html
        model.addAttribute("clientes", clienteService.buscarTodos());
        return "cliente/list";
    }

    // 2. ABRIR FORMULÁRIO DE NOVO CLIENTE
    @GetMapping("/novo")
    public String novoCliente(Model model) {
        // Envia um objeto vazio para o form.html preencher
        model.addAttribute("cliente", new Cliente());
        return "cliente/form";
    }

    // 3. ABRIR FORMULÁRIO DE EDIÇÃO (ALTERAR)
    @GetMapping("/editar/{id}")
    public String editarCliente(@PathVariable Long id, Model model) {
        // Busca o cliente existente pelo ID e envia para o form.html
        Cliente cliente = clienteService.buscarPorId(id);
        model.addAttribute("cliente", cliente);
        return "cliente/form";
    }

    // 4. SALVAR OU ALTERAR CLIENTE (POST DO FORMULÁRIO)
    @PostMapping
    public String salvarCliente(@Validated @ModelAttribute("cliente") Cliente cliente, 
                                BindingResult result, 
                                RedirectAttributes attributes,
                                Model model) {
        
        // Passo A: Verifica se o HTML/Hibernate pegou campos vazios ou inválidos
        if (result.hasErrors()) {
            return "cliente/form"; // Devolve para a tela mostrando os spans de erro em vermelho
        }

        // Passo B: Tenta salvar no banco de dados passando pelas regras de negócio
        try {
            clienteService.salvarOuAlterar(cliente);
            attributes.addFlashAttribute("mensagem", "Cliente salvo com sucesso!");
            return "redirect:/clientes";
            
        } catch (IllegalArgumentException e) {
            // Passo C: Se o Service barrar por E-mail ou CPF duplicado, devolve o erro para a tela
            model.addAttribute("erroUnicidade", e.getMessage());
            return "cliente/form";
        }
    }

    // 5. ABRIR TELA DE CONFIRMAÇÃO DE EXCLUSÃO
    @GetMapping("/deletar/{id}")
    public String abrirTelaDeletar(@PathVariable Long id, Model model) {
        // Busca o cliente para exibir os dados na tela delete.html
        Cliente cliente = clienteService.buscarPorId(id);
        model.addAttribute("cliente", cliente);
        return "cliente/delete";
    }

    // 6. CONFIRMAR E EXECUTAR A EXCLUSÃO NO BANCO
    @PostMapping("/deletar/{id}")
    public String confirmarDelecao(@PathVariable Long id, RedirectAttributes attributes) {
        // Executa a exclusão definitiva
        clienteService.deletar(id);
        attributes.addFlashAttribute("mensagem", "Cliente excluído com sucesso!");
        return "redirect:/clientes";
    }
}