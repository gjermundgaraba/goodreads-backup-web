# Goodreads Backup Web

Web app with RESTful API to make it easy to backup your Goodreads data.

Demo: [http://demo.gjermundbjaanes.com:8080/goodreads-backup/](http://demo.gjermundbjaanes.com:8080/goodreads-backup/)

The app also includes a static HTML page to call the REST API, making it user friendly:

![Screenshot](https://github.com/bjaanes/goodreads-backup-web/raw/master/screenshot.png)

The project uses the python script goodreads-backup to perform the backup:
https://github.com/bjaanes/goodreads-backup

The application uses Spring Boot.

The application.properties file needs to be configured for the app to work:

```
scriptLocation=/location/of/goodreads-backup-script/application.py
pythonExecutable=python3
workFolder=/folder/location/where/the/server/can/work/with/zip/files
```
