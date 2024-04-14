# :lock: MajaTech Certificate Authority :lock:

MajaTech is a fictional technology company who have exposed 1 API endpoint that reveals all of their confidential data.  
This API endpoint is configured to use mTLS and will only respond to client certificates that are signed by their private CA.  
This project includes a portal to access this API endpoint as well as the self-implemented Certificate Authority.

# :floppy_disk: Usage :floppy_disk:

The portal to access the endpoint can be found at https://secret.majatech.net  
This portal requires a password that can be provided on request.  
Once logged in, the user will see a green `Fetch Secrets` button as well as a table of available client certificates that can be used.  
The user is free to create, upload or delete any certificates they wish* in attempting to expose the confidential data.  
Any certificate that is created using the CSR feature will be signed by the MajaTech CA.

# 🛠️ Implementation 🛠️

The core project is written in Java 21 and Spring Boot 3.X.  
The Certificate Authority logic is implemented with the help of the Bouncy Castle library.  
The portal utilises server-side rendering with Thymeleaf templates and is secured with Spring Security.  
The whole project is deployed using AWS services - Route 53, ACM, EC2, S3, RDS.  
The portal and confidential endpoint are both containerised using Docker and hosted separately.  
Comprehensive unit tests are implemented with high coverage.  

`*` Currently, only PKCS12 Certificates / KeyStores are supported
