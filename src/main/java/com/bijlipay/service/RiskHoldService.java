package com.bijlipay.service;

import com.bijlipay.model.RiskHold;
import com.bijlipay.model.dto.RiskHoldDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RiskHoldService {
    int save(List<RiskHoldDto> riskHoldDto);

    int update(List<RiskHoldDto> riskHoldDto);

    Page<RiskHold> getRiskHolds(Pageable pageable, long sessionUserId, Optional<Integer> action, Optional<String> tid);
}
