package com.gjermundbjaanes.backup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class BackupCleaner {

    public final static int MILISECONDS_TIME_LIMIT_FOR_DOWNLOADS = 5 * 60 * 1000;

    @Autowired
    private BackupFileService backupFileService;

    @Scheduled(fixedDelay = MILISECONDS_TIME_LIMIT_FOR_DOWNLOADS)
    public void deleteUnclaimedBackups() {
        List<File> allBackupFilesNotLocked = backupFileService.getAllBackupFilesNotLocked();

        allBackupFilesNotLocked.stream()
                .filter(this::outsideTimeLimit)
                .forEach(File::delete);
    }

    public boolean outsideTimeLimit(File file) {
        return (System.currentTimeMillis() - file.lastModified()) > BackupCleaner.MILISECONDS_TIME_LIMIT_FOR_DOWNLOADS;
    }

}
