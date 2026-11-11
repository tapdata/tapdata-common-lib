package io.tapdata.contracts;

import io.tapdata.ErrorCodeEntity;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/** Test-only maintenance witness; not a production catalog API. */
public final class MaintenanceExercise {
    public static void main(String[] args) throws Exception {
        if (args.length != 2 || !(args[0].equals("export") || args[0].equals("conflict"))) {
            throw new IllegalArgumentException("Usage: export|conflict OUTPUT_FILE");
        }
        Class<?>[] declarations = args[0].equals("conflict")
                ? new Class<?>[]{ExistingErrorContractTest.Codes.class, ExistingErrorContractTest.Duplicate.class}
                : new Class<?>[]{ExistingErrorContractTest.Codes.class, ExistingErrorContractTest.OtherConnector.class};
        // Validate everything before touching the previous output.
        Map<String, ErrorCodeEntity> catalog = ExistingErrorContractTest.collect(declarations);
        StringBuilder text = new StringBuilder("fullCode\trawCode\tfield\n");
        for (Map.Entry<String, ErrorCodeEntity> entry : catalog.entrySet()) {
            text.append(entry.getKey()).append('\t').append(entry.getValue().getCode()).append('\t')
                    .append(entry.getValue().getName()).append('\n');
        }
        Path output = Paths.get(args[1]).toAbsolutePath();
        Path temporary = Files.createTempFile(output.getParent(), ".exercise-", ".tmp");
        try {
            Files.write(temporary, text.toString().getBytes(StandardCharsets.UTF_8));
            Files.move(temporary, output, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
        System.out.println("Exported " + catalog.size() + " declarations to " + output);
    }
}
