package br.com.algafood.api.v1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.algafood.api.v1.AlgaLinks;
import br.com.algafood.api.v1.assembler.FormaPagamentoModelAssembler;
import br.com.algafood.api.v1.model.FormaPagamentoModel;
import br.com.algafood.api.v1.openapi.controller.RestauranteFormaPagamentoControllerOpenApi;
import br.com.algafood.core.security.AlgaSecurity;
import br.com.algafood.core.security.CheckSecurity;
import br.com.algafood.domain.model.Restaurante;
import br.com.algafood.domain.service.CadastroRestauranteService;

@RestController
@RequestMapping(path = "/v1/restaurantes/{restauranteId}/formas-pagamento")
public class RestauranteFormaPagamentoController implements RestauranteFormaPagamentoControllerOpenApi {

	@Autowired
	private CadastroRestauranteService cadastroRestaurante;

	@Autowired
	private FormaPagamentoModelAssembler formaPagamentoModelAssembler;
	
	@Autowired
	private AlgaLinks algaLinks;
	
	@Autowired
	private AlgaSecurity algaSecurity;

	@CheckSecurity.Restaurantes.PodeConsultar
	@Override
	@GetMapping
	public CollectionModel<FormaPagamentoModel> listar(@PathVariable Long restauranteId) {
	    Restaurante restaurante = cadastroRestaurante.buscarOuFalhar(restauranteId);
	    
	    CollectionModel<FormaPagamentoModel> formasPagamentoModel 
	        = formaPagamentoModelAssembler.toCollectionModel(restaurante.getFormasPagamento())
	            .removeLinks();
	    
	    formasPagamentoModel.add(algaLinks.linkToRestauranteFormasPagamento(restauranteId));

	    if (algaSecurity.podeGerenciarFuncionamentoRestaurantes(restauranteId)) {
	        formasPagamentoModel.add(algaLinks.linkToRestauranteFormaPagamentoAssociacao(restauranteId, "associar"));
	        
	        formasPagamentoModel.getContent().forEach(formaPagamentoModel -> {
	            formaPagamentoModel.add(algaLinks.linkToRestauranteFormaPagamentoDesassociacao(
	                    restauranteId, formaPagamentoModel.getId(), "desassociar"));
	        });
	    }
	    
	    return formasPagamentoModel;
	}

	@CheckSecurity.Restaurantes.PodeGerenciarFuncionamento
	@Override
	@DeleteMapping(value = "/{formaPagamentoId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> desassociar(@PathVariable Long restauranteId, @PathVariable Long formaPagamentoId) {
		cadastroRestaurante.desassociarFormaPagamento(restauranteId, formaPagamentoId);
		
		return	ResponseEntity.noContent().build();
	}

	@CheckSecurity.Restaurantes.PodeGerenciarFuncionamento
	@Override
	@PutMapping(value = "/{formaPagamentoId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> associar(@PathVariable Long restauranteId, @PathVariable Long formaPagamentoId) {
		cadastroRestaurante.associarFormaPagamento(restauranteId, formaPagamentoId);
		
		return ResponseEntity.noContent().build();
	}

}