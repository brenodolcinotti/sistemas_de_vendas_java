package br.com.aweb.sistema_vendas.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.aweb.sistema_vendas.model.Cliente;
import br.com.aweb.sistema_vendas.model.ItemPedido;
import br.com.aweb.sistema_vendas.model.Pedido;
import br.com.aweb.sistema_vendas.model.Produto;
import br.com.aweb.sistema_vendas.model.StatusPedido;
import br.com.aweb.sistema_vendas.repository.PedidoRepository;
import br.com.aweb.sistema_vendas.repository.ProdutoRepository;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    @Transactional
    public Pedido criarPedido(Cliente cliente) {
        Pedido pedido = new Pedido(cliente);
        // Aqui PRECISA do save porque o pedido é 100% novo (não veio da base de dados)
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public void adicionarItem(Long pedidoId, Long produtoId, Integer quantidade) {
        Optional<Pedido> optionalPedido = pedidoRepository.findById(pedidoId);
        if (!optionalPedido.isPresent()) {
            throw new IllegalArgumentException("Pedido não encontrado");
        }

        Optional<Produto> optionalProduto = produtoRepository.findById(produtoId);
        if (!optionalProduto.isPresent()) {
            throw new IllegalArgumentException("Produto não encontrado");
        }

        Pedido pedido = optionalPedido.get();
        Produto produto = optionalProduto.get();

        if (pedido.getStatus() != StatusPedido.ATIVO) {
            throw new IllegalStateException("Não é possível alterar um pedido cancelado");
        }

        if (produto.getQuantidadeEmEstoque() < quantidade) {
            throw new IllegalArgumentException("Estoque insuficiente para o produto: " + produto.getNome());
        }

        ItemPedido item = new ItemPedido(produto, quantidade);
        item.setPedido(pedido);
        pedido.getItens().add(item);

        produto.setQuantidadeEmEstoque(produto.getQuantidadeEmEstoque() - quantidade);

        calcularValorTotal(pedido);

        // MÁGICA DO JPA: Como usamos @Transactional, NÃO fazemos .save() aqui!
        // O Spring Boot deteta as alterações no Pedido e Produto e guarda tudo automaticamente no fim da execução.
    }

    @Transactional
    public void removerItem(Long pedidoId, Long itemId) {
        Optional<Pedido> optionalPedido = pedidoRepository.findById(pedidoId);
        if (!optionalPedido.isPresent()) {
            throw new IllegalArgumentException("Pedido não encontrado");
        }
        
        Pedido pedido = optionalPedido.get();

        if (pedido.getStatus() != StatusPedido.ATIVO) {
            throw new IllegalStateException("Não é possível alterar um pedido cancelado");
        }

        ItemPedido itemRemover = null;
        for (ItemPedido item : pedido.getItens()) {
            if (item.getId().equals(itemId)) {
                itemRemover = item;
                break;
            }
        }

        if (itemRemover != null) {
            Produto produto = itemRemover.getProduto();
            produto.setQuantidadeEmEstoque(produto.getQuantidadeEmEstoque() + itemRemover.getQuantidade());

            pedido.getItens().remove(itemRemover);
            calcularValorTotal(pedido);
            
            // Sem .save() manuais! O orphanRemoval apagará o item e atualizará o estoque magicamente.
        } else {
            throw new IllegalArgumentException("Item não encontrado neste pedido");
        }
    }

    @Transactional
    public void cancelarPedido(Long id) {
        Optional<Pedido> optionalPedido = pedidoRepository.findById(id);
        if (!optionalPedido.isPresent()) {
            throw new IllegalArgumentException("Pedido não encontrado");
        }
        
        Pedido pedido = optionalPedido.get();

        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new IllegalArgumentException("O pedido já se encontra cancelado");
        }

        pedido.setStatus(StatusPedido.CANCELADO);

        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getProduto();
            produto.setQuantidadeEmEstoque(produto.getQuantidadeEmEstoque() + item.getQuantidade());
        }
        
        // Sem .save() manuais!
    }

    private void calcularValorTotal(Pedido pedido) {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedido item : pedido.getItens()) {
            total = total.add(item.getSubtotal());
        }
        pedido.setValorTotal(total);
    }
}