package com.davidparker.dms.service;

import com.davidparker.dms.model.RegisteredApplication;

public class ApplicationContext {

    private static final ThreadLocal<RegisteredApplication> currentApplication = new ThreadLocal<>();

    public static void setCurrent(RegisteredApplication application) {
        currentApplication.set(application);
    }

    public static RegisteredApplication getCurrent() {
        return currentApplication.get();
    }

    public static void clear() {
        currentApplication.remove();
    }
}
