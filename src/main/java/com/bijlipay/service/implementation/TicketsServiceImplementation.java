package com.bijlipay.service.implementation;

import com.bijlipay.controller.BaseController;
import com.bijlipay.model.Category;
import com.bijlipay.model.User;
import com.bijlipay.model.UserTidVpaData;
import com.bijlipay.model.dto.TicketStatusDto;
import com.bijlipay.model.tickets.IssuesList;
import com.bijlipay.model.tickets.Tickets;
import com.bijlipay.repository.CategoryRepository;
import com.bijlipay.repository.IssuesListRepository;
import com.bijlipay.repository.TicketsRepository;
import com.bijlipay.repository.UserRepository;
import com.bijlipay.service.FileStorageService;
import com.bijlipay.service.TicketsService;
import com.bijlipay.service.UserTidVpaDataService;
import com.bijlipay.util.DateUtil;
import com.bijlipay.util.TicketsHandler;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.bijlipay.util.Constant.TICKET_ID;
import static com.bijlipay.util.Constant.commonType;

@Service
public class TicketsServiceImplementation extends BaseController implements TicketsService {
    private final Logger logger = LoggerFactory.getLogger(TicketsServiceImplementation.class);
    @Autowired
    TicketsRepository ticketsRepository;
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    private UserTidVpaDataService merchantDetailsService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    IssuesListRepository issuesListRepository;

    @Override
    public List<IssuesList> findAllList() {
        return issuesListRepository.findAllList();
    }

    @Override
    public List<Tickets> getByMid(String mid) {
        return ticketsRepository.getByMid(mid);
    }

    @Override
    public List<HashMap<String, String>> searchByTicketNumber(String ticketNumber) {
        return ticketsRepository.searchByTicketNumber(ticketNumber);
    }

    @Transactional
    @Override
    public Tickets createTicket(Tickets newTicket) {
        Tickets result = ticketsRepository.save(newTicket);
        return result;
    }

    @Override
    public String uploadAttachment(MultipartFile file, String fileName) throws IOException {
        Map<String, String> response = fileStorageService.storeFile(file, fileName);
        return response.get("fileName");
    }

    @Override
    public Tickets saveTicketNumber(Tickets tickets) {
        return ticketsRepository.save(tickets);
    }

    @Override
    public Page<HashMap<String, Object>> getTicketList(Optional<String> searchKeyword, Pageable pageable, Optional<Long> from, Optional<Long> to, long sessionUserId) {
        logger.info("Parsing the user id for getting the tickets : -------------- : ");
//        User user = userRepository.getOne(sessionUserId);
//        logger.info("The current User : ----------------------------------------- : {}", user.getMerchantName());
        User user = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + sessionUserId));

        logger.info("The current User : ----------------------------------------- : {}", user.getMerchantName());

        List<UserTidVpaData> merchantDeatils = merchantDetailsService.getByUserId(user);
        List<String> mid = new ArrayList<>();
        List<String> vpa = new ArrayList<>();

        if (merchantDeatils.size() <= 0) {
            return null;
        } else {
//            cMid = merchantDeatils.stream().map(UserTidVpaData::getMid).collect(Collectors.toList());
            mid = merchantDeatils.stream().filter(m -> m.getMid() != null).map(UserTidVpaData::getMid).toList();
            vpa = merchantDeatils.stream().filter(m -> m.getVpa() != null).map(UserTidVpaData::getVpa).toList();
        }
        logger.info("The MID of the current User---------------------:{}, Vpa of the current user------------------------------- : {}", mid,vpa);
        Page<HashMap<String, Object>> resultPage;
        if (mid.size()>0 && vpa.size()>0) {
            if (from.isPresent() && to.isPresent() && searchKeyword.isPresent()) {
                logger.info("The from date and To date and Search Keyword : --------------------------------- : {}", from + " " + to + " " + searchKeyword);
                resultPage = ticketsRepository.searchListOphPaged(DateUtil.longtoDate(from.get()), DateUtil.longtoDate(to.get()), searchKeyword.get(), mid, vpa, pageable);
            } else if (from.isPresent() && to.isPresent()) {
                logger.info("The from date and To date : --------------------------------- : {}", from + " " + to);
                resultPage = ticketsRepository.searchListOphPaged(DateUtil.longtoDate(from.get()), DateUtil.longtoDate(to.get()), mid, vpa, pageable);
            } else if (searchKeyword.isPresent()) {
                logger.info("Search Keyword : --------------------------------- : {}", searchKeyword);
                resultPage = ticketsRepository.searchListOphPaged(searchKeyword.get(), mid, vpa, pageable);
            } else {
                resultPage = ticketsRepository.findListOphPaged(mid, vpa, pageable);
                logger.info("Plain Search : --------------------------------- : {}", resultPage);
            }
            return resultPage;
        }else if (mid.size()>0){
            if (from.isPresent() && to.isPresent() && searchKeyword.isPresent()) {
                logger.info("The from date and To date and Search Keyword : --------------------------------- : {}", from + " " + to + " " + searchKeyword);
                resultPage = ticketsRepository.searchListOphPagedMid(DateUtil.longtoDate(from.get()), DateUtil.longtoDate(to.get()), searchKeyword.get(), mid, pageable);
            } else if (from.isPresent() && to.isPresent()) {
                logger.info("The from date and To date : --------------------------------- : {}", from + " " + to);
                resultPage = ticketsRepository.searchListOphPagedMid(DateUtil.longtoDate(from.get()), DateUtil.longtoDate(to.get()), mid, pageable);
            } else if (searchKeyword.isPresent()) {
                logger.info("Search Keyword : --------------------------------- : {}", searchKeyword);
                resultPage = ticketsRepository.searchListOphPagedMid(searchKeyword.get(), mid, pageable);
            } else {
                resultPage = ticketsRepository.findListOphPagedMid(mid, pageable);
                logger.info("Plain Search : --------------------------------- : {}", resultPage);
            }
            return resultPage;
        }else if (vpa.size()>0){
            if (from.isPresent() && to.isPresent() && searchKeyword.isPresent()) {
                logger.info("The from date and To date and Search Keyword : --------------------------------- : {}", from + " " + to + " " + searchKeyword);
                resultPage = ticketsRepository.searchListOphPagedVpa(DateUtil.longtoDate(from.get()), DateUtil.longtoDate(to.get()), searchKeyword.get(), vpa, pageable);
            } else if (from.isPresent() && to.isPresent()) {
                logger.info("The from date and To date : --------------------------------- : {}", from + " " + to);
                resultPage = ticketsRepository.searchListOphPagedVpa(DateUtil.longtoDate(from.get()), DateUtil.longtoDate(to.get()), vpa, pageable);
            } else if (searchKeyword.isPresent()) {
                logger.info("Search Keyword : --------------------------------- : {}", searchKeyword);
                resultPage = ticketsRepository.searchListOphPagedVpa(searchKeyword.get(), vpa, pageable);
            } else {
                resultPage = ticketsRepository.findListOphPagedVpa(vpa, pageable);
                logger.info("Plain Search : --------------------------------- : {}", resultPage);
            }
            return resultPage;
        }
        return null;
    }

    @Override
    public Integer updateTicketStatus(long sessionUserId, String kaptureBaseUrl) throws JSONException {
        User user = userRepository.getOne(sessionUserId);
        List<UserTidVpaData> merchantDeatils = merchantDetailsService.getByUserId(user);
        List<String> mid = new ArrayList<>();
        List<String> vpa = new ArrayList<>();
        if (merchantDeatils.size() <= 0) {
            return -1;
        } else {
            mid = merchantDeatils.stream().filter(m -> m.getMid() != null).map(UserTidVpaData::getMid).toList();
            vpa = merchantDeatils.stream().filter(m -> m.getVpa() != null).map(UserTidVpaData::getVpa).toList();
        }
        logger.info("User Mapped Mid --{} -------- Vpa {}", mid, vpa);

        // FIX: getTickeNumberByMid() returns null when both mid and vpa are empty.
        // Wrapping in a null-safe list prevents NullPointerException in String.join().
        List<String> ticketNumberList = getTickeNumberByMid(mid, vpa);
        if (ticketNumberList == null || ticketNumberList.isEmpty()) {
            logger.info("No ticket numbers found for mid/vpa — skipping Kapture CRM sync.");
            return -1;
        }
        String tickNumbers = String.join(",", ticketNumberList);

        TicketsHandler ticketsHandler = new TicketsHandler();
        String url = kaptureBaseUrl + "search-ticket-by-ticket-id.html/v.2.0";
        logger.info("Getting Tickets List Kapture CRM URL --{}", url);
        String AuthKey = "Basic djRvZDkyejV3N3JtbnRiN2dmcWFydWRsanIwNWh6YTFhZWxoNWYzdmdhc2Y1djFiZHI='";
        String Cookies = "JSESSIONID=3F9F2F65A0B2219C5138372D21D8D7B3; _KAPTURECRM_SESSION='";
        String data = "{" + "\"ticket_ids\":" + "\"" + tickNumbers + "\"" + "}";
        // Cast response into string buffer
        StringBuilder response = ticketsHandler.kaptureApiPullStatus(url, AuthKey, Cookies, data);
        if (response != null) {
            JSONArray jsonArray = new JSONArray(response.toString());
            Gson gson = new Gson();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // FIX: Kapture CRM sometimes returns ticket objects without a
                // "task_details" key (e.g., deleted/cancelled/invalid tickets).
                // Previously JSONObject.get("task_details") threw:
                //   JSONException: No value for task_details
                // which aborted the entire loop and surfaced as:
                //   "The exception occurred while getting information of aging tracker"
                // Now we skip such entries with a warning and continue processing
                // the remaining tickets in the array.
                if (!jsonObject.has("task_details") || jsonObject.isNull("task_details")) {
                    logger.warn("Skipping Kapture CRM response entry [{}] — missing 'task_details': {}", i, jsonObject);
                    continue;
                }

                JSONObject taskDetails = new JSONObject(jsonObject.get("task_details").toString());

                // FIX: Use optString() instead of get() so that a missing "ticketId"
                // or "status" key returns null rather than throwing a JSONException.
                String ticketNumber = taskDetails.optString("ticketId", null);
                String ticketStatus = taskDetails.optString("status", null);

                if (ticketNumber == null || ticketNumber.isEmpty()) {
                    logger.warn("Skipping Kapture CRM entry [{}] — 'ticketId' is missing or empty in task_details.", i);
                    continue;
                }
                if (ticketStatus == null || ticketStatus.isEmpty()) {
                    logger.warn("Skipping Kapture CRM entry [{}] — 'status' is missing or empty in task_details for ticketId: {}", i, ticketNumber);
                    continue;
                }

                ticketsRepository.updateTicketStatus(ticketNumber, ticketStatus);
            }
        } else {
            return -1;
        }
        return 0;
    }

    @Override
    public Tickets updateTicketStatus(TicketStatusDto ticketStatusDto) {
        logger.info("My Ticket id to update the status of tickets : ---- : {} : Status : {}", ticketStatusDto.getTicketNumber(), ticketStatusDto.getStatus());
        Tickets result = ticketsRepository.findByTicketNumber(ticketStatusDto.getTicketNumber());
        if (result == null) {
            logger.info("UPDATE TICKET : Ticket not found ! : ----------- : ");
            return null;
        }
        if (ticketStatusDto.getStatus() != null && !ticketStatusDto.getStatus().isEmpty()) {
            result.setStatus(ticketStatusDto.getStatus());
        }
        if (ticketStatusDto.getJustiFication() != null && !ticketStatusDto.getJustiFication().isEmpty()) {
            result.setJustiFication(ticketStatusDto.getJustiFication());
        }
        Tickets saved = ticketsRepository.save(result);
        if (saved == null) {
            logger.info("UPDATE TICKET : Ticket update failed ! : ----------- : ");
            return null;
        }
        if(!saved.isFederalFlag()) {
            logger.info("Notification Data Creation Started : ---- : {}", saved);
            UserTidVpaData merchantDeatils = merchantDetailsService.findByTid(saved.getTid());
            logger.info("Merchant details user : ------------------- : {}", merchantDeatils.getUser().getId());
            User user = userRepository.getOne(merchantDeatils.getUser().getId());
            logger.info("User details : ---------------------------- : {}", user.getEmail());
            Category category = new Category();
            category.setCategoryType(TICKET_ID);
            category.setDescription("Ticket [" + saved.getTicketNumber() + "] Status updated");
            category.setUserTidVpaData(merchantDeatils);
            category.setUser(user);
            logger.info("Category : -------------------------------- : {}", category.getDescription());
            Category finalResponse = categoryRepository.save(category);
            logger.info("FinalResp : ------------------------------- : {}", finalResponse.getDescription());
            String redirectTo = commonType(category.getCategoryType());
            logger.info("Notification data push : title       : ---- : {}", result.getTid() + "_" + redirectTo);
            logger.info("Notification data push : User        : ---- : {}", user.getEmail());
            logger.info("Notification data push : Reason      : ---- : {}", finalResponse.getDescription());
            logger.info("Notification data push : Final Resp  : ---- : {}", finalResponse.getCategoryType());
            logger.info("Notification data push : Redirect To : ---- : {}", redirectTo);
            sendPushNotificationGenerate(result.getTid() + "_" + redirectTo, finalResponse.getDescription(), finalResponse, redirectTo, user);
            logger.info("Notification Data Creation Completed : ---- :");
        }
        return saved;
    }

    @Override
    public List<String> getTickeNumberByMid(List<String> mId,List<String> vpa) {
        Date lastThreeMonth = DateUtil.lastThreeMonthDate();
        Date currentDate = DateUtil.currentDate();
        logger.info("Dates For Getting Ticket ---{}---{}", lastThreeMonth, currentDate);
        if (mId.size()>0 && vpa.size() >0) {
            return ticketsRepository.searchTicketNumberByMidVpa(mId, vpa, lastThreeMonth, currentDate);
        }else if (mId.size()>0){
            return ticketsRepository.searchTicketNumberByMid(mId, lastThreeMonth, currentDate);
        }else if (vpa.size()>0){
            return ticketsRepository.searchTicketNumberByVpa(vpa, lastThreeMonth, currentDate);
        }
        return null;
    }
}
