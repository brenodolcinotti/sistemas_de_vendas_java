package br.com.aweb.sistema_vendas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import br.com.aweb.sistema_vendas.model.Cliente;
import br.com.aweb.sistema_vendas.repository.ClienteRepository;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    // 1. Buscar todos os clientes (para a tela de listagem)
    public List<Cliente> buscarTodos() {
        return clienteRepository.findAll();
    }

    // 2. Buscar um cliente específico pelo ID (para edição e exclusão)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com o ID: " + id));
    }

    // 3. Salvar ou Alterar com validação de regras de negócio (Unicidade)
    public Cliente salvarOuAlterar(Cliente cliente) {
        
        // Validação de unicidade do E-mail
        Optional<Cliente> clienteExistenteEmail = clienteRepository.findByEmail(cliente.getEmail());
        // Se achou alguém com este e-mail, e os IDs são diferentes (ou seja, não é a própria pessoa se editando)
        if (clienteExistenteEmail.isPresent() && !clienteExistenteEmail.get().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("Erro: Este e-mail já está sendo utilizado por outro cliente.");
        }

        // Validação de unicidade do CPF
        Optional<Cliente> clienteExistenteCpf = clienteRepository.findByCpf(cliente.getCpf());
        // Mesma lógica de bloqueio para o CPF
        if (clienteExistenteCpf.isPresent() && !clienteExistenteCpf.get().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("Erro: Este CPF já está cadastrado para outro cliente.");
        }

        // Se passar limpo pelas regras, salva no banco de dados
        return clienteRepository.save(cliente);
    }

    // 4. Excluir o cliente
    public void deletar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new IllegalArgumentException("Erro: Cliente não encontrado para exclusão.");
        }
        clienteRepository.deleteById(id);
    }
}