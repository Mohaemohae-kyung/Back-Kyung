# JSP/WAR Lab Runtime

This branch changes the backend package from an executable JAR to an executable WAR
so the project can verify JSP rendering in a controlled lab environment.

The branch does not add webshell payloads and does not add an arbitrary upload path
that writes user files into a JSP execution directory.

## Runtime check

- URL: `/lab/jsp/status`
- View: `/WEB-INF/jsp/lab/status.jsp`
- Purpose: verify that the WAR package can resolve and render JSP resources.

## Deployment notes

GitHub Actions now builds and deploys `build/libs/app.war` as `app.war`.
The EC2 `springboot` systemd unit must run the WAR artifact, for example:

```text
java -jar /opt/user-app/app.war
```

Do not leave the old unit pointing at `/opt/user-app/app.jar` after this branch is deployed.

## Nginx and PHP-FPM cleanup

This branch does not require PHP-FPM. If the previous PHP-FPM lab configuration is no
longer needed, remove the upload-path FastCGI handler from Nginx and revert PHP-FPM
extension allowances that were added only for the PHP lab.

Nginx can remain a reverse proxy to Spring Boot.
