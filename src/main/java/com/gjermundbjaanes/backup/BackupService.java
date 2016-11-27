package com.gjermundbjaanes.backup;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class BackupService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BackupFileService backupFileService;

    @Value("${scriptLocation}")
    private String scriptLocation;

    @Value("${pythonExecutable}")
    private String pythonExecutable;

    public String performBackup(int userId) throws IOException, InterruptedException {
        Path tempBackupFolder = Files.createTempDirectory("goodreads-temp-");

        Process process = runBackupScript(userId, tempBackupFolder);
        InputStream errorStream = process.getErrorStream();
        process.waitFor();
        if (process.exitValue() != 0) {
            String error = IOUtils.toString(errorStream, "UTF-8");
            logger.error(error);

            throw new RuntimeException("Failed to run backup");
        }

        return zipBackupFiles(tempBackupFolder);
    }




    private Process runBackupScript(int userId, Path tempFolder) throws IOException {
        String[] cmd = {
                pythonExecutable,
                scriptLocation,
                Integer.toString(userId),
                tempFolder.toAbsolutePath().toString()
        };

        return Runtime.getRuntime().exec(cmd);
    }

    private String zipBackupFiles(Path tempBackupFolder) throws IOException {
        String backupId = UUID.randomUUID().toString();
        File zipFile = backupFileService.getBackupFile(backupId);

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (final File fileToBackup : getListOfFilesInFolder(tempBackupFolder)) {
                if (!fileToBackupIsTheZip(fileToBackup, zipFile)) {
                    ZipEntry zipEntry = new ZipEntry(fileToBackup.getName());
                    zipOutputStream.putNextEntry(zipEntry);
                    zipOutputStream.write(Files.readAllBytes(fileToBackup.toPath()));
                    zipOutputStream.closeEntry();
                }
            }
        }

        return backupId;
    }

    private File[] getListOfFilesInFolder(Path tempBackupFolder) {
        return tempBackupFolder.toFile() != null ? tempBackupFolder.toFile().listFiles() : new File[]{};
    }

    private boolean fileToBackupIsTheZip(File fileToBackup, File backupFilesZip) {
        return fileToBackup.getName().equals(backupFilesZip.getName());
    }
}
