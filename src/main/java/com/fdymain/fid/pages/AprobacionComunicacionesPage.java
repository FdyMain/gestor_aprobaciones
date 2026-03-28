package com.fdymain.fid.pages;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.LinkedHashMap;
import java.util.Map;

public class AprobacionComunicacionesPage {

    private final Page page;
    private final BrowserContext context;

    public AprobacionComunicacionesPage(Page page, BrowserContext context) {
        this.page = page;
        this.context = context;
    }

    // ================= LOCATORS =================

    private static final String GRID_ROWS =
            "#BPM_ALL_TASKS-panel tr.dx-data-row";

    private static final String BTN_LAST =
            "a[aria-label='Last']";

    private static final String BTN_PREVIOUS =
            "a[aria-label='Previous']";

    private static final String COL_REFERENCIA =
            "td[aria-colindex='2']";

    private static final String COL_TAREA =
            "td[aria-colindex='3']";

    private static final String COL_PROCESO =
            "td[aria-colindex='4']";

    private static final String COL_ID_PROCESO =
            "td[aria-colindex='5']";

    private static final String COL_RECIBIDO =
            "td[aria-colindex='6']";

    private static final String COL_FECHA_LIM =
            "td[aria-colindex='7']";

    private static final String BTN_ABRIR =
            "td[aria-colindex='8'] a";

    // ================= NAVEGACIÓN =================

    public void navegarBandeja() {

        page.getByLabel("far fa-window-restore").click();

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Abrir bandeja de tareas")
        ).click();

        page.waitForSelector(GRID_ROWS);
    }

    // ================= PAGINACIÓN =================

    public void irUltimaPagina() {

        Locator last = page.locator(BTN_LAST);

        if (last.isVisible()) {

            String disabled = last.getAttribute("aria-disabled");

            if (!"true".equals(disabled)) {

                last.click();

                esperarGridActualizar();
            }
        }
    }

    public boolean hayPaginaAnterior() {

        Locator prev = page.locator(BTN_PREVIOUS);

        String disabled = prev.getAttribute("aria-disabled");

        return !"true".equals(disabled);
    }

    public void irPaginaAnterior() {

        Locator prev = page.locator(BTN_PREVIOUS);

        prev.waitFor();

        prev.click();

        page.waitForTimeout(1500);

        page.waitForSelector(GRID_ROWS);
    }

    // ================= GRID =================

    public int totalFilas() {

        page.waitForSelector(GRID_ROWS);

        return page.locator(GRID_ROWS).count();
    }

    // ================= LECTURA FILA =================

    public Map<String,String> leerFila(Locator row) {

        Map<String,String> datos = new LinkedHashMap<>();

        try {

            datos.put("referencia",
                    safeText(row.locator(COL_REFERENCIA)));

            datos.put("tarea",
                    safeText(row.locator(COL_TAREA)));

            datos.put("proceso",
                    safeText(row.locator(COL_PROCESO)));

            datos.put("idProceso",
                    safeText(row.locator(COL_ID_PROCESO)));

            datos.put("recibido",
                    safeText(row.locator(COL_RECIBIDO)));

            datos.put("fechaLimite",
                    safeText(row.locator(COL_FECHA_LIM)));

        }
        catch (Exception e) {

            System.out.println("⚠️ Error leyendo fila: " + e.getMessage());
        }

        return datos;
    }

    private String safeText(Locator locator) {

        try {

            locator.waitFor(
                    new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.ATTACHED)
                            .setTimeout(5000)
            );

            return locator.innerText().trim();

        }
        catch (Exception e) {

            return "";
        }
    }

    // ================= APROBACIÓN =================

    public boolean aprobarFila(Locator row) {

        try {

            System.out.println(">>> Click abrir tarea");

            Locator botonAbrir = row.locator(BTN_ABRIR);

            botonAbrir.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(5000)
            );

            botonAbrir.click();

            System.out.println(">>> Esperando iframe");

            page.waitForSelector("iframe");
            page.waitForLoadState(LoadState.NETWORKIDLE);

            FrameLocator iframe = page.frameLocator("iframe").first();

            System.out.println(">>> Esperando tab Datos de aprobación");

            Locator tabDatos = iframe.getByRole(
                    AriaRole.TAB,
                    new FrameLocator.GetByRoleOptions()
                            .setName("Datos de aprobación")
            );

            tabDatos.waitFor(
                    new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
            );

            tabDatos.click();

            System.out.println(">>> Esperando radio Aprobar comunicación");

            Locator radioAprobar = iframe.getByRole(
                    AriaRole.RADIO,
                    new FrameLocator.GetByRoleOptions()
                            .setName("Aprobar comunicación")
            );

            radioAprobar.waitFor(
                    new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
            );

            radioAprobar.scrollIntoViewIfNeeded();

            radioAprobar.click();

            System.out.println(">>> Escribiendo comentario");

            iframe.getByLabel("Comentarios de aprobación *")
                    .fill("Aprobado");

            System.out.println(">>> Click botón aprobar");

            Locator btnAprobar = page.getByLabel("fas fa-check-double");
            btnAprobar.waitFor();
            btnAprobar.click();

            System.out.println(">>> Esperando volver a la tabla");

            esperarGridActualizar();

            return true;

        }
        catch (Exception e) {

            System.out.println("❌ Error aprobando fila: " + e.getMessage());

            return false;
        }
    }

    // ================= OTROS CÓDIGOS =================

    public void acumularOtrosCodigos(Map<String, Integer> mapa) {
        page.locator(GRID_ROWS).all().forEach(row -> {
            String id = safeText(row.locator(COL_ID_PROCESO));
            if (!id.isBlank() && !"500".equals(id)) {
                mapa.merge(id, 1, Integer::sum);
            }
        });
    }

    // ================= BÚSQUEDA =================

    public void esperarGrid() {

        page.waitForSelector(GRID_ROWS);

        page.waitForTimeout(800);
    }

    public Locator buscarFilaPorIdProceso(String idProceso) {

        return page.locator(
                GRID_ROWS + ":has(td[aria-colindex='5']:has-text('" + idProceso + "'))"
        ).first();
    }

    // ================= UTILIDADES =================

    private void esperarGridActualizar() {

        page.waitForLoadState(LoadState.NETWORKIDLE);

        page.waitForSelector(
                GRID_ROWS,
                new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.VISIBLE)
        );
    }
}