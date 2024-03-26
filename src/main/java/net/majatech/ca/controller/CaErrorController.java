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

/**
 * Controller to handle any errors that occur while running the application
 * <br><br>
 * Spring Boot automatically redirects to the /error endpoint if an error occurs, and as this project is server-side
 * rendered the model needs to be re-populated before displaying the index page again
 */
@Controller
public class CaErrorController implements ErrorController {

    private final KeyStoreService keyStoreService;

    @Autowired
    public CaErrorController(KeyStoreService keyStoreService) {
        this.keyStoreService = keyStoreService;
    }

    /**
     * Endpoint that Spring Boot automatically redirects to when an error occurs
     * <br><br>
     * The cause of the error is captured inside the HttpServletRequest and the model is repopulated with necessary data
     * as well as the error cause. The user is then redirected to the index page where this information is displayed
     * @param request The HttpServletRequest that contains the Exception explaining the cause of the error
     * @param model The model to be repopulated with necessary information as well as the error cause
     * @return The index page where all of this information is conveyed to the user
     */
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