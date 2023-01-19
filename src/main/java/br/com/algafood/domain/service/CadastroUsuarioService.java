package br.com.algafood.domain.service;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.algafood.domain.exception.NegocioException;
import br.com.algafood.domain.exception.UsuarioNaoEncontradoException;
import br.com.algafood.domain.model.Grupo;
import br.com.algafood.domain.model.Usuario;
import br.com.algafood.domain.repository.UsuarioRepository;

@Service
public class CadastroUsuarioService {

	
	@Autowired
	private CadastroGrupoService cadastroGrupo;
	
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Transactional
    public Usuario salvar(Usuario usuario) {
    	entityManager.detach(usuario);
    	
    	Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(usuario.getEmail());
    	if (usuarioExistente.isPresent() && !usuarioExistente.get().equals(usuario)) {
    		throw new NegocioException(String.format("Já existe um usuário com o e-mail %s", usuario.getEmail()));
    	}
    	
    	if (usuario.isNovo()) {
    		usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
    	}
    	
    	return usuarioRepository.save(usuario);
    }
    @Transactional
    public void desassociarGrupo(Long usuarioId, Long grupoId) {
        Usuario usuario = buscarOuFalhar(usuarioId);
        Grupo grupo = cadastroGrupo.buscarOuFalhar(grupoId);
        
        usuario.removerGrupo(grupo);
    }

    @Transactional
    public void associarGrupo(Long usuarioId, Long grupoId) {
        Usuario usuario = buscarOuFalhar(usuarioId);
        Grupo grupo = cadastroGrupo.buscarOuFalhar(grupoId);
        
        usuario.adicionarGrupo(grupo);
    }
    
    @Transactional
    public void alterarSenha(Long usuarioId, String senhaAtual, String novaSenha) {
        Usuario usuario = buscarOuFalhar(usuarioId);
        
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new NegocioException("Senha atual informada não coincide com a senha do usuário.");
        }
        
        usuario.setSenha(passwordEncoder.encode(novaSenha));
    }

    public Usuario buscarOuFalhar(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));
    }            
}   
