package br.com.algafood.api.v1.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.algafood.api.v1.assembler.FotoProdutoModelAssembler;
import br.com.algafood.api.v1.model.FotoProdutoModel;
import br.com.algafood.api.v1.model.input.FotoProdutoInput;
import br.com.algafood.api.v1.openapi.controller.RestauranteProdutoFotoControllerOpenApi;
import br.com.algafood.core.security.CheckSecurity;
import br.com.algafood.domain.exception.EntidadeNaoEncontradaException;
import br.com.algafood.domain.model.FotoProduto;
import br.com.algafood.domain.model.Produto;
import br.com.algafood.domain.service.CadastroProdutoService;
import br.com.algafood.domain.service.CatalogoFotoProdutoService;
import br.com.algafood.domain.service.FotoStorageService;

@RestController
@RequestMapping("/v1/restaurantes/{restauranteId}/produtos/{produtoId}/foto")
public class RestauranteProdutoFotoController implements RestauranteProdutoFotoControllerOpenApi {

	@Autowired
	private CadastroProdutoService cadastroProduto;

	@Autowired
	private CatalogoFotoProdutoService catalogoFotoProduto;

	@Autowired
	private FotoProdutoModelAssembler fotoProdutoModelAssembler;

	@Autowired
	private FotoStorageService fotoStorage;

	@CheckSecurity.Restaurantes.PodeGerenciarFuncionamento
	@PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public FotoProdutoModel atualizarFoto(@PathVariable Long restauranteId, @PathVariable Long produtoId,
			@Valid FotoProdutoInput fotoProdutoInput) throws IOException {
		Produto produto = cadastroProduto.buscarOuFalhar(restauranteId, produtoId);

		MultipartFile arquivo = fotoProdutoInput.getArquivo();

		FotoProduto foto = new FotoProduto();
		foto.setProduto(produto);
		foto.setDescricao(fotoProdutoInput.getDescricao());
		foto.setContentType(arquivo.getContentType());
		foto.setTamanho(arquivo.getSize());
		foto.setNomeArquivo(arquivo.getOriginalFilename());

		FotoProduto fotoSalva = catalogoFotoProduto.salvar(foto, arquivo.getInputStream());

		return fotoProdutoModelAssembler.toModel(fotoSalva);
	}

	@CheckSecurity.Restaurantes.PodeConsultar
	@Override
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public FotoProdutoModel buscar(@PathVariable Long restauranteId,
	                               @PathVariable Long produtoId) {
		FotoProduto fotoProduto = catalogoFotoProduto.buscarOuFalhar(restauranteId, produtoId);

		return fotoProdutoModelAssembler.toModel(fotoProduto);
	}

	@CheckSecurity.Restaurantes.PodeConsultar
	@GetMapping
	public ResponseEntity<InputStreamResource> servir(@PathVariable Long restauranteId,
			@PathVariable Long produtoId, @RequestHeader(name = "accept") String acceptHeader)
			throws HttpMediaTypeNotAcceptableException {
		try {
			FotoProduto fotoProduto = catalogoFotoProduto.buscarOuFalhar(restauranteId, produtoId);

			MediaType mediaTypeFoto = MediaType.parseMediaType(fotoProduto.getContentType());
			List<MediaType> mediaTypesAceitas = MediaType.parseMediaTypes(acceptHeader);

			verificarCompatibilidadeMediaType(mediaTypeFoto, mediaTypesAceitas);

			InputStream inputStream = fotoStorage.recuperar(fotoProduto.getNomeArquivo());

			return ResponseEntity.ok().contentType(mediaTypeFoto).body(new InputStreamResource(inputStream));
		} catch (EntidadeNaoEncontradaException e) {
			return ResponseEntity.notFound().build();
		}
	}

	private void verificarCompatibilidadeMediaType(MediaType mediaTypeFoto, List<MediaType> mediaTypesAceitas)
			throws HttpMediaTypeNotAcceptableException {

		boolean compativel = mediaTypesAceitas.stream()
				.anyMatch(mediaTypeAceita -> mediaTypeAceita.isCompatibleWith(mediaTypeFoto));

		if (!compativel) {
			throw new HttpMediaTypeNotAcceptableException(mediaTypesAceitas);
		}
	}

	@CheckSecurity.Restaurantes.PodeGerenciarFuncionamento
	@Override
	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> excluir(@PathVariable Long restauranteId, 
			@PathVariable Long produtoId) {
		catalogoFotoProduto.excluir(restauranteId, produtoId);
		return ResponseEntity.noContent().build();
	}

}



//@Override
//@GetMapping(produces = MediaType.ALL_VALUE)
//public ResponseEntity<?> servir(@PathVariable Long restauranteId,
//                                @PathVariable Long produtoId, @RequestHeader(name = "accept") String acceptHeader)
//		throws HttpMediaTypeNotAcceptableException {
//	try {
//		FotoProduto fotoProduto = catalogoFotoProduto.buscarOuFalhar(restauranteId, produtoId);
//
//		MediaType mediaTypeFoto = MediaType.parseMediaType(fotoProduto.getContentType());
//		List<MediaType> mediaTypesAceitas = MediaType.parseMediaTypes(acceptHeader);
//
//		verificarCompatibilidadeMediaType(mediaTypeFoto, mediaTypesAceitas);
//
//		FotoRecuperada fotoRecuperada = fotoStorage.recuperar(fotoProduto.getNomeArquivo());
//
//		if (fotoRecuperada.temUrl()) {
//			return ResponseEntity
//					.status(HttpStatus.FOUND)
//					.header(HttpHeaders.LOCATION, fotoRecuperada.getUrl())
//					.build();
//		} else {
//			return ResponseEntity.ok()
//					.contentType(mediaTypeFoto)
//					.body(new InputStreamResource(fotoRecuperada.getInputStream()));
//		}
//	} catch (EntidadeNaoEncontradaException e) {
//		return ResponseEntity.notFound().build();
//	}
//}





//	@PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public void atualizarFoto(@PathVariable Long restauranteId,
//			@PathVariable Long produtoId, @Valid FotoProdutoInput fotoProdutoInput) {
//		
//		var nomeArquivo = UUID.randomUUID().toString() 
//				+ "_" + fotoProdutoInput.getArquivo().getOriginalFilename();
//		
//		var arquivoFoto = Path.of("C:\\Users\\mathe\\OneDrive\\Documentos\\Catalogo", nomeArquivo);
//		
//		System.out.println(fotoProdutoInput.getDescricao());
//		System.out.println(arquivoFoto);
//		System.out.println(fotoProdutoInput.getArquivo().getContentType());
//		
//		try {
//			fotoProdutoInput.getArquivo().transferTo(arquivoFoto);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
