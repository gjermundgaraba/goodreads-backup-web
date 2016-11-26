package com.gjermundbjaanes;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
public class BackupController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GoodreadsBackup goodreadsBackup;

    @RequestMapping(value = "/backup", method = RequestMethod.GET)
    public void performBackup(@RequestParam(value = "userId") int userId, HttpServletResponse response) {
        try {
            File zipFile = goodreadsBackup.performBackup(userId);

            streamZipFileToUser(response, zipFile);
        } catch (IOException | InterruptedException e) {
            logger.error("Unexpected error occurred", e);
            throw new RuntimeException(e);
        }
    }

    private void streamZipFileToUser(HttpServletResponse response, File zipFile) throws IOException {
        FileInputStream zipFileInputStream = new FileInputStream(zipFile);
        IOUtils.copy(zipFileInputStream, response.getOutputStream());
        response.flushBuffer();
    }
}
