package com.hust.software;

import com.intellij.codeInspection.InspectionToolProvider;

/**
 * Provide inspection named LogAnalysis which grouped in log issues.
 * Created by Yan Yu on 2014-05-18.
 */
public class LogAnalysisProvider implements InspectionToolProvider {
    public Class[] getInspectionClasses() {
        return new Class[]{LogAnalysisInspection.class};
    }
}