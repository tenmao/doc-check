package com.github.tenmao.check;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * @author tenmao
 * @since 2020/5/7
 */
@Data
public class Idcard {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final String number;
    private final LocalDate birthday;
    private final String province;
    private final boolean isMale;

    private String name;
    private String nation;
    private String address;

    public static Idcard parse(String idcardNumber) {
        Preconditions.checkNotNull(idcardNumber);

        idcardNumber = idcardNumber.trim();
        if (!IdcardUtils.validateIdCard18(idcardNumber)) {
            throw new IllegalArgumentException("illegal idcard number");
        }

        //解析出生日期
        String date = idcardNumber.substring(6, 14);
        LocalDate birthday = LocalDate.parse(date, FORMATTER);

        //解析籍贯省份
        String province = IdcardUtils.PROVINCE_CODES.get(idcardNumber.substring(0, 2));
        if (province == null) {
            throw new IllegalArgumentException("there is no province for this idcard number");
        }

        //解析性别
        String genderStr = idcardNumber.substring(16, 17);
        boolean gender = Integer.parseInt(genderStr) % 2 == 1;

        return new Idcard(idcardNumber, birthday, province, gender);
    }

    public int getAge() {
        return birthday.until(LocalDate.now()).getYears();
    }

    private static class IdcardUtils {

        /**
         * 中国公民身份证号码最小长度。
         */
        private static final int CHINA_ID_MIN_LENGTH = 15;

        /**
         * 中国公民身份证号码最大长度。
         */
        private static final int CHINA_ID_MAX_LENGTH = 18;

        /**
         * 每位加权因子
         */
        private static final int[] POWER = {
                7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2
        };

        private static final CharMatcher DIGIT_MATCHER = CharMatcher.forPredicate(Character::isDigit);

        /**
         * 第18位校检码
         */
        public static final String[] verifyCode = {
                "1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"
        };
        /**
         * 最低年限
         */
        private static final int MIN = 1930;
        static final ImmutableMap<String, String> PROVINCE_CODES = ImmutableMap.<String, String>builder()
                .put("11", "北京")
                .put("12", "天津")
                .put("13", "河北")
                .put("14", "山西")
                .put("15", "内蒙古")
                .put("21", "辽宁")
                .put("22", "吉林")
                .put("23", "黑龙江")
                .put("31", "上海")
                .put("32", "江苏")
                .put("33", "浙江")
                .put("34", "安徽")
                .put("35", "福建")
                .put("36", "江西")
                .put("37", "山东")
                .put("41", "河南")
                .put("42", "湖北")
                .put("43", "湖南")
                .put("44", "广东")
                .put("45", "广西")
                .put("46", "海南")
                .put("50", "重庆")
                .put("51", "四川")
                .put("52", "贵州")
                .put("53", "云南")
                .put("54", "西藏")
                .put("61", "陕西")
                .put("62", "甘肃")
                .put("63", "青海")
                .put("64", "宁夏")
                .put("65", "新疆")
                .put("71", "台湾")
                .put("81", "香港")
                .put("82", "澳门")
                .put("91", "国外")
                .build();
        /**
         * 台湾身份首字母对应数字
         */
        private static final ImmutableMap<String, Integer> TW_FIRST_CODE = ImmutableMap.<String, Integer>builder()
                .put("A", 10)
                .put("B", 11)
                .put("C", 12)
                .put("D", 13)
                .put("E", 14)
                .put("F", 15)
                .put("G", 16)
                .put("H", 17)
                .put("J", 18)
                .put("K", 19)
                .put("L", 20)
                .put("M", 21)
                .put("N", 22)
                .put("P", 23)
                .put("Q", 24)
                .put("R", 25)
                .put("S", 26)
                .put("T", 27)
                .put("U", 28)
                .put("V", 29)
                .put("X", 30)
                .put("Y", 31)
                .put("W", 32)
                .put("Z", 33)
                .put("I", 34)
                .put("O", 35)
                .build();

        /**
         * 香港身份首字母对应数字
         */
        private static final ImmutableMap<String, Integer> HK_FIRST_CODE = ImmutableMap.<String, Integer>builder()
                .put("A", 1)
                .put("B", 2)
                .put("C", 3)
                .put("R", 18)
                .put("U", 21)
                .put("Z", 26)
                .put("X", 24)
                .put("W", 23)
                .put("O", 15)
                .put("N", 14)
                .build();


        /**
         * 将15位身份证号码转换为18位
         *
         * @param idCard 15位身份编码
         * @return 18位身份编码
         */
        public static String convert15CardTo18(String idCard) {
            String idCard18 = "";
            if (idCard.length() != CHINA_ID_MIN_LENGTH) {
                return null;
            }
            if (DIGIT_MATCHER.matchesAllOf(idCard)) {
                // 获取出生年月日
                String birthday = idCard.substring(6, 12);
                Date birthDate = null;
                try {
                    birthDate = new SimpleDateFormat("yyMMdd").parse(birthday);
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
                Calendar cal = Calendar.getInstance();
                if (birthDate != null) {
                    cal.setTime(birthDate);
                }
                // 获取出生年(完全表现形式,如：2010)
                String sYear = String.valueOf(cal.get(Calendar.YEAR));
                idCard18 = idCard.substring(0, 6) + sYear + idCard.substring(8);
                // 转换字符数组
                char[] cArr = idCard18.toCharArray();
                int[] iCard = convertCharToInt(cArr);
                int iSum17 = getPowerSum(iCard);
                // 获取校验位
                char sVal = getCheckCode18(iSum17);
                idCard18 += sVal;

            } else {
                return null;
            }
            return idCard18;
        }

        /**
         * 验证18位身份编码是否合法
         *
         * @param idCard 身份编码
         * @return 是否合法
         */
        public static boolean validateIdCard18(String idCard) {
            boolean bTrue = false;
            if (idCard.length() == CHINA_ID_MAX_LENGTH) {
                // 前17位
                String code17 = idCard.substring(0, 17);
                // 第18位
                String code18 = idCard.substring(17, CHINA_ID_MAX_LENGTH);
                if (DIGIT_MATCHER.matchesAllOf(code17)) {
                    char[] cArr = code17.toCharArray();
                    int[] iCard = convertCharToInt(cArr);
                    int iSum17 = getPowerSum(iCard);
                    // 获取校验位
                    char val = getCheckCode18(iSum17);
                    if (String.valueOf(val).equalsIgnoreCase(code18)) {
                        bTrue = true;
                    }
                }
            }
            return bTrue;
        }

        /**
         * 验证15位身份编码是否合法
         *
         * @param idCard 身份编码
         * @return 是否合法
         */
        public static boolean validateIdCard15(String idCard) {
            if (idCard.length() != CHINA_ID_MIN_LENGTH) {
                return false;
            }
            if (DIGIT_MATCHER.matchesAllOf(idCard)) {
                String proCode = idCard.substring(0, 2);
                if (PROVINCE_CODES.get(proCode) == null) {
                    return false;
                }
                String birthCode = idCard.substring(6, 12);
                Date birthDate = null;
                try {
                    birthDate = new SimpleDateFormat("yy").parse(birthCode.substring(0, 2));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar cal = Calendar.getInstance();
                if (birthDate != null) {
                    cal.setTime(birthDate);
                }
                if (!validDate(cal.get(Calendar.YEAR), Integer.parseInt(birthCode.substring(2, 4)),
                        Integer.parseInt(birthCode.substring(4, 6)))) {
                    return false;
                }
            } else {
                return false;
            }
            return true;
        }

        /**
         * 验证10位身份编码是否合法
         *
         * @param idCard 身份编码
         * @return 身份证信息数组
         * <p>
         * [0] - 台湾、澳门、香港 [1] - 性别(男M,女F,未知N) [2] - 是否合法(合法true,不合法false)
         * 若不是身份证件号码则返回null
         * </p>
         */
        public static String[] validateIdCard10(String idCard) {
            String[] info = new String[3];
            String card = idCard.replaceAll("[\\(|\\)]", "");
            if (card.length() != 8 && card.length() != 9 && idCard.length() != 10) {
                return null;
            }
            if (idCard.matches("^[a-zA-Z][0-9]{9}$")) { // 台湾
                info[0] = "台湾";
                String char2 = idCard.substring(1, 2);
                if ("1".equals(char2)) {
                    info[1] = "M";
                } else if ("2".equals(char2)) {
                    info[1] = "F";
                } else {
                    info[1] = "N";
                    info[2] = "false";
                    return info;
                }
                info[2] = validateTwCard(idCard) ? "true" : "false";
            } else if (idCard.matches("^[1|5|7][0-9]{6}\\(?[0-9A-Z]\\)?$")) { // 澳门
                info[0] = "澳门";
                info[1] = "N";
                // TODO
            } else if (idCard.matches("^[A-Z]{1,2}[0-9]{6}\\(?[0-9A]\\)?$")) { // 香港
                info[0] = "香港";
                info[1] = "N";
                info[2] = validateHkCard(idCard) ? "true" : "false";
            } else {
                return null;
            }
            return info;
        }

        /**
         * 验证台湾身份证号码
         *
         * @param idCard 身份证号码
         * @return 验证码是否符合
         */
        private static boolean validateTwCard(String idCard) {
            String start = idCard.substring(0, 1);
            String mid = idCard.substring(1, 9);
            String end = idCard.substring(9, 10);
            Integer iStart = TW_FIRST_CODE.get(start);
            int sum = iStart / 10 + (iStart % 10) * 9;
            char[] chars = mid.toCharArray();
            int iflag = 8;
            for (char c : chars) {
                sum = sum + Integer.parseInt(String.valueOf(c)) * iflag;
                iflag--;
            }
            return (sum % 10 == 0 ? 0 : (10 - sum % 10)) == Integer.parseInt(end);
        }

        /**
         * 验证香港身份证号码(存在Bug，部份特殊身份证无法检查)
         * <p>
         * 身份证前2位为英文字符，如果只出现一个英文字符则表示第一位是空格，对应数字58 前2位英文字符A-Z分别对应数字10-35
         * 最后一位校验码为0-9的数字加上字符"A"，"A"代表10
         * </p>
         * <p>
         * 将身份证号码全部转换为数字，分别对应乘9-1相加的总和，整除11则证件号码有效
         * </p>
         *
         * @param idCard 身份证号码
         * @return 验证码是否符合
         */
        private static boolean validateHkCard(String idCard) {
            String card = idCard.replaceAll("[\\(|\\)]", "");
            int sum = 0;
            if (card.length() == 9) {
                sum = (Integer.valueOf(card.substring(0, 1).toUpperCase().toCharArray()[0]) - 55) * 9
                        + (Integer.valueOf(card.substring(1, 2).toUpperCase().toCharArray()[0]) - 55) * 8;
                card = card.substring(1, 9);
            } else {
                sum = 522 + (Integer.valueOf(card.substring(0, 1).toUpperCase().toCharArray()[0]) - 55) * 8;
            }
            String mid = card.substring(1, 7);
            String end = card.substring(7, 8);
            char[] chars = mid.toCharArray();
            int iflag = 7;
            for (char c : chars) {
                sum = sum + Integer.parseInt(String.valueOf(c)) * iflag;
                iflag--;
            }
            if ("A".equals(end.toUpperCase())) {
                sum = sum + 10;
            } else {
                sum = sum + Integer.parseInt(end);
            }
            return sum % 11 == 0;
        }

        /**
         * 将字符数组转换成数字数组
         *
         * @param ca 字符数组
         * @return 数字数组
         */
        private static int[] convertCharToInt(char[] ca) {
            int len = ca.length;
            int[] iArr = new int[len];
            try {
                for (int i = 0; i < len; i++) {
                    iArr[i] = Integer.parseInt(String.valueOf(ca[i]));
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e);
            }
            return iArr;
        }

        /**
         * 将身份证的每位和对应位的加权因子相乘之后，再得到和值
         *
         * @param iArr
         * @return 身份证编码。
         */
        private static int getPowerSum(int[] iArr) {
            int iSum = 0;
            if (POWER.length == iArr.length) {
                for (int i = 0; i < iArr.length; i++) {
                    for (int j = 0; j < POWER.length; j++) {
                        if (i == j) {
                            iSum = iSum + iArr[i] * POWER[j];
                        }
                    }
                }
            }
            return iSum;
        }

        /**
         * 将power和值与11取模获得余数进行校验码判断
         *
         * @param iSum
         * @return 校验位
         */
        private static char getCheckCode18(int iSum) {
            return "10x98765432".toCharArray()[iSum % 11];
        }

        /**
         * 验证小于当前日期 是否有效
         *
         * @param iYear  待验证日期(年)
         * @param iMonth 待验证日期(月 1-12)
         * @param iDate  待验证日期(日)
         * @return 是否有效
         */
        private static boolean validDate(int iYear, int iMonth, int iDate) {
            try {
                LocalDate.of(iYear, iMonth, iDate);
                return true;
            } catch (RuntimeException e) {
                return false;
            }
        }
    }
}
