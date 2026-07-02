package org.huan.mcp.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.huan.mcp.dto.WeatherDto;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {
    @Tool(description = "根据城市id查询天气信息")
    public WeatherDto getWeather(@ToolParam(description = "城市id") String cityId) {
        String url = "http://t.weather.itboy.net/api/weather/city/" + cityId;
        String res = HttpUtil.get(url);
        JSONObject jsonObject = JSONUtil.parseObj(res);
        // 模拟从数据库中获取天气数据
        return WeatherDto.builder()
                .cityId(jsonObject.getByPath("cityInfo.cityKey", String.class))
                .city(jsonObject.getByPath("cityInfo.city", String.class))
                .temperature(jsonObject.getByPath("data.wendu", String.class))
                .lowTemperature(jsonObject.getByPath("data.forecast[0].low", String.class))
                .highTemperature(jsonObject.getByPath("data.forecast[0].high", String.class))
                .date(jsonObject.getByPath("time", String.class))
                .quality(jsonObject.getByPath("data.quality", String.class))
                .pm25(jsonObject.getByPath("data.pm25", Double.class))
                .build();
    }
}
