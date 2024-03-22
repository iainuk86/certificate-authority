package net.majatech.ca.controller.api;

import jakarta.servlet.http.HttpServletResponse;
import net.majatech.ca.controller.api.model.CsrForm;
import net.majatech.ca.services.KeyStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/certs")
public class CertificateController {

    private final KeyStoreService keyStoreService;

    @Autowired
    public CertificateController(KeyStoreService keyStoreService) {
        this.keyStoreService = keyStoreService;
    }

    @PostMapping(value="/csr", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void generateKeyStoreFromCsr(CsrForm csrForm, HttpServletResponse resp) throws IOException {
        keyStoreService.generateKeyStoreFromCsr(csrForm);

        resp.sendRedirect("/");
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadKeyStore(@RequestParam MultipartFile ks, @RequestParam String pass, @RequestParam String alias,
                               HttpServletResponse resp) throws IOException {
        keyStoreService.saveUploadedKeyStore(ks, pass, alias);

        resp.sendRedirect("/");
    }

    @GetMapping(value = "/download/{ksId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody byte[] downloadCertificate(@PathVariable("ksId") UUID ksId, HttpServletResponse resp) {
        resp.setHeader("Content-Disposition", "attachment; filename=client.p12");

        return keyStoreService.getKeyStore(ksId);
    }

    @PostMapping(value = "/delete/{ksId}")
    public void deleteKeyStore(@PathVariable("ksId") UUID ksId, HttpServletResponse resp) throws IOException {
        keyStoreService.deleteKeyStore(ksId);

        resp.sendRedirect("/");
    }
}
