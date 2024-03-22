package net.majatech.ca.controller;

import net.majatech.ca.controller.api.model.CsrForm;
import net.majatech.ca.services.KeyStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    private final KeyStoreService keyStoreService;

    @Autowired
    public ViewController(KeyStoreService keyStoreService) {
        this.keyStoreService = keyStoreService;
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @GetMapping("/")
    public String showIndex(Model model) {
        CsrForm csrForm = new CsrForm();
        model.addAttribute("csrForm", csrForm);
        model.addAttribute("keyStores", keyStoreService.getKeyStores());

        return "index";
    }
}
