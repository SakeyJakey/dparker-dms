package com.davidparker.dms.e2e.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class TestConfig {
    
    private static final String BASE_URL = System.getProperty("dms.base.url", "http://localhost:80");
    private static final String BROWSER = System.getProperty("browser", "chrome");
    private static final boolean HEADLESS = Boolean.parseBoolean(System.getProperty("headless", "false"));
    private static final String SELENIUM_HUB_URL = System.getProperty("selenium.hub.url");
    
    private static final int IMPLICIT_WAIT_SECONDS = 10;
    private static final int PAGE_LOAD_TIMEOUT_SECONDS = 30;
    
    public static WebDriver createWebDriver() {
        WebDriver driver;
        
        if (SELENIUM_HUB_URL != null && !SELENIUM_HUB_URL.isEmpty()) {
            driver = createRemoteWebDriver();
        } else {
            driver = createLocalWebDriver();
        }
        
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IMPLICIT_WAIT_SECONDS));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_TIMEOUT_SECONDS));
        driver.manage().window().maximize();
        
        return driver;
    }
    
    private static WebDriver createLocalWebDriver() {
        switch (BROWSER.toLowerCase()) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (HEADLESS) {
                    firefoxOptions.addArguments("--headless");
                }
                return new FirefoxDriver(firefoxOptions);
                
            case "chrome":
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                if (HEADLESS) {
                    chromeOptions.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
                }
                chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
                chromeOptions.addArguments("--disable-extensions");
                return new ChromeDriver(chromeOptions);
        }
    }
    
    private static WebDriver createRemoteWebDriver() {
        try {
            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setBrowserName(BROWSER);
            
            if (BROWSER.equals("chrome")) {
                ChromeOptions chromeOptions = new ChromeOptions();
                if (HEADLESS) {
                    chromeOptions.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
                }
                capabilities.merge(chromeOptions);
            } else if (BROWSER.equals("firefox")) {
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (HEADLESS) {
                    firefoxOptions.addArguments("--headless");
                }
                capabilities.merge(firefoxOptions);
            }
            
            return new RemoteWebDriver(new URL(SELENIUM_HUB_URL), capabilities);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Selenium Hub URL: " + SELENIUM_HUB_URL, e);
        }
    }
    
    public static String getBaseUrl() {
        return BASE_URL;
    }
    
    public static String getAdminServiceUrl() {
        return System.getProperty("dms.admin.url", "http://localhost:8081");
    }
    
    public static String getDocumentServiceUrl() {
        return System.getProperty("dms.document.url", "http://localhost:8083");
    }
    
    public static String getLlmServiceUrl() {
        return System.getProperty("dms.llm.url", "http://localhost:8085");
    }
    
    public static String getComplianceServiceUrl() {
        return System.getProperty("dms.compliance.url", "http://localhost:8084");
    }
}
