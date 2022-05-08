package com.simplefanc.voj.judger.common.constants;

import java.util.Arrays;
import java.util.List;

/**
 * @author chenfan
 * @date 2022/5/7 21:46
 **/
public interface JudgeServerConstant {
    List<String> LANGUAGE_LIST = Arrays.asList("G++ 7.5.0", "GCC 7.5.0", "Python 3.7.5", "Python 2.7.17", "OpenJDK 1.8", "Golang 1.16",
            "C# Mono 4.6.2", "PHP 7.3.33", "JavaScript Node 14.19.0", "JavaScript V8 8.4.109",
            "PyPy 2.7.18 (7.3.8)", "PyPy 3.8.12 (7.3.8)");

    String VERSION = "20220306";
}
