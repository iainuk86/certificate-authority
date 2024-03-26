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
@RequestMapping("/api/keystore")
public class KeyStoreController {

    private final KeyStoreService keyStoreService;

    @Autowired
    public KeyStoreController(KeyStoreService keyStoreService) {
        this.keyStoreService = keyStoreService;
    }

    /**
     * Creates a CSR from the CSR / 'Create' form values, which is then signed and all data relating to it is persisted
     * in the database. The actual KeyStore is stored locally for easier retrieval and use with the HttpClient.
     * @param csrForm The input data received from the client
     * @param resp The HttpServletResponse object used to redirect the user after signing is successful
     * @throws IOException If redirection is not possible. Will be handled by the CaErrorController
     */
    @PostMapping(value="/csr", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void generateKeyStoreFromCsr(CsrForm csrForm, HttpServletResponse resp) throws IOException {
        keyStoreService.generateKeyStoreFromCsr(csrForm);

        resp.sendRedirect("/");
    }

    /**
     * Upload a PKCS12 KeyStore which will subsequently be saved and displayed in the Client Certificate table
     * @param ks The KeyStore file
     * @param pass The KeyStore password
     * @param alias The alias of the key to use
     * @param resp The HttpServletResponse object used to redirect the user after signing is successful
     * @throws IOException If redirection is not possible. Will be handled by the CaErrorController
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadKeyStore(@RequestParam MultipartFile ks, @RequestParam String pass, @RequestParam String alias,
                               HttpServletResponse resp) throws IOException {
        keyStoreService.saveUploadedKeyStore(ks, pass, alias);

        resp.sendRedirect("/");
    }

    /**
     * Download the selected KeyStore to the users local file system
     * @param ksId The ID of the KeyStore to be downloaded
     * @param resp The HttpServletResponse object used to redirect the user after signing is successful
     * @return The requested KeyStore represented as a byte array
     */
    @GetMapping(value = "/download/{ksId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody byte[] downloadCertificate(@PathVariable("ksId") UUID ksId, HttpServletResponse resp) {
        resp.setHeader("Content-Disposition", "attachment; filename=client.p12");

        return keyStoreService.getKeyStore(ksId);
    }

    /**
     * Delete the selected KeyStore. Removes the entry from the database as well as the file from local storage
     * @param ksId The ID of the KeyStore to be deleted
     * @param resp The HttpServletResponse object used to redirect the user after signing is successful
     * @throws IOException If redirection is not possible. Will be handled by the CaErrorController
     */
    @PostMapping(value = "/delete/{ksId}")
    public void deleteKeyStore(@PathVariable("ksId") UUID ksId, HttpServletResponse resp) throws IOException {
        keyStoreService.deleteKeyStore(ksId);

        resp.sendRedirect("/");
    }
}
