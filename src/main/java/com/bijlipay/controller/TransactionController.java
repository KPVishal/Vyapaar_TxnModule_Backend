package com.bijlipay.controller;

import com.bijlipay.config.WebConfig;
import com.bijlipay.model.Category;
import com.bijlipay.model.Invoice;
import com.bijlipay.model.SettlementRequestResponse;
import com.bijlipay.model.User;
import com.bijlipay.model.UserTidVpaData;
import com.bijlipay.model.dto.AxisQrTransactionDto;
import com.bijlipay.model.dto.ChargeSlipDto;
import com.bijlipay.model.dto.SettlementNotificationDto;
import com.bijlipay.model.dto.SettlementReportQrDto;
import com.bijlipay.model.dto.SettlementReportsDto;
import com.bijlipay.model.dto.SwitchTransactionsDto;
import com.bijlipay.model.dto.TidAmount;
import com.bijlipay.model.dto.TransactionRequestDto;
import com.bijlipay.model.dto.notification.SettlementNotificationUpdate;
import com.bijlipay.repository.SettlementRequestResponseRepository;
import com.bijlipay.repository.UserRepository;
import com.bijlipay.service.BijlipayTransactionService;
import com.bijlipay.service.CategoryService;
import com.bijlipay.service.FileStorageService;
import com.bijlipay.service.UserTidVpaDataService;
import com.bijlipay.util.ApiResponse;
import com.bijlipay.util.DateUtil;
import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

@RestController
@RequestMapping({"/txn"})
public class TransactionController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserTidVpaDataService userTidVpaDataService;
    @Autowired
    private BijlipayTransactionService bijlipayTransactionService;
    @Value("${settlement.report.url}")
    private String settlementReportUrl;
    @Value("${static.settlement.report.url}")
    private String staticQRSettlementReportUrl;
    @Value("${axis.settlement.report.url}")
    private String axisSettlementReportUrl;
    @Value("${mars.nii}")
    private String nIi;
    @Value("${axis.base.url}")
    private String axisBaseUrl;
    @Value("${axis.missing.data.file}")
    private String axisMissingDataFilePath;
    @Value("${chargeslip.db}")
    private String chargeSlipDb;
    @Autowired
    private WebConfig webConfig;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private S3Client s3Client;
    @Value("${amazon.bucket.name}")
    private String amazonBucketName;
    @Autowired
    private SettlementRequestResponseRepository settlementRequestResponseRepository;

    @Value("${yesbank.settlement.report.url}")
    private String yesbankSettlementReportUrl;

    @Transactional(
            readOnly = true
    )
    @GetMapping({"/fetch-transaction-summary"})
    public ResponseEntity<?> fetchTransactionSummary(@RequestParam("tid") Optional<String> tid, @RequestParam("vpa") Optional<String> vpa, HttpServletRequest httpServletRequest) throws URISyntaxException, IOException, SQLException {
        this.logger.info("Start to Fetch Transaction Summary Api ----/api/fetch-transaction-summary/{tid}  ---Tid ---{}", tid);
        System.out.println("path " + httpServletRequest.getServletPath());
        User user = (User)this.userRepository.getOne(this.getSessionUserId());
        if (user != null) {
            Map<String, Object> map = new HashMap();
            List<Object> output = new ArrayList();
            List<Object> vpaObject = new ArrayList();
            List<Object> voidRevObj = new ArrayList();
            List<SwitchTransactionsDto> posTransactions = new ArrayList();
            List<AxisQrTransactionDto> qrTransactions = new ArrayList();
            if (tid.isPresent() && vpa.isPresent()) {
                UserTidVpaData merchantDeatils = this.userTidVpaDataService.findByTid((String)tid.get());
                if (merchantDeatils != null) {
                    List<Object[]> sumOfVoidReversalTxnAmount = this.bijlipayTransactionService.getSumOfTodayAndYesterdayVoidAndReversalTxnAmount((String)tid.get(), this.getSessionUserId());
                    List<Object[]> TodayAndYesterdaySumCountForTid = this.bijlipayTransactionService.getSumAndCountOfTodayAndYesterdayTxnAmountForTid((String)tid.get(), this.getSessionUserId());
                    List<Object[]> TodayAndYesterdaySumCountForVpa = this.bijlipayTransactionService.getSumAndCountOfTodayAndYesterdayTxnAmountForVpa((String)vpa.get(), this.getSessionUserId());
                    List<SwitchTransactionsDto> var16 = this.bijlipayTransactionService.getLastFiveSwicthTransactions((String)tid.get());
                    List<AxisQrTransactionDto> var18 = this.bijlipayTransactionService.getLastFiveAxisQrTransactions((String)vpa.get());
                    TodayAndYesterdaySumCountForTid.forEach((l) -> {
                        Map<String, Object> maps = new HashMap();
                        maps.put("todaySum", l[0]);
                        maps.put("todayCount", l[1]);
                        maps.put("yesterdaySum", l[2]);
                        maps.put("yesterdayCount", l[3]);
                        output.add(maps);
                    });
                    TodayAndYesterdaySumCountForVpa.forEach((l) -> {
                        Map<String, Object> maps = new HashMap();
                        maps.put("todaySum", l[0]);
                        maps.put("todayCount", l[1]);
                        maps.put("yesterdaySum", l[2]);
                        maps.put("yesterdayCount", l[3]);
                        vpaObject.add(maps);
                    });
                    sumOfVoidReversalTxnAmount.forEach((l) -> {
                        Map<String, Object> maps = new HashMap();
                        maps.put("todayVoidReversalSum", l[0]);
                        maps.put("todayVoidReversalCount", l[1]);
                        maps.put("yesterdayVoidReversalSum", l[2]);
                        maps.put("yesterdayVoidReversalCount", l[3]);
                        voidRevObj.add(maps);
                    });
                    map.put("voidReversal", voidRevObj);
                    map.put("tid", output);
                    map.put("staticQR", vpaObject);
                    map.put("lastFiveTxnAmount", var16);
                    map.put("lastFiveTxnAmountForQr", var18);
                    this.logger.info("End For Fetch Transaction Summary Api ----/api/fetch-transaction-summary/{tid}");
                    return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success", map), HttpStatus.OK);
                }
            } else if (tid.isPresent()) {
                UserTidVpaData merchantDeatils = this.userTidVpaDataService.findByTid((String)tid.get());
                if (merchantDeatils != null) {
                    List<Object[]> sumOfVoidReversalTxnAmount = this.bijlipayTransactionService.getSumOfTodayAndYesterdayVoidAndReversalTxnAmount((String)tid.get(), this.getSessionUserId());
                    List<Object[]> TodayAndYesterdaySumCountForTid = this.bijlipayTransactionService.getSumAndCountOfTodayAndYesterdayTxnAmountForTid((String)tid.get(), this.getSessionUserId());
                    List<SwitchTransactionsDto> var15 = this.bijlipayTransactionService.getLastFiveSwicthTransactions((String)tid.get());
                    TodayAndYesterdaySumCountForTid.forEach((l) -> {
                        Map<String, Object> maps = new HashMap();
                        maps.put("todaySum", l[0]);
                        maps.put("todayCount", l[1]);
                        maps.put("yesterdaySum", l[2]);
                        maps.put("yesterdayCount", l[3]);
                        output.add(maps);
                    });
                    sumOfVoidReversalTxnAmount.forEach((l) -> {
                        Map<String, Object> maps = new HashMap();
                        maps.put("todayVoidReversalSum", l[0]);
                        maps.put("todayVoidReversalCount", l[1]);
                        maps.put("yesterdayVoidReversalSum", l[2]);
                        maps.put("yesterdayVoidReversalCount", l[3]);
                        voidRevObj.add(maps);
                    });
                    map.put("voidReversal", voidRevObj);
                    map.put("tid", output);
                    map.put("staticQR", vpaObject);
                    map.put("lastFiveTxnAmount", var15);
                    map.put("lastFiveTxnAmountForQr", qrTransactions);
                    this.logger.info("End For Fetch Transaction Summary Api ----/api/fetch-transaction-summary/{tid}");
                    return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success", map), HttpStatus.OK);
                }
            } else if (vpa.isPresent()) {
                this.userTidVpaDataService.findByVpa(user);
                List<Object[]> TodayAndYesterdaySumCountForVpa = this.bijlipayTransactionService.getSumAndCountOfTodayAndYesterdayTxnAmountForVpa((String)vpa.get(), this.getSessionUserId());
                List<AxisQrTransactionDto> var17 = this.bijlipayTransactionService.getLastFiveAxisQrTransactions((String)vpa.get());
                TodayAndYesterdaySumCountForVpa.forEach((l) -> {
                    Map<String, Object> maps = new HashMap();
                    maps.put("todaySum", l[0]);
                    maps.put("todayCount", l[1]);
                    maps.put("yesterdaySum", l[2]);
                    maps.put("yesterdayCount", l[3]);
                    vpaObject.add(maps);
                });
                map.put("voidReversal", voidRevObj);
                map.put("tid", output);
                map.put("staticQR", vpaObject);
                map.put("lastFiveTxnAmount", posTransactions);
                map.put("lastFiveTxnAmountForQr", var17);
                this.logger.info("End For Fetch Transaction Summary Api ----/api/fetch-transaction-summary/{tid}");
                return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success", map), HttpStatus.OK);
            }
        }

        return new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "Merchant not found!"), HttpStatus.NOT_FOUND);
    }

    @Transactional(
            readOnly = true
    )
    @GetMapping({"/fetch-transaction-summary-portal"})
    public ResponseEntity<?> fetchTransactionSummaryForPortal(@RequestParam("tid") Optional<String> tid, @RequestParam("vpa") Optional<String> vpa, HttpServletRequest httpServletRequest) throws URISyntaxException, IOException, SQLException {
        this.logger.info("Start to Fetch Transaction Summary Api ----/api/fetch-transaction-summary/{tid}  ---Tid ---{}", tid);
        System.out.println("path " + httpServletRequest.getServletPath());
        User user = (User)this.userRepository.getOne(this.getSessionUserId());
        if (user != null) {
            List<String> tidList = this.userTidVpaDataService.fetchTidData(user);
            List<String> vpaList = this.userTidVpaDataService.findByVpa(user);
            Map<String, Object> map = new HashMap();
            List<Object> output = new ArrayList();
            List<Object> vpaObject = new ArrayList();
            List<Object> voidRevObj = new ArrayList();
            List<SwitchTransactionsDto> posTransactions = new ArrayList();
            List<AxisQrTransactionDto> qrTransactions = new ArrayList();
            if (tid.isPresent() && vpa.isPresent()) {
                UserTidVpaData merchantDeatils = this.userTidVpaDataService.findByTid((String)tid.get());
                if (merchantDeatils != null) {
                    List<Object[]> sumOfVoidReversalTxnAmount = this.bijlipayTransactionService.getSumOfTodayAndYesterdayVoidAndReversalTxnAmount((String)tid.get(), this.getSessionUserId());
                    List<Object[]> TodayAndYesterdaySumCountForTid = this.bijlipayTransactionService.getSumAndCountOfTodayAndYesterdayTxnAmountForTid((String)tid.get(), this.getSessionUserId());
                    List<Object[]> TodayAndYesterdaySumCountForVpa = this.bijlipayTransactionService.getSumAndCountOfTodayAndYesterdayTxnAmountForVpa((String)vpa.get(), this.getSessionUserId());
                    List<SwitchTransactionsDto> var18 = this.bijlipayTransactionService.getLastFiveSwicthTransactionsBasedOnUserPortal(tidList);
                    List<AxisQrTransactionDto> var20 = this.bijlipayTransactionService.getLastFiveAxisQrTransactionsBasedOnUserPortal(vpaList);
                    TodayAndYesterdaySumCountForTid.forEach((l) -> {
                        Map<String, Object> maps = new HashMap();
                        maps.put("todaySum", l[0]);
                        maps.put("todayCount", l[1]);
                        maps.put("yesterdaySum", l[2]);
                        maps.put("yesterdayCount", l[3]);
                        output.add(maps);
                    });
                    TodayAndYesterdaySumCountForVpa.forEach((l) -> {
                        Map<String, Object> maps = new HashMap();
                        maps.put("todaySum", l[0]);
                        maps.put("todayCount", l[1]);
                        maps.put("yesterdaySum", l[2]);
                        maps.put("yesterdayCount", l[3]);
                        vpaObject.add(maps);
                    });
                    sumOfVoidReversalTxnAmount.forEach((l) -> {
                        Map<String, Object> maps = new HashMap();
                        maps.put("todayVoidReversalSum", l[0]);
                        maps.put("todayVoidReversalCount", l[1]);
                        maps.put("yesterdayVoidReversalSum", l[2]);
                        maps.put("yesterdayVoidReversalCount", l[3]);
                        voidRevObj.add(maps);
                    });
                    map.put("voidReversal", voidRevObj);
                    map.put("tid", output);
                    map.put("staticQR", vpaObject);
                    map.put("lastFiveTxnAmount", var18);
                    map.put("lastFiveTxnAmountForQr", var20);
                    this.logger.info("End For Fetch Transaction Summary Api ----/api/fetch-transaction-summary/{tid}");
                    return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success", map), HttpStatus.OK);
                }
            } else if (tid.isPresent()) {
                UserTidVpaData merchantDeatils = this.userTidVpaDataService.findByTid((String)tid.get());
                if (merchantDeatils != null) {
                    List<Object[]> sumOfVoidReversalTxnAmount = this.bijlipayTransactionService.getSumOfTodayAndYesterdayVoidAndReversalTxnAmount((String)tid.get(), this.getSessionUserId());
                    List<Object[]> TodayAndYesterdaySumCountForTid = this.bijlipayTransactionService.getSumAndCountOfTodayAndYesterdayTxnAmountForTid((String)tid.get(), this.getSessionUserId());
                    List<SwitchTransactionsDto> var17 = this.bijlipayTransactionService.getLastFiveSwicthTransactionsBasedOnUserPortal(tidList);
                    TodayAndYesterdaySumCountForTid.forEach((l) -> {
                        Map<String, Object> maps = new HashMap();
                        maps.put("todaySum", l[0]);
                        maps.put("todayCount", l[1]);
                        maps.put("yesterdaySum", l[2]);
                        maps.put("yesterdayCount", l[3]);
                        output.add(maps);
                    });
                    sumOfVoidReversalTxnAmount.forEach((l) -> {
                        Map<String, Object> maps = new HashMap();
                        maps.put("todayVoidReversalSum", l[0]);
                        maps.put("todayVoidReversalCount", l[1]);
                        maps.put("yesterdayVoidReversalSum", l[2]);
                        maps.put("yesterdayVoidReversalCount", l[3]);
                        voidRevObj.add(maps);
                    });
                    map.put("voidReversal", voidRevObj);
                    map.put("tid", output);
                    map.put("staticQR", vpaObject);
                    map.put("lastFiveTxnAmount", var17);
                    map.put("lastFiveTxnAmountForQr", qrTransactions);
                    this.logger.info("End For Fetch Transaction Summary Api ----/api/fetch-transaction-summary/{tid}");
                    return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success", map), HttpStatus.OK);
                }
            } else if (vpa.isPresent()) {
                this.userTidVpaDataService.findByVpa(user);
                List<Object[]> TodayAndYesterdaySumCountForVpa = this.bijlipayTransactionService.getSumAndCountOfTodayAndYesterdayTxnAmountForVpa((String)vpa.get(), this.getSessionUserId());
                List<AxisQrTransactionDto> var19 = this.bijlipayTransactionService.getLastFiveAxisQrTransactionsBasedOnUserPortal(vpaList);
                TodayAndYesterdaySumCountForVpa.forEach((l) -> {
                    Map<String, Object> maps = new HashMap();
                    maps.put("todaySum", l[0]);
                    maps.put("todayCount", l[1]);
                    maps.put("yesterdaySum", l[2]);
                    maps.put("yesterdayCount", l[3]);
                    vpaObject.add(maps);
                });
                map.put("voidReversal", voidRevObj);
                map.put("tid", output);
                map.put("staticQR", vpaObject);
                map.put("lastFiveTxnAmount", posTransactions);
                map.put("lastFiveTxnAmountForQr", var19);
                this.logger.info("End For Fetch Transaction Summary Api ----/api/fetch-transaction-summary/{tid}");
                return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success", map), HttpStatus.OK);
            }
        }

        return new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "Merchant not found!"), HttpStatus.NOT_FOUND);
    }

    @GetMapping({"/getQrTransaction-pageable", "/getQrTransaction-pageable/{action}/{value}"})
    public ResponseEntity<?> getAxisQrTransactions(@PathVariable("action") Optional<Integer> action, @PathVariable("value") Optional<Integer> value, @RequestParam("vpa") Optional<List<String>> vpa, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestParam Map<String, String> requestParams, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to, Pageable pageable, HttpServletRequest httpServletRequest) throws URISyntaxException, IOException, SQLException, ParseException {
        String queryString = httpServletRequest.getQueryString();
        this.logger.info("request param : ------------------------------- : {}", queryString);
        User user = (User)this.userRepository.getOne(this.getSessionUserId());
        pageable = this.webConfig.resolvePageableCamelToSnake(requestParams, pageable);
        if (user != null) {
            List<String> vpaDetails = this.userTidVpaDataService.findByVpa(user);
            this.logger.info("Get transactions based on Vpa  : ------------------------------- : {}", vpaDetails.get(0));
            Page<AxisQrTransactionDto> response = this.bijlipayTransactionService.getAxisQrTransactionDetails(action, value, searchTerm, from, to, vpaDetails, vpa, pageable);
            return !response.isEmpty() ? new ResponseEntity(new ApiResponse(HttpStatus.OK, "Success", response), HttpStatus.OK) : new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "Data Not Found !"), HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "User Not Found !"), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping({"/getQrTransaction-amount", "/getQrTransaction-amount/{action}/{value}"})
    public ResponseEntity<?> getAxisQrTransactionsAmount(@PathVariable("action") Optional<Integer> action, @PathVariable("value") Optional<Integer> value, @RequestParam("vpa") Optional<List<String>> vpa, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestParam Map<String, String> requestParams, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to, HttpServletRequest httpServletRequest) throws URISyntaxException, IOException, SQLException, ParseException {
        String queryString = httpServletRequest.getQueryString();
        this.logger.info("request param : ------------------------------- : {}", queryString);
        User user = (User)this.userRepository.getOne(this.getSessionUserId());
        if (user != null) {
            List<String> vpaDetails = this.userTidVpaDataService.findByVpa(user);
            this.logger.info("Get transactions based on Vpa  : ------------------------------- : {}", vpaDetails.get(0));
            Double amount = this.bijlipayTransactionService.getAxisQrTransactionDetailsAmount(action, value, searchTerm, from, to, vpaDetails, vpa);
            double response = amount != null ? amount : (double)0.0F;
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            String formatted = decimalFormat.format(response);
            return new ResponseEntity(new ApiResponse(HttpStatus.OK, "Success", formatted), HttpStatus.OK);
        } else {
            return new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "User Not Found !"), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping({"get-pos-transaction-pageable", "get-pos-transaction-pageable/{action}"})
    public ResponseEntity<?> getPosTransaction(@RequestParam("tid") Optional<List<String>> tid, @PathVariable("action") Optional<Integer> action, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestBody TransactionRequestDto requestDto, @RequestParam Map<String, String> requestParams, Pageable pageable, HttpServletRequest httpServletRequest) {
        this.logger.info("Get transactions pageable : ------------------------------- : ");
        String queryString = httpServletRequest.getQueryString();
        pageable = this.webConfig.resolvePageableCamelToSnake(requestParams, pageable);
        this.logger.info("request param : ------------------------------- : {}", queryString);
        User user = (User)this.userRepository.getOne(this.getSessionUserId());
        if (user != null) {
            try {
                List<String> tidData = this.userTidVpaDataService.fetchTidData(user);
                this.logger.info("The tid is in our Switch , fetching SWITCH txns ");

                try {
                    Page<SwitchTransactionsDto> response = this.bijlipayTransactionService.getSwitchTransactionsSearch(action, tidData, tid, requestDto, from, to, searchTerm, pageable);
                    if (!response.isEmpty()) {
                        return new ResponseEntity(new ApiResponse(HttpStatus.OK, "Success", response), HttpStatus.OK);
                    }

                    return new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "Data Not Found !"), HttpStatus.NOT_FOUND);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } catch (HttpServerErrorException | HttpClientErrorException ce) {
                return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "error", ((HttpStatusCodeException)ce).getResponseBodyAsString()), HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity(new ApiResponse(HttpStatus.OK, "Success"), HttpStatus.OK);
    }

    @PostMapping({"get-pos-transaction-pageable-portal", "get-pos-transaction-pageable-portal/{action}"})
    @Transactional(
            readOnly = true
    )
    public ResponseEntity<?> getPosTransactionForPortal(@RequestParam("tid") Optional<List<String>> tid, @PathVariable("action") Optional<Integer> action, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestBody TransactionRequestDto requestDto, @RequestParam Map<String, String> requestParams, Pageable pageable, HttpServletRequest httpServletRequest) {
        this.logger.info("Get transactions pageable : ------------------------------- : ");
        String queryString = httpServletRequest.getQueryString();
        pageable = this.webConfig.resolvePageableCamelToSnake(requestParams, pageable);
        this.logger.info("request param : ------------------------------- : {}", queryString);
        User user = (User)this.userRepository.getOne(this.getSessionUserId());
        if (user != null) {
            try {
                List<String> tidData = this.userTidVpaDataService.fetchTidData(user);
                this.logger.info("The tid is in our Switch , fetching SWITCH txns ");

                try {
                    Page<SwitchTransactionsDto> response = this.bijlipayTransactionService.getSwitchTransactionsSearchData(action, tidData, tid, requestDto, from, to, searchTerm, pageable);
                    if (!response.isEmpty()) {
                        return new ResponseEntity(new ApiResponse(HttpStatus.OK, "Success", response), HttpStatus.OK);
                    }

                    return new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "Data Not Found !"), HttpStatus.NOT_FOUND);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } catch (HttpServerErrorException | HttpClientErrorException ce) {
                return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "error", ((HttpStatusCodeException)ce).getResponseBodyAsString()), HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity(new ApiResponse(HttpStatus.OK, "Success"), HttpStatus.OK);
    }

    @GetMapping({"get-chargeslip-data/{rrn}/{txn_type}/{tid}"})
    public ResponseEntity<?> fetchChargeSlipData(@PathVariable("rrn") String rrn, @PathVariable("txn_type") String txnType, @PathVariable("tid") String tid) {
        Connection con = null;
        Statement stmt = null;
        String query = "select * FROM " + this.chargeSlipDb + ".charge_slips_data cs WHERE cs.txn_type = '" + txnType + "' AND cs.tid = '" + tid + "'AND cs.rrn = '" + rrn + "'";

        try {
            con = this.getConnection();
            stmt = con.createStatement();
            List<ChargeSlipDto> chargeSlipDtoList = new ArrayList();

            Object chargeSlipDto;
            try {
                ResultSet rs = stmt.executeQuery(query);

                while(rs.next()) {
                    ChargeSlipDto chargeSlipDto_1 = new ChargeSlipDto();
                    chargeSlipDto_1.setId(rs.getLong("charge_slip_id"));
                    chargeSlipDto_1.setAPPLICATION_ID(rs.getString("application_id"));
                    chargeSlipDto_1.setAPPLICATION_NAME(rs.getString("application_name"));
                    chargeSlipDto_1.setAPPLICATION_VERSION(rs.getString("application_version"));
                    chargeSlipDto_1.setBANK_LOGO_ID(rs.getString("bank_logo_id"));
                    chargeSlipDto_1.setAUTH_CODE(rs.getString("auth_code"));
                    chargeSlipDto_1.setBATCH_NUMBER(rs.getString("batch_number"));
                    chargeSlipDto_1.setCARD_HOLDER_NAME(rs.getString("card_holder_name"));
                    chargeSlipDto_1.setCARD_MASKED(rs.getString("card_masked"));
                    chargeSlipDto_1.setCARD_TYPE(rs.getString("card_type"));
                    chargeSlipDto_1.setINVOICE_NUMBER(rs.getString("invoice_number"));
                    chargeSlipDto_1.setLOCATION(rs.getString("location"));
                    chargeSlipDto_1.setMERCHANT_NAME(rs.getString("merchant_name"));
                    chargeSlipDto_1.setMID(rs.getString("mid"));
                    chargeSlipDto_1.setMOBILE_NUMBER(rs.getString("mobile_number"));
                    chargeSlipDto_1.setMODE(rs.getString("mode"));
                    chargeSlipDto_1.setRECEIPT_HEADER1(rs.getString("receipt_header1"));
                    chargeSlipDto_1.setRRN(rs.getString("rrn"));
                    chargeSlipDto_1.setTID(rs.getString("tid"));
                    chargeSlipDto_1.setTSI(rs.getString("tsi"));
                    chargeSlipDto_1.setTVR(rs.getString("tvr"));
                    chargeSlipDto_1.setTXN_AMOUNT_SALE(rs.getString("txn_amount_sale"));
                    chargeSlipDto_1.setTXN_AMOUNT_TIP(rs.getString("txn_amount_tip"));
                    chargeSlipDto_1.setTXN_AMOUNT_TOTAL(rs.getString("txn_amount_total"));
                    chargeSlipDto_1.setTXN_CERTIFICATE(rs.getString("txn_certificate"));
                    chargeSlipDto_1.setTXN_DATE(rs.getString("txn_date"));
                    chargeSlipDto_1.setTXN_TIME(rs.getString("txn_time"));
                    chargeSlipDto_1.setTXN_TYPE(rs.getString("txn_type"));
                    chargeSlipDto_1.setCreatedDate(rs.getDate("created_date"));
                    chargeSlipDto_1.setPIN_VERIFICATION(rs.getString("pin_verification"));
                    chargeSlipDtoList.add(chargeSlipDto_1);
                }

                this.logger.info("charge slip data success", chargeSlipDtoList);
                ResponseEntity var20 = new ResponseEntity(new ApiResponse(HttpStatus.OK, "success", chargeSlipDtoList), HttpStatus.OK);
                return var20;
            } catch (Exception e) {
                e.printStackTrace();
                chargeSlipDto = null;
            } finally {
                stmt.close();
                con.close();
            }

            return (ResponseEntity<?>)chargeSlipDto;
        } catch (Exception e) {
            e.printStackTrace();
            this.logger.info("charge slip data not found");
            return new ResponseEntity(new ApiResponse(HttpStatus.CONFLICT, "Data not Found"), HttpStatus.CONFLICT);
        }
    }

    @PostMapping({"/api/download-pos-txn", "/api/download-pos-txn/{action}"})
    public ResponseEntity<?> downloadBpRegionsMenu(HttpServletResponse response, @RequestParam("tid") Optional<String> tid, @PathVariable("action") Optional<Integer> action, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestBody TransactionRequestDto requestDto) throws IOException {
        this.logger.info("Start to Download Tid Transaction Api ----/api/download/{tid} ---Tid --{}", tid);

        try {
            this.logger.info("Switch Transactions download report");
            Optional<User> user = this.userRepository.findById(this.getSessionUserId());
            if (user.isPresent()) {
                this.logger.info("Merchant details : ---- : {}", ((User)user.get()).getMobileNo());
                List<UserTidVpaData> merchantDetails = this.userTidVpaDataService.getByUserId((User)user.get());
                if (merchantDetails.size() <= 0) {
                    return new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "User not mapped with tid !"), HttpStatus.NOT_FOUND);
                } else {
                    List<String> tidData = this.userTidVpaDataService.fetchTidData((User)user.get());
                    List<String> mid = merchantDetails.stream().filter((m) -> m.getTid() != null).map(UserTidVpaData::getMid).toList();
                    String fileName = "";
                    if (from.isPresent() && to.isPresent()) {
                        String var10000 = (String)mid.get(0) + "%s.xlsx";
                        Object[] var10001 = new Object[1];
                        String var10004 = DateUtil.longtoDate((Long)from.get(), "yyyMMdd");
                        var10001[0] = var10004 + DateUtil.longtoDate((Long)to.get(), "yyyMMdd");
                        fileName = String.format(var10000, var10001);
                    } else {
                        fileName = (String)mid.get(0) + ".xlsx";
                    }

                    this.logger.info("The file name : ------------- : {}", fileName);
                    response.setContentType("application/vnd.ms-excel");
                    response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
                    new ByteArrayOutputStream();
                    InputStreamResource file = this.bijlipayTransactionService.downloadTransactionReport(action, tidData, tid, requestDto, from, to, searchTerm);
                    this.logger.info("Work book is created : --- : {}", file.getFilename());
                    if (file == null) {
                        return new ResponseEntity(new ApiResponse(HttpStatus.NO_CONTENT, "NO DATA FOUND"), HttpStatus.NO_CONTENT);
                    } else {
                        this.logger.info("End For Download Tid Transaction Api ----/api/download/{tid}");
                        return ((ResponseEntity.BodyBuilder)ResponseEntity.ok().header("Content-Disposition", new String[]{"attachment; filename=" + fileName})).contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
                    }
                }
            } else {
                return new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "User not found !"), HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "The Exception occurred while generating the Open Tracker Excel Report " + String.valueOf(e)), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping({"/api/download-pos-txn-portal", "/api/download-pos-txn-portal/{action}"})
    public ResponseEntity<?> downloadPosTxnData(@RequestParam("tid") Optional<String> tid, @PathVariable("action") Optional<Integer> action, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestBody TransactionRequestDto requestDto) throws IOException {
        this.logger.info("Start to Download Tid Transaction Api ----/api/download/{tid} ---Tid --{}", tid);

        try {
            this.logger.info("Switch Transactions download report");
            Optional<User> user = this.userRepository.findById(this.getSessionUserId());
            if (user.isPresent()) {
                this.logger.info("Merchant details : ---- : {}", ((User)user.get()).getMobileNo());
                List<UserTidVpaData> merchantDetails = this.userTidVpaDataService.getByUserId((User)user.get());
                if (merchantDetails.size() <= 0) {
                    return new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "User not mapped with tid !"), HttpStatus.NOT_FOUND);
                } else {
                    List<String> tidData = this.userTidVpaDataService.fetchTidData((User)user.get());
                    List<String> mid = merchantDetails.stream().filter((m) -> m.getTid() != null).map(UserTidVpaData::getMid).toList();
                    String fileName = "";
                    if (from.isPresent() && to.isPresent()) {
                        String var10000 = (String)mid.get(0) + "%s.xlsx";
                        Object[] var10001 = new Object[1];
                        String var10004 = DateUtil.longtoDate((Long)from.get(), "yyyMMdd");
                        var10001[0] = var10004 + DateUtil.longtoDate((Long)to.get(), "yyyMMdd");
                        fileName = String.format(var10000, var10001);
                    } else {
                        fileName = (String)mid.get(0) + ".xlsx";
                    }

                    this.logger.info("The file name : ------------- : {}", fileName);
                    InputStreamResource file = this.bijlipayTransactionService.downloadTransactionReport(action, tidData, tid, requestDto, from, to, searchTerm);
                    this.logger.info("Work book is created : --- : {}", file.getFilename());
                    if (file == null) {
                        return new ResponseEntity(new ApiResponse(HttpStatus.NO_CONTENT, "NO DATA FOUND"), HttpStatus.NO_CONTENT);
                    } else {
                        this.logger.info("End For Download Tid Transaction Api ----/api/download/{tid}");
                        return ((ResponseEntity.BodyBuilder)ResponseEntity.ok().header("Content-Disposition", new String[]{"attachment; filename=" + fileName})).contentType(MediaType.APPLICATION_OCTET_STREAM).body(file);
                    }
                }
            } else {
                return new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "User not found !"), HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "The Exception occurred while generating the Open Tracker Excel Report " + String.valueOf(e)), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping({"/api/downloadAxisQrTxnData", "/api/downloadAxisQrTxnData/{action}/{value}"})
    public ResponseEntity<?> downloadQrTransactionData(HttpServletResponse response, @PathVariable("action") Optional<Integer> action, @PathVariable("value") Optional<Integer> value, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestParam Map<String, String> requestParams, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to, @RequestParam("vpa") Optional<String> vpa) throws IOException {
        this.logger.info("Start to Download Axis QR Transaction Api ----/api/downloadAxisQrTxnData");

        try {
            User user = this.userRepository.findByUser(this.getSessionUserId());
            if (user == null) {
                return new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "User not found !"), HttpStatus.NOT_FOUND);
            } else {
                this.logger.info("User details : ---- : {}", user.getId());
                List<String> vpaDetails = this.userTidVpaDataService.findByVpa(user);
                String fileName = "";
                if (from.isPresent() && to.isPresent()) {
                    Object[] var10001 = new Object[1];
                    String var10004 = DateUtil.longtoDate((Long)from.get(), "yyyMMdd");
                    var10001[0] = var10004 + DateUtil.longtoDate((Long)to.get(), "yyyMMdd");
                    fileName = String.format("Qr_Txn_Data_%s.xlsx", var10001);
                } else {
                    fileName = String.format("Qr_Txn_Data_%s.xlsx", DateUtil.getDate());
                }

                this.logger.info("The file name : ------------- : {}", fileName);
                response.setContentType("application/vnd.ms-excel");
                response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
                new ByteArrayOutputStream();
                InputStreamResource file = this.bijlipayTransactionService.downloadQrTransactionReport(action, value, searchTerm, from, to, vpaDetails, vpa);
                this.logger.info("Work book is created : --- : {}", file.getFilename());
                if (file == null) {
                    return new ResponseEntity(new ApiResponse(HttpStatus.NO_CONTENT, "NO DATA FOUND"), HttpStatus.NO_CONTENT);
                } else {
                    this.logger.info("End For Download Axis QR Transaction Api ----/api/downloadAxisQrTxnData");
                    return ((ResponseEntity.BodyBuilder)ResponseEntity.ok().header("Content-Disposition", new String[]{"attachment; filename=" + fileName})).contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
                }
            }
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @GetMapping({"/api/downloadAxisQrTxnDataForPortal", "/api/downloadAxisQrTxnDataForPortal/{action}/{value}"})
    public ResponseEntity<?> downloadAxisQrTxnDataForPortal(@PathVariable("action") Optional<Integer> action, @PathVariable("value") Optional<Integer> value, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestParam("vpa") Optional<String> vpa, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to) throws IOException {
        this.logger.info("Start to Download Axis QR Transaction Api ----/api/downloadAxisQrTxnData");

        try {
            User user = this.userRepository.findByUser(this.getSessionUserId());
            if (user == null) {
                return new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "User not found !"), HttpStatus.NOT_FOUND);
            } else {
                this.logger.info("User details : ---- : {}", user.getId());
                List<String> vpaDetails = this.userTidVpaDataService.findByVpa(user);
                String fileName = "";
                if (from.isPresent() && to.isPresent()) {
                    Object[] var10001 = new Object[1];
                    String var10004 = DateUtil.longtoDate((Long)from.get(), "yyyMMdd");
                    var10001[0] = var10004 + DateUtil.longtoDate((Long)to.get(), "yyyMMdd");
                    fileName = String.format("Qr_Txn_Data_%s.xlsx", var10001);
                } else {
                    fileName = String.format("Qr_Txn_Data_%s.xlsx", DateUtil.getDate());
                }

                this.logger.info("The file name : ------------- : {}", fileName);
                new ByteArrayOutputStream();
                InputStreamResource file = this.bijlipayTransactionService.downloadQrTransactionReportData(action, value, searchTerm, from, to, vpaDetails, vpa);
                this.logger.info("Work book is created : --- : {}", file.getFilename());
                if (file == null) {
                    return new ResponseEntity(new ApiResponse(HttpStatus.NO_CONTENT, "NO DATA FOUND"), HttpStatus.NO_CONTENT);
                } else {
                    this.logger.info("End For Download Axis QR Transaction Api ----/api/downloadAxisQrTxnData");
                    return ((ResponseEntity.BodyBuilder)ResponseEntity.ok().header("Content-Disposition", new String[]{"attachment; filename=" + fileName})).contentType(MediaType.APPLICATION_OCTET_STREAM).body(file);
                }
            }
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @GetMapping({"/settlement-report"})
    public ResponseEntity<?> settlementReportRequest(@RequestParam("tid") Optional<String> tid, @RequestParam("vpa") Optional<String> vpa, @RequestParam("from") Optional<Long> from, @RequestParam("to") Optional<Long> to, @RequestParam("fileType") String fileType) throws MessagingException, URISyntaxException, org.json.simple.parser.ParseException, ParseException {
        this.logger.info("Start to Get Settlement Report Api ----/api/settlement-report-send-mail");
        User user = this.userRepository.findByUser(this.getSessionUserId());
        List<UserTidVpaData> userTidVpaData = this.userTidVpaDataService.getByUserId(user);
        if (user == null) {
            return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "user not found"), HttpStatus.BAD_REQUEST);
        } else {
            this.logger.info("User --{}", this.getSessionUserId());
            String vFrom = "";
            String vTo = "";
            if (from.isPresent() && to.isPresent()) {
                vFrom = DateUtil.longtoDate((Long)from.get(), "dd-MM-yyyy");
                vTo = DateUtil.longtoDate((Long)to.get(), "dd-MM-yyyy");
            }

            if (tid.isPresent()) {
                List<String> mid = userTidVpaData.stream().filter((m) -> m.getTid() != null && m.getTid().equalsIgnoreCase((String)tid.get())).map(UserTidVpaData::getMid).toList();
                List<String> institution = userTidVpaData.stream().filter((m) -> m.getTid() != null && m.getTid().equalsIgnoreCase((String)tid.get())).map(UserTidVpaData::getInstitutionCode).toList();
                this.logger.info("mid ----------- : {} , institution ----------: {}", mid, institution);
                this.logger.info("Settlement Controller options tid: ----------- : {} : ----------- : fdate {} : ----------- : toDate {}", new Object[]{tid, vFrom, vTo});
                SettlementReportsDto settlementReportsDto = new SettlementReportsDto();
                settlementReportsDto.setMerchantId(mid.size() > 0 ? (String)mid.get(0) : null);
                settlementReportsDto.setFromDate(vFrom);
                settlementReportsDto.setToDate(vTo);
                settlementReportsDto.setTerminalId((String)tid.get());
                String uniqueId = String.valueOf(System.currentTimeMillis());
                settlementReportsDto.setUniqueId(uniqueId);
                settlementReportsDto.setFileType(fileType);
                this.logger.info("Request DTO : ------------- : {}", settlementReportsDto);
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.set("content-type", "application/json");
                String institutionCode = institution.size() > 0 ? (String)institution.get(0) : null;
                this.logger.info("institution ------------- : {}", institutionCode);
                httpHeaders.set("NII", institutionCode);
                HttpEntity<SettlementReportsDto> httpEntity = new HttpEntity(settlementReportsDto, httpHeaders);
                URI uri = null;
                if (institutionCode != null && institutionCode.equalsIgnoreCase("104")) {
                    uri = new URI(this.axisSettlementReportUrl);
                }
                else if (institutionCode != null && institutionCode.equalsIgnoreCase("115")){
                    logger.info("Calling Yes based institution code ----->{}",institutionCode!=null?institutionCode:"NA");
                    uri = new URI(yesbankSettlementReportUrl);
                }
                else {
                    uri = new URI(this.settlementReportUrl);
                }

                ResponseEntity<String> stringResponseEntity = null;
                new JSONParser();

                try {
                    RestTemplate restTemplate = new RestTemplate();
                    this.logger.info("Trying to connect with Mars settlement report Api : ----- : {}", uri);
                    stringResponseEntity = restTemplate.postForEntity(uri, httpEntity, String.class);
                    this.logger.info("Got response : ---------- : {}", stringResponseEntity);
                    SettlementRequestResponse settlementRequestResponse = new SettlementRequestResponse();
                    settlementRequestResponse.setTid(settlementReportsDto.getTerminalId());
                    settlementRequestResponse.setMid(settlementReportsDto.getMerchantId());
                    settlementRequestResponse.setFromDate(settlementReportsDto.getFromDate());
                    settlementRequestResponse.setToDate(settlementReportsDto.getToDate());
                    settlementRequestResponse.setUniqueId(uniqueId);
                    settlementRequestResponse.setUser(user);
                    settlementRequestResponse.setCategoryType("Settlement");
                    settlementRequestResponse.setFileType(fileType);
                    this.settlementRequestResponseRepository.save(settlementRequestResponse);
                } catch (HttpClientErrorException clientErrorException) {
                    this.logger.info("HttpClientErrorException getMessage {}", clientErrorException.getMessage());
                    this.logger.info("HttpClientErrorException getLocalizedMessage {}", clientErrorException.getLocalizedMessage());
                    return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "error", clientErrorException.getResponseBodyAsString()), HttpStatus.BAD_REQUEST);
                } catch (HttpServerErrorException httpServerErrorException) {
                    this.logger.info("HttpServerErrorException {}", httpServerErrorException.getMessage());
                    this.logger.info("HttpClientErrorException httpServerErrorException {}", httpServerErrorException.getLocalizedMessage());
                    return new ResponseEntity(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", httpServerErrorException.getResponseBodyAsString()), HttpStatus.INTERNAL_SERVER_ERROR);
                } catch (Exception e) {
                    e.printStackTrace();
                    this.logger.info("Exception e {}", e.getMessage());
                    return new ResponseEntity(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
                }

                this.logger.info("End For Get Settlement Report Api");
                return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success", stringResponseEntity.toString()), HttpStatus.OK);
            } else {
                this.logger.info("Settlement Controller options vpa : ----------- : {} : ----------- : fdate {} : ----------- : toDate {}", new Object[]{vpa, vFrom, vTo});
                this.logger.info("Qr Vpa Details ---{}", vpa.orElse((String) null));
                List<String> institution = userTidVpaData.stream().filter((m) -> m.getVpa() != null && m.getVpa().equalsIgnoreCase((String)vpa.get())).map(UserTidVpaData::getInstitutionCode).toList();
                SettlementReportQrDto settlementReportQrDto = new SettlementReportQrDto();
                settlementReportQrDto.setVpa((String)vpa.orElse((String) null));
                settlementReportQrDto.setFromDate(vFrom);
                settlementReportQrDto.setToDate(vTo);
                String uniqueId = String.valueOf(System.currentTimeMillis());
                settlementReportQrDto.setUniqueId(uniqueId);
                settlementReportQrDto.setFileType(fileType);
                this.logger.info("Request DTO : ------------- : {}", settlementReportQrDto);
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.set("content-type", "application/json");
                httpHeaders.set("NII", institution.size() > 0 ? (String)institution.get(0) : null);
                HttpEntity<SettlementReportQrDto> httpEntity = new HttpEntity(settlementReportQrDto, httpHeaders);
                URI uri = new URI(this.staticQRSettlementReportUrl);
                ResponseEntity<String> stringResponseEntity = null;
                new JSONParser();

                try {
                    RestTemplate restTemplate = new RestTemplate();
                    this.logger.info("Trying to connect with Mars settlement report Api : ----- : {}", this.settlementReportUrl);
                    stringResponseEntity = restTemplate.postForEntity(uri, httpEntity, String.class);
                    this.logger.info("Got response : ---------- : {}", stringResponseEntity);
                    SettlementRequestResponse settlementRequestResponse = new SettlementRequestResponse();
                    settlementRequestResponse.setVpa(settlementReportQrDto.getVpa());
                    settlementRequestResponse.setFromDate(settlementReportQrDto.getFromDate());
                    settlementRequestResponse.setToDate(settlementReportQrDto.getToDate());
                    settlementRequestResponse.setUniqueId(uniqueId);
                    settlementRequestResponse.setUser(user);
                    settlementRequestResponse.setCategoryType("Settlement");
                    settlementRequestResponse.setFileType(fileType);
                    this.settlementRequestResponseRepository.save(settlementRequestResponse);
                } catch (HttpClientErrorException clientErrorException) {
                    this.logger.info("HttpClientErrorException getMessage {}", clientErrorException.getMessage());
                    this.logger.info("HttpClientErrorException getLocalizedMessage {}", clientErrorException.getLocalizedMessage());
                    return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "error", clientErrorException.getResponseBodyAsString()), HttpStatus.BAD_REQUEST);
                } catch (HttpServerErrorException httpServerErrorException) {
                    this.logger.info("HttpServerErrorException {}", httpServerErrorException.getMessage());
                    this.logger.info("HttpClientErrorException httpServerErrorException {}", httpServerErrorException.getLocalizedMessage());
                    return new ResponseEntity(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", httpServerErrorException.getResponseBodyAsString()), HttpStatus.INTERNAL_SERVER_ERROR);
                } catch (Exception e) {
                    e.printStackTrace();
                    this.logger.info("Exception e {}", e.getMessage());
                    return new ResponseEntity(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
                }

                this.logger.info("End For Get Settlement Report Api ----/api/settlement-report-send-mail");
                return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success", stringResponseEntity.toString()), HttpStatus.OK);
            }
        }
    }

    @GetMapping({"/api/category/{tid}/{action}", "/api/category/{tid}/{action}/{categoryId}"})
    public ResponseEntity<?> getCategoryTypeByTid(@PathVariable("tid") Optional<String> tid, @PathVariable("action") Optional<Integer> action, @PathVariable("categoryId") Optional<Long> categoryId) throws Exception {
        this.logger.info("Get category by tid  : ------------------------{} : ", tid);
        List<Category> category = this.categoryService.findByTid(tid, action, categoryId, this.getSessionUserId());
        if (category == null) {
            this.logger.info("Get All Category has an error : ------------------------------- : ");
            return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "Error", (Object)null), HttpStatus.BAD_REQUEST);
        } else {
            this.logger.info("Get All Category is success : ------------------------------- {}: ", category);
            return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success", category), HttpStatus.OK);
        }
    }

    @GetMapping({"/api/getAllCategory"})
    public ResponseEntity<?> getCategoryTypeByTid() throws Exception {
        this.logger.info("Get All Category : ------------------------------- : ");
        List<Category> category = this.categoryService.findByUser(this.getSessionUserId());
        if (category == null) {
            this.logger.info("Get All Category has an error : ------------------------------- : ");
            return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "Error", (Object)null), HttpStatus.BAD_REQUEST);
        } else {
            this.logger.info("Get All Category is success : ------------------------------- : ");
            return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success", category), HttpStatus.OK);
        }
    }

    @PostMapping({"/api/invoice-request"})
    public ResponseEntity<?> invoiceGenerate(@RequestBody Invoice invoice) throws MessagingException {
        this.logger.info("Start To Generate Invoice Request Api And Body: -----/api/invoice-request--------:  {}", invoice);
        this.bijlipayTransactionService.requestMailForInvoiceCreation(invoice);
        this.logger.info("End For Generate Invoice Request Api : -----/api/invoice-request");
        return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
    }

    @PostMapping({"/tpt/txnNotifications"})
    public ResponseEntity<?> tidTxnNotifications(@RequestBody SwitchTransactionsDto switchTransactionsDto) {
        this.logger.info("Start to Notification Api ---/tpt/txnNotifications");
        Gson gson = new Gson();
        this.logger.info("Request Payload ---/tpt/txnNotifications ------- {}", gson.toJson(switchTransactionsDto));
        this.bijlipayTransactionService.pushTxnNotifications(switchTransactionsDto);
        this.logger.info("End For Notification Api ---/tpt/txnNotifications");
        return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
    }

    @PostMapping({"/tpt/settlementCallback"})
    public ResponseEntity<?> settleMentCallback(@RequestBody SettlementNotificationDto requestDto) throws MessagingException {
        this.logger.info("pos settlement callback ---------- : {}", requestDto);
        this.logger.info("Start to Settlement Call Back Api ---/tpt/settlement-callback");
        this.bijlipayTransactionService.pushSettlementNotifications(requestDto);
        this.logger.info("End For Settlement Call Back Api ---/tpt/settlement-callback");
        return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success", ""), HttpStatus.OK);
    }

    @PostMapping({"/tpt/QRSettlementCallback"})
    public ResponseEntity<?> QRSettlementCallback(@RequestBody SettlementNotificationDto requestDto) throws MessagingException {
        this.logger.info("qr settlement callback ---------- : {}", requestDto);
        this.logger.info("Start to Settlement Call Back Api ---/tpt/settlement-callback");
        this.bijlipayTransactionService.pushQRSettlementNotifications(requestDto);
        this.logger.info("End For Settlement Call Back Api ---/tpt/settlement-callback");
        return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success", ""), HttpStatus.OK);
    }

    @PostMapping({"download-settlemt-report"})
    public ResponseEntity<?> downloadSettlementReport(@RequestParam("url") String url) throws IOException {
        String[] parts = url.split("/");
        String key = parts[parts.length - 1];
        this.logger.info("file key ---------------: {}", key);
        GetObjectRequest getObjectRequest = (GetObjectRequest)GetObjectRequest.builder().bucket(this.amazonBucketName).key(key).build();

        try {
            ResponseBytes<GetObjectResponse> objectBytes = this.s3Client.getObjectAsBytes(getObjectRequest);
            this.fileStorageService.storeS3File(objectBytes, key);
        } catch (NoSuchBucketException e) {
            throw new RuntimeException("The specified bucket does not exist: " + e.getMessage(), e);
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping({"/tpt/axisQr-txnNotifications"})
    public ResponseEntity<?> axisQrTxnNotifications(@RequestBody AxisQrTransactionDto axisQrTransactionDto) {
        this.logger.info("Start to Push Qr Transaction Notification Api --/axisQr-txnNotifications");
        CompletableFuture.runAsync(() -> {
            try {
                this.bijlipayTransactionService.insertStaticQrTxnData(axisQrTransactionDto);
            } catch (ParseException var3) {
                this.logger.info("DB insertion error Axis SQR Callback --: {}", axisQrTransactionDto.getMerchantTransactionId());
            }

        });
        this.bijlipayTransactionService.pushTxnNotificationsForAxisQr(axisQrTransactionDto);
        this.logger.info("DB inserted and notified successfully --: {}", axisQrTransactionDto.getMerchantTransactionId());
        this.logger.info("End For Push Qr Transaction Notification Api --/axisQr-txnNotifications");
        return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
    }

    @GetMapping({"/insert-axis-missing-data"})
    public void insertAxisMissingData() {
        this.logger.info("Axis Static Qr Missing Data Insert Started");
        String var10000 = this.axisMissingDataFilePath;
        String missingDataFile = var10000 + "/Axis_Missing_Data-" + DateUtil.today() + ".csv";
        File file = new File(missingDataFile);
        boolean dataInserted = false;
        if (file.exists()) {
            dataInserted = this.bijlipayTransactionService.insertAxisMissingData(missingDataFile);
            if (dataInserted) {
                this.logger.info("All Missing Axis File Data Inserted Successfully----!!!");
            }
        }

    }

    @GetMapping({"/getSettlementReportData", "/getSettlementReportData/{categoryType}"})
    public ResponseEntity<?> getSettlementReportData(@PathVariable("categoryType") Optional<String> categoryType, @RequestParam Map<String, String> requestParams, Pageable pageable) {
        pageable = this.webConfig.resolvePageable(requestParams, pageable);
        Page<SettlementRequestResponse> notifications;
        if (categoryType.isPresent()) {
            notifications = this.categoryService.getSettlementReportDataBasedOnCategory(this.userTidVpaDataService.getUserById(this.getSessionUser().getId()), (String)categoryType.get(), pageable);
        } else {
            notifications = this.categoryService.getSettlementReportData(this.userTidVpaDataService.getUserById(this.getSessionUser().getId()), pageable);
        }

        return !notifications.isEmpty() ? new ResponseEntity(new ApiResponse(HttpStatus.OK, "success", notifications), HttpStatus.OK) : new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "Data not Found"), HttpStatus.NOT_FOUND);
    }

    @GetMapping({"/download-txn-data"})
    public ResponseEntity<?> downloadTxnData(@RequestParam("tid") Optional<String> tid, @RequestParam("vpa") Optional<String> vpa, @RequestParam("from") Optional<Long> from, @RequestParam("to") Optional<Long> to) throws ParseException, IOException {
        this.logger.info("Start download transaction report ------- : ");
        if (tid.isPresent()) {
            UserTidVpaData tidData = this.userTidVpaDataService.findByTid((String)tid.get());
            if (tidData == null) {
                return new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "Tid Not Found"), HttpStatus.NOT_FOUND);
            } else {
                this.bijlipayTransactionService.downloadTxnReport(from, to, tid, tidData);
                return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
            }
        } else {
            UserTidVpaData vpaData = this.userTidVpaDataService.checkVpa((String)vpa.get());
            if (vpaData == null) {
                return new ResponseEntity(new ApiResponse(HttpStatus.NOT_FOUND, "Vpa not Found"), HttpStatus.NOT_FOUND);
            } else {
                this.bijlipayTransactionService.downloadQrReport(from, to, vpa);
                return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
            }
        }
    }

    @PostMapping({"/tpt/settlement-callback"})
    public ResponseEntity<?> statementCallback(@RequestBody SettlementNotificationUpdate requestDto) throws MessagingException {
        this.logger.info("Start to Settlement Call Back Api ---/tpt/settlement-callback");
        this.bijlipayTransactionService.pushSettlementNotificationsForToday(requestDto);
        this.logger.info("End For Settlement Call Back Api ---/tpt/settlement-callback");
        return new ResponseEntity(new ApiResponse(HttpStatus.OK, "success", ""), HttpStatus.OK);
    }

    @GetMapping({"/tpt/sync-missing-txn-data"})
    public void syncMissingTxnData() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        String previousDate = DateUtil.yesterday();

        try {
            conn = this.getTSDBConnection();
            stmt = conn.createStatement();
            String settlementQuery = "select  0 as txn_additional_amount,null as auth_code,batch_number,invoice_number,null as auth_code, RIGHT(REPLACE(Card_No, '*', ''), 4) AS masked_card_number , MID as merchant_id ,'0200' as mti,null as panentry_mode,RRN as rrn_number ,Txn_date as response_received_time,stan,TID as terminal_id ,Txnamount as txn_amount ,id as txn_correlation_id, FORMAT(cast(txn_date as DATETIME2) , 'MMdd') as txn_date,'00' as txn_response_code, FORMAT(cast(txn_date as DATETIME2), 'HHmmss') as txn_time,'00' as txn_type,null as urn,LEFT(REPLACE(Card_No, '*', ''), 6) as bin from BijlipaySettlement where Card_No NOT LIKE '%[^0-9*]%' AND TID in (select tid from burgerData where tmd_model like 'ANTERA%' OR tmd_model like 'PAYTIVO%' and Terminal_status='Active') and Paymentdate like '" + previousDate + "%';";
            ResultSet resultSet = stmt.executeQuery(settlementQuery);
            List<Object[]> txnData = new ArrayList();

            while(resultSet.next()) {
                int cols = resultSet.getMetaData().getColumnCount();
                Object[] arr = new Object[cols];

                for(int i = 0; i < cols; ++i) {
                    arr[i] = resultSet.getObject(i + 1);
                }

                txnData.add(arr);
            }

            resultSet.close();
            this.logger.info("ANTERA, PAYTIVO Txn Data Original Size " + previousDate + "---{}", txnData.size());
            this.bijlipayTransactionService.insertAnteraPaytivoTxn(txnData);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.close();
            }

        }

    }

    @PostMapping({"get-pos-transaction-amount", "get-pos-transaction-amount/{action}"})
    @Transactional(
            readOnly = true
    )
    public ResponseEntity<?> getPosTransactionForAmount(@RequestParam("tid") Optional<List<String>> tid, @PathVariable("action") Optional<Integer> action, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestBody TransactionRequestDto requestDto, @RequestParam Map<String, String> requestParams, HttpServletRequest httpServletRequest) {
        this.logger.info("Get transactions pageable : ------------------------------- : ");
        String queryString = httpServletRequest.getQueryString();
        this.logger.info("request param : ------------------------------- : {}", queryString);
        User user = (User)this.userRepository.getOne(this.getSessionUserId());
        TidAmount tidAmount = new TidAmount();
        if (user != null) {
            try {
                List<String> tidData = this.userTidVpaDataService.fetchTidData(user);
                this.logger.info("The tid is in our Switch , fetching Amount ");
                if (from.isEmpty() & to.isEmpty()) {
                    Date date = DateUtil.currentDate();
                    long sec = date.getTime() / 1000L;
                    from = Optional.of(sec);
                    to = Optional.of(sec);
                    requestDto.setDateRange(3);
                    action = action.isPresent() ? action : Optional.of(3);
                    tidAmount.setFlag(true);
                    this.logger.info("Date ----{} Date2--{}", from, to);
                }

                this.logger.info("from--{} to--{}", from.isPresent(), to.isPresent());

                try {
                    Double amount = this.bijlipayTransactionService.getSwitchTransactionsSearchAmount(action, tidData, tid, requestDto, from, to, searchTerm);
                    double response = amount != null ? amount : (double)0.0F;
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    String formatted = decimalFormat.format(response);
                    tidAmount.setAmount(formatted);
                    return new ResponseEntity(new ApiResponse(HttpStatus.OK, "Success", tidAmount), HttpStatus.OK);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } catch (HttpServerErrorException | HttpClientErrorException ce) {
                return new ResponseEntity(new ApiResponse(HttpStatus.BAD_REQUEST, "error", ((HttpStatusCodeException)ce).getResponseBodyAsString()), HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity(new ApiResponse(HttpStatus.OK, "Success"), HttpStatus.OK);
    }
}