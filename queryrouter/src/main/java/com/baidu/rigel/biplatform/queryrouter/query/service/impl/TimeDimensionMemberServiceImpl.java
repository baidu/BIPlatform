/**
 * Copyright (c) 2014 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.rigel.biplatform.queryrouter.query.service.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.TimeRangeDetail;
import com.baidu.rigel.biplatform.ac.util.TimeUtils;
import com.baidu.rigel.biplatform.queryrouter.query.exception.MetaException;
import com.baidu.rigel.biplatform.queryrouter.query.service.DimensionMemberService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * 
 * 时间维度成员计算实现类
 *
 * @author david.wang
 * @version 1.0.0.1
 */
@Service(DimensionMemberService.TIME_MEMBER_SERVICE)
public class TimeDimensionMemberServiceImpl implements DimensionMemberService {

    /**
     * 季度名称
     */
    private static final String[] QUARTER_NAMES = new String[] { "Q1", "Q2", "Q3", "Q4" };

    /**
     * 季度月份对应关系
     */
    private static final String[][] QUARTER_MONTH_MAPPING = new String[][] { new String[] { "0101", "0201", "0301" },
            new String[] { "0401", "0501", "0601" }, new String[] { "0701", "0801", "0901" },
            new String[] { "1001", "1101", "1201" } };

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MiniCubeMember> getMembers(Cube cube, Level level, DataSourceInfo dataSourceInfo, Member parentMember,
            Map<String, String> params) throws MetaException {
        List<MiniCubeMember> members = Lists.newArrayList();
        // 如果params里面有限制时间那么取params里面的filter的时间，
        // TODO 此方案只针对level为day的情况，不支持时间维度组及其他时间维度
        Set<String> values = Sets.newHashSet();
        for (Dimension dim : cube.getDimensions().values()) {
            String filterValue = params.get (dim.getId());
            if (dim.getType() == DimensionType.TIME_DIMENSION
                    && filterValue != null
                    && !MetaNameUtil.isAllMemberUniqueName(StringUtils.split(filterValue, ",")[0])) {
                Set<String> uniquesValues = Sets.newHashSet();
                String[] uniquesNames = StringUtils.split(filterValue, ",");
                for (String uniqueName : uniquesNames) {
                    uniquesValues.add(MetaNameUtil.getNameFromMetaName(uniqueName));
                }
                // 每一个维度的查询值求交集
                if (values.isEmpty()) {
                    values.addAll(uniquesValues);
                } else {
                    values.retainAll(uniquesValues);
                }
            }
        }
        if (!values.isEmpty()) {
            try {
                return this.genDayMembersWithParent(level, parentMember, values.toArray());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new MetaException(e);
            }
        }
        
        // 判断是否依据父节点获取成员信息
        if (parentMember != null) {
            switch (parentMember.getLevel().getType()) {
                case TIME_YEARS:
                    List<MiniCubeMember> membersWithYearParent = genMembersWithYearParent(level, parentMember);
                    members.addAll(membersWithYearParent);
                    return members;
                case TIME_QUARTERS:
                    List<MiniCubeMember> membersWithQuarterParent = genMembersWithQuarterParent(level, parentMember);
                    members.addAll(membersWithQuarterParent);
                    return members;
                case TIME_MONTHS:
                    List<MiniCubeMember> membersWithMonthParent = genMembersWithMonthParent(level, parentMember);
                    members.addAll(membersWithMonthParent);
                    return members;
                case TIME_WEEKS:
                    List<MiniCubeMember> membersWithWeekParent = genMembersWithWeekParent(level, parentMember);
                    members.addAll(membersWithWeekParent);
                    return members;
                case TIME_DAYS:
                    List<MiniCubeMember> membersWithDayParent = genMembersWithDayParent(level, parentMember);
                    members.addAll(membersWithDayParent);
                    return members;
                default:
                    throw new IllegalArgumentException("Invalidate time dimension level type : "
                            + parentMember.getLevel().getType());
            }
        }
        // 如果父成员为空，根据level获取默认成员信息
        // （当前年份、当前年的季度、当前年的月份、当前年的星期、当前年的天的信息）
        genDefaultMembers(level, parentMember, members);
        return members;
    }

    /**
     * 
     * @param level
     * @param parentMember
     * @return
     */
    private List<MiniCubeMember> genMembersWithDayParent(Level level, Member parentMember) {
        // 如果为All节点，取本年到当前日期的所有天
        if (parentMember.isAll()) {
            return genMembersWithDayParentForAll(level, parentMember);
        } else {
            // 如果为非All，取当前月份的所有天
            try {
                return genDayMembersWithParent(level, parentMember, TimeUtils.getMonthDays(null));
            } catch (Exception e) {
                return Lists.newArrayList();
            }
        }
    }

    /**
     * 获取当前年份第一天到当前天的所有日期
     * 
     * @param level
     * @param parentMember
     * @return
     */
    private List<MiniCubeMember> genMembersWithDayParentForAll(Level level, Member parentMember) {
        List<MiniCubeMember> members = Lists.newArrayList();
        Calendar calNow = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.add (Calendar.MONTH,  -1);
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        while (cal.before(calNow) || (cal.compareTo(calNow) == 0)) {
            String day = sf.format(cal.getTime());
            MiniCubeMember dayMember = new MiniCubeMember(day);
            dayMember.setCaption(day);
            dayMember.setLevel(level);
            dayMember.setParent(parentMember);
            dayMember.setName(day);
            dayMember.setVisible(true);
            dayMember.getQueryNodes().add(day);
            members.add(dayMember);
            cal.add(Calendar.DATE, 1);
        }
        return members;
    }

    /**
     * 获取成员信息（父成员为星期）
     * 
     * @param level
     * @param parentMember
     * @return
     */
    private List<MiniCubeMember> genMembersWithWeekParent(Level level, Member parentMember) {
        // 如果为All节点，取当年起始星期到当前星期
        if (parentMember.isAll()) {
            try {
                return genDayMembersWithWeekParentForAll(level, parentMember);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else {
            // 获取该星期所在的所有天
            switch (level.getType()) {
                case TIME_DAYS:
                    try {
                        String name = parentMember.getName();
                        Date date = TimeRangeDetail.getTime(name);
                        TimeRangeDetail monthRange = TimeUtils.getWeekDays(date);
                        return genDayMembersWithParent(level, parentMember, monthRange);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                case TIME_MONTHS:
                case TIME_YEARS:
                case TIME_QUARTERS:
                case TIME_WEEKS:
                default:
                    throw new IllegalArgumentException("Invalidate level type : " + level.getType()
                            + " with parent type : " + parentMember.getLevel().getType());
            }
        }
    }

    /**
     * 获取某一年的所有星期所在的第一天
     * 
     * @param year
     * @return
     */
    private List<MiniCubeMember> genDayMembersWithWeekParentForAll(Level level, Member parentMember) throws Exception {
        List<MiniCubeMember> members = Lists.newArrayList();
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int weekNow = cal.get(Calendar.WEEK_OF_YEAR);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date firstWeek = getFirstDayOfWeek(cal.getTime());
        cal.setTime(firstWeek);
        int week = cal.get (Calendar.WEEK_OF_YEAR);
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        while (week <= weekNow) {
            String day = sf.format(cal.getTime());
            MiniCubeMember dayMember = new MiniCubeMember(day);
            String caption = year + "年第" + week + "周-" + day;
            dayMember.setCaption(caption);
            dayMember.setLevel(level);
            dayMember.setParent(parentMember);
            dayMember.setName(day);
            dayMember.setVisible(true);
            for (int i = 0; i <= 6; i++) {
                day = sf.format(cal.getTime());
                dayMember.getQueryNodes().add(day);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            members.add(dayMember);
            week++;
        }
        return members;
    }

    /**
     * 获取某个日期所在周的第一天
     * 
     * @param date
     * @return
     */
    private Date getFirstDayOfWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        return cal.getTime();
    }

    /**
     * 获取成员信息（父成员为月份）
     * 
     * @param level
     * @param parentMember
     * @return
     */
    private List<MiniCubeMember> genMembersWithMonthParent(Level level, Member parentMember) {
        if (parentMember.isAll()) {
            return genMembersWithMonthParentForAll(level, parentMember);
        } else {
            String parentName = parentMember.getName();
            switch (level.getType()) {
                // 获取当前月的所有天数
                case TIME_DAYS:
                    String year = parentName.substring(0, 4);
                    String month = parentName.substring(4, 6);
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, Integer.valueOf(year));
                    cal.set(Calendar.MONTH, Integer.valueOf(month) - 1);
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    TimeRangeDetail monthRange = TimeUtils.getMonthDays(cal.getTime(), 0, 0);
                    try {
                        return genDayMembersWithParent(level, parentMember, monthRange);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                case TIME_MONTHS:
                case TIME_YEARS:
                case TIME_QUARTERS:
                case TIME_WEEKS:
                    return genMembersFromOtherToWeek(level, parentMember);
                default:
                    throw new IllegalArgumentException("Invalidate level type : " + level.getType()
                            + " with parent type : " + parentMember.getLevel().getType());
            }
        }
    }

    /**
     * 
     * @param level
     * @param parentMember
     * @param members
     * 
     */
    private List<MiniCubeMember> genMembersWithMonthParentForAll(Level level, Member parentMember) {
        List<MiniCubeMember> members = Lists.newArrayList();
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR); // 当前年份
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        cal.set(Calendar.DAY_OF_MONTH, 1); // 设置每月第一天
        String day = sf.format(cal.getTime());
        String month = day.substring(4, 6);
        String caption = year + "年" + month + "月";
        MiniCubeMember firstDayOfMonth = new MiniCubeMember(day);
        firstDayOfMonth.setCaption(caption);
        firstDayOfMonth.setLevel(level);
        firstDayOfMonth.setParent(parentMember);
        firstDayOfMonth.setName(day);
        firstDayOfMonth.setVisible(true);
        int daysOfMonth = cal.getActualMaximum(Calendar.DATE);
        for (int j = 0; j < daysOfMonth; j++) {
            firstDayOfMonth.getQueryNodes().add(sf.format(cal.getTime()));
            cal.add(Calendar.DATE, 1);
        }
        members.add(firstDayOfMonth);
        return members;
    }

    /**
     * 通过父成员获取成员信息（父成员为季度）
     * 
     * @param level
     * @param parentMember
     * @return
     * 
     */
    private List<MiniCubeMember> genMembersWithQuarterParent(Level level, Member parentMember) {
        if (parentMember.isAll()) {
            return genMembersWithQuarterParentForAll(level, parentMember);
        } else {
            String name = parentMember.getName();
            String year = name.substring(0, 4);
            String month = name.substring(4, 6);
            String quarterStr = "Q" + (Integer.valueOf(month) / 3 + 1);
            String[] tmpArray = { "[Time]", "[" + year + "]", "[" + quarterStr + "]" };
            int quarterIndex = Integer.valueOf(tmpArray[2].substring(2, 3)) - 1;
            switch (level.getType()) {
                case TIME_MONTHS:
                    return genMonthMemberWithQuarterParent(level, parentMember, name, tmpArray, quarterIndex);
                case TIME_DAYS:
                    String time = tmpArray[1].substring(1, 5) + QUARTER_MONTH_MAPPING[quarterIndex][1];
                    try {
                        TimeRangeDetail monthRange = TimeUtils.getMonthDays(TimeRangeDetail.getTime(time), 1, 1);
                        try {
                            return genDayMembersWithParent(level, parentMember, monthRange);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                case TIME_YEARS:
                case TIME_QUARTERS:
                case TIME_WEEKS:
                    return genMembersFromOtherToWeek(level, parentMember);
                default:
                    throw new IllegalArgumentException("Invalidate level type : " + level.getType()
                            + " with parent type : " + parentMember.getLevel().getType());
            }
        }
    }

    /**
     * 获取当前年的所有季度
     * 
     * @param level
     * @param parentMember
     * @return
     */
    private List<MiniCubeMember> genMembersWithQuarterParentForAll(Level level, Member parentMember) {
        List<MiniCubeMember> members = Lists.newArrayList();
        Calendar cal = Calendar.getInstance(); // 当前日期
        cal.set(Calendar.DAY_OF_MONTH, 1); // 设置1号
        int nowMonth = cal.get(Calendar.MONTH) + 1; // 当前月份
        int quarterIndex = nowMonth / 3; // 季度索引
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        cal.set(Calendar.MONTH, quarterIndex * 3);
        cal.set(Calendar.DATE, 1);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(cal.getTime());
        calEnd.add(Calendar.MONTH, 2);
        calEnd.add(Calendar.DATE, calEnd.getActualMaximum(Calendar.DATE) - 1); // 截止日期
        String day = sf.format(cal.getTime());
        String year = day.substring(0, 4);
        String caption = year + "年第" + (quarterIndex + 1) + "季度";
        MiniCubeMember firstDayOfQuarter = new MiniCubeMember(day);
        firstDayOfQuarter.setCaption(caption);
        firstDayOfQuarter.setLevel(level);
        firstDayOfQuarter.setParent(parentMember);
        firstDayOfQuarter.setName(day);
        firstDayOfQuarter.setVisible(true);
        while (cal.before(calEnd) || (cal.compareTo(calEnd) == 0)) {
            firstDayOfQuarter.getQueryNodes().add(sf.format(cal.getTime()));
            cal.add(Calendar.DATE, 1);
        }
        members.add(firstDayOfQuarter);
        return members;
    }

    /**
     * 获取天成员信息
     * 
     * @param level
     * @param parentMember
     * @param time
     * @return
     * @throws Exception
     * 
     */
    private List<MiniCubeMember> genDayMembersWithParent(Level level, Member parentMember, TimeRangeDetail range)
            throws Exception {
        return this.genDayMembersWithParent(level, parentMember, range.getDays());
    }
    
    /**
     * 获取天成员信息
     * 
     * @param level
     * @param parentMember
     * @param time
     * @return
     * @throws Exception
     * 
     */
    private List<MiniCubeMember> genDayMembersWithParent(Level level, Member parentMember, Object[] days)
            throws Exception {
        List<MiniCubeMember> members = Lists.newArrayList();
        for (Object day : days) {
            String dayStr = day.toString();
            MiniCubeMember dayMember = new MiniCubeMember(dayStr);
            dayMember.setCaption(dayStr);
            dayMember.setLevel(level);
            dayMember.setParent(parentMember);
            dayMember.setName(dayStr);
            dayMember.setVisible(true);
            dayMember.getQueryNodes().add(dayStr);
            members.add(dayMember);
        }
        return members;
    } 

    /**
     * 通过父成员获取月成员信息（父成员为季度）
     * 
     * @param level
     * @param parentMember
     * @param name
     * @param tmpArray
     * @param quarterIndex
     * 
     */
    private List<MiniCubeMember> genMonthMemberWithQuarterParent(Level level, Member parentMember,
            String name, String[] tmpArray, int quarterIndex) {
        List<MiniCubeMember> members = Lists.newArrayList();
        Calendar cal = Calendar.getInstance();
        String[] months = QUARTER_MONTH_MAPPING[quarterIndex];
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        for (String month : months) {
            String memberName = "[Time]." + tmpArray[1] + ".[" + month.substring(0, 2) + "]";
            MiniCubeMember monthMember = new MiniCubeMember(memberName);
            monthMember.setCaption(tmpArray[1].substring(1, 5) + "年" + month.substring(0, 2) + "月");
            monthMember.setLevel(level);
            monthMember.setName(tmpArray[1].substring(1, 5) + month);
            monthMember.setParent(parentMember);
            monthMember.setVisible(true);
            cal.set(Calendar.YEAR, Integer.valueOf(tmpArray[1].substring(1, 5)));
            cal.set(Calendar.MONTH, Integer.valueOf(month.substring(0, 2)) - 1);
            cal.set(Calendar.DATE, 1);
            int daysOfMonth = cal.getActualMaximum(Calendar.DATE);
            for (int i = 0; i < daysOfMonth; i++) {
                String day = sf.format(cal.getTime());
                monthMember.getQueryNodes().add(day);
                cal.add(Calendar.DATE, 1);
            }
            members.add(monthMember);
        }
        return members;
    }

    /**
     * 通过父成员获取成员信息（父成员为年）
     * 
     * @param level
     * @param parentMember
     */
    private List<MiniCubeMember> genMembersWithYearParent(Level level, Member parentMember) {
        int year = Integer.valueOf(parentMember.getCaption());
        switch (level.getType()) {
            case TIME_QUARTERS:
                return genQuarterMembersWithYear(year, level, parentMember);
            case TIME_MONTHS:
                return genMonthMembersWithYear(year, level, parentMember);
            case TIME_DAYS:
                return genDayOfYearMembers(level, parentMember, year);
            case TIME_WEEKS:
            case TIME_YEARS:
            default:
                throw new IllegalArgumentException("Invalidate level type : " + level.getType()
                        + " with parent type : " + parentMember.getLevel().getType());

        }
    }

    /**
     * 
     * @param level
     * @param parentMember
     * @param year
     * @return
     * 
     */
    private List<MiniCubeMember> genDayOfYearMembers(Level level, Member parentMember, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        TimeRangeDetail yearDays = TimeUtils.getYearDays(cal.getTime());
        String[] days = yearDays.getDays();
        List<MiniCubeMember> members = Lists.newArrayList();
        for (String day : days) {
            MiniCubeMember dayMember = new MiniCubeMember(day);
            dayMember.setCaption(day);
            dayMember.setLevel(level);
            dayMember.setParent(parentMember);
            dayMember.setName(day);
            dayMember.setVisible(true);
            members.add(dayMember);
        }
        return members;
    }

    /**
     * 
     * @param level
     * @param parentMember
     * @param members
     * 
     */
    private void genDefaultMembers(Level level, Member parentMember, List<MiniCubeMember> members) {
        switch (level.getType()) {
            case TIME_YEARS:
                MiniCubeMember member = genMemberWithCurrentYear(level, parentMember);
                members.add(member);
                break;
            case TIME_QUARTERS:
                int quarterYear = TimeUtils.getCurrentYear();
                List<MiniCubeMember> quarterMembers = genQuarterMembersWithYear(quarterYear, level, parentMember);
                members.addAll(quarterMembers);
                break;
            case TIME_MONTHS:
                int year = TimeUtils.getCurrentYear();
                List<MiniCubeMember> monthMembers = genMonthMembersWithYear(year, level, parentMember);
                members.addAll(monthMembers);
                break;
            case TIME_WEEKS:
                MiniCubeMember weekMember = genWeekMemberWithCurrentYear(level, parentMember);
                members.add(weekMember);
                break;
            case TIME_DAYS:
                MiniCubeMember dayMember = genDayMemberWithCurrentTime(level, parentMember);
                members.add(dayMember);
                break;
            default:
                throw new IllegalStateException("Invalidate time dimension level type : " + level.getType());
        }
    }

    /**
     * 
     * @param level
     * @param parentMember
     * @return
     * 
     */
    private MiniCubeMember genDayMemberWithCurrentTime(Level level, Member parentMember) {
        String current = TimeUtils.getDays(0, 0).getStart();
        MiniCubeMember dayMember = new MiniCubeMember(current);
        dayMember.setCaption(current);
        dayMember.setLevel(level);
        dayMember.setParent(parentMember);
        dayMember.setName(current);
        dayMember.setVisible(true);
        return dayMember;
    }

    /**
     * 
     * @param level
     * @param parentMember
     * @return
     * 
     */
    private MiniCubeMember genWeekMemberWithCurrentYear(Level level, Member parentMember) {
        String firstDayOfWeek = TimeUtils.getWeekDays(null).getStart();
        MiniCubeMember weekMember = new MiniCubeMember(firstDayOfWeek);
        try {
            weekMember.setCaption(firstDayOfWeek.substring(0, 4) + "-" + TimeUtils.getWeekIndex(firstDayOfWeek) + "W");
        } catch (Exception e) {
            throw new RuntimeException("Invalidate date formate, expected [yyyyMMdd] " + "partten, but was : "
                    + firstDayOfWeek);
        }
        weekMember.setLevel(level);
        weekMember.setParent(parentMember);
        weekMember.setVisible(true);
        return weekMember;
    }

    /**
     * 
     * @param level
     * @param parentMember
     * 
     */
    private List<MiniCubeMember> genMonthMembersWithYear(int year, Level level, Member parentMember) {
        List<MiniCubeMember> rs = Lists.newArrayList();
        for (int i = 1; i <= 12; ++i) {
            String name = "[Time].[" + year + "].[";
            if (i < 10) {
                name += 0;
            }
            name = name + i + "]";
            MiniCubeMember monthMember = new MiniCubeMember(name);
            monthMember.setCaption(i + "月");
            monthMember.setLevel(level);
            monthMember.setName(name);
            monthMember.setParent(parentMember);
            monthMember.setVisible(true);
            rs.add(monthMember);
        }
        return rs;
    }

    /**
     * 
     * @param level
     * @param parentMember
     * 
     */
    private List<MiniCubeMember> genQuarterMembersWithYear(int year, Level level, Member parentMember) {
        List<MiniCubeMember> rs = Lists.newArrayList();
        for (int i = 0; i < 4; ++i) {
            String name = "[Time].[" + year + "].[" + QUARTER_NAMES[i] + "]";
            MiniCubeMember quarterMember = new MiniCubeMember(name);
            quarterMember.setCaption(QUARTER_NAMES[i]);
            quarterMember.setLevel(level);
            quarterMember.setName(name);
            quarterMember.setParent(parentMember);
            quarterMember.setVisible(true);
            rs.add(quarterMember);
        }
        return rs;
    }

    /**
     * 
     * 依据当前时间生成时间维度成员（适用于年粒度，返回当前年）
     * 
     * @param level
     * @param parentMember
     * @return
     * 
     */
    private MiniCubeMember genMemberWithCurrentYear(Level level, Member parentMember) {
        int year = TimeUtils.getCurrentYear();
        MiniCubeMember member = new MiniCubeMember("");
        member.setCaption(String.valueOf(year));
        member.setLevel(level);
        member.setParent(parentMember);
        member.setVisible(true);
        member.setName("[Time].[" + year + "]");
        return member;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiniCubeMember getMemberFromLevelByName(DataSourceInfo dataSourceInfo, Cube cube, Level level, String name,
            MiniCubeMember parent, Map<String, String> params) throws MetaException {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name can not be null");
        }
        
        if (name.startsWith ("All_")) {
            MiniCubeMember dayMember = new MiniCubeMember(name);
            dayMember.setCaption(name);
            dayMember.setLevel(level);
            dayMember.setParent(null);
            dayMember.setName(name);
            dayMember.setVisible(true);
            Dimension d = level.getDimension ();
            String days = params.get (d.getId ());
            String[] tmp = null;
            if (StringUtils.isNotEmpty (days)) {
                for (String day : days.split (",")) {
                    if (day.contains (name)) {
                        continue;
                    }
                    if (MetaNameUtil.isUniqueName (day)) {
                        tmp = MetaNameUtil.parseUnique2NameArray (day);
                        dayMember.getQueryNodes ().add (tmp[tmp.length - 1]);
                    }
                }
            }
            // 判断是否需要默认的组织时间,判断是否有其他时间维度条件
            boolean isGenDefaultTime = true;
            for (Dimension dim : cube.getDimensions().values()) {
                String filterValue = params.get (dim.getId());
                if (dim.getType() == DimensionType.TIME_DIMENSION
                        && filterValue != null
                        && !dim.getId().equals(level.getDimension().getId())
                        && !MetaNameUtil.isAllMemberUniqueName(filterValue)) {
                    // 忽略当前查询的层级
                    isGenDefaultTime = false;
                    break;
                }
            }
            if (CollectionUtils.isEmpty (dayMember.getQueryNodes ()) && isGenDefaultTime) {
                Set<String> queryNodes = Sets.newHashSet ();
                for (String day : TimeUtils.getMonthDays (Calendar.getInstance ().getTime ()).getDays ()) {
                    queryNodes.add (day);
                }
                dayMember.setQueryNodes (queryNodes);
            }
            return dayMember;
        } else {
            MiniCubeMember dayMember = new MiniCubeMember(name);
            dayMember.setCaption(name);
            dayMember.setLevel(level);
            dayMember.setParent(null);
            dayMember.setName(name);
            dayMember.setVisible(true);
            return dayMember;
        }
    }


    /**
     * 当从其他时间维度下钻到周粒度时的处理措施
     * 
     * @param level
     * @param parentMember
     * @return
     */
    private List<MiniCubeMember> genMembersFromOtherToWeek(Level level, Member parentMember) {
        List<MiniCubeMember> members = Lists.newArrayList();
        String parentName = parentMember.getName();
        String year = parentName.substring(0, 4);
        String month = parentName.substring(4, 6);
        int quarterIndex = Integer.valueOf(month) - 1; // 季度索引
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime(); // 当前日期
        Date nowWeek = this.getFirstDayOfWeek(now); // 当前日期所在的周一

        cal.set(Calendar.YEAR, Integer.valueOf(year));
        cal.set(Calendar.MONTH, Integer.valueOf(month) - 1);
        cal.set(Calendar.DATE, 1);
        cal.setFirstDayOfWeek(Calendar.MONDAY);

        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        int count = 1; // 周计数
        Date firstDay = cal.getTime();
        Date firstWeek = this.getFirstDayOfWeek(firstDay); // 当前季度第一天所在周的第一天
        Date lastDay;
        Date lastWeek;
        int daysOfMonth;
        switch (parentMember.getLevel().getType()) {
            case TIME_YEARS:
                break;
            case TIME_QUARTERS:
                cal.add(Calendar.MONTH, 2); // 当前季度的最后一个月份
                daysOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH); // 最后一个月天数
                cal.add(Calendar.DATE, daysOfMonth);
                lastDay = cal.getTime(); // 截止日期
                lastWeek = this.getFirstDayOfWeek(lastDay); // 截止日期所在的周一
                // 将当月最后一天的所在的周一同当前日期比较
                if (nowWeek.before(lastWeek) || (nowWeek.compareTo(lastWeek) == 0)) {
                    lastWeek = nowWeek;
                }
                cal.setTime(firstWeek); // 将日期设置为当前月第一天所在的周一
                while (cal.getTime().before(lastWeek) || (cal.getTime().compareTo(lastWeek) == 0)) {
                    String day = sf.format(cal.getTime());
                    String caption = "第" + (quarterIndex + 1) + "季度第" + count + "周";
                    caption = caption + day;
                    MiniCubeMember dayMember = new MiniCubeMember(day);
                    dayMember.setCaption(caption);
                    dayMember.setLevel(level);
                    dayMember.setParent(parentMember);
                    dayMember.setName(level.getDimension ().getName ());
                    dayMember.setVisible(true);
                    for (int i = 0; i <= 6; i++) {
                        dayMember.getQueryNodes().add(sf.format(cal.getTime()));
                        cal.add(Calendar.DATE, 1);
                    }
                    members.add(dayMember);
                    // cal.add(Calendar.DATE, 1);
                    count++;
                }
                break;
            case TIME_MONTHS:
                daysOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH); // 本月一共有多少天
                cal.add(Calendar.DATE, daysOfMonth);
                lastDay = cal.getTime(); // 截止日期
                lastWeek = this.getFirstDayOfWeek(lastDay); // 截止日期所在的周一
                // 将当月最后一天的所在的周一同当前日期比较
                if (nowWeek.before(lastWeek) || (nowWeek.compareTo(lastWeek) == 0)) {
                    lastWeek = nowWeek;
                }
                cal.setTime(firstWeek); // 将日期设置为当前月第一天所在的周一
                while (cal.getTime().before(lastWeek) || (cal.getTime().compareTo(lastWeek) == 0)) {
                    String day = sf.format(cal.getTime());
                    String caption = month + "月第" + count + "周";
                    MiniCubeMember dayMember = new MiniCubeMember(day);
                    dayMember.setCaption(caption);
                    dayMember.setLevel(level);
                    dayMember.setParent(parentMember);
                    dayMember.setName(level.getDimension ().getName ());
                    dayMember.setVisible(true);
                    for (int i = 0; i <= 6; i++) {
                        dayMember.getQueryNodes().add(sf.format(cal.getTime()));
                        cal.add(Calendar.DATE, 1);
                    }
                    members.add(dayMember);
                    // cal.add(Calendar.DATE, 1);
                    count++;
                }
                break;
            case TIME_WEEKS:
                break;
            case TIME_DAYS:
                break;
            default:
        }
        return members;
    }

    @Override
    public List<MiniCubeMember> getMemberFromLevelByNames(
            DataSourceInfo dataSourceInfo, Cube cube, Level level,
            Map<String, String> params, List<String> uniqueNameList) {
        return Lists.newArrayList ();
    }
}
