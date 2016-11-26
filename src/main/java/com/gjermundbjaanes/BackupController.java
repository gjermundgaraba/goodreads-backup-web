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

            File outZip = zipBackupFiles(tempBackupFolder);
            returnBackupFileToUser(response, outZip);
        } catch (IOException | InterruptedException e) {
            // TODO: Handle errors a bit better?
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private File zipBackupFiles(Path tempFolder) throws IOException {
        File outZip = new File(tempFolder.toAbsolutePath().toString(), "out.zip");
        FileOutputStream fout = new FileOutputStream(outZip);
        ZipOutputStream zout = new ZipOutputStream(fout);
        for(final File backupFile : tempFolder.toFile().listFiles()) {
            if (!backupFile.getName().equals("out.zip")) {
                ZipEntry zipEntry = new ZipEntry(backupFile.getName());
                zout.putNextEntry(zipEntry);
                zout.write(Files.readAllBytes(backupFile.toPath()));
                zout.closeEntry();
            }
        }
        zout.close();
        return outZip;
    }


    private void returnBackupFileToUser(HttpServletResponse response, File outZip) throws IOException {
        FileInputStream zipFileInputStream = new FileInputStream(outZip);
        IOUtils.copy(zipFileInputStream, response.getOutputStream());
        response.flushBuffer();
    }

    private Process runBackupScript(@RequestParam(value = "userId") String userId, Path tempFolder) throws IOException {
        String[] cmd = {
                pythonExecutable,
                scriptLocation,
                userId,
                tempFolder.toAbsolutePath().toString()
        };

        return Runtime.getRuntime().exec(cmd);
    }

}
