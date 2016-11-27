package com.gjermundbjaanes;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static com.gjermundbjaanes.BackupCleaner.MILISECONDS_TIME_LIMIT_FOR_DOWNLOADS;

@Component
public class BackupService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${scriptLocation}")
    private String scriptLocation;

    @Value("${pythonExecutable}")
    private String pythonExecutable;

    @Value("${workFolder}")
    private String workFolder;

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

    public File findBackupFile(String backupId) {
        File backupFile = getBackupFile(backupId);

        if (!backupFile.exists()) {
            throw new RuntimeException("Backup file does not exist");
        }

        if (System.currentTimeMillis() - backupFile.lastModified() > MILISECONDS_TIME_LIMIT_FOR_DOWNLOADS) {
            throw new RuntimeException("It is too long since you created the backup. Please create it again");
        }

        return backupFile;
    }

    private File getBackupFile(String backupId) {
        return new File(workFolder, backupId + ".zip");
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
        File zipFile = getBackupFile(backupId);

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
