package br.com.aweb.sistema_vendas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import br.com.aweb.sistema_vendas.model.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Método customizado para buscar um cliente pelo E-mail exato
    // Usado no ClienteService para garantir que não existam e-mails duplicados
    Optional<Cliente> findByEmail(String email);

    // Método customizado para buscar um cliente pelo CPF exato
    // Usado no ClienteService para garantir que não existam CPFs duplicados
    Optional<Cliente> findByCpf(String cpf);

}