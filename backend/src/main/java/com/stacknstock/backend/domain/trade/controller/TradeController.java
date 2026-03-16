package com.stacknstock.backend.domain.trade.controller;

import com.stacknstock.backend.domain.trade.dto.TradeRequest;
import com.stacknstock.backend.domain.trade.service.TradeService;
import com.stacknstock.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "주식 거래", description = "주식 매수/매도 API")
@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    @PostMapping("/execute")
    @Operation(summary = "주식 거래 체결")
    public ResponseEntity<Void> executeTrade(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody TradeRequest request
    ) {

        Long userId = userDetails.getUser().getUserId();

        tradeService.executeTrade(userId, request);

        return ResponseEntity.ok().build();
    }
}