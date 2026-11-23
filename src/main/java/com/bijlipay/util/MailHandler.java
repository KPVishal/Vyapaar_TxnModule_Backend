package com.bijlipay.util;

import com.bijlipay.model.dto.ForgotPasswordEmail;
import com.bijlipay.model.dto.MailFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class MailHandler {
    private static final Logger log = LoggerFactory.getLogger(MailHandler.class);
    @Autowired
    private JavaMailSender sender;

    public void commonMailRequest(MailFormat mailFormat) throws MessagingException {
        log.info("Mail Sending.......");
        ByteArrayOutputStream outputStream = null;

        try {
            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText(mailFormat.getForgotPasswordEmail().getBody());
            outputStream = new ByteArrayOutputStream();
            Workbook excel = ReportUtils.createReport(mailFormat.getSheetName(), mailFormat.getColumnName(), mailFormat.getListList());
            excel.write(outputStream);
            byte[] bytes = outputStream.toByteArray();
            DataSource dataSource = new ByteArrayDataSource(bytes, "application/vnd.ms-excel");
            MimeBodyPart pdfBodyPart = new MimeBodyPart();
            pdfBodyPart.setDataHandler(new DataHandler(dataSource));
            pdfBodyPart.setFileName(DateUtil.getDate() + ".xlsx");
            MimeMultipart mimeMultipart = new MimeMultipart();
            mimeMultipart.addBodyPart(pdfBodyPart);
            mimeMultipart.addBodyPart(textBodyPart);
            MimeMessage message = this.sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("misreports@bijlipay.co.in");
            helper.setTo(mailFormat.getForgotPasswordEmail().getReceiverEmail());
            helper.setSubject(mailFormat.getForgotPasswordEmail().getSubject());
            helper.setText(mailFormat.getForgotPasswordEmail().getBody());
            message.setContent(mimeMultipart);
            this.sender.send(message);
            log.info("Mail Sent.......");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void commonMailRequestWithoutAttachment(ForgotPasswordEmail mailFormat) throws MessagingException {
        log.info("Mail Sending.......");
        MimeMessage message = this.sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom("reports@bijlipay.co.in");
        helper.setTo(mailFormat.getReceiverEmail());
        helper.setText(mailFormat.getBody());
        helper.setSubject(mailFormat.getSubject());
        this.sender.send(message);
        log.info("Mail Sent.......");
    }
}