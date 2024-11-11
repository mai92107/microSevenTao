package com.rafa.room_service.exception;

import java.time.LocalDate;
import java.util.List;

public class LivingDateErrorException extends RoomException{
    public LivingDateErrorException(LocalDate start,LocalDate end) {
        super("此住宿日期無法查詢："+start+"到"+end);
    }
    public LivingDateErrorException(List<LocalDate> livingDays) {
        super("此住宿日期無法查詢："+livingDays);
    }
}
