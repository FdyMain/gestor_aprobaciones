package com.fdymain.fid.pages;

import com.fdymain.fid.utils.Constants;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

public class LoginPageFidu {

    private final Page page;

    // Selectores estables
    private static final String USER_INPUT = "input[name='userName']";
    private static final String PASS_INPUT = "input[type='password']";

    public LoginPageFidu(Page page) {
        this.page = page;
    }

    private Locator username() { return page.locator(USER_INPUT); }
    private Locator password() { return page.locator(PASS_INPUT); }

    private Locator loginBtn() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Iniciar sesión"));
    }

    public void open() {
        page.navigate(Constants.getLoginUrl());
        username().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    public void login(String user, String pass) {
        open();

        username().click();
        username().fill(user);

        password().click();
        password().fill(pass);

        loginBtn().click();

        //page.pause();


        // Ajusta esta espera a un elemento real post-login
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }
}
