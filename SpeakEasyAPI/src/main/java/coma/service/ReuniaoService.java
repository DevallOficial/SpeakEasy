package coma.service;

import coma.models.Participante;
import coma.models.Reuniao;

import coma.repository.ReuniaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;
import java.util.Properties;
/**
 * Serviço responsável pela lógica de envio de email para o usuário e participantes
 */
@Service
public class ReuniaoService {

    @Autowired
    private ReuniaoRepository reuniaoRepository;

    @Autowired
    @Lazy
    private JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String fromEmail;


    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername("devall.fiap@gmail.com");
        mailSender.setPassword("x o s s n h p q q x s p a u g a");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

    public Reuniao save(Reuniao reuniao) {
        Reuniao reuniaoSalva = reuniaoRepository.save(reuniao);

        // Lógica para enviar e-mails para o organizador e participantes
        enviarParaOrganizador(reuniaoSalva);
        enviarParaParticipante(reuniaoSalva);

        return reuniaoSalva;
    }


    public void enviarParaOrganizador(Reuniao reuniao) {
        String subject = "Sua reunião foi salva com sucesso!";
        String message = "Sua reunião com o título '" + reuniao.getTitulo() + "' foi salva com sucesso.";

        enviarEmail(reuniao.getOrganizador().getEmail(), subject, message);
    }

    private void enviarParaParticipante(Reuniao reuniao) {
        String subject = "Nova reunião disponível: " + reuniao.getTitulo();
        String message = "Uma nova reunião foi criada com o título '" + reuniao.getTitulo() +
                "'. Detalhes da reunião:\n" +
                "Descrição: " + reuniao.getDescricao() + "\n" +
                "Data: " + reuniao.getData();

        for (Participante participantes : reuniao.getParticipantes()) {
            enviarEmail(participantes.getEmail(), subject, message);
        }
    }

    private void enviarEmail(String toEmail, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromEmail);
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        javaMailSender.send(mailMessage);
    }
    public Reuniao obterReuniaobyId(String id) throws Exception {
        Optional<Reuniao> reuniaoOptional = reuniaoRepository.findById(id);
        if (reuniaoOptional.isPresent()) {
            return reuniaoOptional.get();
        } else {
            throw new Exception("Reunião não encontrado com o id: " + id);
        }
    }
}