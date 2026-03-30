package com.fdymain.fid.runners;

import com.fdymain.fid.pages.AprobacionComunicacionesPage;
import com.fdymain.fid.pages.LoginPageFidu;
import com.fdymain.fid.pages.LogoutPage;
import com.fdymain.fid.playwright_config.EvidenceManager;
import com.fdymain.fid.utils.Constants;
import com.fdymain.fid.utils.ExcelManager;
import com.fdymain.fid.utils.EmailNotifier;
import com.fdymain.fid.utils.ResultadoExport;
import com.fdymain.fid.utils.WhatsAppNotifier;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AprobacionComunicacionesTest {

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    private ExcelManager excelManager;

    private int aprobadas  = 0;
    private int reintentos = 0;
    private int procesadas = 0;

    private Map<String, Integer> otrosCodigos = new LinkedHashMap<>();

    private long   startTime;
    private String fechaInicio;

    // ================= SETUP =================

    @BeforeAll
    void beforeAll() {

        EvidenceManager.createEvidenceFolder();

        playwright = Playwright.create();

        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(headless);

        if (!headless) {
            launchOptions.setArgs(Arrays.asList("--start-maximized"));
        }

        browser = playwright.chromium().launch(launchOptions);
    }

    @AfterAll
    void afterAll() {

        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void beforeEach() {

        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(null)
                        .setRecordVideoDir(java.nio.file.Paths.get(EvidenceManager.getVideoPath()))
        );

        context.tracing().start(
                new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true)
        );

        page = context.newPage();
    }

    @AfterEach
    void afterEach() {

        try {

            context.tracing().stop(
                    new Tracing.StopOptions()
                            .setPath(java.nio.file.Paths.get(
                                    EvidenceManager.getTracePath("trace.zip")
                            ))
            );

        } catch (Exception e) {

            System.out.println("Error guardando trace: " + e.getMessage());
        }

        if (context != null) context.close();
    }

    // ================= TEST =================

    @Test
    void aprobarComunicacionesIdProceso500() throws Exception {

        startTime = System.currentTimeMillis();

        try {

            fechaInicio = obtenerFecha();

            excelManager = new ExcelManager();
            excelManager.inicializar();

            log("Inicio del proceso automático");

            new LoginPageFidu(page)
                    .login(Constants.getUsername(), Constants.getPassword());

            AprobacionComunicacionesPage aprobPage =
                    new AprobacionComunicacionesPage(page, context);

            aprobPage.navegarBandeja();
            aprobPage.irUltimaPagina();

            while (true) {

                aprobPage.esperarGrid();

                Locator fila500 = aprobPage.buscarFilaPorIdProceso("500");

                if (fila500.count() > 0) {

                    log("Fila con idProceso 500 encontrada");

                    Map<String, String> datos = aprobPage.leerFila(fila500);

                    logDatos(datos);

                    boolean aprobado = aprobPage.aprobarFila(fila500);

                    procesadas++;

                    if (aprobado) {
                        aprobadas++;
                    } else {
                        reintentos++;
                    }

                    datos.put("estado",
                            aprobado
                                    ? "aprobado"
                                    : "falló en la aprobación, revise");

                    excelManager.guardarRegistro(datos);

                    log("Registro guardado en Excel");

                    if (!aprobado) {

                        log("Fallo la aprobación → regresando a bandeja");
                        aprobPage.navegarBandeja();
                    }

                    page.waitForTimeout(2000);
                    aprobPage.irUltimaPagina();

                    continue;
                }

                log("No hay idProceso 500 en esta página");

                if (aprobPage.hayPaginaAnterior()) {

                    log("Ir a página anterior");
                    aprobPage.irPaginaAnterior();

                } else {

                    log("No quedan páginas");
                    aprobPage.acumularOtrosCodigos(otrosCodigos);
                    break;
                }
            }

            // ===== CERRAR SESIÓN =====

            new LogoutPage(page).cerrarSesion();

            log("Sesión cerrada correctamente");

            excelManager.cerrar();

            log("Proceso terminado");

            // ===== EXPORTAR RESULTADO PARA PIPELINE =====

            try {
                ResultadoExport.escribir(
                        fechaInicio,
                        obtenerFecha(),
                        aprobadas,
                        reintentos,
                        procesadas,
                        calcularDuracion(),
                        "EXITOSO",
                        otrosCodigos
                );
            } catch (Exception e) {
                log("No se pudo exportar resultado: " + e.getMessage());
            }

            // ===== NOTIFICACIÓN WHATSAPP =====

            try {

                WhatsAppNotifier.enviarResultadoRobot(
                        obtenerFecha(),
                        procesadas,
                        reintentos,
                        calcularDuracion()
                );

            } catch (Exception e) {

                log("No se pudo enviar WhatsApp: " + e.getMessage());
            }

            // ===== NOTIFICACIÓN EMAIL =====

            try {

                EmailNotifier.enviarReporte(
                        fechaInicio,
                        obtenerFecha(),
                        aprobadas,
                        reintentos,
                        procesadas,
                        calcularDuracion(),
                        "EXITOSO",
                        otrosCodigos
                );

            } catch (Exception e) {

                log("No se pudo enviar email: " + e.getMessage());
            }

            log("Excel actualizado: " + excelManager.getExcelPath().toAbsolutePath());

        } catch (Exception e) {

            log("ERROR CRÍTICO: " + e.getMessage());

            notificarError(e);

            throw e;
        }
    }

    // ================= LOGGING =================

    private void log(String msg) {

        System.out.println("[" + Instant.now() + "] " + msg);
    }

    private void logDatos(Map<String, String> datos) {

        log("--------------------------------------------------");
        log("Procesando registro:");
        log("Referencia   : " + datos.get("referencia"));
        log("Tarea        : " + datos.get("tarea"));
        log("Proceso      : " + datos.get("proceso"));
        log("IdProceso    : " + datos.get("idProceso"));
        log("Recibido     : " + datos.get("recibido"));
        log("FechaLimite  : " + datos.get("fechaLimite"));
        log("--------------------------------------------------");
    }

    // ================= UTILIDADES =================

    private String calcularDuracion() {

        long duration = System.currentTimeMillis() - startTime;
        long seconds  = duration / 1000;
        long minutes  = seconds / 60;
        seconds = seconds % 60;

        return minutes + "m" + seconds + "s";
    }

    private String obtenerFecha() {

        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private void notificarError(Exception e) {

        try {

            WhatsAppNotifier.enviarResultadoRobot(
                    obtenerFecha(),
                    procesadas,
                    reintentos + 1,
                    calcularDuracion()
            );

        } catch (Exception ex) {

            log("No se pudo enviar notificación de error: " + ex.getMessage());
        }

        try {

            ResultadoExport.escribir(
                    fechaInicio != null ? fechaInicio : obtenerFecha(),
                    obtenerFecha(),
                    aprobadas,
                    reintentos + 1,
                    procesadas,
                    calcularDuracion(),
                    "CON ERRORES",
                    otrosCodigos
            );

        } catch (Exception ex) {

            log("No se pudo exportar resultado de error: " + ex.getMessage());
        }

        try {

            EmailNotifier.enviarReporte(
                    fechaInicio != null ? fechaInicio : obtenerFecha(),
                    obtenerFecha(),
                    aprobadas,
                    reintentos + 1,
                    procesadas,
                    calcularDuracion(),
                    "CON ERRORES",
                    otrosCodigos
            );

        } catch (Exception ex) {

            log("No se pudo enviar email de error: " + ex.getMessage());
        }
    }
}
