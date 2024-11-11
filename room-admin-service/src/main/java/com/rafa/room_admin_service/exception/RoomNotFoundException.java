package com.rafa.room_admin_service.exception;

public class RoomNotFoundException extends RoomException{
    public RoomNotFoundException(Long roomId) {
        super("查無此房間"+roomId);
    }

    public RoomNotFoundException(Long hotelId,Long roomId) {
        super("此旅店查無房間"+hotelId);
    }

}
