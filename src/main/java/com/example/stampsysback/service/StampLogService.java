package com.example.stampsysback.service;

import com.example.stampsysback.dto.StampLogDto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * StampLog を返すサービスインターフェース。
 */
public interface StampLogService {
    /**
     * roomId に紐づく stamp log を返す（sender name を含む）。
     *
     * @param roomId  必須
     * @param startTs optional（inclusive）
     * @param endTs   optional（inclusive）
     * @param limit   optional（null = DB のデフォルト/全件。一律推奨: 指定すること）
     * @param offset  optional
     * @return StampLogDto のリスト
     */
    List<StampLogDto> getStampLogs(Long roomId, OffsetDateTime startTs, OffsetDateTime endTs, Integer limit, Integer offset);
}