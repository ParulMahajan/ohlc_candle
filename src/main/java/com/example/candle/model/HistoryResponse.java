package com.example.candle.model;


import java.util.ArrayList;
import java.util.List;

import com.example.candle.entity.Candle;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoryResponse {

    private String s;
    private String errmsg;
    private List<Long> t;
    private List<Double> o;
    private List<Double> h;
    private List<Double> l;
    private List<Double> c;
    private List<Long> v;

   
    public static HistoryResponse okFromEntities(List<Candle> candles) {
        List<Long> t = new ArrayList<>();
        List<Double> o = new ArrayList<>();
        List<Double> h = new ArrayList<>();
        List<Double> l = new ArrayList<>();
        List<Double> c = new ArrayList<>();
        List<Long> v = new ArrayList<>();

        candles.forEach(ce -> {
            t.add(ce.getTime().getEpochSecond());
            o.add(ce.getOpen());
            h.add(ce.getHigh());
            l.add(ce.getLow());
            c.add(ce.getClose());
            v.add(ce.getVolume());
        });

        return new HistoryResponse("ok", null, t, o, h, l, c, v);
    }

    public static HistoryResponse error(String message) {
        return new HistoryResponse("error", message, null, null, null, null, null, null);
    }

  
}

