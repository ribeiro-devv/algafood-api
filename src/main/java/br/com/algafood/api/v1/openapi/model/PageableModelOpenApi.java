package br.com.algafood.api.v1.openapi.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

//@ApiModel("Pageable")
@Getter
@Setter
public class PageableModelOpenApi {

//	@ApiModelProperty(example = "0", value = "Número da página (começa em 0)")
	private int page;
	
//	@ApiModelProperty(example = "10", value = "Quantidade de elementos pos página")
	private int size;
	
//	@ApiModelProperty(example = "nome,asc", value = "Nome da propriedade para ordenação")
	private List<String> sort;
}
