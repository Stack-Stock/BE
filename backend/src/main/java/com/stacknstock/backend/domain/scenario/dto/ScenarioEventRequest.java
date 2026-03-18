package com.stacknstock.backend.domain.scenario.dto;

/**
 * 시나리오 이벤트 예약을 위한 요청 DTO
 */
public record ScenarioEventRequest(
        Long runId,
        Integer currentDayNo
) {}