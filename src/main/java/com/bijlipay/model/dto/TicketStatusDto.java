package com.bijlipay.model.dto;

public class TicketStatusDto {
    private String ticketNumber;
    private String status;
    private String justiFication;

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJustiFication() {
        return justiFication;
    }

    public void setJustiFication(String justiFication) {
        this.justiFication = justiFication;
    }

    @Override
    public String toString() {
        return "TicketStatusDto{" + "ticketNumber='" + ticketNumber + '\'' + ", status='" + status + '\'' + ", justiFication='" + justiFication + '\'' + '}';
    }
}
