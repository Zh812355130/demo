package org.huan.mcp.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeatherDto {
    @JsonPropertyDescription("城市ID")
    private String cityId;
    @JsonPropertyDescription("城市名称")
    private String city;
    @JsonPropertyDescription("当前温度（单位：℃）")
    private String temperature;
    @JsonPropertyDescription("最低温度（单位：℃）")
    private String lowTemperature;
    @JsonPropertyDescription("最高温度（单位：℃）")
    private String highTemperature;
    @JsonPropertyDescription("数据时间（格式：yyyy-mm-dd hh:mm:ss)")
    private String date;
    @JsonPropertyDescription("空气质量指数")
    private String quality;
    @JsonPropertyDescription("PM2.5浓度（单位：微克/立方米）")
    private double pm25;
}
