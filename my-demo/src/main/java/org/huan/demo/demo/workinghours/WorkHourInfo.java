package org.huan.demo.demo.workinghours;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.Getter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <dependency>
 * <groupId>com.alibaba</groupId>
 * <artifactId>easyexcel</artifactId>
 * <version>3.3.4</version>
 * </dependency>
 */
@Getter
public class WorkHourInfo {

    public static final Integer DAY_HOURS = 8;

    private final List<WorkHourPerson> workHourPersonList = new ArrayList<>();

    private final String excelPath;

    private final Integer month;

    private Integer sheetIndex;

    private List<Integer> allMonthDays = new ArrayList<>();

    private Set<Integer> holidays = new HashSet<>();

    private int totalWorkHour = 0;

    public static String PERSON_NAME = "K";
    public static String PROJECT_NAME = "I";
    public static String WORK_HOUR = "U";



    public WorkHourInfo(String excelPath, Integer month, Integer sheetIndex) {
        this.excelPath = excelPath;
        this.month = month;
        this.sheetIndex = sheetIndex;
        generateHolidays();
    }

    private void writeExcel(String filePath) throws IOException {
        //计算总工时
        calculateTotalHour();
        //读取excel
        readExcel();
        //每个人计算工时并写入excel
        EasyExcel.write(filePath).head(head()).sheet("工时").doWrite(dataList());
    }


    private List<List<Object>> dataList() {
        List<List<Object>> dataList = new ArrayList<>();
        int index =1;
        for (WorkHourPerson workHourPerson : workHourPersonList) {
            System.out.println("all:"+workHourPersonList.size()+",start :" +index+"  ,person:"+workHourPerson);
            dataList.add(getPersonDataList(workHourPerson));
            dataList.add(getTotalHourList(workHourPerson));

            index++;
        }
        return dataList;
    }

    private List<Object> getPersonDataList(WorkHourPerson workHourPerson) {
        List<Object> list = new ArrayList<>();
        list.add(workHourPerson.getProjectName());
        list.add(workHourPerson.getName());
        //日期
        List<Integer> days = allocationDays(workHourPerson);
        for (Integer day : allMonthDays) {
            if (days.contains(day)) {
                list.add(DAY_HOURS);
            } else {
                list.add("");
            }
        }
        //工时
        list.add(days.size()*DAY_HOURS);
        //总工时
        list.add(totalWorkHour);
        return list;
    }

    public List<Integer> allocationDays(WorkHourPerson workHourPerson) {
        double workHourPercent = workHourPerson.getWorkHourPercent();
        boolean random = workHourPerson.isRandom();
        //先算出来需要取几天
        int choiceDay = 0;
        if (random) { //random 取三种可能
            int days = (int) Math.ceil(workHourPercent * totalWorkHour / DAY_HOURS);
            choiceDay = RandomUtil.randomEle(new Integer[]{days, days - 1, days + 1});
        } else {
            //取两种可能
            int days = (int) Math.ceil(workHourPercent * totalWorkHour / DAY_HOURS);
//            choiceDay = RandomUtil.randomEle(new Integer[]{days, days - 1});
            choiceDay = days;
            if (choiceDay == 0) {
                choiceDay = 1;
            }
        }
        //从所有的日期里面取符合的日期 不能取节假日
        if(random){
            List<Integer> randomDays = getRandomDays(choiceDay);
            Collections.sort(randomDays);
            workHourPerson.setDayIndex(randomDays);
            return randomDays;
        }else{
            List<Integer> multiPersonDays = getMultiPersonDays(choiceDay, workHourPerson);
            Collections.sort(multiPersonDays);
            workHourPerson.setDayIndex(multiPersonDays);
            return multiPersonDays;
        }
    }
    public List<Integer> getMultiPersonDays(int days,WorkHourPerson workHourPerson){
        List<Integer> choiceDays = new ArrayList<>();
        List<Integer> usedDays = workHourPersonList.stream().filter(x -> Objects.equals(x.getName(), workHourPerson.getName()))
                .flatMap(v -> v.getDayIndex().stream()).collect(Collectors.toList());
        int index = 0;
        while (true){
            int x = RandomUtil.randomInt(allMonthDays.size());
            Integer day = allMonthDays.get(x);
            index++;
            if(index>100000){
                break;
            }
            if(holidays.contains(day)){
                continue;
            }
            if(choiceDays.contains(day)){
                continue;
            }
            if(usedDays.contains(day)){
                continue;
            }
            choiceDays.add(day);
            if(choiceDays.size() == days){
                break;
            }
        }
        if(choiceDays.size() != days){
            List<WorkHourPerson> samePeopleList = workHourPersonList.stream().filter(x -> Objects.equals(x.getName(), workHourPerson.getName())).collect(Collectors.toList());
            WorkHourPerson samePerson = samePeopleList.get(samePeopleList.size() - 1);
            if(samePerson == workHourPerson){
                //最后一个人
                for (Integer day : allMonthDays) {
                    if(choiceDays.contains(day)){
                        continue;
                    }
                    if(usedDays.contains(day)){
                        continue;
                    }
                    if(holidays.contains(day)){
                        continue;
                    }
                    choiceDays.add(day);
                }
            }
        }
        return choiceDays;
    }

    public List<Integer> getRandomDays(int days){
        List<Integer> choiceDays = new ArrayList<>();
        int index = 0;
        while (true){
            int x = RandomUtil.randomInt(allMonthDays.size());
            Integer day = allMonthDays.get(x);
            if(holidays.contains(day)){
                continue;
            }
            if(choiceDays.contains(day)){
                continue;
            }
            choiceDays.add(day);
            index++;
            if(choiceDays.size() == days || index>10000){
                break;
            }
        }
        return choiceDays;
    }



    private List<Object> getTotalHourList(WorkHourPerson workHourPerson) {
        List<Object> list = new ArrayList<>();
        list.add("");
        list.add("");
        //日期
        for (Integer day : allMonthDays) {
            if (holidays.contains(day)) {
                list.add("");
            } else {
                list.add(DAY_HOURS);
            }
        }
        //工时
        list.add("");
        //总工时
        list.add(totalWorkHour);
        return list;
    }


    private List<List<String>> head() {
        List<List<String>> list = new ArrayList<>();
        List<String> head0 = new ArrayList<>();
        head0.add("项目");
        List<String> head1 = new ArrayList<>();
        head1.add("姓名");
        list.add(head0);
        list.add(head1);
        for (Integer allMonthDay : allMonthDays) {
            List<String> tmp = new ArrayList<>();
            tmp.add(allMonthDay.toString());
            list.add(tmp);
        }
        List<String> head3 = new ArrayList<>();
        head3.add("工时");
        list.add(head3);
        List<String> head4 = new ArrayList<>();
        head4.add("总工时");
        list.add(head4);
        return list;
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


    public void readExcel() throws IOException {
        List<Object> objects = EasyExcel.read(excelPath, new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> data, AnalysisContext context) {
                String name = data.get(columnIndex(PERSON_NAME));
                String projectName = data.get(columnIndex(PROJECT_NAME));
                String workHourPercent = data.get(columnIndex(WORK_HOUR));
                if (StrUtil.isBlank(name) || StrUtil.isBlank(workHourPercent)) {
                    return;
                }
                WorkHourPerson workHourPerson = new WorkHourPerson();
                workHourPerson.setName(name);
                workHourPerson.setProjectName(projectName);
                double workHourPercentDouble ;
                if (workHourPercent.contains("随机")) {
                    workHourPerson.setRandom(true);
                    workHourPercentDouble = Double.parseDouble(workHourPercent.split("%")[0].substring(2)) / 100;
                } else {
                    workHourPerson.setRandom(false);
                    workHourPercentDouble =Double.parseDouble(workHourPercent);
                }
                workHourPerson.setWorkHourPercent(workHourPercentDouble);
                if(workHourPercentDouble == 0){
                    return;
                }
                workHourPersonList.add(workHourPerson);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                System.out.println("所有数据解析完了");
            }
        }).sheet(sheetIndex).doReadSync();
    }

    public void addHolidays(Integer... days) {
        holidays.addAll(Arrays.asList(days));
    }

    public void removeHolidays(Integer... days) {
        for (Integer day : days) {
            holidays.remove(day);
        }
    }


    private void calculateTotalHour() {
        long count = allMonthDays.stream().filter(x -> !holidays.contains(x)).count();
        totalWorkHour = (int) count * DAY_HOURS;
    }

    /**
     * 根据excel列名获取对应下标 从0开始
     */
    private int columnIndex(String columnName) {
        int result = 0;
        for (int i = 0; i < columnName.length(); i++) {
            char c = Character.toUpperCase(columnName.charAt(i));
            result = result * 26 + (c - 'A' + 1);
        }
        return result - 1;
    }

    public static void main(String[] args) throws IOException {
        String path = "D:\\个人\\temp\\2026研发人员分配.xlsx";
        WorkHourInfo workHourInfo = new WorkHourInfo(path, 7, 0);
//        workHourInfo.addHolidays(19);
        workHourInfo.writeExcel("D:\\个人\\temp\\2026研发人员分配-完成.xlsx");
    }

}
