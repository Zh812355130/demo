package org.huan.demo.demo.workinghours;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@Data
public class WorkHourPerson {

    private String name;

    private String projectName;

    private double workHourPercent;

    private List<Integer> dayIndex = new ArrayList<>();

    private boolean random;

}
