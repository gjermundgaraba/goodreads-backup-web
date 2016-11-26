package com.gjermundbjaanes;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
public class BackupController {

    @Value("${scriptLocation}")
    private String scriptLocation;

    @Value("${pythonExecutable}")
    private String pythonExecutable;


    @RequestMapping(value = "/backup", method = RequestMethod.GET)
    public void performBackup(@RequestParam(value="userId") String userId, HttpServletResponse response) {
        // TODO: DO SOME CHECKS ON USERID TO ENSURE NO INJECTION

        try {
            Path tempBackupFolder = Files.createTempDirectory("goodreads-backup-folder");
            Process process = runBackupScript(userId, tempBackupFolder);

            // TODO: Log ERRORS
            // InputStream inputStream = process.getErrorStream();
            // TODO: If script fails, fail the call

            process.waitFor();

            File zipFile = zipBackupFiles(tempBackupFolder);
            streamZipFileToUser(response, zipFile);
        } catch (IOException | InterruptedException e) {
            // TODO: Handle errors a bit better?
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private File zipBackupFiles(Path tempBackupFolder) throws IOException {
        File zipFile = new File(tempBackupFolder.toAbsolutePath().toString(), "backupFiles.zip");

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for(final File fileToBackup : tempBackupFolder.toFile().listFiles()) {
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

    private boolean fileToBackupIsTheZip(File fileToBackup, File backupFilesZip) {
        return fileToBackup.getName().equals(backupFilesZip.getName());
    }


    private void streamZipFileToUser(HttpServletResponse response, File zipFile) throws IOException {
        FileInputStream zipFileInputStream = new FileInputStream(zipFile);
        IOUtils.copy(zipFileInputStream, response.getOutputStream());
        response.flushBuffer();
    }

    private Process runBackupScript(String userId, Path tempFolder) throws IOException {
        String[] cmd = {
                pythonExecutable,
                scriptLocation,
                userId,
                tempFolder.toAbsolutePath().toString()
        };

        return Runtime.getRuntime().exec(cmd);
    }

}
