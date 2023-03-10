package br.com.algafood.api.v1.openapi.model;

import java.util.List;

import org.springframework.hateoas.Links;

import br.com.algafood.api.v1.model.UsuarioModel;
import lombok.Data;

//@ApiModel("UsuariosModel")
@Data
public class UsuariosModelOpenApi {

    private UsuariosEmbeddedModelOpenApi _embedded;
    private Links _links;
    
//    @ApiModel("UsuariosEmbeddedModel")
    @Data
    public class UsuariosEmbeddedModelOpenApi {
        
        private List<UsuarioModel> usuarios;
        
    }   
} 
