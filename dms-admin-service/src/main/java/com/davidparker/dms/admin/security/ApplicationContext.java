package com.davidparker.dms.admin.security;

import com.davidparker.dms.admin.model.RegisteredApplication;

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
