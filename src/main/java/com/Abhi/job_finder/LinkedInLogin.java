package com.Abhi.job_finder;

import com.microsoft.playwright.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class LinkedInLogin {

    public static void main(String[] args) throws InterruptedException {
        Path sessionPath = Paths.get("playwright-session");

        try (Playwright playwright = Playwright.create()) {
            BrowserContext context = playwright.chromium().launchPersistentContext(
                    sessionPath,
                    new BrowserType.LaunchPersistentContextOptions()
                            .setHeadless(false)
            );

            Page page = context.newPage();
            page.navigate("https://www.linkedin.com/login");

            System.out.println("===========================================");
            System.out.println("Log in to LinkedIn in the browser window.");
            System.out.println("After you are fully logged in and can see");
            System.out.println("your LinkedIn feed, come back here and");
            System.out.println("press ENTER to save the session.");
            System.out.println("===========================================");

            new Scanner(System.in).nextLine(); // Wait for you to press Enter

            context.close();
            System.out.println("Session saved successfully!");
        }
    }
}