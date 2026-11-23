package com.bijlipay.controller;

import com.bijlipay.config.WebConfig;
import com.bijlipay.model.UserTidVpaData;
import com.bijlipay.model.dto.EmailConversationDto;
import com.bijlipay.model.dto.TicketRequestDto;
import com.bijlipay.model.dto.TicketStatusDto;
import com.bijlipay.model.tickets.Tickets;
import com.bijlipay.service.TicketsService;
import com.bijlipay.service.UserTidVpaDataService;
import com.bijlipay.util.ApiResponse;
import com.bijlipay.util.DateUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping({"/txn"})
public class TicketController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);
    @Value("${kapture.base.url}")
    private String kaptureBaseUrl;
    @Value("${attachemnt.down.url}")
    private String attachementDownUrl;
    @Autowired
    private WebConfig webConfig;
    @Autowired
    private TicketsService ticketsService;
    @Autowired
    private UserTidVpaDataService userTidVpaDataService;

    @GetMapping({"/tickets/get-issue-list"})
    public ResponseEntity<?> getIssueList() throws URISyntaxException {
        logger.info("Start to get Ticket Issue List Api --/tickets/get-issue-list");
        return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success", this.ticketsService.findAllList()), HttpStatus.OK);
    }

    @RequestMapping(
            value = {"/tickets/create-ticket"},
            method = {RequestMethod.PUT}
    )
    @ResponseBody
    public ResponseEntity<?> createTicket(@RequestParam("request") String request, @RequestParam("imageFile") Optional<MultipartFile> attachment, @RequestParam("tid") Optional<String> tid, @RequestParam("vpa") Optional<String> vpa) throws URISyntaxException {
        logger.info("Start to Create Ticket Api --/tickets/create-ticket");
        logger.info("CREATE TICKET : The Data request {}", request);

        try {
            Tickets ticketsRequest = (Tickets)(new ObjectMapper()).readValue(request.replace("\\", ""), Tickets.class);
            UserTidVpaData merchantDeatils;
            if (tid.isPresent()) {
                merchantDeatils = this.userTidVpaDataService.findByTid((String)tid.get());
            } else if (vpa.isPresent()) {
                merchantDeatils = this.userTidVpaDataService.checkVpa((String)vpa.get());
            } else {
                merchantDeatils = this.userTidVpaDataService.findByTid(ticketsRequest.getTid());
            }

            if (merchantDeatils == null) {
                return new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "Merchant Not Found"), HttpStatus.NOT_FOUND);
            }

            logger.info("CREATE TICKET : Get the merchantId and title for ticket reporting : ---- : {} --{} --{}", new Object[]{merchantDeatils.getMid(), merchantDeatils.getVpa(), ticketsRequest.getTicketTitle()});
            if (merchantDeatils.getMid() == null && merchantDeatils.getVpa() == null) {
                logger.info("CREATE TICKET : Merchant details is null : ---- : ");
                return new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "Merchant Not Found"), HttpStatus.NOT_FOUND);
            }

            Tickets result = this.ticketsService.createTicket(ticketsRequest);
            logger.info("CREATE TICKET : The ticket is created in local DB : ---- : {}", result.getTicketTitle());
            String attachmentName = "";
            if (result != null && attachment.isPresent() && !((MultipartFile)attachment.get()).isEmpty()) {
                long var10000 = result.getId();
                String filePartName = var10000 + "_" + result.getTid() + "_" + result.getMid();
                if (((MultipartFile)attachment.get()).getSize() > 0L) {
                    attachmentName = this.ticketsService.uploadAttachment((MultipartFile)attachment.get(), filePartName);
                } else {
                    attachmentName = "";
                }
            }

            TicketRequestDto ticketRequestDto = new TicketRequestDto();
            EmailConversationDto emailConversationDto = new EmailConversationDto();
            ticketRequestDto.setTicket_details(ticketsRequest.getTicketDetails());
            ticketRequestDto.setIgnore_add_customer("yes");
            emailConversationDto.setTo("service@bijlipay.co.in");
            emailConversationDto.setReply_to("service@bijlipay.co.in");
            emailConversationDto.setBody(ticketsRequest.getTicketDetails());
            emailConversationDto.setCc("");
            emailConversationDto.setBcc("");
            emailConversationDto.setAttachments(attachmentName);
            if (tid.isPresent()) {
                String var10001 = ticketsRequest.getTicketTitle();
                ticketRequestDto.setTitle(var10001 + " - " + (String)tid.get());
                ticketRequestDto.setEmail_id(this.userTidVpaDataService.findByTid(ticketsRequest.getTid()).getUser().getEmail());
                ticketRequestDto.setPhone(this.userTidVpaDataService.findByTid(ticketsRequest.getTid()).getUser().getMobileNo());
                emailConversationDto.setFrom(this.userTidVpaDataService.findByTid(ticketsRequest.getTid()).getUser().getEmail());
                var10001 = ticketsRequest.getTicketTitle();
                emailConversationDto.setSubject(var10001 + " - " + (String)tid.get());
                emailConversationDto.setPerson_name(this.userTidVpaDataService.findByTid(ticketsRequest.getTid()).getUser().getMerchantName());
            } else if (vpa.isPresent()) {
                String var26 = ticketsRequest.getTicketTitle();
                ticketRequestDto.setTitle(var26 + " - " + (String)vpa.get());
                ticketRequestDto.setEmail_id(this.userTidVpaDataService.checkVpa(ticketsRequest.getVpa()).getUser().getEmail());
                ticketRequestDto.setPhone(this.userTidVpaDataService.checkVpa(ticketsRequest.getVpa()).getUser().getMobileNo());
                emailConversationDto.setFrom(this.userTidVpaDataService.checkVpa(ticketsRequest.getVpa()).getUser().getEmail());
                var26 = ticketsRequest.getTicketTitle();
                emailConversationDto.setSubject(var26 + " - " + (String)vpa.get());
                emailConversationDto.setPerson_name(this.userTidVpaDataService.checkVpa(ticketsRequest.getVpa()).getUser().getMerchantName());
            } else {
                ticketRequestDto.setTitle(ticketsRequest.getTicketTitle());
                ticketRequestDto.setEmail_id(this.userTidVpaDataService.findByTid(ticketsRequest.getTid()).getUser().getEmail());
                ticketRequestDto.setPhone(this.userTidVpaDataService.findByTid(ticketsRequest.getTid()).getUser().getMobileNo());
                emailConversationDto.setFrom(this.userTidVpaDataService.findByTid(ticketsRequest.getTid()).getUser().getEmail());
                emailConversationDto.setSubject(ticketsRequest.getTicketTitle());
                emailConversationDto.setPerson_name(this.userTidVpaDataService.findByTid(ticketsRequest.getTid()).getUser().getMerchantName());
            }

            ticketRequestDto.setEmail_conversation(emailConversationDto);
            if (!attachmentName.isEmpty()) {
                logger.info("CREATE TICKET : Tickets mapped attachments : ----------------- : {}", this.attachementDownUrl + attachmentName);
                result.setAttachments(this.attachementDownUrl + attachmentName);
            } else {
                logger.info("CREATE TICKET : Tickets doesn't have mapped attachments : ---- :");
                result.setAttachments("");
            }

            new HashMap();

            try {
                String url = this.kaptureBaseUrl + "add-ticket-from-other-source.html/v.2.0";
                URL obj = new URL(url);
                logger.info("Ticket Creation Kapture CRM URL --{}", url);
                HttpURLConnection conn = (HttpURLConnection)obj.openConnection();
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Basic cDI4Y2N5M205NHpjcDhsMHVpZWVnYjN5Z2NvdHZmamkxOGNnZmt5amNtOThmOG5mMmg='");
                conn.setRequestProperty("Cookie", "JSESSIONID=3CCCC328D5D4BCB4080F6A774BDDCE5B; _KAPTURECRM_SESSION=");
                String data = "[" + String.valueOf(ticketRequestDto) + "]";
                logger.info("CREATE TICKET : Creating Tickets Data: -------------------------- : {}", data);
                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
                out.write(data);
                out.close();
                logger.info("CREATE TICKET : Ticket is created and send to KCRM : -------- : ");
                if (conn.getResponseCode() != 200) {
                    logger.info("CREATE TICKET : Ticket creating is failed : ------------- : ");
                    conn.disconnect();
                    return new ResponseEntity(new ApiResponse(HttpStatus.resolve(conn.getResponseCode()), conn.getResponseMessage()), HttpStatus.resolve(conn.getResponseCode()));
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer response = new StringBuffer();

                String inputLine;
                while((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                logger.info("Ticket Creation Response From Kapture CRM --{}", response);
                in.close();
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> map = (Map)mapper.readValue(response.toString(), Map.class);
                result.setTicketNumber(map.get("ticket_id").toString());
                result.setTicketDueDate(DateUtil.convertStringToDate(map.get("due_date").toString(), "YYYY-MM-DD HH:MM:SS"));
                result.setStatus(((LinkedHashMap)map.get("ticket")).get("statusName").toString());
                this.ticketsService.saveTicketNumber(result);
                logger.info("CREATE TICKET : Ticket is created successfully : -------- : {}", result.getTicketNumber());
                logger.info("End For Create Ticket Api --/tickets/create-ticket");
            } catch (HttpClientErrorException e) {
                logger.info("CREATE TICKET : BadRequest {}", e.getLocalizedMessage());
                return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "BadRequest", e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
            } catch (HttpServerErrorException e) {
                logger.info("CREATE TICKET : error {}", e.getLocalizedMessage());
                return new ResponseEntity(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", e.getLocalizedMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
    }

    @PostMapping({"/tpt/tickets/status-update-callback"})
    public ResponseEntity<?> statusCallback(@RequestBody String requestDto) throws JsonProcessingException {
        logger.info("Start to Update Status CallBack Api ---/tpt/tickets/status-update-callback");
        TicketStatusDto ticketStatusDto = new TicketStatusDto();
        if (requestDto == null) {
            logger.info("Ticket bad request object.!");
            return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "Bad Request"), HttpStatus.OK);
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(requestDto);
            logger.info("STATUS UPDATE CALLBACK : Ticket request DTO : {}", requestDto);
            logger.info("STATUS UPDATE CALLBACK : Matcher found data : {}", jsonNode.get("remark").textValue() != null ? jsonNode.get("remark").textValue() : "NA");
            ticketStatusDto.setStatus(jsonNode.get("status").textValue());
            ticketStatusDto.setTicketNumber(jsonNode.get("ticket-id").textValue());
            String remark = jsonNode.get("remark").textValue() != null ? jsonNode.get("remark").textValue() : "NA";
            ticketStatusDto.setJustiFication(remark);
            logger.info("STATUS UPDATE CALLBACK : Ticket status update API call : TicketId : ---- : {}", jsonNode.get("ticket-id").textValue());
            logger.info("STATUS UPDATE CALLBACK : Ticket status update API call : Status ---- : {}", jsonNode.get("status").textValue());
            logger.info("STATUS UPDATE CALLBACK : Ticket status update API call : Remark---- : {}", remark);
            logger.info("STATUS UPDATE CALLBACK : Ticket status update API call : subStatus---- : {}", jsonNode.get("sub-status").textValue());
            Tickets saved = this.ticketsService.updateTicketStatus(ticketStatusDto);
            if (saved == null) {
                logger.info("STATUS UPDATE CALLBACK : Ticket not found to update status.!");
                return new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "Ticket Not Found"), HttpStatus.OK);
            } else {
                logger.info("STATUS UPDATE CALLBACK : Ticket update status Successfully.!--{}", saved);
                logger.info("End for Update Status CallBack Api ---/tpt/tickets/status-update-callback");
                return new ResponseEntity(new ApiResponse(HttpStatus.OK, "UPDATE TICKET SUCCESS"), HttpStatus.OK);
            }
        }
    }

    @GetMapping({"/tickets/check-tickets"})
    public ResponseEntity<?> checkTickets() throws JSONException {
        logger.info("Start to get Check Tickets Api ----/tickets/check-tickets");
        Integer data = this.ticketsService.updateTicketStatus(this.getSessionUserId(), this.kaptureBaseUrl);
        if (data == 0) {
            logger.info("CHECK TICKETS : Tickets updated successfully : ---- :{} ", data);
            return new ResponseEntity(new ApiResponse(HttpStatus.OK, "UPDATE TICKET SUCCESS"), HttpStatus.OK);
        } else {
            logger.info("CHECK TICKETS : Tickets failed to updated : ---- : ");
            return new ResponseEntity(new ApiResponse(HttpStatus.NO_CONTENT, "DATA NOT FOUND"), HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping({"/tickets/get-tickets-list"})
    public ResponseEntity<?> getTicketsList(@RequestParam Map<String, String> requestParams, Pageable pageable, @RequestParam("search") Optional<String> searchKeyword, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to) throws URISyntaxException {
        try {
            logger.info("Start to get Ticket List Api --/tickets/get-tickets-list");
            pageable = this.webConfig.resolvePageable(requestParams, pageable);
            logger.info("GET TICKETS : Getting the list of tickets of current user : --------------------- : {}", this.getSessionUserId());
            Integer data = this.ticketsService.updateTicketStatus(this.getSessionUserId(), this.kaptureBaseUrl);
            if (data == 0) {
                logger.info("GET TICKETS : Tickets status were updated successfully !--{}", data);
            } else {
                logger.info("GET TICKETS : Tickets status were not updated !");
            }

            Page<HashMap<String, Object>> paged = this.ticketsService.getTicketList(searchKeyword, pageable, from, to, this.getSessionUserId());
            logger.info("End For get Ticket List Api --/tickets/get-tickets-list");
            return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success", paged), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("GET TICKETS : The exception occurred while getting information of aging tracker {}", e.getMessage());
            return new ResponseEntity(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}