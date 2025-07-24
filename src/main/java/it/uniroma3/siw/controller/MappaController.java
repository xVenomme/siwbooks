package it.uniroma3.siw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MappaController {

    @GetMapping("/mappa")
    public String mostraMappa(Model model) {
        return "mappa"; // Questo si riferisce a templates/mappa.html
    }
}
