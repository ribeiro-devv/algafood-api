package br.com.algafood.infrastructure.service.email;

import java.io.IOException;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import br.com.algafood.core.email.EmailProperties;
import br.com.algafood.domain.service.EnvioEmailService;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;

@Service
public class SmtpEnvioEmailService implements EnvioEmailService {

	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private EmailProperties emailProperties;
	
	@Autowired
	private Configuration freemarkerConfig;
	
	@Override
	public void enviar(Mensagem mensagem) {
		try {
			String corpo = processarTemplate(mensagem);
			
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
			helper.setFrom(emailProperties.getRemetente());
			helper.setTo(mensagem.getDestinatarios().toArray(new String[0]));
			helper.setSubject(mensagem.getAssunto());
			helper.setText(corpo, true);
			
			mailSender.send(mimeMessage);
		} catch (Exception e) {
			throw new EmailException("Não foi possível enviar e-mail", e);
		}
	}
	
	private String processarTemplate(Mensagem  mensagem) {
		try {
			Template template = freemarkerConfig.getTemplate(mensagem.getCorpo());
					
			return FreeMarkerTemplateUtils.processTemplateIntoString(template, mensagem.getVariaveis());
			
			

		} catch (Exception e) {
			throw new EmailException("Não foi possível montar o template do e-mail", e);
		}
		
	}
//	@Override
//	public void enviar(Mensagem mensagem) {
//	    try {
//	        MimeMessage mimeMessage = criarMimeMessage(mensagem);
//	        
//	        mailSender.send(mimeMessage);
//	    } catch (Exception e) {
//	        throw new EmailException("Não foi possível enviar e-mail", e);
//	    }
//	}
//
//	protected MimeMessage criarMimeMessage(Mensagem mensagem) throws MessagingException {
//	    String corpo = processarTemplate(mensagem);
//	    
//	    MimeMessage mimeMessage = mailSender.createMimeMessage();
//	    
//	    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
//	    helper.setFrom(emailProperties.getRemetente());
//	    helper.setTo(mensagem.getDestinatarios().toArray(new String[0]));
//	    helper.setSubject(mensagem.getAssunto());
//	    helper.setText(corpo, true);
//	    
//	    return mimeMessage;

}
