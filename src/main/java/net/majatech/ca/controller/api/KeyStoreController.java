package net.majatech.ca.controller.api;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.majatech.ca.controller.api.model.CsrForm;
import net.majatech.ca.services.KeyStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import static org.springframework.http.MediaType.*;

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
    @PostMapping(value="/csr", consumes = APPLICATION_FORM_URLENCODED_VALUE, produces = TEXT_PLAIN_VALUE)
    public String generateKeyStoreFromCsr(@Valid CsrForm csrForm, HttpServletResponse resp) throws IOException {
        resp.sendRedirect("/");

        return keyStoreService.generateKeyStoreFromCsr(csrForm);
    }

    /**
     * Upload a PKCS12 KeyStore which will subsequently be saved and displayed in the Client Certificate table
     * @param ks The KeyStore file
     * @param pass The KeyStore password
     * @param alias The alias of the key to use
     * @param resp The HttpServletResponse object used to redirect the user after signing is successful
     * @throws IOException If redirection is not possible. Will be handled by the CaErrorController
     */
    @PostMapping(value = "/upload", consumes = MULTIPART_FORM_DATA_VALUE, produces = TEXT_PLAIN_VALUE)
    public String uploadKeyStore(@RequestParam MultipartFile ks, @RequestParam String pass, @RequestParam String alias,
                               HttpServletResponse resp) throws IOException {
        resp.sendRedirect("/");

        return keyStoreService.saveUploadedKeyStore(ks, pass, alias);
    }

    /**
     * Download the selected KeyStore to the users local file system
     * @param ksId The ID of the KeyStore to be downloaded
     * @param resp The HttpServletResponse object used to redirect the user after signing is successful
     * @return The requested KeyStore represented as a byte array
     */
    @GetMapping(value = "/download/{ksId}", produces = APPLICATION_OCTET_STREAM_VALUE)
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
        resp.sendRedirect("/");

        keyStoreService.deleteKeyStore(ksId);
    }
}
