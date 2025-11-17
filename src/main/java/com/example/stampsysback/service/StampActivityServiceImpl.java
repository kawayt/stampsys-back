package com.example.stampsysback.service;

import com.example.stampsysback.dto.StampActivityResponse;
import com.example.stampsysback.dto.StampSeries;
import com.example.stampsysback.mapper.StampActivityMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation that pivots flat mapper output into timeline + aligned series.
 * Relies on StampActivityMapper.findBucketedStampCounts to return rows with:
 *   ts (timestamp), stampId (int), cnt (int), total_cnt (int), pct (numeric)
 * Also queries stamps table to attach stamp metadata.
 */
@Service
public class StampActivityServiceImpl implements StampActivityService {

    private final StampActivityMapper mapper;
    private final NamedParameterJdbcTemplate jdbc;

    public StampActivityServiceImpl(StampActivityMapper mapper, NamedParameterJdbcTemplate jdbc) {
        this.mapper = mapper;
        this.jdbc = jdbc;
    }

    @Override
    public StampActivityResponse getStampActivity(Long roomId, String intervalStr, OffsetDateTime startTs, OffsetDateTime endTs) {
        List<Map<String, Object>> rows = mapper.findBucketedStampCounts(roomId, intervalStr, startTs, endTs);

        // If no rows -> return empty response
        if (rows == null || rows.isEmpty()) {
            StampActivityResponse empty = new StampActivityResponse();
            empty.setTimeline(Collections.emptyList());
            empty.setTotals(Collections.emptyList());
            empty.setSeries(Collections.emptyList());
            return empty;
        }

        // 1) Collect unique sorted timeline (ascending) and stamp ids (default -1 for missing)
        TreeSet<OffsetDateTime> timelineSet = new TreeSet<>();
        Set<Integer> stampIdSet = new TreeSet<>();

        for (Map<String, Object> r : rows) {
            Object tsObj = getMapValue(r, "ts", "TS", "bucket_start", "bucketStart");
            OffsetDateTime ts = toOffsetDateTime(tsObj);
            if (ts != null) timelineSet.add(ts);

            // try multiple possible column keys for stamp id
            Integer stampId = toInteger(getMapValue(r, "stampId", "stampid", "stamp_id"), -1);
            if (stampId == null) stampId = -1;
            stampIdSet.add(stampId);
        }

        List<OffsetDateTime> timeline = new ArrayList<>(timelineSet);
        if (timeline.isEmpty()) {
            StampActivityResponse empty = new StampActivityResponse();
            empty.setTimeline(Collections.emptyList());
            empty.setTotals(Collections.emptyList());
            empty.setSeries(Collections.emptyList());
            return empty;
        }

        int bucketCount = timeline.size();
        Map<OffsetDateTime, Integer> tsIndex = new HashMap<>();
        for (int i = 0; i < timeline.size(); i++) tsIndex.put(timeline.get(i), i);

        // 2) Initialize per-stamp arrays (include NO_STAMP if present)
        Map<Integer, int[]> valuesMap = new LinkedHashMap<>();
        Map<Integer, double[]> pctMap = new LinkedHashMap<>();
        for (Integer sid : stampIdSet) {
            valuesMap.put(sid, new int[bucketCount]);
            pctMap.put(sid, new double[bucketCount]);
        }
        int[] totals = new int[bucketCount];

        // 3) Populate arrays from rows
        for (Map<String, Object> r : rows) {
            OffsetDateTime ts = toOffsetDateTime(getMapValue(r, "ts", "TS", "bucket_start", "bucketStart"));
            if (ts == null) continue;
            Integer idx = tsIndex.get(ts);
            if (idx == null) continue;

            Integer stampId = toInteger(getMapValue(r, "stampId", "stampid", "stamp_id"), -1);
            if (stampId == null) stampId = -1;
            int cnt = toInteger(getMapValue(r, "cnt", "CNT"), 0);

            // ensure entry exists
            valuesMap.computeIfAbsent(stampId, k -> new int[bucketCount]);
            pctMap.computeIfAbsent(stampId, k -> new double[bucketCount]);

            valuesMap.get(stampId)[idx] = cnt;
            totals[idx] += cnt;
        }

        // 4) Compute pct values (bucket-based)
        for (Map.Entry<Integer, int[]> e : valuesMap.entrySet()) {
            int[] vals = e.getValue();
            double[] pcts = pctMap.get(e.getKey());
            for (int i = 0; i < bucketCount; i++) {
                if (totals[i] == 0) {
                    pcts[i] = 0.0;
                } else {
                    double pct = (double) vals[i] * 100.0 / (double) totals[i];
                    pcts[i] = Math.round(pct * 100.0) / 100.0;
                }
            }
        }

        // 5) Fetch stamp metadata for all stampIds (exclude -1)
        List<Integer> stampIdsForMeta = valuesMap.keySet().stream()
                .filter(id -> id != null && id != -1)
                .collect(Collectors.toList());
        Map<Integer, StampMeta> metaMap = fetchStampMeta(stampIdsForMeta);

        // 6) Build StampSeries list
        List<StampSeries> seriesList = new ArrayList<>();
        for (Map.Entry<Integer, int[]> e : valuesMap.entrySet()) {
            Integer sid = e.getKey();
            int[] vals = e.getValue();
            double[] pcts = pctMap.get(sid);

            StampSeries s = new StampSeries();
            s.setStampId(sid);

            if (sid != null && sid != -1) {
                StampMeta meta = metaMap.get(sid);
                if (meta != null) {
                    s.setStampName(meta.name);
                    s.setStampColor(meta.color);
                    s.setStampIcon(meta.icon);
                } else {
                    s.setStampName("stamp-" + sid);
                    s.setStampColor(0);
                    s.setStampIcon(0);
                }
            } else {
                // NO_STAMP
                s.setStampName("NO_STAMP");
                s.setStampColor(0);
                s.setStampIcon(0);
            }

            List<Integer> valList = Arrays.stream(vals).boxed().collect(Collectors.toList());
            List<Double> pctList = Arrays.stream(pcts).boxed().collect(Collectors.toList());

            s.setValues(valList);
            s.setPctValues(pctList);

            seriesList.add(s);
        }

        // 7) Build totals list
        List<Integer> totalsList = Arrays.stream(totals).boxed().collect(Collectors.toList());

        StampActivityResponse resp = new StampActivityResponse();
        resp.setTimeline(timeline);
        resp.setTotals(totalsList);
        resp.setSeries(seriesList);
        return resp;
    }

    // Helper to convert various timestamp representations into OffsetDateTime (UTC)
    private OffsetDateTime toOffsetDateTime(Object o) {
        switch (o) {
            case null -> {
                return null;
            }
            case OffsetDateTime offsetDateTime -> {
                return offsetDateTime;
            }
            case Instant instant -> {
                return instant.atOffset(ZoneOffset.UTC);
            }
            case Timestamp timestamp -> {
                Instant inst = timestamp.toInstant();
                return inst.atOffset(ZoneOffset.UTC);
            }
            case Date date -> {
                Instant inst = date.toInstant();
                return inst.atOffset(ZoneOffset.UTC);
            }
            case String s -> {
                try {
                    return OffsetDateTime.parse(s);
                } catch (Exception ex) {
                    try {
                        Instant inst = Instant.parse(s);
                        return inst.atOffset(ZoneOffset.UTC);
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
            default -> {
            }
        }
        return null;
    }

    private Integer toInteger(Object o) {
        return toInteger(o, null);
    }
    private Integer toInteger(Object o, Integer def) {
        if (o == null) return def;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return def; }
    }

    /**
     * Helper: try multiple possible keys in map and return first non-null value.
     * MyBatis/JDBC may return column keys in different case or format (stampId / stampid / stamp_id).
     */
    private Object getMapValue(Map<String, Object> m, String... keys) {
        if (m == null) return null;
        for (String k : keys) {
            if (k == null) continue;
            if (m.containsKey(k)) {
                Object v = m.get(k);
                if (v != null) return v;
            }
        }
        // try case-insensitive scan as a fallback
        for (Map.Entry<String, Object> e : m.entrySet()) {
            String key = e.getKey();
            if (key == null) continue;
            for (String k : keys) {
                if (k == null) continue;
                if (key.equalsIgnoreCase(k) && e.getValue() != null) return e.getValue();
            }
        }
        return null;
    }

    // Simple metadata holder
    private static class StampMeta {
        final String name;
        final Integer color;
        final Integer icon;
        StampMeta(String name, Integer color, Integer icon) {
            this.name = name; this.color = color; this.icon = icon;
        }
    }

    // Query stamps table for metadata
    private Map<Integer, StampMeta> fetchStampMeta(List<Integer> stampIds) {
        if (CollectionUtils.isEmpty(stampIds)) return Collections.emptyMap();

        String sql = "SELECT stamp_id, stamp_name, stamp_color, stamp_icon FROM stamps WHERE stamp_id IN (:ids)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", stampIds);

        List<Map<String, Object>> rows = jdbc.queryForList(sql, params);
        Map<Integer, StampMeta> map = new HashMap<>();
        for (Map<String, Object> r : rows) {
            Integer id = toInteger(r.get("stamp_id"));
            String name = r.get("stamp_name") != null ? r.get("stamp_name").toString() : null;
            Integer color = toInteger(r.get("stamp_color"));
            Integer icon = toInteger(r.get("stamp_icon"));
            map.put(id, new StampMeta(name, color, icon));
        }
        return map;
    }
}