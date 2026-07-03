package org.huan.demo.demo.zjhours;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.handler.context.RowWriteHandlerContext;
import com.alibaba.excel.write.merge.LoopMergeStrategy;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZjExcelHandler {

    private final static String PATTERN_STR = "^(\\d+)月(\\d+)天";

    private final static Integer[] START_HOURS = new Integer[]{6, 7, 8, 9, 10, 11};

    public static final Integer DAY_HOURS = 24;

    private final List<ZJProject> projectList = new ArrayList<>();
    private final String excelPath;

    private final Integer month;

    private final Integer startSheetIndex;
    private List<Integer> allMonthDays = new ArrayList<>();

    private Set<Integer> holidays = new HashSet<>();

    public ZjExcelHandler(String excelPath, Integer month, Integer startSheetIndex) {
        this.excelPath = excelPath;
        this.month = month;
        this.startSheetIndex = startSheetIndex;
        generateHolidays();
    }

    public void handle(String savePath) throws IOException {
        //读取excel
        readExcel();

        EasyExcel.write(savePath).registerWriteHandler(new MyMergeStrategy(1, projectList)).registerWriteHandler(new LoopMergeStrategy(2, 1)).head(head()).sheet("工时").doWrite(dataList());
    }

    private static class MyMergeStrategy implements RowWriteHandler {
        private int rowIndex;
        private final List<ZJProject> projectList;
        private int equipIndex;

        public MyMergeStrategy(int rowIndex, List<ZJProject> projectList) {
            this.rowIndex = rowIndex;
            this.projectList = projectList;
            this.equipIndex = 0;
        }

        @Override
        public void afterRowDispose(RowWriteHandlerContext context) {
            if (!context.getHead() && context.getRelativeRowIndex() != null) {
                if (context.getRowIndex() == rowIndex && equipIndex < projectList.size()) {
                    int size = projectList.get(equipIndex).getEquips().size() * 2;
                    CellRangeAddress cellRangeAddress = new CellRangeAddress(context.getRowIndex(), context.getRowIndex() + size - 1, 0, 0);
                    CellRangeAddress rangeAddress = new CellRangeAddress(context.getRowIndex(), context.getRowIndex() + size - 1, 3, 3);
                    context.getWriteSheetHolder().getSheet().addMergedRegionUnsafe(cellRangeAddress);
                    context.getWriteSheetHolder().getSheet().addMergedRegionUnsafe(rangeAddress);
                    rowIndex += size;
                    equipIndex++;
                }
            }
        }
    }


    private List<List<Object>> dataList() {
        List<List<Object>> dataList = new ArrayList<>();

        for (ZJProject zjProject : projectList) {
            setBeginDays(zjProject);
            for (ZJEquip equip : zjProject.getEquips()) {
                dataList.add(getEquipDataList(zjProject, equip));
                dataList.add(getAllMonthDataList(zjProject, equip));
            }
        }
        return dataList;
    }

    private List<Object> getAllMonthDataList(ZJProject zjProject, ZJEquip equip) {
        List<Object> list = new ArrayList<>();
        list.add(zjProject.getProjectName());
        list.add(equip.getName());
        list.add("总工时");
        list.add(zjProject.getProjectCode());
        for (int i = 0; i < allMonthDays.size(); i++) {
            list.add(DAY_HOURS);
        }
        list.add(equip.getDays());
        return list;
    }

    private List<Object> getEquipDataList(ZJProject zjProject, ZJEquip equip) {
        List<Object> list = new ArrayList<>();
        list.add(zjProject.getProjectName());
        list.add(equip.getName());
        list.add("研发工时");
        list.add(zjProject.getProjectCode());
        if (Objects.isNull(zjProject.getBeginDays())) {
            for (int i = 0; i < allMonthDays.size(); i++) {
                list.add(null);
            }
        } else {
            int beginDays = zjProject.getBeginDays();
            int startHour = RandomUtil.randomEle(START_HOURS);
            int endHour = DAY_HOURS - startHour;
            for (Integer dayOfMonth : allMonthDays) {
                if (dayOfMonth == beginDays) {
                    list.add(startHour);
                } else if (dayOfMonth == (beginDays + equip.getDays())) {
                    list.add(endHour);
                } else if (dayOfMonth < (beginDays + equip.getDays()) && dayOfMonth > beginDays) {
                    list.add(DAY_HOURS);
                } else {
                    list.add(null);
                }
            }
        }
        list.add(equip.getDays());
        return list;
    }


    private void setBeginDays(ZJProject zjProject) {
        int index = 0;
        while (index < 10000) {
            index++;
            Integer startDay = RandomUtil.randomEle(allMonthDays);
            if (holidays.contains(startDay)) {
                continue;
            }
            boolean endDayOk = true;
            for (Integer equipDiffDay : zjProject.getEquipDiffDays()) {
                int endDays = startDay + equipDiffDay;
                if (holidays.contains(endDays)) {
                    endDayOk = false;
                    break;
                }
                if (endDays > allMonthDays.get(allMonthDays.size() - 1)) {
                    endDayOk = false;
                    break;
                }
            }
            if (!endDayOk) {
                continue;
            }
            zjProject.setBeginDays(startDay);
        }
    }

    private List<List<String>> head() {
        List<List<String>> list = new ArrayList<>();
        List<String> projectName = new ArrayList<>();
        projectName.add("项目名称");
        list.add(projectName);
        List<String> head0 = new ArrayList<>();
        head0.add("设备名称");
        List<String> head1 = new ArrayList<>();
        head1.add("工时类别");
        List<String> head2 = new ArrayList<>();
        head2.add("RD序号");
        list.add(head0);
        list.add(head1);
        list.add(head2);
        for (Integer allMonthDay : allMonthDays) {
            List<String> tmp = new ArrayList<>();
            tmp.add(allMonthDay.toString());
            list.add(tmp);
        }
        List<String> head3 = new ArrayList<>();
        head3.add("天数");
        list.add(head3);
        return list;
    }

    public void readExcel() {
        int sheetCount = ExcelUtil.getReader(excelPath).getSheetCount();
        for (int i = startSheetIndex; i < sheetCount; i++) {
            ZJProject zjProject = new ZJProject();
            projectList.add(zjProject);
            EasyExcel.read(excelPath, new AnalysisEventListener<Map<Integer, String>>() {
                @Override
                public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                    zjProject.setProjectCode(headMap.get(0));
                }

                @Override
                public void invoke(Map<Integer, String> data, AnalysisContext context) {
                    Integer rowIndex = context.readRowHolder().getRowIndex();
                    if (rowIndex == 1) {
                        zjProject.setProjectName(data.get(0));
                    }
                    if (rowIndex > 2 && !Objects.isNull(data.get(1))) {
                        String equipName = data.get(1);
                        String daysDesc = data.get(10).trim();
                        ZJEquip zjEquip = new ZJEquip();
                        zjEquip.setName(equipName);
                        Pattern pattern = Pattern.compile(PATTERN_STR);
                        Matcher matcher = pattern.matcher(daysDesc);
                        if (matcher.find()) {
                            Integer days = Integer.valueOf(matcher.group(2));
                            zjEquip.setDays(days);
                            zjProject.getEquipDiffDays().add(days);
                        }
                        zjProject.getEquips().add(zjEquip);
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                }
            }).sheet(i).doReadSync();
        }
    }


    private void generateHolidays() {
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.MONTH, this.month - 1);
        instance.set(Calendar.DAY_OF_MONTH, 1);
        int actualMaximum = instance.getActualMaximum(Calendar.DAY_OF_MONTH);
        Date date = instance.getTime();
        for (int i = 1; i <= actualMaximum; i++) {
            //是否是周末
            if (DateUtil.isWeekend(date)) {
                holidays.add(i);
            }
            allMonthDays.add(i);
            date = DateUtil.offset(date, DateField.DAY_OF_YEAR, 1);
        }
    }

    public void addHolidays(Integer... days) {
        holidays.addAll(Arrays.asList(days));
    }

    public void removeHolidays(Integer... days) {
        for (Integer day : days) {
            holidays.remove(day);
        }
    }

    public static void main(String[] args) throws IOException {
        String path = "D:\\个人\\temp\\2026.06月设备工时统计表-简版.xlsx";
        ZjExcelHandler zjExcelHandler = new ZjExcelHandler(path, 6, 2);
        zjExcelHandler.addHolidays(19);
//        zjExcelHandler.removeHolidays(9);
        zjExcelHandler.handle("D:\\个人\\temp\\2026.06月设备工时统计表-简版-生成.xlsx");
    }

}
