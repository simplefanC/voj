package com.simplefanc.voj.judger.common.constants;

import java.util.List;

/**
 * {0} --> tmpfs_dir
 * {1} --> srcName
 * {2} --> exeName
 */
public enum CompileConfig {
    C("C", "main.c", "main", 3000L, 10000L, 256 * 1024 * 1024L, "/usr/bin/gcc -DONLINE_JUDGE -w -fmax-errors=3 -std=c11 {1} -lm -o {2}", Constants.DEFAULT_ENV),

    CWithO2("C With O2", "main.c", "main", 3000L, 10000L, 256 * 1024 * 1024L, "/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c11 {1} -lm -o {2}", Constants.DEFAULT_ENV),

    CPP("C++", "main.cpp", "main", 10000L, 20000L, 512 * 1024 * 1024L, "/usr/bin/g++ -DONLINE_JUDGE -w -fmax-errors=3 -std=c++14 {1} -lm -o {2}", Constants.DEFAULT_ENV),

    CPPWithO2("C++ With O2", "main.cpp", "main", 10000L, 20000L, 512 * 1024 * 1024L, "/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c++14 {1} -lm -o {2}", Constants.DEFAULT_ENV),

    JAVA("Java", "Main.java", "Main.jar", 10000L, 20000L, 512 * 1024 * 1024L, "/bin/bash -c \"javac -encoding utf8 {1} && jar -cvf {2} *.class\"", Constants.DEFAULT_ENV),

    PYTHON2("Python2", "main.py", "main.pyc", 3000L, 10000L, 128 * 1024 * 1024L, "/usr/bin/python -m py_compile ./{1}", Constants.DEFAULT_ENV),

    PYTHON3("Python3", "main.py", "__pycache__/main.cpython-37.pyc", 3000L, 10000L, 128 * 1024 * 1024L, "/usr/bin/python3.7 -m py_compile ./{1}", Constants.DEFAULT_ENV),

    GOLANG("Golang", "main.go", "main", 3000L, 5000L, 512 * 1024 * 1024L, "/usr/bin/go build -o {2} {1}", Constants.DEFAULT_ENV),

    CS("C#", "Main.cs", "main", 5000L, 10000L, 512 * 1024 * 1024L, "/usr/bin/mcs -optimize+ -out:{0}/{2} {0}/{1}", Constants.DEFAULT_ENV),

    PyPy2("PyPy2", "main.py", "__pycache__/main.pypy-73.pyc", 3000L, 10000L, 256 * 1024 * 1024L, "/usr/bin/pypy -m py_compile {0}/{1}", Constants.DEFAULT_ENV),

    PyPy3("PyPy3", "main.py", "__pycache__/main.pypy38.pyc", 3000L, 10000L, 256 * 1024 * 1024L, "/usr/bin/pypy3 -m py_compile {0}/{1}", Constants.DEFAULT_ENV),

    SPJ_C("SPJ-C", "spj.c", "spj", 3000L, 5000L, 512 * 1024 * 1024L, "/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c99 {1} -lm -o {2}", Constants.DEFAULT_ENV),

    SPJ_CPP("SPJ-C++", "spj.cpp", "spj", 10000L, 20000L, 512 * 1024 * 1024L, "/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c++14 {1} -lm -o {2}", Constants.DEFAULT_ENV),

    INTERACTIVE_C("INTERACTIVE-C", "interactive.c", "interactive", 3000L, 5000L, 512 * 1024 * 1024L, "/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c99 {1} -lm -o {2}", Constants.DEFAULT_ENV),

    INTERACTIVE_CPP("INTERACTIVE-C++", "interactive.cpp", "interactive", 10000L, 20000L, 512 * 1024 * 1024L, "/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c++14 {1} -lm -o {2}", Constants.DEFAULT_ENV);

    private final String language;
    private final String srcName;
    private final String exeName;
    private final Long maxCpuTime;
    private final Long maxRealTime;
    private final Long maxMemory;
    private final String command;
    private final List<String> envs;

    // TODO 参数过多
    CompileConfig(String language, String srcName, String exeName, Long maxCpuTime, Long maxRealTime, Long maxMemory,
                  String command, List<String> envs) {
        this.language = language;
        this.srcName = srcName;
        this.exeName = exeName;
        this.maxCpuTime = maxCpuTime;
        this.maxRealTime = maxRealTime;
        this.maxMemory = maxMemory;
        this.command = command;
        this.envs = envs;
    }

    public static CompileConfig getCompilerByLanguage(String language) {
        for (CompileConfig compileConfig : CompileConfig.values()) {
            if (compileConfig.getLanguage().equals(language)) {
                return compileConfig;
            }
        }
        return null;
    }

    public String getLanguage() {
        return language;
    }

    public String getSrcName() {
        return srcName;
    }

    public String getExeName() {
        return exeName;
    }

    public Long getMaxCpuTime() {
        return maxCpuTime;
    }

    public Long getMaxRealTime() {
        return maxRealTime;
    }

    public Long getMaxMemory() {
        return maxMemory;
    }

    public String getCommand() {
        return command;
    }

    public List<String> getEnvs() {
        return envs;
    }
}
