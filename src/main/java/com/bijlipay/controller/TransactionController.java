package com.bijlipay.controller;

import com.bijlipay.config.WebConfig;
import com.bijlipay.model.*;
import com.bijlipay.model.dto.*;
import com.bijlipay.model.dto.notification.NotificationResponseDto;
import com.bijlipay.model.dto.notification.SettlementNotificationUpdate;
import com.bijlipay.repository.SettlementRequestResponseRepository;
import com.bijlipay.repository.UserRepository;
import com.bijlipay.service.*;
import com.bijlipay.util.ApiResponse;
import com.bijlipay.util.DateUtil;
import com.google.gson.Gson;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.bijlipay.util.Constant.CATEGORY_TYPE_SETTLEMENT;

@RestController
@RequestMapping("/txn")
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


    @Transactional(readOnly = true)
    @GetMapping(value = {"/fetch-transaction-summary"})
    public ResponseEntity<?> fetchTransactionSummary(@RequestParam("tid") Optional<String> tid, @RequestParam("vpa") Optional<String> vpa, HttpServletRequest httpServletRequest) throws URISyntaxException, IOException, SQLException {
        logger.info("Start to Fetch Transaction Summary Api ----/api/fetch-transaction-summary/{tid}  ---Tid ---{}", tid);
        System.out.println("path " + httpServletRequest.getServletPath());
        User user = userRepository.getOne(getSessionUserId());
        if (user != null) {
            Map<String, Object> map = new HashMap<>();
            List<Object> output = new ArrayList<>();
            List<Object> vpaObject = new ArrayList<>();
            List<Object> voidRevObj = new ArrayList<>();
            List<SwitchTransactionsDto> posTransactions = new ArrayList<>();
            List<AxisQrTransactionDto> qrTransactions = new ArrayList<>();
            if (tid.isPresent() && vpa.isPresent()) {
                UserTidVpaData merchantDeatils = userTidVpaDataService.findByTid(tid.get());
                if (merchantDeatils != null) {
                    List<Object[]> sumOfVoidReversalTxnAmount = bijlipayTransactionService.getSumOfTodayAndYesterdayVoidAndReversalTxnAmount(tid.get(), getSessionUserId());
                    List<Object[]> TodayAndYesterdaySumCountForTid = bijlipayTransactionService.getSumAndCountOfTodayAndYesterdayTxnAmountForTid(tid.get(), getSessionUserId());
                    List<Object[]> TodayAndYesterdaySumCountForVpa = bijlipayTransactionService.getSumAndCountOfTodayAndYesterdayTxnAmountForVpa(vpa.get(), getSessionUserId());
                    posTransactions = bijlipayTransactionService.getLastFiveSwicthTransactions(tid.get());
                    qrTransactions = bijlipayTransactionService.getLastFiveAxisQrTransactions(vpa.get());
                    TodayAndYesterdaySumCountForTid.forEach(l -> {
                        Map<String, Object> maps = new HashMap<>();
                        maps.put("todaySum", l[0]);
                        maps.put("todayCount", l[1]);
                        maps.put("yesterdaySum", l[2]);
                        maps.put("yesterdayCount", l[3]);
                        output.add(maps);
                    });
                    TodayAndYesterdaySumCountForVpa.forEach(l -> {
                        Map<String, Object> maps = new HashMap<>();
                        maps.put("todaySum", l[0]);
                        maps.put("todayCount", l[1]);
                        maps.put("yesterdaySum", l[2]);
                        maps.put("yesterdayCount", l[3]);
                        vpaObject.add(maps);
                    });
                    sumOfVoidReversalTxnAmount.forEach(l -> {
                        Map<String, Object> maps = new HashMap<>();
                        maps.put("todayVoidReversalSum", l[0]);
                        maps.put("todayVoidReversalCount", l[1]);
                        maps.put("yesterdayVoidReversalSum", l[2]);
                        maps.put("yesterdayVoidReversalCount", l[3]);
                        voidRevObj.add(maps);
                    });
                    map.put("voidReversal", voidRevObj);
                    map.put("tid", output);
                    map.put("staticQR", vpaObject);
                    map.put("lastFiveTxnAmount", posTransactions);
                    map.put("lastFiveTxnAmountForQr", qrTransactions);
                    logger.info("End For Fetch Transaction Summary Api ----/api/fetch-transaction-summary/{tid}");
                    return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", map), HttpStatus.OK);
                }
            } else if (tid.isPresent()) {
                UserTidVpaData merchantDeatils = userTidVpaDataService.findByTid(tid.get());
                if (merchantDeatils != null) {
                    List<Object[]> sumOfVoidReversalTxnAmount = bijlipayTransactionService.getSumOfTodayAndYesterdayVoidAndReversalTxnAmount(tid.get(), getSessionUserId());
                    List<Object[]> TodayAndYesterdaySumCountForTid = bijlipayTransactionService.getSumAndCountOfTodayAndYesterdayTxnAmountForTid(tid.get(), getSessionUserId());
                    posTransactions = bijlipayTransactionService.getLastFiveSwicthTransactions(tid.get());
                    TodayAndYesterdaySumCountForTid.forEach(l -> {
                        Map<String, Object> maps = new HashMap<>();
                        maps.put("todaySum", l[0]);
                        maps.put("todayCount", l[1]);
                        maps.put("yesterdaySum", l[2]);
                        maps.put("yesterdayCount", l[3]);
                        output.add(maps);
                    });
                    sumOfVoidReversalTxnAmount.forEach(l -> {
                        Map<String, Object> maps = new HashMap<>();
                        maps.put("todayVoidReversalSum", l[0]);
                        maps.put("todayVoidReversalCount", l[1]);
                        maps.put("yesterdayVoidReversalSum", l[2]);
                        maps.put("yesterdayVoidReversalCount", l[3]);
                        voidRevObj.add(maps);
                    });
                    map.put("voidReversal", voidRevObj);
                    map.put("tid", output);
                    map.put("staticQR", vpaObject);
                    map.put("lastFiveTxnAmount", posTransactions);
                    map.put("lastFiveTxnAmountForQr", qrTransactions);
                    logger.info("End For Fetch Transaction Summary Api ----/api/fetch-transaction-summary/{tid}");
                    return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", map), HttpStatus.OK);
                }
            } else if (vpa.isPresent()) {
                List<String> vpaDetails = userTidVpaDataService.findByVpa(user);
                List<Object[]> TodayAndYesterdaySumCountForVpa = bijlipayTransactionService.getSumAndCountOfTodayAndYesterdayTxnAmountForVpa(vpa.get(), getSessionUserId());
                qrTransactions = bijlipayTransactionService.getLastFiveAxisQrTransactions(vpa.get());
                TodayAndYesterdaySumCountForVpa.forEach(l -> {
                    Map<String, Object> maps = new HashMap<>();
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
                map.put("lastFiveTxnAmountForQr", qrTransactions);
                logger.info("End For Fetch Transaction Summary Api ----/api/fetch-transaction-summary/{tid}");
                return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", map), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND, "Merchant not found!"), HttpStatus.NOT_FOUND);
    }

    @Transactional(readOnly = true)
    @GetMapping(value = {"/fetch-transaction-summary-portal"})
    public ResponseEntity<?> fetchTransactionSummaryForPortal(@RequestParam("tid") Optional<String> tid, @RequestParam("vpa") Optional<String> vpa, HttpServletRequest httpServletRequest) throws URISyntaxException, IOException, SQLException {
        logger.info("Start to Fetch Transaction Summary Api ----/api/fetch-transaction-summary/{tid}  ---Tid ---{}", tid);
        System.out.println("path " + httpServletRequest.getServletPath());
        User user = userRepository.getOne(getSessionUserId());
        if (user != null) {
            List<String> tidList = userTidVpaDataService.fetchTidData(user);
            List<String> vpaList = userTidVpaDataService.findByVpa(user);
            Map<String, Object> map = new HashMap<>();
            List<Object> output = new ArrayList<>();
            List<Object> vpaObject = new ArrayList<>();
            List<Object> voidRevObj = new ArrayList<>();
            List<SwitchTransactionsDto> posTransactions = new ArrayList<>();
            List<AxisQrTransactionDto> qrTransactions = new ArrayList<>();
            if (tid.isPresent() && vpa.isPresent()) {
                UserTidVpaData merchantDeatils = userTidVpaDataService.findByTid(tid.get());
                if (merchantDeatils != null) {
                    List<Object[]> sumOfVoidReversalTxnAmount = bijlipayTransactionService.getSumOfTodayAndYesterdayVoidAndReversalTxnAmount(tid.get(), getSessionUserId());
                    List<Object[]> TodayAndYesterdaySumCountForTid = bijlipayTransactionService.getSumAndCountOfTodayAndYesterdayTxnAmountForTid(tid.get(), getSessionUserId());
                    List<Object[]> TodayAndYesterdaySumCountForVpa = bijlipayTransactionService.getSumAndCountOfTodayAndYesterdayTxnAmountForVpa(vpa.get(), getSessionUserId());
                    posTransactions = bijlipayTransactionService.getLastFiveSwicthTransactionsBasedOnUserPortal(tidList);
                    qrTransactions = bijlipayTransactionService.getLastFiveAxisQrTransactionsBasedOnUserPortal(vpaList);
                    TodayAndYesterdaySumCountForTid.forEach(l -> {
                        Map<String, Object> maps = new HashMap<>();
                        maps.put("todaySum", l[0]);
                        maps.put("todayCount", l[1]);
                        maps.put("yesterdaySum", l[2]);
                        maps.put("yesterdayCount", l[3]);
                        output.add(maps);
                    });
                    TodayAndYesterdaySumCountForVpa.forEach(l -> {
                        Map<String, Object> maps = new HashMap<>();
                        maps.put("todaySum", l[0]);
                        maps.put("todayCount", l[1]);
                        maps.put("yesterdaySum", l[2]);
                        maps.put("yesterdayCount", l[3]);
                        vpaObject.add(maps);
                    });
                    sumOfVoidReversalTxnAmount.forEach(l -> {
                        Map<String, Object> maps = new HashMap<>();
                        maps.put("todayVoidReversalSum", l[0]);
                        maps.put("todayVoidReversalCount", l[1]);
                        maps.put("yesterdayVoidReversalSum", l[2]);
                        maps.put("yesterdayVoidReversalCount", l[3]);
                        voidRevObj.add(maps);
                    });
                    map.put("voidReversal", voidRevObj);
                    map.put("tid", output);
                    map.put("staticQR", vpaObject);
                    map.put("lastFiveTxnAmount", posTransactions);
                    map.put("lastFiveTxnAmountForQr", qrTransactions);
                    logger.info("End For Fetch Transaction Summary Api ----/api/fetch-transaction-summary/{tid}");
                    return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", map), HttpStatus.OK);
                }
            } else if (tid.isPresent()) {
                UserTidVpaData merchantDeatils = userTidVpaDataService.findByTid(tid.get());
                if (merchantDeatils != null) {
                    List<Object[]> sumOfVoidReversalTxnAmount = bijlipayTransactionService.getSumOfTodayAndYesterdayVoidAndReversalTxnAmount(tid.get(), getSessionUserId());
                    List<Object[]> TodayAndYesterdaySumCountForTid = bijlipayTransactionService.getSumAndCountOfTodayAndYesterdayTxnAmountForTid(tid.get(), getSessionUserId());
                    posTransactions = bijlipayTransactionService.getLastFiveSwicthTransactionsBasedOnUserPortal(tidList);
                    TodayAndYesterdaySumCountForTid.forEach(l -> {
                        Map<String, Object> maps = new HashMap<>();
                        maps.put("todaySum", l[0]);
                        maps.put("todayCount", l[1]);
                        maps.put("yesterdaySum", l[2]);
                        maps.put("yesterdayCount", l[3]);
                        output.add(maps);
                    });
                    sumOfVoidReversalTxnAmount.forEach(l -> {
                        Map<String, Object> maps = new HashMap<>();
                        maps.put("todayVoidReversalSum", l[0]);
                        maps.put("todayVoidReversalCount", l[1]);
                        maps.put("yesterdayVoidReversalSum", l[2]);
                        maps.put("yesterdayVoidReversalCount", l[3]);
                        voidRevObj.add(maps);
                    });
                    map.put("voidReversal", voidRevObj);
                    map.put("tid", output);
                    map.put("staticQR", vpaObject);
                    map.put("lastFiveTxnAmount", posTransactions);
                    map.put("lastFiveTxnAmountForQr", qrTransactions);
                    logger.info("End For Fetch Transaction Summary Api ----/api/fetch-transaction-summary/{tid}");
                    return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", map), HttpStatus.OK);
                }
            } else if (vpa.isPresent()) {
                List<String> vpaDetails = userTidVpaDataService.findByVpa(user);
                List<Object[]> TodayAndYesterdaySumCountForVpa = bijlipayTransactionService.getSumAndCountOfTodayAndYesterdayTxnAmountForVpa(vpa.get(), getSessionUserId());
                qrTransactions = bijlipayTransactionService.getLastFiveAxisQrTransactionsBasedOnUserPortal(vpaList);
                TodayAndYesterdaySumCountForVpa.forEach(l -> {
                    Map<String, Object> maps = new HashMap<>();
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
                map.put("lastFiveTxnAmountForQr", qrTransactions);
                logger.info("End For Fetch Transaction Summary Api ----/api/fetch-transaction-summary/{tid}");
                return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", map), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND, "Merchant not found!"), HttpStatus.NOT_FOUND);
    }

    /**
     * Action
     * 1 - Date Range
     * <p>
     * Value
     * 1 - This Month Data
     * 2 - Last 30 Days Data
     * 3 - Choose Date
     */
    @GetMapping(value = {"/getQrTransaction-pageable", "/getQrTransaction-pageable/{action}/{value}"})
//    @Transactional(readOnly = true)
    public ResponseEntity<?> getAxisQrTransactions(@PathVariable("action") Optional<Integer> action, @PathVariable("value") Optional<Integer> value, @RequestParam("vpa") Optional<List<String>> vpa, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestParam Map<String, String> requestParams, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to, Pageable pageable, HttpServletRequest httpServletRequest) throws URISyntaxException, IOException, SQLException, java.text.ParseException {
        String queryString = httpServletRequest.getQueryString();
        logger.info("request param : ------------------------------- : {}", queryString);
        User user = userRepository.getOne(getSessionUserId());
        pageable = webConfig.resolvePageableCamelToSnake(requestParams, pageable);
        if (user != null) {
            List<String> vpaDetails = userTidVpaDataService.findByVpa(user);
            logger.info("Get transactions based on Vpa  : ------------------------------- : {}", vpaDetails.get(0));
            Page<AxisQrTransactionDto> response = bijlipayTransactionService.getAxisQrTransactionDetails(action, value, searchTerm, from, to, vpaDetails, vpa, pageable);
            if (!response.isEmpty()) {
                return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success", response), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND, "Data Not Found !"), HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND, "User Not Found !"), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = {"/getQrTransaction-amount", "/getQrTransaction-amount/{action}/{value}"})
//    @Transactional(readOnly = true)
    public ResponseEntity<?> getAxisQrTransactionsAmount(@PathVariable("action") Optional<Integer> action, @PathVariable("value") Optional<Integer> value, @RequestParam("vpa") Optional<List<String>> vpa, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestParam Map<String, String> requestParams, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to, HttpServletRequest httpServletRequest) throws URISyntaxException, IOException, SQLException, java.text.ParseException {
        String queryString = httpServletRequest.getQueryString();
        logger.info("request param : ------------------------------- : {}", queryString);
        User user = userRepository.getOne(getSessionUserId());
        if (user != null) {
            List<String> vpaDetails = userTidVpaDataService.findByVpa(user);
            logger.info("Get transactions based on Vpa  : ------------------------------- : {}", vpaDetails.get(0));
            Double amount = bijlipayTransactionService.getAxisQrTransactionDetailsAmount(action, value, searchTerm, from, to, vpaDetails, vpa);
            double response=amount!=null?amount:0;
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                String formatted = decimalFormat.format(response);
                return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success", formatted), HttpStatus.OK);

        } else {
            return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND, "User Not Found !"), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Action
     * 1 - Txn Status
     * Value
     * 1 - Success
     * 2 - Failure
     * <p>
     * 2 - Txn Type
     * Value
     * 1 - Upi
     * 2 - Card
     * 3 - Void
     * <p>
     * 3 - Date Range
     * Value
     * 1 - This Month Data
     * 2 - Last 30 Days Data
     * 3 - Choose Date
     * <p>
     * 4 - All Actions
     */
    @PostMapping(value = {"get-pos-transaction-pageable", "get-pos-transaction-pageable/{action}"})
//    @Transactional(readOnly = true)
    public ResponseEntity<?> getPosTransaction(@RequestParam("tid") Optional<List<String>> tid, @PathVariable("action") Optional<Integer> action, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestBody TransactionRequestDto requestDto, @RequestParam Map<String, String> requestParams, Pageable pageable, HttpServletRequest httpServletRequest) {
        logger.info("Get transactions pageable : ------------------------------- : ");
        String queryString = httpServletRequest.getQueryString();
        pageable = webConfig.resolvePageableCamelToSnake(requestParams, pageable);
        logger.info("request param : ------------------------------- : {}", queryString);
        User user = userRepository.getOne(getSessionUserId());
        if (user != null) {
            try {
                List<String> tidData = userTidVpaDataService.fetchTidData(user);
                logger.info("The tid is in our Switch , fetching SWITCH txns ");
                try {
                    Page<SwitchTransactionsDto> response = bijlipayTransactionService.getSwitchTransactionsSearch(action, tidData, tid, requestDto, from, to, searchTerm, pageable);
                    if (!response.isEmpty()) {
                        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success", response), HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND, "Data Not Found !"), HttpStatus.NOT_FOUND);
                    }
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
            } catch (HttpClientErrorException | HttpServerErrorException ce) {
                return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, "error", ce.getResponseBodyAsString()), HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success"), HttpStatus.OK);
    }

    @PostMapping(value = {"get-pos-transaction-pageable-portal", "get-pos-transaction-pageable-portal/{action}"})
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPosTransactionForPortal(@RequestParam("tid") Optional<List<String>> tid, @PathVariable("action") Optional<Integer> action, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestBody TransactionRequestDto requestDto, @RequestParam Map<String, String> requestParams, Pageable pageable, HttpServletRequest httpServletRequest) {
        logger.info("Get transactions pageable : ------------------------------- : ");
        String queryString = httpServletRequest.getQueryString();
        pageable = webConfig.resolvePageableCamelToSnake(requestParams, pageable);
        logger.info("request param : ------------------------------- : {}", queryString);
//       Optional<List<String>> tids= Optional.ofNullable(requestDto.getTid());
        User user = userRepository.getOne(getSessionUserId());
        if (user != null) {
            try {
                List<String> tidData = userTidVpaDataService.fetchTidData(user);
                logger.info("The tid is in our Switch , fetching SWITCH txns ");
                try {
                    Page<SwitchTransactionsDto> response = bijlipayTransactionService.getSwitchTransactionsSearchData(action, tidData, tid, requestDto, from, to, searchTerm, pageable);
                    if (!response.isEmpty()) {
                        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success", response), HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND, "Data Not Found !"), HttpStatus.NOT_FOUND);
                    }
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
            } catch (HttpClientErrorException | HttpServerErrorException ce) {
                return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, "error", ce.getResponseBodyAsString()), HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success"), HttpStatus.OK);
    }

    @GetMapping("get-chargeslip-data/{rrn}/{txn_type}/{tid}")
    public ResponseEntity<?> fetchChargeSlipData(@PathVariable("rrn") String rrn, @PathVariable("txn_type") String txnType,@PathVariable("tid") String tid) {
        Connection con = null;
        Statement stmt = null;
        String query = "select * FROM " + chargeSlipDb + ".charge_slips_data cs WHERE cs.txn_type = '" + txnType + "' AND cs.tid = '" + tid +"'AND cs.rrn = '" + rrn + "'";
        try {
            con = getConnection();
            stmt = con.createStatement();
            List<ChargeSlipDto> chargeSlipDtoList = new ArrayList<>();
            try {
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()) {
                    ChargeSlipDto chargeSlipDto = new ChargeSlipDto();
                    chargeSlipDto.setId(rs.getLong("charge_slip_id"));
                    chargeSlipDto.setAPPLICATION_ID(rs.getString("application_id"));
                    chargeSlipDto.setAPPLICATION_NAME(rs.getString("application_name"));
                    chargeSlipDto.setAPPLICATION_VERSION(rs.getString("application_version"));
                    chargeSlipDto.setBANK_LOGO_ID(rs.getString("bank_logo_id"));
                    chargeSlipDto.setAUTH_CODE(rs.getString("auth_code"));
                    chargeSlipDto.setBATCH_NUMBER(rs.getString("batch_number"));
                    chargeSlipDto.setCARD_HOLDER_NAME(rs.getString("card_holder_name"));
                    chargeSlipDto.setCARD_MASKED(rs.getString("card_masked"));
                    chargeSlipDto.setCARD_TYPE(rs.getString("card_type"));
                    chargeSlipDto.setINVOICE_NUMBER(rs.getString("invoice_number"));
                    chargeSlipDto.setLOCATION(rs.getString("location"));
                    chargeSlipDto.setMERCHANT_NAME(rs.getString("merchant_name"));
                    chargeSlipDto.setMID(rs.getString("mid"));
                    chargeSlipDto.setMOBILE_NUMBER(rs.getString("mobile_number"));
                    chargeSlipDto.setMODE(rs.getString("mode"));
                    chargeSlipDto.setRECEIPT_HEADER1(rs.getString("receipt_header1"));
                    chargeSlipDto.setRRN(rs.getString("rrn"));
                    chargeSlipDto.setTID(rs.getString("tid"));
                    chargeSlipDto.setTSI(rs.getString("tsi"));
                    chargeSlipDto.setTVR(rs.getString("tvr"));
                    chargeSlipDto.setTXN_AMOUNT_SALE(rs.getString("txn_amount_sale"));
                    chargeSlipDto.setTXN_AMOUNT_TIP(rs.getString("txn_amount_tip"));
                    chargeSlipDto.setTXN_AMOUNT_TOTAL(rs.getString("txn_amount_total"));
                    chargeSlipDto.setTXN_CERTIFICATE(rs.getString("txn_certificate"));
                    chargeSlipDto.setTXN_DATE(rs.getString("txn_date"));
                    chargeSlipDto.setTXN_TIME(rs.getString("txn_time"));
                    chargeSlipDto.setTXN_TYPE(rs.getString("txn_type"));
                    chargeSlipDto.setCreatedDate(rs.getDate("created_date"));
                    chargeSlipDto.setPIN_VERIFICATION(rs.getString("pin_verification"));
                    chargeSlipDtoList.add(chargeSlipDto);
                }
                logger.info("charge slip data success",chargeSlipDtoList);
                return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", chargeSlipDtoList), HttpStatus.OK);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                stmt.close();
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("charge slip data not found");
            return new ResponseEntity<>(new ApiResponse(HttpStatus.CONFLICT, "Data not Found"), HttpStatus.CONFLICT);
        }
    }

    @PostMapping(value = {"/api/download-pos-txn", "/api/download-pos-txn/{action}"})
    public ResponseEntity<?> downloadBpRegionsMenu(HttpServletResponse response, @RequestParam("tid") Optional<String> tid, @PathVariable("action") Optional<Integer> action, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestBody TransactionRequestDto requestDto) throws IOException {
        logger.info("Start to Download Tid Transaction Api ----/api/download/{tid} ---Tid --{}", tid);
        try {
            logger.info("Switch Transactions download report");
            Optional<User> user = userRepository.findById(getSessionUserId());
            if (user.isPresent()) {
                logger.info("Merchant details : ---- : {}", user.get().getMobileNo());
                List<UserTidVpaData> merchantDetails = userTidVpaDataService.getByUserId(user.get());
                if (merchantDetails.size() > 0) {
                    List<String> tidData = userTidVpaDataService.fetchTidData(user.get());
                    List<String> mid = merchantDetails.stream().filter(m -> m.getTid() != null).map(UserTidVpaData::getMid).toList();
//                    String mid = merchantDetails.get(0).getMid();
                    String fileName = "";
                    if (from.isPresent() && to.isPresent())
                        fileName = String.format(mid.get(0) + "%s.xlsx", DateUtil.longtoDate(from.get(), "yyyMMdd") + DateUtil.longtoDate(to.get(), "yyyMMdd"));
                    else fileName = mid.get(0) + ".xlsx";
                    logger.info("The file name : ------------- : {}", fileName);
                    response.setContentType("application/vnd.ms-excel");
                    response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    InputStreamResource file = bijlipayTransactionService.downloadTransactionReport(action, tidData, tid, requestDto, from, to, searchTerm);
                    logger.info("Work book is created : --- : {}", file.getFilename());
                    if (file == null) {
                        return new ResponseEntity<>(new ApiResponse(HttpStatus.NO_CONTENT, "NO DATA FOUND"), HttpStatus.NO_CONTENT);
                    }
//                    try {
//                        workBook.write(out);
//                        workBook.dispose();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        throw new RuntimeException("Error while generating Excel report", e);
//                    } finally {
//                        try {
//                            workBook.close();
//                        } catch (IOException e) {
//                            logger.error("Error closing workbook: {}", e.getMessage());
//                        }
//                    }
//                    ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray());
//                    InputStreamResource file = new InputStreamResource(input);
                    logger.info("End For Download Tid Transaction Api ----/api/download/{tid}");
                    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName).contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
                } else {
                    return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND, "User not mapped with tid !"), HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND, "User not found !"), HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "The Exception occurred while generating the Open Tracker Excel Report " + e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = {"/api/download-pos-txn-portal", "/api/download-pos-txn-portal/{action}"})
    public ResponseEntity<?> downloadPosTxnData(@RequestParam("tid") Optional<String> tid, @PathVariable("action") Optional<Integer> action, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestBody TransactionRequestDto requestDto) throws IOException {
        logger.info("Start to Download Tid Transaction Api ----/api/download/{tid} ---Tid --{}", tid);
        try {
            logger.info("Switch Transactions download report");
            Optional<User> user = userRepository.findById(getSessionUserId());
            if (user.isPresent()) {
                logger.info("Merchant details : ---- : {}", user.get().getMobileNo());
                List<UserTidVpaData> merchantDetails = userTidVpaDataService.getByUserId(user.get());

                if (merchantDetails.size() > 0) {
                    List<String> tidData = userTidVpaDataService.fetchTidData(user.get());
                    List<String> mid = merchantDetails.stream().filter(m -> m.getTid() != null).map(UserTidVpaData::getMid).toList();
//                    String mid = merchantDetails.get(0).getMid();
                    String fileName = "";
                    if (from.isPresent() && to.isPresent())
                        fileName = String.format(mid.get(0) + "%s.xlsx", DateUtil.longtoDate(from.get(), "yyyMMdd") + DateUtil.longtoDate(to.get(), "yyyMMdd"));
                    else fileName = mid.get(0) + ".xlsx";
                    logger.info("The file name : ------------- : {}", fileName);
//                    response.setContentType("application/vnd.ms-excel");
//                    response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
//                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    InputStreamResource file = bijlipayTransactionService.downloadTransactionReport(action, tidData, tid, requestDto, from, to, searchTerm);
                    logger.info("Work book is created : --- : {}", file.getFilename());
                    if (file == null) {
                        return new ResponseEntity<>(new ApiResponse(HttpStatus.NO_CONTENT, "NO DATA FOUND"), HttpStatus.NO_CONTENT);
                    }
//                    workBook.write(out);
//                    ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray());
//                    InputStreamResource file = new InputStreamResource(input);
//                    workBook.close();
                    logger.info("End For Download Tid Transaction Api ----/api/download/{tid}");
                    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName).contentType(MediaType.APPLICATION_OCTET_STREAM).body(file);
                } else {
                    return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND, "User not mapped with tid !"), HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND, "User not found !"), HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "The Exception occurred while generating the Open Tracker Excel Report " + e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Action
     * 1 - Date Range
     * <p>
     * Value
     * 1 - This Month Data
     * 2 - Last 30 Days Data
     * 3 - Choose Date
     */
    @GetMapping(value = {"/api/downloadAxisQrTxnData", "/api/downloadAxisQrTxnData/{action}/{value}"})
    public ResponseEntity<?> downloadQrTransactionData(HttpServletResponse response, @PathVariable("action") Optional<Integer> action, @PathVariable("value") Optional<Integer> value, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestParam Map<String, String> requestParams, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to,@RequestParam("vpa") Optional<String> vpa) throws IOException {
        logger.info("Start to Download Axis QR Transaction Api ----/api/downloadAxisQrTxnData");
        try {
            User user = userRepository.findByUser(getSessionUserId());
            if (user != null) {
                logger.info("User details : ---- : {}", user.getId());
                List<String> vpaDetails = userTidVpaDataService.findByVpa(user);
                String fileName = "";
                if (from.isPresent() && to.isPresent())
                    fileName = String.format("Qr_Txn_Data_%s.xlsx", DateUtil.longtoDate(from.get(), "yyyMMdd") + DateUtil.longtoDate(to.get(), "yyyMMdd"));
                else fileName = String.format("Qr_Txn_Data_%s.xlsx", DateUtil.getDate());
                logger.info("The file name : ------------- : {}", fileName);
                response.setContentType("application/vnd.ms-excel");
                response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                InputStreamResource file = bijlipayTransactionService.downloadQrTransactionReport(action, value, searchTerm, from, to, vpaDetails,vpa);
                logger.info("Work book is created : --- : {}", file.getFilename());
                if (file == null) {
                    return new ResponseEntity<>(new ApiResponse(HttpStatus.NO_CONTENT, "NO DATA FOUND"), HttpStatus.NO_CONTENT);
                }
//                workBook.write(out);
//                ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray());
//                InputStreamResource file = new InputStreamResource(input);
//                workBook.close();
                logger.info("End For Download Axis QR Transaction Api ----/api/downloadAxisQrTxnData");
                return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName).contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
            } else {
                return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND, "User not found !"), HttpStatus.NOT_FOUND);
            }
        } catch (java.text.ParseException ex) {
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Action
     * 1 - Date Range
     * <p>
     * Value
     * 1 - This Month Data
     * 2 - Last 30 Days Data
     * 3 - Choose Date
     */
    @GetMapping(value = {"/api/downloadAxisQrTxnDataForPortal", "/api/downloadAxisQrTxnDataForPortal/{action}/{value}"})
    public ResponseEntity<?> downloadAxisQrTxnDataForPortal(@PathVariable("action") Optional<Integer> action, @PathVariable("value") Optional<Integer> value, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestParam("vpa") Optional<String> vpa, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to) throws IOException {
        logger.info("Start to Download Axis QR Transaction Api ----/api/downloadAxisQrTxnData");
        try {
            User user = userRepository.findByUser(getSessionUserId());
            if (user != null) {
                logger.info("User details : ---- : {}", user.getId());
                List<String> vpaDetails = userTidVpaDataService.findByVpa(user);
                String fileName = "";
                if (from.isPresent() && to.isPresent())
                    fileName = String.format("Qr_Txn_Data_%s.xlsx", DateUtil.longtoDate(from.get(), "yyyMMdd") + DateUtil.longtoDate(to.get(), "yyyMMdd"));
                else fileName = String.format("Qr_Txn_Data_%s.xlsx", DateUtil.getDate());
                logger.info("The file name : ------------- : {}", fileName);
//                response.setContentType("application/vnd.ms-excel");
//                response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                InputStreamResource file = bijlipayTransactionService.downloadQrTransactionReportData(action, value, searchTerm, from, to, vpaDetails,vpa);
                logger.info("Work book is created : --- : {}", file.getFilename());
                if (file == null) {
                    return new ResponseEntity<>(new ApiResponse(HttpStatus.NO_CONTENT, "NO DATA FOUND"), HttpStatus.NO_CONTENT);
                }
//                workBook.write(out);
//                ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray());
//                InputStreamResource file = new InputStreamResource(input);
//                workBook.close();
                logger.info("End For Download Axis QR Transaction Api ----/api/downloadAxisQrTxnData");
                return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName).contentType(MediaType.APPLICATION_OCTET_STREAM).body(file);
            } else {
                return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND, "User not found !"), HttpStatus.NOT_FOUND);
            }
        } catch (java.text.ParseException ex) {
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @GetMapping("/settlement-report")
    public ResponseEntity<?> settlementReportRequest(@RequestParam("tid") Optional<String> tid, @RequestParam("vpa") Optional<String> vpa, @RequestParam("from") Optional<Long> from, @RequestParam("to") Optional<Long> to, @RequestParam("fileType") String fileType) throws MessagingException, URISyntaxException, ParseException, java.text.ParseException {
        logger.info("Start to Get Settlement Report Api ----/api/settlement-report-send-mail");
        User user = userRepository.findByUser(getSessionUserId());
        List<UserTidVpaData> userTidVpaData = userTidVpaDataService.getByUserId(user);
        if (user == null) {
            return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, "user not found"), HttpStatus.BAD_REQUEST);
        }
        logger.info("User --{}", getSessionUserId());
        String vFrom = "";
        String vTo = "";
        if (from.isPresent() && to.isPresent()) {
            vFrom = DateUtil.longtoDate(from.get(), "dd-MM-yyyy");
            vTo = DateUtil.longtoDate(to.get(), "dd-MM-yyyy");
        }
        if (tid.isPresent()) {
            List<String> mid = userTidVpaData.stream().filter(m -> m.getTid() != null && m.getTid().equalsIgnoreCase(tid.get())).map(UserTidVpaData::getMid).toList();
            List<String> institution = userTidVpaData.stream().filter(m -> m.getTid() != null && m.getTid().equalsIgnoreCase(tid.get())).map(UserTidVpaData::getInstitutionCode).toList();
            logger.info("mid ----------- : {} , institution ----------: {}",mid,institution);
            logger.info("Settlement Controller options tid: ----------- : {} : ----------- : fdate {} : ----------- : toDate {}", tid, vFrom, vTo);
            SettlementReportsDto settlementReportsDto = new SettlementReportsDto();
            settlementReportsDto.setMerchantId(mid.size() > 0 ? mid.get(0) : null);
            settlementReportsDto.setFromDate(vFrom);
            settlementReportsDto.setToDate(vTo);
            settlementReportsDto.setTerminalId(tid.get());
            String uniqueId = String.valueOf(System.currentTimeMillis());
            settlementReportsDto.setUniqueId(uniqueId);
            settlementReportsDto.setFileType(fileType);
            logger.info("Request DTO : ------------- : {}", settlementReportsDto);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("content-type", "application/json");
//            httpHeaders.set("NII", nIi);
            String institutionCode = institution.size() > 0 ? institution.get(0) : null;
            logger.info("institution ------------- : {}",institutionCode);
            httpHeaders.set("NII", institutionCode);
            HttpEntity<SettlementReportsDto> httpEntity = new HttpEntity<>(settlementReportsDto, httpHeaders);
            URI uri = null;
            if (institutionCode != null && institutionCode.equalsIgnoreCase("104")){
                uri = new URI(axisSettlementReportUrl);
            }else {
                uri = new URI(settlementReportUrl);
            }
            ResponseEntity<String> stringResponseEntity = null;
            JSONObject response;
            JSONParser parser = new JSONParser();
            try {
                RestTemplate restTemplate = new RestTemplate();
                logger.info("Trying to connect with Mars settlement report Api : ----- : {}", uri);
                stringResponseEntity = restTemplate.postForEntity(uri, httpEntity, String.class);
                logger.info("Got response : ---------- : {}", stringResponseEntity);

                SettlementRequestResponse settlementRequestResponse = new SettlementRequestResponse();
                settlementRequestResponse.setTid(settlementReportsDto.getTerminalId());
                settlementRequestResponse.setMid(settlementReportsDto.getMerchantId());
                settlementRequestResponse.setFromDate(settlementReportsDto.getFromDate());
                settlementRequestResponse.setToDate(settlementReportsDto.getToDate());
                settlementRequestResponse.setUniqueId(uniqueId);
                settlementRequestResponse.setUser(user);
                settlementRequestResponse.setCategoryType(CATEGORY_TYPE_SETTLEMENT);
                settlementRequestResponse.setFileType(fileType);
                settlementRequestResponseRepository.save(settlementRequestResponse);

            } catch (HttpClientErrorException clientErrorException) {
                logger.info("HttpClientErrorException getMessage {}", clientErrorException.getMessage());
                logger.info("HttpClientErrorException getLocalizedMessage {}", clientErrorException.getLocalizedMessage());
//                response = (JSONObject) parser.parse(clientErrorException.getResponseBodyAsString());
                return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, "error", clientErrorException.getResponseBodyAsString()), HttpStatus.BAD_REQUEST);
            } catch (HttpServerErrorException httpServerErrorException) {
                logger.info("HttpServerErrorException {}", httpServerErrorException.getMessage());
                logger.info("HttpClientErrorException httpServerErrorException {}", httpServerErrorException.getLocalizedMessage());
//                response = (JSONObject) parser.parse(httpServerErrorException.getResponseBodyAsString());
                return new ResponseEntity<>(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", httpServerErrorException.getResponseBodyAsString()), HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("Exception e {}", e.getMessage());
//                response = (JSONObject) parser.parse(e.getMessage());
                return new ResponseEntity<>(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error",e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            logger.info("End For Get Settlement Report Api");
            return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", stringResponseEntity.toString()), HttpStatus.OK);
        } else {
            logger.info("Settlement Controller options vpa : ----------- : {} : ----------- : fdate {} : ----------- : toDate {}", vpa, vFrom, vTo);
//            List<String> qrVpa = new ArrayList<>();
//            userTidVpaData.forEach(l -> {
//                qrVpa.add(l.getVpa());
//            });
//            String listString = String.join(", ", qrVpa);
//            logger.info("Qr Vpa Details ---{}", listString);
            logger.info("Qr Vpa Details ---{}", vpa.orElse(null));
            List<String> institution = userTidVpaData.stream().filter(m -> m.getVpa() != null && m.getVpa().equalsIgnoreCase(vpa.get())).map(UserTidVpaData::getInstitutionCode).toList();
            SettlementReportQrDto settlementReportQrDto = new SettlementReportQrDto();
            settlementReportQrDto.setVpa(vpa.orElse(null));
            settlementReportQrDto.setFromDate(vFrom);
            settlementReportQrDto.setToDate(vTo);
            String uniqueId = String.valueOf(System.currentTimeMillis());
            settlementReportQrDto.setUniqueId(uniqueId);
            settlementReportQrDto.setFileType(fileType);
            logger.info("Request DTO : ------------- : {}", settlementReportQrDto);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("content-type", "application/json");
//            httpHeaders.set("NII", nIi);
            httpHeaders.set("NII", institution.size() > 0 ? institution.get(0) : null);
            HttpEntity<SettlementReportQrDto> httpEntity = new HttpEntity<>(settlementReportQrDto, httpHeaders);
            URI uri = new URI(staticQRSettlementReportUrl);
            ResponseEntity<String> stringResponseEntity = null;
            JSONObject response;
            JSONParser parser = new JSONParser();
            try {
                RestTemplate restTemplate = new RestTemplate();
                logger.info("Trying to connect with Mars settlement report Api : ----- : {}", settlementReportUrl);
                stringResponseEntity = restTemplate.postForEntity(uri, httpEntity, String.class);
                logger.info("Got response : ---------- : {}", stringResponseEntity);

                SettlementRequestResponse settlementRequestResponse = new SettlementRequestResponse();
                settlementRequestResponse.setVpa(settlementReportQrDto.getVpa());
                settlementRequestResponse.setFromDate(settlementReportQrDto.getFromDate());
                settlementRequestResponse.setToDate(settlementReportQrDto.getToDate());
                settlementRequestResponse.setUniqueId(uniqueId);
                settlementRequestResponse.setUser(user);
                settlementRequestResponse.setCategoryType(CATEGORY_TYPE_SETTLEMENT);
                settlementRequestResponse.setFileType(fileType);
                settlementRequestResponseRepository.save(settlementRequestResponse);

            } catch (HttpClientErrorException clientErrorException) {
                logger.info("HttpClientErrorException getMessage {}", clientErrorException.getMessage());
                logger.info("HttpClientErrorException getLocalizedMessage {}", clientErrorException.getLocalizedMessage());
//                response = (JSONObject) parser.parse(clientErrorException.getResponseBodyAsString());
                return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, "error", clientErrorException.getResponseBodyAsString()), HttpStatus.BAD_REQUEST);
            } catch (HttpServerErrorException httpServerErrorException) {
                logger.info("HttpServerErrorException {}", httpServerErrorException.getMessage());
                logger.info("HttpClientErrorException httpServerErrorException {}", httpServerErrorException.getLocalizedMessage());
//                response = (JSONObject) parser.parse(httpServerErrorException.getResponseBodyAsString());
                return new ResponseEntity<>(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", httpServerErrorException.getResponseBodyAsString()), HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("Exception e {}", e.getMessage());
//                response = (JSONObject) parser.parse(e.getMessage());
                return new ResponseEntity<>(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            logger.info("End For Get Settlement Report Api ----/api/settlement-report-send-mail");
            return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", stringResponseEntity.toString()), HttpStatus.OK);
        }
    }

    @GetMapping(value = {"/api/category/{tid}/{action}", "/api/category/{tid}/{action}/{categoryId}"})
    public ResponseEntity<?> getCategoryTypeByTid(@PathVariable("tid") Optional<String> tid, @PathVariable("action") Optional<Integer> action, @PathVariable("categoryId") Optional<Long> categoryId) throws Exception {
        logger.info("Get category by tid  : ------------------------{} : ", tid);
        List<Category> category = categoryService.findByTid(tid, action, categoryId, getSessionUserId());
        if (category == null) {
            logger.info("Get All Category has an error : ------------------------------- : ");
            return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, "Error", null), HttpStatus.BAD_REQUEST);
        } else {
            logger.info("Get All Category is success : ------------------------------- {}: ", category);
            return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", category), HttpStatus.OK);
        }
    }

    @GetMapping("/api/getAllCategory")
    public ResponseEntity<?> getCategoryTypeByTid() throws Exception {
        logger.info("Get All Category : ------------------------------- : ");
        List<Category> category = categoryService.findByUser(getSessionUserId());
        if (category == null) {
            logger.info("Get All Category has an error : ------------------------------- : ");
            return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, "Error", null), HttpStatus.BAD_REQUEST);
        } else {
            logger.info("Get All Category is success : ------------------------------- : ");
            return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", category), HttpStatus.OK);
        }
    }

    @PostMapping("/api/invoice-request")
    public ResponseEntity<?> invoiceGenerate(@RequestBody Invoice invoice) throws MessagingException {
        logger.info("Start To Generate Invoice Request Api And Body: -----/api/invoice-request--------:  {}", invoice);
        bijlipayTransactionService.requestMailForInvoiceCreation(invoice);
        logger.info("End For Generate Invoice Request Api : -----/api/invoice-request");
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
    }

    @PostMapping("/tpt/txnNotifications")
    public ResponseEntity<?> tidTxnNotifications(@RequestBody SwitchTransactionsDto switchTransactionsDto) {
        logger.info("Start to Notification Api ---/tpt/txnNotifications");
        Gson gson = new Gson();
        logger.info("Request Payload ---/tpt/txnNotifications ------- {}",gson.toJson(switchTransactionsDto));
//        CompletableFuture.runAsync(()->{
            bijlipayTransactionService.pushTxnNotifications(switchTransactionsDto);
//        });
        logger.info("End For Notification Api ---/tpt/txnNotifications");
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
    }

    @PostMapping("/tpt/settlementCallback")
    public ResponseEntity<?> settleMentCallback(@RequestBody SettlementNotificationDto requestDto) throws MessagingException {
        logger.info("pos settlement callback ---------- : {}",requestDto);
        logger.info("Start to Settlement Call Back Api ---/tpt/settlement-callback");
        bijlipayTransactionService.pushSettlementNotifications(requestDto);
        logger.info("End For Settlement Call Back Api ---/tpt/settlement-callback");
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", ""), HttpStatus.OK);
    }

    @PostMapping("/tpt/QRSettlementCallback")
    public ResponseEntity<?> QRSettlementCallback(@RequestBody SettlementNotificationDto requestDto) throws MessagingException {
        logger.info("qr settlement callback ---------- : {}",requestDto);
        logger.info("Start to Settlement Call Back Api ---/tpt/settlement-callback");
        bijlipayTransactionService.pushQRSettlementNotifications(requestDto);
        logger.info("End For Settlement Call Back Api ---/tpt/settlement-callback");
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", ""), HttpStatus.OK);
    }

    @PostMapping("download-settlemt-report")
    public ResponseEntity<?> downloadSettlementReport(@RequestParam("url") String url) throws IOException {
        String[] parts = url.split("/");
        String key = parts[parts.length - 1];
        logger.info("file key ---------------: {}", key);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(amazonBucketName)
                .key(key)
                .build();
        try {
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            fileStorageService.storeS3File(objectBytes, key);

        } catch (NoSuchBucketException e) {
            throw new RuntimeException("The specified bucket does not exist: " + e.getMessage(), e);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }


    // push axis qr transaction notification to merchant app
    @PostMapping("/tpt/axisQr-txnNotifications")
    public ResponseEntity<?> axisQrTxnNotifications(@RequestBody AxisQrTransactionDto axisQrTransactionDto) {
        logger.info("Start to Push Qr Transaction Notification Api --/axisQr-txnNotifications");
        CompletableFuture.runAsync(()->{
            try {
                bijlipayTransactionService.insertStaticQrTxnData(axisQrTransactionDto);
            } catch (java.text.ParseException e) {
                logger.info("DB insertion error Axis SQR Callback --: {}",axisQrTransactionDto.getMerchantTransactionId());
            }
        });
            bijlipayTransactionService.pushTxnNotificationsForAxisQr(axisQrTransactionDto);
            logger.info("DB inserted and notified successfully --: {}",axisQrTransactionDto.getMerchantTransactionId());
//        });
//        try {
//            bijlipayTransactionService.insertStaticQrTxnData(axisQrTransactionDto);
//            bijlipayTransactionService.pushTxnNotificationsForAxisQr(axisQrTransactionDto);
//            return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
//        } catch (Exception e) {
//            logger.info("Exception occurred while insert axis static qr txn data--{}", axisQrTransactionDto.getRrn());
//            e.printStackTrace();
//        }
        logger.info("End For Push Qr Transaction Notification Api --/axisQr-txnNotifications");
//        return new ResponseEntity<>(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
    }

    @GetMapping("/insert-axis-missing-data")
    public void insertAxisMissingData() {
        logger.info("Axis Static Qr Missing Data Insert Started");
        String missingDataFile = axisMissingDataFilePath + "/Axis_Missing_Data-" + DateUtil.today() + ".csv";
        File file = new File(missingDataFile);
        boolean dataInserted = false;
        if (file.exists()) {
            dataInserted = bijlipayTransactionService.insertAxisMissingData(missingDataFile);
            if (dataInserted) {
                logger.info("All Missing Axis File Data Inserted Successfully----!!!");
            }
        }
    }

    @GetMapping(value = {"/getSettlementReportData","/getSettlementReportData/{categoryType}"})
    public ResponseEntity<?> getSettlementReportData(@PathVariable("categoryType") Optional<String> categoryType,@RequestParam Map<String,String> requestParams, Pageable pageable){
        pageable = webConfig.resolvePageable(requestParams, pageable);
        Page<SettlementRequestResponse> notifications;
        if (categoryType.isPresent()){
            notifications = categoryService.getSettlementReportDataBasedOnCategory(userTidVpaDataService.getUserById(getSessionUser().getId()),categoryType.get(), pageable);
        }else {
            notifications = categoryService.getSettlementReportData(userTidVpaDataService.getUserById(getSessionUser().getId()), pageable);
        }
        if (!notifications.isEmpty()){
            return new ResponseEntity<>(new ApiResponse(HttpStatus.OK,"success",notifications),HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND,"Data not Found"),HttpStatus.NOT_FOUND);
    }

    @GetMapping("/download-txn-data")
    public ResponseEntity<?> downloadTxnData(@RequestParam("tid") Optional<String> tid, @RequestParam("vpa") Optional<String> vpa, @RequestParam("from") Optional<Long> from, @RequestParam("to") Optional<Long> to) throws java.text.ParseException, IOException {
        logger.info("Start download transaction report ------- : ");

        if (tid.isPresent()) {
            UserTidVpaData tidData = userTidVpaDataService.findByTid(tid.get());
            if (tidData == null) {
                return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND, "Tid Not Found"), HttpStatus.NOT_FOUND);
            }
            bijlipayTransactionService.downloadTxnReport(from, to, tid, tidData);
            return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
        } else {
            UserTidVpaData vpaData = userTidVpaDataService.checkVpa(vpa.get());
            if (vpaData == null) {
                return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND, "Vpa not Found"), HttpStatus.NOT_FOUND);
            }
            bijlipayTransactionService.downloadQrReport(from, to, vpa);
            return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
        }
    }

    @PostMapping("/tpt/settlement-callback")
    public ResponseEntity<?> statementCallback(@RequestBody SettlementNotificationUpdate requestDto) throws MessagingException {
        logger.info("Start to Settlement Call Back Api ---/tpt/settlement-callback");
        bijlipayTransactionService.pushSettlementNotificationsForToday(requestDto);
        logger.info("End For Settlement Call Back Api ---/tpt/settlement-callback");
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", ""), HttpStatus.OK);
    }

    @GetMapping("/tpt/sync-missing-txn-data")
    public void syncMissingTxnData() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        String previousDate = DateUtil.yesterday();
        try {
            conn = getTSDBConnection();
            stmt = conn.createStatement();
            String settlementQuery = "select  0 as txn_additional_amount,null as auth_code,batch_number,invoice_number,null as auth_code, " +
                    "RIGHT(REPLACE(Card_No, '*', ''), 4) AS masked_card_number , " +
                    "MID as merchant_id ,'0200' as mti,null as panentry_mode,RRN as rrn_number ,Txn_date as response_received_time,stan,TID as terminal_id ,Txnamount as txn_amount ,id as txn_correlation_id, " +
                    "FORMAT(cast(txn_date as DATETIME2) , 'MMdd') as txn_date,'00' as txn_response_code, " +
                    "FORMAT(cast(txn_date as DATETIME2), 'HHmmss') as txn_time,'00' as txn_type,null as urn,LEFT(REPLACE(Card_No, '*', ''), 6) as bin from BijlipaySettlement where Card_No NOT LIKE '%[^0-9*]%' AND TID in (select tid from burgerData where tmd_model like 'ANTERA%' OR tmd_model like 'PAYTIVO%' and Terminal_status='Active') " +
                    "and Paymentdate like '"+previousDate+"%';";
            ResultSet resultSet = stmt.executeQuery(settlementQuery);
            List<Object[]> txnData = new ArrayList<>();
            while (resultSet.next()){
                int cols = resultSet.getMetaData().getColumnCount();
                Object[] arr = new Object[cols];
                for (int i = 0; i < cols; i++) {
                    arr[i] = resultSet.getObject(i + 1);
                }
                txnData.add(arr);
            }
            resultSet.close();
            logger.info("ANTERA, PAYTIVO Txn Data Original Size "+previousDate+"---{}",txnData.size());
            bijlipayTransactionService.insertAnteraPaytivoTxn(txnData);

        }catch (SQLException e){
            throw new RuntimeException(e);
        }finally {
            if (conn != null){
                conn.close();
            }
        }
    }

    @PostMapping(value = {"get-pos-transaction-amount", "get-pos-transaction-amount/{action}"})
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPosTransactionForAmount(@RequestParam("tid") Optional<List<String>> tid, @PathVariable("action") Optional<Integer> action, @RequestParam("fromDate") Optional<Long> from, @RequestParam("toDate") Optional<Long> to, @RequestParam("searchTerm") Optional<String> searchTerm, @RequestBody TransactionRequestDto requestDto, @RequestParam Map<String, String> requestParams, HttpServletRequest httpServletRequest) {
        logger.info("Get transactions pageable : ------------------------------- : ");
        String queryString = httpServletRequest.getQueryString();
        logger.info("request param : ------------------------------- : {}", queryString);
        User user = userRepository.getOne(getSessionUserId());
        TidAmount tidAmount=new TidAmount();
        if (user != null) {
            try {
                List<String> tidData = userTidVpaDataService.fetchTidData(user);
                logger.info("The tid is in our Switch , fetching Amount ");
                if (from.isEmpty() & to.isEmpty()) {
                    Date date=DateUtil.currentDate();
                    long sec=date.getTime()/1000;
                    from = Optional.of(sec);
                    to = Optional.of(sec);
                    requestDto.setDateRange(3);
                    action= action.isPresent()?action:Optional.of(3);
                    tidAmount.setFlag(true);
                    logger.info("Date ----{} Date2--{}", from, to);
                }

                logger.info("from--{} to--{}",from.isPresent(),to.isPresent());
                try {
                    Double amount = bijlipayTransactionService.getSwitchTransactionsSearchAmount(action, tidData, tid, requestDto, from, to, searchTerm);
                  double response=amount!=null?amount:0;
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    String formatted = decimalFormat.format(response);
                    tidAmount.setAmount(formatted);
                    return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success", tidAmount), HttpStatus.OK);

                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
            } catch (HttpClientErrorException | HttpServerErrorException ce) {
                return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, "error", ce.getResponseBodyAsString()), HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "Success"), HttpStatus.OK);
    }
}
