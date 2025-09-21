package in.gov.ayush.terminology.runner;


import in.gov.ayush.terminology.model.ConceptMapping;
import in.gov.ayush.terminology.model.Icd11Code;
import in.gov.ayush.terminology.model.NamasteCode;
import in.gov.ayush.terminology.service.ConceptMappingService;
import in.gov.ayush.terminology.service.Icd11Service;
import in.gov.ayush.terminology.service.NamasteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
@Profile("cli")
public class TerminologyCliRunner implements CommandLineRunner {

    @Autowired
    private NamasteService namasteService;

    @Autowired
    private Icd11Service icd11Service;

    @Autowired
    private ConceptMappingService mappingService;

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("NAMASTE-ICD11 Terminology Service CLI");
        System.out.println("=====================================");

        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> searchNamasteCodes(scanner);
                case "2" -> searchIcd11Codes(scanner);
                case "3" -> translateNamesteToTm2(scanner);
                case "4" -> translateNamasteToBiomedicine(scanner);
                case "5" -> showStatistics();
                case "6" -> createFhirCondition(scanner);
                case "7" -> generateMappings();
                case "8" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\nSelect an option:");
        System.out.println("1. Search NAMASTE codes");
        System.out.println("2. Search ICD-11 codes");
        System.out.println("3. Translate NAMASTE to TM2");
        System.out.println("4. Translate NAMASTE to Biomedicine");
        System.out.println("5. Show statistics");
        System.out.println("6. Create FHIR Condition");
        System.out.println("7. Generate automatic mappings");
        System.out.println("8. Exit");
        System.out.print("Choice: ");
    }

    private void searchNamasteCodes(Scanner scanner) {
        System.out.print("Enter search term: ");
        String term = scanner.nextLine().trim();

        var results = namasteService.searchCodes(term, 0, 10);

        System.out.println("\nNAMASTE Search Results:");
        System.out.println("-----------------------");

        if (results.isEmpty()) {
            System.out.println("No results found.");
        } else {
            for (NamasteCode code : results.getContent()) {
                System.out.printf("Code: %s | Display: %s | System: %s%n",
                        code.getCode(), code.getDisplay(), code.getSystem());
                if (code.getDefinition() != null) {
                    System.out.printf("Definition: %s%n", code.getDefinition());
                }
                System.out.println();
            }
            System.out.printf("Showing %d of %d total results%n",
                    results.getNumberOfElements(), results.getTotalElements());
        }
    }

    private void searchIcd11Codes(Scanner scanner) {
        System.out.print("Enter search term: ");
        String term = scanner.nextLine().trim();

        var results = icd11Service.searchCodes(term, 0, 10);

        System.out.println("\nICD-11 Search Results:");
        System.out.println("----------------------");

        if (results.isEmpty()) {
            System.out.println("No results found.");
        } else {
            for (Icd11Code code : results.getContent()) {
                System.out.printf("Code: %s | Title: %s | Type: %s%n",
                        code.getCode(), code.getTitle(), code.getCodeType());
                if (code.getDefinition() != null) {
                    System.out.printf("Definition: %s%n", code.getDefinition());
                }
                System.out.println();
            }
            System.out.printf("Showing %d of %d total results%n",
                    results.getNumberOfElements(), results.getTotalElements());
        }
    }

    private void translateNamesteToTm2(Scanner scanner) {
        System.out.print("Enter NAMASTE code: ");
        String code = scanner.nextLine().trim();

        List<ConceptMapping> mappings = mappingService.translateNamesteToTm2(code);

        System.out.println("\nTranslation Results (NAMASTE → TM2):");
        System.out.println("------------------------------------");

        if (mappings.isEmpty()) {
            System.out.println("No mappings found for code: " + code);
        } else {
            for (ConceptMapping mapping : mappings) {
                System.out.printf("Target Code: %s | Equivalence: %s%n",
                        mapping.getTargetCode(), mapping.getEquivalence());
                if (mapping.getComment() != null) {
                    System.out.printf("Comment: %s%n", mapping.getComment());
                }
                System.out.println();
            }
        }
    }

    private void translateNamasteToBiomedicine(Scanner scanner) {
        System.out.print("Enter NAMASTE code: ");
        String code = scanner.nextLine().trim();

        List<ConceptMapping> mappings = mappingService.translateNamasteToBiomedicine(code);

        System.out.println("\nTranslation Results (NAMASTE → Biomedicine):");
        System.out.println("--------------------------------------------");

        if (mappings.isEmpty()) {
            System.out.println("No mappings found for code: " + code);
        } else {
            for (ConceptMapping mapping : mappings) {
                System.out.printf("Target Code: %s | Equivalence: %s%n",
                        mapping.getTargetCode(), mapping.getEquivalence());
                if (mapping.getComment() != null) {
                    System.out.printf("Comment: %s%n", mapping.getComment());
                }
                System.out.println();
            }
        }
    }

    private void showStatistics() {
        System.out.println("\nSystem Statistics:");
        System.out.println("==================");
        System.out.printf("NAMASTE Codes: %d%n", namasteService.getCodeCount());
        System.out.printf("  - Ayurveda: %d%n", namasteService.getCodeCountBySystem(NamasteCode.TraditionalSystem.AYURVEDA));
        System.out.printf("  - Siddha: %d%n", namasteService.getCodeCountBySystem(NamasteCode.TraditionalSystem.SIDDHA));
        System.out.printf("  - Unani: %d%n", namasteService.getCodeCountBySystem(NamasteCode.TraditionalSystem.UNANI));
        System.out.printf("ICD-11 Codes: %d%n", icd11Service.getCodeCount());
        System.out.printf("  - TM2: %d%n", icd11Service.getCodeCountByType(Icd11Code.CodeType.TM2));
        System.out.printf("  - Biomedicine: %d%n", icd11Service.getCodeCountByType(Icd11Code.CodeType.BIOMEDICINE));
        System.out.printf("Total Mappings: %d%n", mappingService.getMappingCount());
    }

    private void createFhirCondition(Scanner scanner) {
        System.out.print("Enter NAMASTE code: ");
        String namasteCode = scanner.nextLine().trim();
        System.out.print("Enter Patient ID: ");
        String patientId = scanner.nextLine().trim();

        System.out.println("\nGenerating FHIR Condition with dual coding...");
        System.out.println("Condition would be created with:");
        System.out.printf("- NAMASTE Code: %s%n", namasteCode);
        System.out.printf("- Patient ID: %s%n", patientId);
        System.out.println("- Mapped ICD-11 codes (if available)");
        System.out.println("\nFHIR Condition resource would be returned in JSON/XML format.");
    }

    private void generateMappings() {
        System.out.println("\nGenerating automatic mappings...");
        mappingService.generateAutomaticMappings();
        System.out.println("Automatic mapping generation completed.");
    }
}
