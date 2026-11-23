package com.bijlipay.model.dto;

public class TicketRequestDto {
    private String title;
    private String ticket_details;
    private String email_id;
    private String phone;
    private String ignore_add_customer = "yes";
    private EmailConversationDto email_conversation;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTicket_details() {
        return this.ticket_details;
    }

    public void setTicket_details(String ticket_details) {
        this.ticket_details = ticket_details;
    }

    public String getEmail_id() {
        return this.email_id;
    }

    public void setEmail_id(String email_id) {
        this.email_id = email_id;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIgnore_add_customer() {
        return this.ignore_add_customer;
    }

    public void setIgnore_add_customer(String ignore_add_customer) {
        this.ignore_add_customer = ignore_add_customer;
    }

    public EmailConversationDto getEmail_conversation() {
        return this.email_conversation;
    }

    public void setEmail_conversation(EmailConversationDto email_conversation) {
        this.email_conversation = email_conversation;
    }

    public String toString() {
        String var10000 = this.title;
        return "{title='" + var10000 + "', ticket_details='" + this.ticket_details + "', email_id='" + this.email_id + "', phone='" + this.phone + "', ignore_add_customer='" + this.ignore_add_customer + "', email_conversation=" + String.valueOf(this.email_conversation) + "}";
    }
}