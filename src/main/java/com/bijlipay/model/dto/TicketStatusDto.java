package com.bijlipay.model.dto;

public class TicketStatusDto {
    private String ticketNumber;
    private String status;
    private String justiFication;

    public String getTicketNumber() {
        return this.ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJustiFication() {
        return this.justiFication;
    }

    public void setJustiFication(String justiFication) {
        this.justiFication = justiFication;
    }

    public String toString() {
        return "TicketStatusDto{ticketNumber='" + this.ticketNumber + "', status='" + this.status + "', justiFication='" + this.justiFication + "'}";
    }
}