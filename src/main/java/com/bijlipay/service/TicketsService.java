package com.bijlipay.service;

import com.bijlipay.model.dto.TicketStatusDto;
import com.bijlipay.model.tickets.IssuesList;
import com.bijlipay.model.tickets.Tickets;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.json.JSONException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface TicketsService {
    List<Tickets> getByMid(String mid);

    List<HashMap<String, String>> searchByTicketNumber(String ticketNumber);

    String uploadAttachment(MultipartFile file, String fileName) throws IOException;

    Tickets createTicket(Tickets newTicket);

    Tickets saveTicketNumber(Tickets ticket);

    Page<HashMap<String, Object>> getTicketList(Optional<String> searchKeyword, Pageable pageable, Optional<Long> from, Optional<Long> to, long sessionUserId);

    Integer updateTicketStatus(long sessionUserId, String kaptureBaseUrl) throws JSONException;

    Tickets updateTicketStatus(TicketStatusDto ticketStatusDto);

    List<String> getTickeNumberByMid(List<String> mId, List<String> vpa);

    List<IssuesList> findAllList();
}