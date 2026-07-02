package org.huan.demo.demo.zjhours;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ZJProject {
    private String ProjectName;
    private String ProjectCode;
    private Integer beginDays;
    private Set<Integer> equipDiffDays = new HashSet<>();
    private List<ZJEquip> equips = new ArrayList<>();
}
