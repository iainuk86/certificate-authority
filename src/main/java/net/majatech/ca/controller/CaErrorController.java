package net.majatech.ca.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import net.majatech.ca.controller.api.model.CsrForm;
import net.majatech.ca.services.KeyStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CaErrorController implements ErrorController {

    private final KeyStoreService keyStoreService;

    @Autowired
    public CaErrorController(KeyStoreService keyStoreService) {
        this.keyStoreService = keyStoreService;
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Exception ex = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        CsrForm csrForm = new CsrForm();
        model.addAttribute("csrForm", csrForm);
        model.addAttribute("errorMessage", ex == null ? "Unexpected error" : ex.getCause().getMessage());
        model.addAttribute("keyStores", keyStoreService.getKeyStores());

        return "index";
    }
}