package com.omnicharge.rechargeservice.client;

import com.omnicharge.rechargeservice.dto.PlanDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "operator-service")
public interface OperatorClient {

    @GetMapping("/api/operators/plans/{planId}")
    PlanDto getPlanById(@PathVariable("planId") Long planId);
}
