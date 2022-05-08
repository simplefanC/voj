package com.simplefanc.voj.judger.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JudgeLanguage {
    C("C"),
    CWithO2("C With O2"),
    CPP("C++"),
    CPPWithO2("C++ With O2"),
    JAVA("Java"),
    PYTHON2("Python2"),
    PYTHON3("Python3"),
    GOLANG("Golang"),
    CS("C#");
    private final String language;
}
