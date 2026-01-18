package com.example.demo.realtimesupportservice.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatbotService {

    private static class Rule {
        private final String contains;
        private final String reply;

        Rule(String contains, String reply) {
            this.contains = contains;
            this.reply = reply;
        }
    }

    private final List<Rule> rules = new ArrayList<>();

    public ChatbotService() {
        // 10+ reguli simple (keyword match)
        rules.add(new Rule("login", "Dacă nu te poți loga: verifică user/parolă și încearcă refresh. Dacă tokenul expiră, fă login din nou."));
        rules.add(new Rule("register", "Pentru cont nou: completează register și apoi loghează-te. Dacă emailul există deja, încearcă 'forgot password'."));
        rules.add(new Rule("token", "Dacă ai probleme cu token-ul: deloghează-te și loghează-te iar. Token-urile expiră și trebuie regenerate."));
        rules.add(new Rule("device", "Pentru device: verifică dacă device-ul e asignat userului și dacă device-service e up."));
        rules.add(new Rule("add device", "Pentru a adăuga un device: mergi în pagina de devices și completează formularul, apoi assign la user."));
        rules.add(new Rule("assign", "Pentru assign: ai nevoie de userId și deviceId. Verifică în Device Service că asignarea s-a salvat."));
        rules.add(new Rule("consumption", "Consumul se calculează din măsurători. Dacă vezi valori ciudate, verifică timezone și intervalul selectat."));
        rules.add(new Rule("chart", "Dacă nu apar graficele: verifică că monitoring-service răspunde și că ai date pentru perioada aleasă."));
        rules.add(new Rule("overconsumption", "Overconsumption apare când depășești limita maximă/oră. Vei primi notificare în timp real."));
        rules.add(new Rule("admin", "Dacă ai nevoie de ajutor uman, scrie problema clar și o trimit către admin."));
        // poți adăuga și mai multe, nu strică
    }

    public String tryReply(String message) {
        if (message == null) return null;

        String m = message.toLowerCase();

        for (Rule r : rules) {
            if (m.contains(r.contains)) {
                return r.reply;
            }
        }
        return null; // nu a match-uit nimic => mergem la admin
    }
}
