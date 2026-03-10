package com.fdymain.fid.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class LogoutPage {

    private final Page page;

    public LogoutPage(Page page) {
        this.page = page;
    }

    public void cerrarSesion() {

        System.out.println(">>> Cerrando sesión de usuario");

        page.getByRole(AriaRole.BANNER)
                .getByRole(AriaRole.BUTTON)
                .nth(4)
                .click();

        page.getByText("Cerrar Sesión").click();

        page.getByLabel("Sí").click();

        System.out.println(">>> Sesión cerrada correctamente");
    }
}