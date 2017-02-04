package com.gjermundbjaanes.backup;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
public class BackupController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BackupService backupService;

    @Autowired
    private BackupFileService backupFileService;

    @RequestMapping(value = "/backup", method = RequestMethod.GET)
    public String performBackup(@RequestParam(value = "userId") int userId) {
        try {
            return backupService.performBackup(userId);
        } catch (IOException | InterruptedException e) {
            logger.error("Unexpected error occurred", e);
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/backup/{backupId}/download")
    public void downloadBackup(@PathVariable String backupId, HttpServletResponse response) {
        // TODO: Perform some sanity checks on the backupId, don't want injections and stuff here
        File backupFile = backupFileService.findBackupFile(backupId);

        backupFileService.createLockFileForBackupFile(backupFile);
        try {
            response.setHeader("Content-Type", "application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"goodreads_backup.zip\"");

            streamFileToUser(response, backupFile);
            backupFileService.deleteLockFileForBackupFile(backupFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file", e);
        }
    }

    private void streamFileToUser(HttpServletResponse response, File file) throws IOException {
        try (FileInputStream zipFileInputStream = new FileInputStream(file)) {
            IOUtils.copy(zipFileInputStream, response.getOutputStream());
            response.flushBuffer();
        }
    }
}
