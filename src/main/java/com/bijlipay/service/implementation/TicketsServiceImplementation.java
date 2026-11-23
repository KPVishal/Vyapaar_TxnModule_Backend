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
import com.bijlipay.util.Constant;
import com.bijlipay.util.DateUtil;
import com.bijlipay.util.TicketsHandler;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public List<IssuesList> findAllList() {
        return this.issuesListRepository.findAllList();
    }

    public List<Tickets> getByMid(String mid) {
        return this.ticketsRepository.getByMid(mid);
    }

    public List<HashMap<String, String>> searchByTicketNumber(String ticketNumber) {
        return this.ticketsRepository.searchByTicketNumber(ticketNumber);
    }

    @Transactional
    public Tickets createTicket(Tickets newTicket) {
        Tickets result = (Tickets)this.ticketsRepository.save(newTicket);
        return result;
    }

    public String uploadAttachment(MultipartFile file, String fileName) throws IOException {
        Map<String, String> response = this.fileStorageService.storeFile(file, fileName);
        return (String)response.get("fileName");
    }

    public Tickets saveTicketNumber(Tickets tickets) {
        return (Tickets)this.ticketsRepository.save(tickets);
    }

    public Page<HashMap<String, Object>> getTicketList(Optional<String> searchKeyword, Pageable pageable, Optional<Long> from, Optional<Long> to, long sessionUserId) {
        this.logger.info("Parsing the user id for getting the tickets : -------------- : ");
        User user = (User)this.userRepository.findById(sessionUserId).orElseThrow(() -> new RuntimeException("User not found with id: " + sessionUserId));
        this.logger.info("The current User : ----------------------------------------- : {}", user.getMerchantName());
        List<UserTidVpaData> merchantDeatils = this.merchantDetailsService.getByUserId(user);
        new ArrayList();
        new ArrayList();
        if (merchantDeatils.size() <= 0) {
            return null;
        } else {
            List<String> mid = merchantDeatils.stream().filter((m) -> m.getMid() != null).map(UserTidVpaData::getMid).toList();
            List<String> vpa = merchantDeatils.stream().filter((m) -> m.getVpa() != null).map(UserTidVpaData::getVpa).toList();
            this.logger.info("The MID of the current User---------------------:{}, Vpa of the current user------------------------------- : {}", mid, vpa);
            if (mid.size() > 0 && vpa.size() > 0) {
                Page<HashMap<String, Object>> resultPage;
                if (from.isPresent() && to.isPresent() && searchKeyword.isPresent()) {
                    Logger var18 = this.logger;
                    String var23 = String.valueOf(from);
                    var18.info("The from date and To date and Search Keyword : --------------------------------- : {}", var23 + " " + String.valueOf(to) + " " + String.valueOf(searchKeyword));
                    resultPage = this.ticketsRepository.searchListOphPaged(DateUtil.longtoDate((Long)from.get()), DateUtil.longtoDate((Long)to.get()), (String)searchKeyword.get(), mid, vpa, pageable);
                } else if (from.isPresent() && to.isPresent()) {
                    Logger var17 = this.logger;
                    String var22 = String.valueOf(from);
                    var17.info("The from date and To date : --------------------------------- : {}", var22 + " " + String.valueOf(to));
                    resultPage = this.ticketsRepository.searchListOphPaged(DateUtil.longtoDate((Long)from.get()), DateUtil.longtoDate((Long)to.get()), mid, vpa, pageable);
                } else if (searchKeyword.isPresent()) {
                    this.logger.info("Search Keyword : --------------------------------- : {}", searchKeyword);
                    resultPage = this.ticketsRepository.searchListOphPaged((String)searchKeyword.get(), mid, vpa, pageable);
                } else {
                    resultPage = this.ticketsRepository.findListOphPaged(mid, vpa, pageable);
                    this.logger.info("Plain Search : --------------------------------- : {}", resultPage);
                }

                return resultPage;
            } else if (mid.size() > 0) {
                Page<HashMap<String, Object>> resultPage;
                if (from.isPresent() && to.isPresent() && searchKeyword.isPresent()) {
                    Logger var16 = this.logger;
                    String var21 = String.valueOf(from);
                    var16.info("The from date and To date and Search Keyword : --------------------------------- : {}", var21 + " " + String.valueOf(to) + " " + String.valueOf(searchKeyword));
                    resultPage = this.ticketsRepository.searchListOphPagedMid(DateUtil.longtoDate((Long)from.get()), DateUtil.longtoDate((Long)to.get()), (String)searchKeyword.get(), mid, pageable);
                } else if (from.isPresent() && to.isPresent()) {
                    Logger var15 = this.logger;
                    String var20 = String.valueOf(from);
                    var15.info("The from date and To date : --------------------------------- : {}", var20 + " " + String.valueOf(to));
                    resultPage = this.ticketsRepository.searchListOphPagedMid(DateUtil.longtoDate((Long)from.get()), DateUtil.longtoDate((Long)to.get()), mid, pageable);
                } else if (searchKeyword.isPresent()) {
                    this.logger.info("Search Keyword : --------------------------------- : {}", searchKeyword);
                    resultPage = this.ticketsRepository.searchListOphPagedMid((String)searchKeyword.get(), mid, pageable);
                } else {
                    resultPage = this.ticketsRepository.findListOphPagedMid(mid, pageable);
                    this.logger.info("Plain Search : --------------------------------- : {}", resultPage);
                }

                return resultPage;
            } else if (vpa.size() <= 0) {
                return null;
            } else {
                Page<HashMap<String, Object>> resultPage;
                if (from.isPresent() && to.isPresent() && searchKeyword.isPresent()) {
                    Logger var14 = this.logger;
                    String var19 = String.valueOf(from);
                    var14.info("The from date and To date and Search Keyword : --------------------------------- : {}", var19 + " " + String.valueOf(to) + " " + String.valueOf(searchKeyword));
                    resultPage = this.ticketsRepository.searchListOphPagedVpa(DateUtil.longtoDate((Long)from.get()), DateUtil.longtoDate((Long)to.get()), (String)searchKeyword.get(), vpa, pageable);
                } else if (from.isPresent() && to.isPresent()) {
                    Logger var10000 = this.logger;
                    String var10002 = String.valueOf(from);
                    var10000.info("The from date and To date : --------------------------------- : {}", var10002 + " " + String.valueOf(to));
                    resultPage = this.ticketsRepository.searchListOphPagedVpa(DateUtil.longtoDate((Long)from.get()), DateUtil.longtoDate((Long)to.get()), vpa, pageable);
                } else if (searchKeyword.isPresent()) {
                    this.logger.info("Search Keyword : --------------------------------- : {}", searchKeyword);
                    resultPage = this.ticketsRepository.searchListOphPagedVpa((String)searchKeyword.get(), vpa, pageable);
                } else {
                    resultPage = this.ticketsRepository.findListOphPagedVpa(vpa, pageable);
                    this.logger.info("Plain Search : --------------------------------- : {}", resultPage);
                }

                return resultPage;
            }
        }
    }

    public Integer updateTicketStatus(long sessionUserId, String kaptureBaseUrl) throws JSONException {
        User user = (User)this.userRepository.getOne(sessionUserId);
        List<UserTidVpaData> merchantDeatils = this.merchantDetailsService.getByUserId(user);
        new ArrayList();
        new ArrayList();
        if (merchantDeatils.size() <= 0) {
            return -1;
        } else {
            List<String> mid = merchantDeatils.stream().filter((m) -> m.getMid() != null).map(UserTidVpaData::getMid).toList();
            List<String> vpa = merchantDeatils.stream().filter((m) -> m.getVpa() != null).map(UserTidVpaData::getVpa).toList();
            this.logger.info("User Mapped Mid --{} -------- Vpa {}", mid, vpa);
            List<String> ticketNumberList = this.getTickeNumberByMid(mid, vpa);
            if (ticketNumberList != null && !ticketNumberList.isEmpty()) {
                String tickNumbers = String.join(",", ticketNumberList);
                TicketsHandler ticketsHandler = new TicketsHandler();
                String url = kaptureBaseUrl + "search-ticket-by-ticket-id.html/v.2.0";
                this.logger.info("Getting Tickets List Kapture CRM URL --{}", url);
                String AuthKey = "Basic djRvZDkyejV3N3JtbnRiN2dmcWFydWRsanIwNWh6YTFhZWxoNWYzdmdhc2Y1djFiZHI='";
                String Cookies = "JSESSIONID=3F9F2F65A0B2219C5138372D21D8D7B3; _KAPTURECRM_SESSION='";
                String data = "{\"ticket_ids\":\"" + tickNumbers + "\"}";
                StringBuilder response = ticketsHandler.kaptureApiPullStatus(url, AuthKey, Cookies, data);
                if (response == null) {
                    return -1;
                } else {
                    JSONArray jsonArray = new JSONArray(response.toString());
                    new Gson();

                    for(int i = 0; i < jsonArray.length(); ++i) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (jsonObject.has("task_details") && !jsonObject.isNull("task_details")) {
                            JSONObject taskDetails = new JSONObject(jsonObject.get("task_details").toString());
                            String ticketNumber = taskDetails.optString("ticketId", (String)null);
                            String ticketStatus = taskDetails.optString("status", (String)null);
                            if (ticketNumber != null && !ticketNumber.isEmpty()) {
                                if (ticketStatus != null && !ticketStatus.isEmpty()) {
                                    this.ticketsRepository.updateTicketStatus(ticketNumber, ticketStatus);
                                } else {
                                    this.logger.warn("Skipping Kapture CRM entry [{}] — 'status' is missing or empty in task_details for ticketId: {}", i, ticketNumber);
                                }
                            } else {
                                this.logger.warn("Skipping Kapture CRM entry [{}] — 'ticketId' is missing or empty in task_details.", i);
                            }
                        } else {
                            this.logger.warn("Skipping Kapture CRM response entry [{}] — missing 'task_details': {}", i, jsonObject);
                        }
                    }

                    return 0;
                }
            } else {
                this.logger.info("No ticket numbers found for mid/vpa — skipping Kapture CRM sync.");
                return -1;
            }
        }
    }

    public Tickets updateTicketStatus(TicketStatusDto ticketStatusDto) {
        this.logger.info("My Ticket id to update the status of tickets : ---- : {} : Status : {}", ticketStatusDto.getTicketNumber(), ticketStatusDto.getStatus());
        Tickets result = this.ticketsRepository.findByTicketNumber(ticketStatusDto.getTicketNumber());
        if (result == null) {
            this.logger.info("UPDATE TICKET : Ticket not found ! : ----------- : ");
            return null;
        } else {
            if (ticketStatusDto.getStatus() != null && !ticketStatusDto.getStatus().isEmpty()) {
                result.setStatus(ticketStatusDto.getStatus());
            }

            if (ticketStatusDto.getJustiFication() != null && !ticketStatusDto.getJustiFication().isEmpty()) {
                result.setJustiFication(ticketStatusDto.getJustiFication());
            }

            Tickets saved = (Tickets)this.ticketsRepository.save(result);
            if (saved == null) {
                this.logger.info("UPDATE TICKET : Ticket update failed ! : ----------- : ");
                return null;
            } else {
                if (!saved.isFederalFlag()) {
                    this.logger.info("Notification Data Creation Started : ---- : {}", saved);
                    UserTidVpaData merchantDeatils = this.merchantDetailsService.findByTid(saved.getTid());
                    this.logger.info("Merchant details user : ------------------- : {}", merchantDeatils.getUser().getId());
                    User user = (User)this.userRepository.getOne(merchantDeatils.getUser().getId());
                    this.logger.info("User details : ---------------------------- : {}", user.getEmail());
                    Category category = new Category();
                    category.setCategoryType(5);
                    category.setDescription("Ticket [" + saved.getTicketNumber() + "] Status updated");
                    category.setUserTidVpaData(merchantDeatils);
                    category.setUser(user);
                    this.logger.info("Category : -------------------------------- : {}", category.getDescription());
                    Category finalResponse = (Category)this.categoryRepository.save(category);
                    this.logger.info("FinalResp : ------------------------------- : {}", finalResponse.getDescription());
                    String redirectTo = Constant.commonType(category.getCategoryType());
                    Logger var10000 = this.logger;
                    String var10002 = result.getTid();
                    var10000.info("Notification data push : title       : ---- : {}", var10002 + "_" + redirectTo);
                    this.logger.info("Notification data push : User        : ---- : {}", user.getEmail());
                    this.logger.info("Notification data push : Reason      : ---- : {}", finalResponse.getDescription());
                    this.logger.info("Notification data push : Final Resp  : ---- : {}", finalResponse.getCategoryType());
                    this.logger.info("Notification data push : Redirect To : ---- : {}", redirectTo);
                    this.sendPushNotificationGenerate(result.getTid() + "_" + redirectTo, finalResponse.getDescription(), finalResponse, redirectTo, user);
                    this.logger.info("Notification Data Creation Completed : ---- :");
                }

                return saved;
            }
        }
    }

    public List<String> getTickeNumberByMid(List<String> mId, List<String> vpa) {
        Date lastThreeMonth = DateUtil.lastThreeMonthDate();
        Date currentDate = DateUtil.currentDate();
        this.logger.info("Dates For Getting Ticket ---{}---{}", lastThreeMonth, currentDate);
        if (mId.size() > 0 && vpa.size() > 0) {
            return this.ticketsRepository.searchTicketNumberByMidVpa(mId, vpa, lastThreeMonth, currentDate);
        } else if (mId.size() > 0) {
            return this.ticketsRepository.searchTicketNumberByMid(mId, lastThreeMonth, currentDate);
        } else {
            return vpa.size() > 0 ? this.ticketsRepository.searchTicketNumberByVpa(vpa, lastThreeMonth, currentDate) : null;
        }
    }
}