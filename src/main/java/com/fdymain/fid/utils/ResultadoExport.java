package com.fdymain.fid.utils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ResultadoExport {

    private static final String OUTPUT_FILE = "resultado.properties";

    public static void escribir(
            String fechaInicio,
            String fechaFin,
            int aprobadas,
            int fallidas,
            int procesadas,
            String duracion) throws Exception {

        String estado = (fallidas == 0) ? "EXITOSO" : "CON ERRORES";

        String contenido =
                "aprobadas="   + aprobadas   + "\n" +
                "fallidas="    + fallidas    + "\n" +
                "procesadas="  + procesadas  + "\n" +
                "duracion="    + duracion    + "\n" +
                "fechaInicio=" + fechaInicio + "\n" +
                "fechaFin="    + fechaFin    + "\n" +
                "estado="      + estado;

        Files.writeString(Paths.get(OUTPUT_FILE), contenido, StandardCharsets.UTF_8);
    }
}
