package com.gttcgf.nanoscan.tools;

import java.util.regex.Pattern;

public class InputDataVerification {
    private InputDataVerification(){}

    // 密码输入框的输入限制
    public static boolean passwordInputVerification(String password) {
        Pattern p = Pattern.compile("[\\x21-\\x7E]{8,32}");
        return p.matcher(password).matches();
    }
    // 手机号输入框的输入限制
    public static boolean phoneNumberInputVerification(String phoneNumber) {
        Pattern p = Pattern.compile("1[3-9]\\d{9}");
        return p.matcher(phoneNumber).matches();
    }
    // 短信验证码输入框的输入限制
    public static boolean smsVerificationCodeVerification(String smsCode){
        Pattern p = Pattern.compile("\\d{6}");
        return p.matcher(smsCode).matches();
    }

    public static boolean checkCodeVerification(String checkCode){
        Pattern p = Pattern.compile("[\\x21-\\x7E]{6}");
        return p.matcher(checkCode).matches();
    }
}
