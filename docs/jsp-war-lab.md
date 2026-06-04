# JSP/WAR Lab Runtime

This branch changes the backend package from an executable JAR to an executable WAR
so the project can verify JSP rendering in a controlled lab environment.

The branch does not add webshell payloads. It mounts the expert profile upload
directory as a Tomcat webapp resource for lab validation, while blocking direct
URL access to `.jsp` family files under that upload path.

## Runtime check

- URL: `/lab/jsp/status`
- View: `/WEB-INF/jsp/lab/status.jsp`
- Purpose: verify that the WAR package can resolve and render JSP resources.

## Expert profile upload resource mount

- Source directory: `${EXPERT_PROFILE_IMAGE_UPLOAD_DIR}`
- Webapp mount: `/uploads/expert-profile`
- Direct JSP access block: `/uploads/expert-profile/**/*.jsp`, `.jspx`, `.jspf`

The direct-access block is implemented in both Spring Security request matchers
and an early servlet filter. This keeps uploaded profile assets web-accessible
while denying JSP-family URLs in the upload namespace.

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
