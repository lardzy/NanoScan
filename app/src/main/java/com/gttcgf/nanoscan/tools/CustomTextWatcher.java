package com.gttcgf.nanoscan.tools;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.function.Function;

public class CustomTextWatcher implements TextWatcher {
    private final EditText editText;
    private final Function<String, Boolean> validator;
    private final String ErrorMsg;
    private int lengthToMeet;
    private Runnable onLengthMet;

    // 构造函数，用于初始化输入框、验证函数和错误提示信息
    public CustomTextWatcher(EditText editText, Function<String, Boolean> validator, String ErrorMsg) {
        this.editText = editText;
        this.validator = validator;
        this.ErrorMsg = ErrorMsg;
    }

    // 重载构造函数，增加了一个参数lengthToMeet，用于指定输入框的长度
    public CustomTextWatcher(EditText editText, Function<String, Boolean> validator, String errorMsg, int lengthToMeet, Runnable onLengthMet) {
        this.editText = editText;
        this.validator = validator;
        this.ErrorMsg = errorMsg;
        this.lengthToMeet = lengthToMeet;
        this.onLengthMet = onLengthMet;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (!validator.apply(editable.toString())) {
            editText.setError(ErrorMsg);
        } else {
            editText.setError(null);
            if (lengthToMeet != 0 && onLengthMet != null && editable.length() == lengthToMeet) {
                onLengthMet.run();
            }
        }
    }
}
