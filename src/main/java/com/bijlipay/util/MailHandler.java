package com.bijlipay.util;

import com.bijlipay.model.dto.ForgotPasswordEmail;
import com.bijlipay.model.dto.MailFormat;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

import static com.bijlipay.util.Constant.FORGOT_PASSWORD_MAIL_STATIC_IP;

@Component
public class MailHandler {
    private static final Logger log = LoggerFactory.getLogger(MailHandler.class);
    @Autowired
    private JavaMailSender sender;

    public void commonMailRequest(MailFormat mailFormat) throws MessagingException {
        log.info("Mail Sending.......");
        ByteArrayOutputStream outputStream = null;
        try {
            //construct the text body part
            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText(mailFormat.getForgotPasswordEmail().getBody());
            //now write the PDF content to the output stream
            outputStream = new ByteArrayOutputStream();
            Workbook excel = ReportUtils.createReport(mailFormat.getSheetName(), mailFormat.getColumnName(), mailFormat.getListList());
            excel.write(outputStream);
            byte[] bytes = outputStream.toByteArray();
            //construct the pdf body part
            DataSource dataSource = new ByteArrayDataSource(bytes, "application/vnd.ms-excel");
            MimeBodyPart pdfBodyPart = new MimeBodyPart();
            pdfBodyPart.setDataHandler(new DataHandler(dataSource));
            pdfBodyPart.setFileName(DateUtil.getDate() + ".xlsx");
            //construct the mime multi part
            MimeMultipart mimeMultipart = new MimeMultipart();
            mimeMultipart.addBodyPart(pdfBodyPart);
            mimeMultipart.addBodyPart(textBodyPart);
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("misreports@bijlipay.co.in");
            helper.setTo(mailFormat.getForgotPasswordEmail().getReceiverEmail());
            helper.setSubject(mailFormat.getForgotPasswordEmail().getSubject());
            helper.setText(mailFormat.getForgotPasswordEmail().getBody());
            message.setContent(mimeMultipart);
            sender.send(message);
            log.info("Mail Sent.......");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commonMailRequestWithoutAttachment(ForgotPasswordEmail mailFormat) throws MessagingException {
        log.info("Mail Sending.......");
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
//        helper.setFrom("misreports@bijlipay.co.in");
        helper.setFrom("reports@bijlipay.co.in");
        helper.setTo(mailFormat.getReceiverEmail());
        helper.setText(mailFormat.getBody());
        helper.setSubject(mailFormat.getSubject());
        sender.send(message);
        log.info("Mail Sent.......");
    }
}
