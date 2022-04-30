package com.simplefanc.voj.judger.common.constants;

import java.util.List;

/**
 * {0} --> tmpfs_dir
 * {1} --> exeName (user or spj)
 * {2} --> The test case standard input file name of question
 * {3} --> The user's program output file name of question
 * {4} --> The test case standard output file name of question
 */
public enum RunConfig {
    C("C", "{0}/{1}", "main", Constants.DEFAULT_ENV),

    CWithO2("C With O2", "{0}/{1}", "main", Constants.DEFAULT_ENV),

    CPP("C++", "{0}/{1}", "main", Constants.DEFAULT_ENV),

    CPPWithO2("C++ With O2", "{0}/{1}", "main", Constants.DEFAULT_ENV),

    JAVA("Java", "/usr/bin/java -cp {0}/{1} Main", "Main.jar", Constants.DEFAULT_ENV),

    PYTHON2("Python2", "/usr/bin/python {1}", "main", Constants.DEFAULT_ENV),

    PYTHON3("Python3", "/usr/bin/python3.7 {1}", "main", Constants.PYTHON3_ENV),

    GOLANG("Golang", "{0}/{1}", "main", Constants.GOLANG_ENV),

    CS("C#", "/usr/bin/mono {0}/{1}", "main", Constants.DEFAULT_ENV),

    PyPy2("PyPy2", "/usr/bin/pypy {1}", "main.pyc", Constants.DEFAULT_ENV),

    PyPy3("PyPy3", "/usr/bin/pypy3 {1}", "main.pyc", Constants.PYTHON3_ENV),

    PHP("PHP", "/usr/bin/php {1}", "main.php", Constants.DEFAULT_ENV),

    JS_NODE("JavaScript Node", "/usr/bin/node {1}", "main.js", Constants.DEFAULT_ENV),

    JS_V8("JavaScript V8", "/usr/bin/jsv8/d8 {1}", "main.js", Constants.DEFAULT_ENV),

    SPJ_C("SPJ-C", "{0}/{1} {2} {3} {4}", "spj", Constants.DEFAULT_ENV),

    SPJ_CPP("SPJ-C++", "{0}/{1} {2} {3} {4}", "spj", Constants.DEFAULT_ENV),

    INTERACTIVE_C("INTERACTIVE-C", "{0}/{1} {2} {3} {4}", "interactive", Constants.DEFAULT_ENV),

    INTERACTIVE_CPP("INTERACTIVE-C++", "{0}/{1} {2} {3} {4}", "interactive", Constants.DEFAULT_ENV);

    private final String language;
    private final String command;
    private final String exeName;
    private final List<String> envs;

    RunConfig(String language, String command, String exeName, List<String> envs) {
        this.language = language;
        this.command = command;
        this.exeName = exeName;
        this.envs = envs;
    }

    public static RunConfig getRunnerByLanguage(String language) {
        for (RunConfig runConfig : RunConfig.values()) {
            if (runConfig.getLanguage().equals(language)) {
                return runConfig;
            }
        }
        return null;
    }

    public String getLanguage() {
        return language;
    }

    public String getCommand() {
        return command;
    }

    public String getExeName() {
        return exeName;
    }

    public List<String> getEnvs() {
        return envs;
    }

}
