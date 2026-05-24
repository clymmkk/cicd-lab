package com.faultlab.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * 故障触发参数既支持统一字段，也保留 extras 给不同场景扩展。
 */
@Data
public class FaultParam {

    @Size(max = 50)
    private String operator = "ops_user";

    private Integer durationSeconds;

    private Integer intensity;

    private Map<String, Object> extras = new HashMap<>();

    @JsonIgnore
    public int getIntValue(String key, int defaultValue) {
        Object value = extras.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.parseInt(text.trim());
        }
        return defaultValue;
    }

    @JsonIgnore
    public boolean getBooleanValue(String key, boolean defaultValue) {
        Object value = extras.get(key);
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof String text && !text.isBlank()) {
            return Boolean.parseBoolean(text.trim());
        }
        return defaultValue;
    }

    public FaultParam copy() {
        FaultParam faultParam = new FaultParam();
        faultParam.setOperator(operator);
        faultParam.setDurationSeconds(durationSeconds);
        faultParam.setIntensity(intensity);
        faultParam.setExtras(new HashMap<>(extras));
        return faultParam;
    }
}
