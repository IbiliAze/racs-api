package uk.co.eightmile.racs.sockets;

import uk.co.eightmile.racs.cards.CardUpdate;
import uk.co.eightmile.racs.scans.ScanUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MeshSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // readerId -> session
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    // readerId -> campaignId
    private final Map<String, String> campaigns = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Socket connected: {}", session.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
        try {
            Map<String, Object> data = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) data.get("type");

            switch (type) {
                case "join" -> handleJoin(session, data);
                case "offer", "answer", "ice_candidate" -> forward(data);
                case null -> sendError(session, "Missing message type");
                default -> log.warn("Unknown message type: {}", type);
            }
        } catch (Exception e) {
            // Never let a bad frame propagate: the session would be closed and the
            // peer would reconnect and resend it, looping forever.
            log.warn("Failed to handle message on session {}: {}", session.getId(), e.getMessage());
            sendError(session, "Malformed message");
        }
    }

    private void handleJoin(WebSocketSession session, Map<String, Object> data) throws IOException {
        String readerId = (String) data.get("readerId");
        String campaignId = (String) data.get("campaignId");

        if (readerId == null) {
            sendError(session, "join requires a readerId");
            return;
        }

        sessions.put(readerId, session);
        if (campaignId == null) {
            campaigns.remove(readerId);
        } else {
            campaigns.put(readerId, campaignId);
        }
        log.info("Peer joined: {} at campaign {}", readerId, campaignId);

        // A peer without an campaign has no mesh to join yet; it stays reachable for
        // direct signalling until it rejoins with an campaignId.
        List<String> peers = campaignId == null ? List.of() : campaigns.entrySet().stream()
                .filter(e -> campaignId.equals(e.getValue()) && !e.getKey().equals(readerId))
                .map(Map.Entry::getKey)
                .toList();

        send(session, Map.of("type", "peer_list", "peers", peers));

        for (String peerId : peers) {
            WebSocketSession peerSession = sessions.get(peerId);
            if (peerSession != null && peerSession.isOpen()) {
                send(peerSession, Map.of("type", "peer_joined", "peerId", readerId));
            }
        }
    }

    private void forward(Map<String, Object> data) throws IOException {
        String to = (String) data.get("to");
        WebSocketSession target = sessions.get(to);
        if (target != null && target.isOpen()) {
            send(target, data);
        } else {
            log.warn("Target peer not found or disconnected: {}", to);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCardUpdate(CardUpdate cardUpdate) {
        log.info("New card update");

        var card = cardUpdate.card();
        var action = cardUpdate.action();
        String campaignId = card.getCampaignId();

        if (campaignId == null) return;

        Map<String, Object> message = Map.of(
                "type","card_update",
                "action", action,
                "card", card
        );

        campaigns.entrySet().stream()
                .filter(e -> campaignId.equals(e.getValue()))
                .map(e -> sessions.get(e.getKey()))
                .filter(s -> s != null && s.isOpen())
                .forEach(s -> {
                    try {
                        send(s, message);
                    } catch (IOException ex) {
                        log.warn("Failed to send card_update to session {}: {}", s.getId(), ex.getMessage());
                    }
                });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onScanUpdate(ScanUpdate scanUpdate) {
        log.info("New scan update");

        var scan = scanUpdate.scan();
        var action = scanUpdate.action();
        String readerId = scan.getReaderId() != null ? scan.getReaderId().toString() : null;
        String campaignId = readerId != null ? campaigns.get(readerId) : null;

        if (campaignId == null) return;

        Map<String, Object> message = Map.of(
                "type", "scan_update",
                "action", action,
                "scan", scan
        );

        campaigns.entrySet().stream()
                .filter(e -> campaignId.equals(e.getValue()))
                .map(e -> sessions.get(e.getKey()))
                .filter(s -> s != null && s.isOpen())
                .forEach(s -> {
                    try {
                        send(s, message);
                    } catch (IOException ex) {
                        log.warn("Failed to send scan_update to session {}: {}", s.getId(), ex.getMessage());
                    }
                });
    }

    private void send(WebSocketSession session, Object payload) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
    }

    private void sendError(WebSocketSession session, String reason) {
        if (!session.isOpen()) return;
        try {
            send(session, Map.of("type", "error", "reason", reason));
        } catch (IOException ex) {
            log.warn("Failed to send error to session {}: {}", session.getId(), ex.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        sessions.entrySet().removeIf(e -> e.getValue().equals(session));
        campaigns.entrySet().removeIf(e -> {
            if (!sessions.containsKey(e.getKey())) {
                log.info("Peer disconnected: {}", e.getKey());
                return true;
            }
            return false;
        });
    }
}
