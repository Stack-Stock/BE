package com.stacknstock.backend.domain.scenario.controller;

import com.stacknstock.backend.domain.scenario.dto.ScenarioEventRequest;
import com.stacknstock.backend.domain.scenario.service.ScenarioDayService;
import com.stacknstock.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "시나리오 조작", description = "게임 내 시나리오 및 이벤트 변경 API")
@RestController
@RequestMapping("/api/scenarios")
@RequiredArgsConstructor
public class ScenarioDayController {

    private final ScenarioDayService scenarioDayService;

    @PostMapping("/police")
    @Operation(summary = "경찰 출두 이벤트 예약", description = "3일 뒤 경찰 출두 이벤트를 확정합니다.")
    public ResponseEntity<Void> triggerPoliceEvent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ScenarioEventRequest request
    ) {
        scenarioDayService.policeEvent(
                userDetails.getUser().getUserId(),
                request.runId(),
                request.currentDayNo()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/guilty")
    @Operation(summary = "죄책감 이벤트 예약", description = "1일, 2일 뒤 죄책감 이벤트를 확정합니다.")
    public ResponseEntity<Void> triggerGuiltyEvent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ScenarioEventRequest request
    ) {
        scenarioDayService.guiltyEvent(
                userDetails.getUser().getUserId(),
                request.runId(),
                request.currentDayNo()
        );
        return ResponseEntity.ok().build();
    }
}