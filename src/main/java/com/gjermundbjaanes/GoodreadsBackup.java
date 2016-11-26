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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class GoodreadsBackup {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${scriptLocation}")
    private String scriptLocation;

    @Value("${pythonExecutable}")
    private String pythonExecutable;

    public File performBackup(int userId) throws IOException, InterruptedException {
        Path tempBackupFolder = Files.createTempDirectory("goodreads-backup-folder");

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

    private File zipBackupFiles(Path tempBackupFolder) throws IOException {
        File zipFile = new File(tempBackupFolder.toAbsolutePath().toString(), "backupFiles.zip");

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

        return zipFile;
    }

    private File[] getListOfFilesInFolder(Path tempBackupFolder) {
        return tempBackupFolder.toFile() != null ? tempBackupFolder.toFile().listFiles() : new File[]{};
    }

    private boolean fileToBackupIsTheZip(File fileToBackup, File backupFilesZip) {
        return fileToBackup.getName().equals(backupFilesZip.getName());
    }
}
