package com.simplefanc.voj.judger.common.constants;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/1/1 13:00
 * @Description: 常量类
 */
public interface Constants {

    List<String> DEFAULT_ENV = Arrays.asList("PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
            "LANG=en_US.UTF-8", "LC_ALL=en_US.UTF-8", "LANGUAGE=en_US:en", "HOME=/w");

    List<String> PYTHON3_ENV = Arrays.asList("LANG=en_US.UTF-8", "LANGUAGE=en_US:en", "LC_ALL=en_US.UTF-8",
            "PYTHONIOENCODING=utf-8");

    List<String> GOLANG_ENV = Arrays.asList("GODEBUG=madvdontneed=1", "GOCACHE=off",
            "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin", "LANG=en_US.UTF-8",
            "LANGUAGE=en_US:en", "LC_ALL=en_US.UTF-8");

}