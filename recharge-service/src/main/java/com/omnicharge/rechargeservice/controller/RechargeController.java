package com.omnicharge.rechargeservice.controller;

import com.omnicharge.rechargeservice.dto.RechargeRequest;
import com.omnicharge.rechargeservice.model.RechargeRecord;
import com.omnicharge.rechargeservice.repository.RechargeRepository;
import com.omnicharge.rechargeservice.service.RechargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recharges")
public class RechargeController {

    @Autowired
    private RechargeService rechargeService;

    @Autowired
    private RechargeRepository rechargeRepository;

    @PostMapping("/initiate")
    public ResponseEntity<RechargeRecord> initiateRecharge(@RequestHeader(value = "loggedInUser", required = false) String username,
                                                           @RequestBody RechargeRequest request) {
        Long mockUserId = 1L; // Mock mapping username to ID for simplicity
        RechargeRecord record = rechargeService.initiateRecharge(mockUserId, request);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RechargeRecord>> getRechargesByUser(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(rechargeRepository.findByUserId(userId));
    }
}
