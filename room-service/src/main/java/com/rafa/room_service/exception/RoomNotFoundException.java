package com.rafa.room_service.exception;

public class RoomNotFoundException extends RoomException{
    public RoomNotFoundException(Long roomId) {
        super("查無此房間"+roomId);
    }
}
