package com.bijlipay.controller;

import com.bijlipay.config.WebConfig;
import com.bijlipay.model.RiskHold;
import com.bijlipay.model.dto.RiskHoldDto;
import com.bijlipay.repository.RiskHoldRepository;
import com.bijlipay.service.RiskHoldService;
import com.bijlipay.util.ApiResponse;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/txn")
public class RiskHoldController extends BaseController {
    @Autowired
    RiskHoldRepository riskHoldRepository;
    @Autowired
    private WebConfig webConfig;

    @Autowired
    private RiskHoldService riskHoldService;

    private static final Logger logger = LoggerFactory.getLogger(RiskHoldController.class);

    /**
     * Risk Hold Call Back Request
     */
    @PostMapping("/tpt/risk_hold_call_back")
    public ResponseEntity<?> riskHoldCallBack(@RequestBody List<RiskHoldDto> requestDto) throws MessagingException {
        logger.info("Start to Risk Hold CallBack Api And RequestBody ----/tpt/risk_hold_call_back");
        Gson gson = new Gson();
        String requestBody = gson.toJson(requestDto);
        logger.info("save Risk Hold call back Request Body --{}", requestBody);
        try {
            int result = riskHoldService.save(requestDto);
            logger.info("End For Risk Hold CallBack Api  ----/tpt/risk_hold_call_back");
            if (result == 0) {
                return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
            }
            return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, "Failed to save data"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("RISK HOLD CALLBACK : Risk Hold Creation Failed {}", e.getMessage());
            return new ResponseEntity<>(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Risk Hold Call Back Request
     *
     * @param requestDto
     * @return
     */
    @PutMapping("/tpt/risk_hold_call_back")
    public ResponseEntity<?> riskHoldStatusUpdate(@RequestBody List<RiskHoldDto> requestDto) throws MessagingException {
        logger.info("Start to update Risk Hold call back Data API----/tpt/risk_hold_call_back");
        Gson gson = new Gson();
        String requestBody = gson.toJson(requestDto);
        logger.info("Update Risk Hold call back Request Body --{}", requestBody);
        try {
            int result = riskHoldService.update(requestDto);
            logger.info("End For update Risk Hold call back Data API----/tpt/risk_hold_call_back");
            if (result == 0) {
                return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success"), HttpStatus.OK);
            }
            return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, "Failed to save data"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("RISK HOLD CALLBACK UPDATE: Risk Hold Status Update Failed {}", e.getMessage());
            return new ResponseEntity<>(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * ACTION
     * 1 - Completed
     * 2 - InProgress
     */
    @GetMapping(value = {"/api/risk_hold_list","/api/risk_hold_list/{action}","/api/risk_hold_list/{action}/{tid}"})
    public ResponseEntity getRiskHoldList(@PathVariable("action") Optional<Integer> action, @PathVariable(value = "tid") Optional<String> tid, @RequestParam Map<String, String> requestParams, Pageable pageable) throws URISyntaxException {
        logger.info("Start to get Risk Hold List Api ----/api/risk_hold_list/{tid}   ----Tid --{}", tid);
        try {
            pageable = webConfig.resolvePageable(requestParams, pageable);
            if (tid != null) {
                Page<RiskHold> paged = riskHoldService.getRiskHolds(pageable, getSessionUserId(),action, tid);
//                if (paged.getContent() != null && !paged.getContent().isEmpty()) {
//                    paged.getContent().get(0).setTotalAmt(riskHoldRepository.sumOfTid(tid));
//                    logger.info("RISK HOLD LIST : Risk Hold data : {}", paged.getContent().get(0).getTotalAmt());
//                }
                logger.info("End For get Risk Hold List Api ----/api/risk_hold_list/{tid}   ----Tid --{}", tid);
                if (paged  != null){
                    return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, "success", paged), HttpStatus.OK);
                }
            } else {
                logger.info("RISK HOLD LIST : TID is null no data can be found.!");
            }
            return new ResponseEntity<>(new ApiResponse(HttpStatus.NOT_FOUND, "Data is not found.!"), HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            logger.error("RISK HOLD LIST : The exception occurred while getting information of Risk Hold Data {}", e.getMessage());
            return new ResponseEntity<>(new ApiResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
