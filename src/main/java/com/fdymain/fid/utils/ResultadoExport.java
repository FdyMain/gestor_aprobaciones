package com.fdymain.fid.utils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

public class ResultadoExport {

    private static final String OUTPUT_FILE = "resultado.properties";

    public static void escribir(
            String fechaInicio,
            String fechaFin,
            int aprobadas,
            int reintentos,
            int procesadas,
            String duracion,
            String estado,
            Map<String, Integer> otrosCodigos) throws Exception {

        String otrosStr = otrosCodigos == null || otrosCodigos.isEmpty()
                ? ""
                : otrosCodigos.entrySet().stream()
                        .map(e -> e.getKey() + ":" + e.getValue())
                        .collect(Collectors.joining(","));

        String contenido =
                "aprobadas="    + aprobadas   + "\n" +
                "reintentos="   + reintentos  + "\n" +
                "procesadas="   + procesadas  + "\n" +
                "duracion="     + duracion    + "\n" +
                "fechaInicio="  + fechaInicio + "\n" +
                "fechaFin="     + fechaFin    + "\n" +
                "estado="       + estado      + "\n" +
                "otros_codigos=" + otrosStr;

        Files.writeString(Paths.get(OUTPUT_FILE), contenido, StandardCharsets.UTF_8);
    }
}
