package com.example.stampsysback.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Series (time-aligned) for a single stamp.
 */

@Getter
@Setter
public class StampSeries {
    private Integer stampId;
    private String stampName;
    private Integer stampColor;
    private Integer stampIcon;
    private List<Integer> values;     // cnt per bucket, aligned with timeline
    private List<Double> pctValues;   // pct per bucket, aligned with timeline
}