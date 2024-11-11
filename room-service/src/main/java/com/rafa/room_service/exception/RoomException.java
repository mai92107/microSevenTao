package com.rafa.room_service.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RoomException extends Exception{
    String msg;
}
