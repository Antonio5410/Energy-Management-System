package com.example.demo.realtimesupportservice.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class ChatbotService {

    private static class Rule {
        private final Pattern pattern;
        private final String response;

        Rule(String regex, String response) {
            this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            this.response = response;
        }

        boolean matches(String text) {
            return pattern.matcher(text).find();
        }

        String response() {
            return response;
        }
    }

    private final List<Rule> rules = new ArrayList<>();

    public ChatbotService() {

        // 1) salut
        rules.add(new Rule("\\b(buna|bună|salut|hello|hi|hey)\\b",
                "Salut! 👋 Spune-mi, te pot ajuta cu login, device-uri sau monitoring?"));

        // 2) login problems
        rules.add(new Rule("\\b(login|logare|autentificare|credentiale|parola|password|token|jwt)\\b",
                "Pentru login: verifică user/pass, apoi încearcă logout/login. Dacă e JWT: vezi să fie token-ul pus în Authorization: Bearer <token>."));

        // 3) invalid credentials
        rules.add(new Rule("\\b(invalid credentials|gresit|greșit|parola gresita|parola greșită)\\b",
                "Dacă apare 'invalid credentials': confirmă că user-ul există și parola e cea corectă. Dacă ai făcut update la user, parola s-a schimbat."));

        // 4) token expired / unauthorized
        rules.add(new Rule("\\b(401|unauthorized|forbidden|403|token expired|expired)\\b",
                "Dacă primești 401/403: token-ul poate fi expirat sau lipsește. Re-loghează-te și asigură-te că trimiți Bearer token la fiecare request."));

        // 5) refresh token
        rules.add(new Rule("\\b(refresh token|refresh)\\b",
                "Pentru refresh: folosește endpoint-ul de refresh (dacă îl ai) sau re-loghează-te. La demo, re-login e cel mai safe."));

        // 6) devices list
        rules.add(new Rule("\\b(device|devices|aparat|dispozitiv)\\b",
                "La devices: verifică /devices (admin) sau /devices (client -> doar ale lui). Dacă nu apare nimic, ownerId poate fi greșit."));

        // 7) device create / ownerId
        rules.add(new Rule("\\b(ownerid|owner id|proprietar|user id)\\b",
                "La creare device, ownerId trebuie să fie UUID-ul unui CLIENT din /people. Dacă e alt UUID, nu vei vedea device-ul la client."));

        // 8) monitoring / chart
        rules.add(new Rule("\\b(monitoring|chart|grafic|consumption|consum|kwh)\\b",
                "Monitoring: alege device-ul și apasă Load monitoring. Dacă graficul e gol, verifică range-ul de timp și că monitoring-service are date."));

        // 9) websocket
        rules.add(new Rule("\\b(ws|websocket|stomp|sockjs)\\b",
                "WS: conexiunea e pe endpoint-ul /ws. Clientul se subscribe la /topic/chat.user.<uuid> și admin la /topic/chat.admin."));

        // 10) rabbitmq
        rules.add(new Rule("\\b(rabbit|rabbitmq|queue|exchange|amqp)\\b",
                "RabbitMQ: verifică dacă brokerul e up și credentialele sunt corecte. În docker: host-ul e 'rabbitmq-broker', local poate fi 'localhost'."));

        // 11) overconsumption
        rules.add(new Rule("\\b(overconsumption|depase|depășe|alerta|alertă|limit)\\b",
                "Overconsumption: când consumul depășește maximul, se trimite alertă către user. Asigură-te că listener-ul rulează și queue-ul există."));

        // 12) docker / traefik
        rules.add(new Rule("\\b(docker|traefik|reverse proxy|proxy)\\b",
                "Docker/Traefik: ideal toate serviciile în aceeași rețea. Dacă rulezi WS local separat, conectează-te direct la http://localhost:8085/ws."));

        // 13) update person validation
        rules.add(new Rule("\\b(update|put|validation|role is required)\\b",
                "La update person: dacă primești 'role is required', trimite și role în body (ADMIN/CLIENT). Unele DTO-uri au @NotNull pe role."));

        }

    public String reply(String userMessage) {
        String msg = normalize(userMessage);

        for (Rule r : rules) {
            if (r.matches(msg)) return r.response();
        }

        // dacă nu a găsit nimic relevant => botul tace
        return null;
    }


    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase(Locale.ROOT);
    }
}
