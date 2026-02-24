package com.afei.boxyledger.ui.utils

import java.time.LocalDate
import java.util.Calendar

/**
 * Simplified Lunar Calendar Utility
 * Note: A full implementation is very complex. This is a placeholder/simplified version
 * for demonstration purposes. In a real app, use a library like 'lunar-java'.
 */
object LunarUtils {
    private val lunarInfo = longArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0
    )

    private val solarTermInfo = intArrayOf(
        0, 21208, 42467, 63836, 85337, 107014, 128867, 150921, 173149, 195551, 218072, 240693,
        263343, 285989, 308563, 331033, 353350, 375494, 397447, 419210, 440795, 462224, 483532, 504758
    )

    private val solarTerms = arrayOf(
        "小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨",
        "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋", "处暑",
        "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"
    )

    private val lunarMonthName = arrayOf("正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊")
    private val lunarDayName = arrayOf(
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )

    fun getLunarDate(date: LocalDate): String {
        // Lunar calendar calculation is limited to 1900-2049 due to data availability
        if (date.year < 1900 || date.year >= 2050) {
            android.util.Log.w("LunarUtils", "Date out of range: ${date.year}")
            return ""
        }

        try {
            val iLunarDate = solarToLunar(date)
            val month = iLunarDate[1]
            val day = iLunarDate[2]
            val isLeap = iLunarDate[3] == 1

            // Safety check for array bounds
            if (month < 1 || month > 12) {
                android.util.Log.e("LunarUtils", "Invalid lunar month: $month")
                return ""
            }
            if (day < 1 || day > 30) {
                android.util.Log.e("LunarUtils", "Invalid lunar day: $day")
                return ""
            }
            
            // Holidays & Terms
            val term = getSolarTerm(date.year, date.monthValue, date.dayOfMonth)
            if (term.isNotEmpty()) return term
            
            // Common Festivals
            if (date.monthValue == 1 && date.dayOfMonth == 1) return "元旦"
            if (date.monthValue == 5 && date.dayOfMonth == 1) return "劳动节"
            if (date.monthValue == 10 && date.dayOfMonth == 1) return "国庆"
            if (date.monthValue == 2 && date.dayOfMonth == 14) return "情人节"

            // Lunar Festivals
            // Leap month festivals are generally not celebrated, or handled specifically.
            // Simplified here: if it is leap month, skip standard festivals or treat as normal?
            // Usually festivals like Spring Festival (1.1) only on normal month.
            if (!isLeap) {
                if (month == 1 && day == 1) return "春节"
                if (month == 1 && day == 15) return "元宵"
                if (month == 5 && day == 5) return "端午"
                if (month == 8 && day == 15) return "中秋"
            }
            
            val monthStr = if (isLeap) "闰${lunarMonthName[month - 1]}月" else "${lunarMonthName[month - 1]}月"
            return if (day == 1) monthStr else lunarDayName[day - 1]
        } catch (e: Exception) {
            android.util.Log.e("LunarUtils", "Error calculating lunar date for $date", e)
            return ""
        }
    }

    private fun solarToLunar(date: LocalDate): IntArray {
        val cal = Calendar.getInstance()
        cal.set(date.year, date.monthValue - 1, date.dayOfMonth, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        val base = Calendar.getInstance()
        base.set(1900, 0, 31, 0, 0, 0)
        base.set(Calendar.MILLISECOND, 0)

        var offset = ((cal.timeInMillis - base.timeInMillis) / 86400000).toInt()
        
        var iYear = 1900
        var daysOfYear = 0
        // Data only available for 1900-2049
        while (iYear < 2050 && offset > 0) {
            daysOfYear = yearDays(iYear)
            if (offset < daysOfYear) break // Found the current lunar year
            offset -= daysOfYear
            iYear++
        }
        
        // If offset is still huge or negative after loop, something is wrong
        if (iYear >= 2050 || offset < 0) {
             android.util.Log.e("LunarUtils", "Calculation failed: iYear=$iYear, offset=$offset")
             // Fallback to avoid crash
             return intArrayOf(1900, 1, 1, 0) 
        }

        val lunarYear = iYear
        val leapMonth = leapMonth(iYear)
        var isLeap = false
        
        var iMonth = 1
        var daysOfMonth = 0
        while (iMonth < 13 && offset > 0) {
            if (leapMonth > 0 && iMonth == (leapMonth + 1) && !isLeap) {
                --iMonth
                isLeap = true
                daysOfMonth = leapDays(lunarYear)
            } else {
                daysOfMonth = monthDays(lunarYear, iMonth)
            }
            
            if (offset < daysOfMonth) break // Found the current lunar month
            
            offset -= daysOfMonth
            if (isLeap && iMonth == (leapMonth + 1)) isLeap = false
            iMonth++
        }
        
        // The original logic had some complex offset adjustments for negative/zero cases
        // But with the "break" logic above, offset should be the day index within the month (0-29 or 0-30)
        
        return intArrayOf(lunarYear, iMonth, offset + 1, if (isLeap) 1 else 0)
    }

    private fun yearDays(y: Int): Int {
        var i = 348
        // Iterate through bits for months 1-12 (bit 15 down to 4)
        for (m in 1..12) {
             if ((lunarInfo[y - 1900] and (0x10000L shr m)) != 0L) i++
        }
        return i + leapDays(y)
    }

    private fun leapDays(y: Int): Int {
        return if (leapMonth(y) != 0) {
            if ((lunarInfo[y - 1900] and 0x10000) != 0L) 30 else 29
        } else 0
    }

    private fun leapMonth(y: Int): Int {
        return (lunarInfo[y - 1900] and 0xf).toInt()
    }

    private fun monthDays(y: Int, m: Int): Int {
        return if ((lunarInfo[y - 1900] and (0x10000 shr m).toLong()) == 0L) 29 else 30
    }
    
    private fun getSolarTerm(year: Int, month: Int, day: Int): String {
        // Simplified Solar Term Calculation based on 1900
        // This is complex to implement perfectly without a large table or complex algo.
        // Returning empty for now to avoid errors, or implement very basic check if needed.
        return "" 
    }
}
