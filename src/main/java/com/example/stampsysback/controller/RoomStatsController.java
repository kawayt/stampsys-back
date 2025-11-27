package com.example.stampsysback.controller;

import com.example.stampsysback.dto.RoomStampCountDto;
import com.example.stampsysback.repository.StampRepository;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

@RestController
@RequestMapping("/api/classes/{classId}")
public class RoomStatsController {

    private final StampRepository stampRepository;

    public RoomStatsController(StampRepository stampRepository) {
        this.stampRepository = stampRepository;
    }

    @GetMapping("/rooms/stamp-counts")
    public List<RoomStampCountDto> getRoomStampCounts(@PathVariable("classId") Long classId) {
        List<Object[]> rows = stampRepository.findRoomStampCountsByClassId(classId);
        System.out.println("DEBUG: room stamp count rows=" + rows.size());
        for (Object[] r : rows) {
            System.out.println("DEBUG row: " + Arrays.toString(r));
        }
        List<RoomStampCountDto> result = new ArrayList<>();
        for (Object[] r : rows) {
            Long roomId = r[0] == null ? null : ((Number) r[0]).longValue();
            Long cnt = r[1] == null ? 0L : ((Number) r[1]).longValue();
            result.add(new RoomStampCountDto(roomId, cnt));
        }
        return result;
    }
}