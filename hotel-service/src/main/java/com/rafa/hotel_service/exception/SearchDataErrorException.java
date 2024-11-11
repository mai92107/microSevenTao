package com.rafa.hotel_service.exception;

import lombok.Data;

import java.time.LocalDate;

public class SearchDataErrorException extends HotelException {
    public SearchDataErrorException(Integer cityCode,
                                    String keyword,
                                    LocalDate start,
                                    LocalDate end,
                                    Integer people) {
        super("搜尋資料錯誤：城市代碼" + cityCode + "，關鍵字" + keyword + "，入住日期" + start + "，退房日期" + end + "，入住人數" + people);
    }
}
