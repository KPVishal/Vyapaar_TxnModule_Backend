package com.bijlipay.model.dto;

public class EmailConversationDto {
    private String reply_to = "service@bijlipay.co.in";
    private String to = "service@bijlipay.co.in";
    private String from;
    private String subject;
    private String body;
    private String cc;
    private String bcc;
    private String person_name;
    private String attachments;

    public String getReply_to() {
        return reply_to;
    }

    public void setReply_to(String reply_to) {
        this.reply_to = reply_to;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public String getPerson_name() {
        return person_name;
    }

    public void setPerson_name(String person_name) {
        this.person_name = person_name;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    @Override
    public String toString() {
        return "{" + "reply_to='" + reply_to + '\'' + ", to='" + to + '\'' + ", from='" + from + '\'' + ", subject='" + subject + '\'' + ", body='" + body + '\'' + ", cc='" + cc + '\'' + ", bcc='" + bcc + '\'' + ", person_name='" + person_name + '\'' + ", attachments='" + attachments + '\'' + '}';
    }
}
