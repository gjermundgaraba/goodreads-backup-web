package com.gjermundbjaanes.backup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class BackupFileService {

    private static final String LOCK_EXTENSION = ".lock";

    @Value("${workFolder}")
    private String workFolder;

    @Autowired
    private BackupCleaner backupCleaner;

    public File findBackupFile(String backupId) {
        File backupFile = getBackupFile(backupId);

        if (!backupFile.exists()) {
            throw new RuntimeException("Backup file does not exist");
        }

        if (backupCleaner.outsideTimeLimit(backupFile)) {
            throw new RuntimeException("It is too long since you created the backup. Please create it again");
        }

        return backupFile;
    }

    public void createLockFileForBackupFile(File backupFile) {
        File lockFile = getLockFileForBackupFile(backupFile);
        try {
            boolean success = lockFile.createNewFile();

            if (!success) {
                throw new RuntimeException("Wasn't able to create lock file");
            }
        } catch (IOException e) {
            throw new RuntimeException("Wasn't able to create lock file", e);
        }
    }

    public void deleteLockFileForBackupFile(File backupFile) {
        File lockFile = getLockFileForBackupFile(backupFile);

        lockFile.delete();
    }

    private File getLockFileForBackupFile(File backupFile) {
        return new File(backupFile.getAbsolutePath() + LOCK_EXTENSION);
    }

    public File getBackupFile(String backupId) {
        return new File(workFolder, backupId + ".zip");
    }

    public List<File> getAllBackupFilesNotLocked() {
        try {
            return Files.list(Paths.get(workFolder))
                    .filter(file -> {
                        File lockFile = new File(file.getFileName().toString() + LOCK_EXTENSION);
                        return !lockFile.exists();
                    })
                    .map(Path::toFile)
                    .collect(toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
