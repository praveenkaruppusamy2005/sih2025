package in.gov.ayush.terminology.utils;


import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import in.gov.ayush.terminology.model.NamasteCode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvParser {

    private static final Logger logger = LoggerFactory.getLogger(CsvParser.class);

    public List<NamasteCode> parseNamasteCsv(String filePath) throws IOException {
        List<NamasteCode> codes = new ArrayList<>();

        try {
            InputStreamReader reader;

            if (filePath.startsWith("classpath:")) {
                ClassPathResource resource = new ClassPathResource(filePath.substring(10));
                reader = new InputStreamReader(resource.getInputStream());
            } else {
                reader = new FileReader(filePath);
            }

            try (CSVReader csvReader = new CSVReader(reader)) {
                List<String[]> records = csvReader.readAll();

                // Skip header row
                for (int i = 1; i < records.size(); i++) {
                    String[] record = records.get(i);

                    if (record.length >= 4) {
                        try {
                            NamasteCode code = new NamasteCode();
                            code.setCode(record[0].trim());
                            code.setDisplay(record[1].trim());
                            code.setDefinition(record.length > 2 ? record[2].trim() : null);
                            code.setSystem(parseTraditionalSystem(record[3].trim()));
                            code.setCategory(record.length > 4 ? record[4].trim() : null);
                            code.setSubcategory(record.length > 5 ? record[5].trim() : null);
                            code.setWhoTerminologyCode(record.length > 6 ? record[6].trim() : null);
                            code.setIcd11Tm2Code(record.length > 7 ? record[7].trim() : null);
                            code.setIcd11BiomedicineCode(record.length > 8 ? record[8].trim() : null);
                            code.setVersion("1.0");

                            codes.add(code);
                        } catch (Exception e) {
                            logger.warn("Failed to parse CSV record at line {}: {}", i + 1, e.getMessage());
                        }
                    }
                }
            } catch (CsvException e) {
                logger.error("Failed to read CSV file", e);
                throw new IOException("Failed to read CSV file", e);
            }

        } catch (IOException e) {
            logger.error("Failed to open CSV file: {}", filePath, e);
            throw e;
        }

        logger.info("Parsed {} NAMASTE codes from CSV", codes.size());
        return codes;
    }

    private NamasteCode.TraditionalSystem parseTraditionalSystem(String systemStr) {
        if (systemStr == null || systemStr.trim().isEmpty()) {
            return NamasteCode.TraditionalSystem.AYURVEDA; // Default
        }

        return switch (systemStr.toUpperCase()) {
            case "SIDDHA" -> NamasteCode.TraditionalSystem.SIDDHA;
            case "UNANI" -> NamasteCode.TraditionalSystem.UNANI;
            default -> NamasteCode.TraditionalSystem.AYURVEDA;
        };
    }
}