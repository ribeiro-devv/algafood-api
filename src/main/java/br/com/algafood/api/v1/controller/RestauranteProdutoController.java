package br.com.algafood.api.v1.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.algafood.api.v1.AlgaLinks;
import br.com.algafood.api.v1.assembler.ProdutoInputDisassembler;
import br.com.algafood.api.v1.assembler.ProdutoModelAssembler;
import br.com.algafood.api.v1.model.ProdutoModel;
import br.com.algafood.api.v1.model.input.ProdutoInput;
import br.com.algafood.api.v1.openapi.controller.RestauranteProdutoControllerOpenApi;
import br.com.algafood.core.security.CheckSecurity;
import br.com.algafood.domain.model.Produto;
import br.com.algafood.domain.model.Restaurante;
import br.com.algafood.domain.repository.ProdutoRepository;
import br.com.algafood.domain.service.CadastroProdutoService;
import br.com.algafood.domain.service.CadastroRestauranteService;

@RestController
@RequestMapping("/v1/restaurantes/{restauranteId}/produtos")
public class RestauranteProdutoController implements RestauranteProdutoControllerOpenApi{

    @Autowired
    private ProdutoRepository produtoRepository;
    
    @Autowired
    private CadastroProdutoService cadastroProduto;
    
    @Autowired
    private CadastroRestauranteService cadastroRestaurante;
    
    @Autowired
    private ProdutoModelAssembler produtoModelAssembler;
    
    @Autowired
    private ProdutoInputDisassembler produtoInputDisassembler;
    
    @Autowired
    private AlgaLinks algaLinks;
    
    @CheckSecurity.Restaurantes.PodeConsultar
    @GetMapping(value = "/{produtoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProdutoModel buscar(@PathVariable Long restauranteId, @PathVariable Long produtoId) {
        Produto produto = cadastroProduto.buscarOuFalhar(restauranteId, produtoId);
        
        return produtoModelAssembler.toModel(produto);
    }
    
    @CheckSecurity.Restaurantes.PodeGerenciarFuncionamento
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoModel adicionar(@PathVariable Long restauranteId,
            @RequestBody @Valid ProdutoInput produtoInput) {
        Restaurante restaurante = cadastroRestaurante.buscarOuFalhar(restauranteId);
        
        Produto produto = produtoInputDisassembler.toDomainObject(produtoInput);
        produto.setRestaurante(restaurante);
        
        produto = cadastroProduto.salvar(produto);
        
        return produtoModelAssembler.toModel(produto);
    }
    
    @CheckSecurity.Restaurantes.PodeGerenciarFuncionamento
    @PutMapping(value = "/{produtoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProdutoModel atualizar(@PathVariable Long restauranteId, @PathVariable Long produtoId,
            @RequestBody @Valid ProdutoInput produtoInput) {
        Produto produtoAtual = cadastroProduto.buscarOuFalhar(restauranteId, produtoId);
        
        produtoInputDisassembler.copyToDomainObject(produtoInput, produtoAtual);
        
        produtoAtual = cadastroProduto.salvar(produtoAtual);
        
        return produtoModelAssembler.toModel(produtoAtual);
    }

    @CheckSecurity.Restaurantes.PodeConsultar
    @Override
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public CollectionModel<ProdutoModel> listar(@PathVariable Long restauranteId,
			@RequestParam(required = false, defaultValue = "false") Boolean incluirInativos) {
		Restaurante restaurante = cadastroRestaurante.buscarOuFalhar(restauranteId);
		
		List<Produto> todosProdutos = null;
		
		if (incluirInativos) {
			todosProdutos = produtoRepository.findTodosByRestaurante(restaurante);
		} else {
			todosProdutos = produtoRepository.findAtivosByRestaurante(restaurante);
		}
		
		return produtoModelAssembler.toCollectionModel(todosProdutos)
				.add(algaLinks.linkToProdutos(restauranteId));
	}
}  
