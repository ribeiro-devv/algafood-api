//package br.com.algafood.core.email;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import br.com.algafood.domain.service.EnvioEmailService;
//import br.com.algafood.infrastructure.service.email.SmtpEnvioEmailService;
//
//@Configuration
//public class EmailConfig {
//
//    @Autowired
//    private EmailProperties emailProperties;
//
//    @Bean
//    public EnvioEmailService envioEmailService() {
//        // Acho melhor usar switch aqui do que if/else if
//        switch (emailProperties.getImpl()) {
//            case FAKE:
//                return new FakeEnvioEmailService();
//            case SMTP:
//                return new SmtpEnvioEmailService();
//            default:
//                return null;
//        }
//    }
//case SANDBOX:
//    return new SandboxEnvioEmailService();
//} 
