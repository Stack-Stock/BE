package com.stacknstock.backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 공통
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E500", "서버 오류"),

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U404", "사용자를 찾을 수 없습니다"),
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "U400", "이미 존재하는 이메일입니다"),

    // 게임
    RUN_NOT_FOUND(HttpStatus.NOT_FOUND, "G404", "진행 중인 게임이 없습니다"),
    RUN_STATE_NOT_FOUND(HttpStatus.NOT_FOUND, "G405", "RunState가 없습니다"),

    // Day
    DAY_NOT_FOUND(HttpStatus.NOT_FOUND, "D404", "Day가 존재하지 않습니다"),
    DAY_STATE_NOT_FOUND(HttpStatus.NOT_FOUND, "D405", "DayState가 존재하지 않습니다"),

    // Scenario
    SCENARIO_NOT_FOUND(HttpStatus.NOT_FOUND, "S404", "ScenarioDay가 없습니다"),
    GAME_CASE_NOT_FOUND(HttpStatus.NOT_FOUND, "S405", "GameCase가 없습니다"),
    STOCK_PRICE_NOT_FOUND(HttpStatus.NOT_FOUND, "S406", "주가 정보를 찾을 수 없습니다"),

    // Action
    ACTION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "A400", "허용되지 않은 행동입니다"),
    AP_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "A401", "AP가 부족합니다"),
    ALREADY_STUDIED(HttpStatus.BAD_REQUEST, "A402", "이미 공부했습니다"),
    JSON_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E501", "JSON 파싱 중 오류 발생");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
