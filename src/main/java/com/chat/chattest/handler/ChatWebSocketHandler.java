package com.chat.chattest.handler;

import com.chat.chattest.model.ChatRoom;
import com.chat.chattest.model.ChatRoomManager;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private ChatRoomManager chatRoomManager = new ChatRoomManager();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 방 정보는 세션 URI에서 가져옵니다. 예: ws://localhost:8080/chat/roomId
        String roomId = getRoomId(session);
        ChatRoom room = chatRoomManager.findRoomById(roomId);
        if (room == null) {
            room = chatRoomManager.createRoom(roomId);
        }
        room.addSession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomId = getRoomId(session);
        ChatRoom room = chatRoomManager.findRoomById(roomId);

        // 메시지를 방 안의 모든 사용자에게 전송
        for (WebSocketSession webSocketSession : room.getSessions()) {
            if (webSocketSession.isOpen()) {
                webSocketSession.sendMessage(message);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        String roomId = getRoomId(session);
        ChatRoom room = chatRoomManager.findRoomById(roomId);
        if (room != null) {
            room.removeSession(session);
        }
    }

    private String getRoomId(WebSocketSession session) {
        String uri = session.getUri().toString();
        return uri.substring(uri.lastIndexOf("/") + 1);  // URI에서 roomId 추출
    }
}