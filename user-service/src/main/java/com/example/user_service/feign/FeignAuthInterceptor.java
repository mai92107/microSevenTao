//package com.example.user_service.feign;
//
//import feign.RequestInterceptor;
//import feign.RequestTemplate;
//import org.springframework.stereotype.Component;
//
//@Component
//public class FeignAuthInterceptor implements RequestInterceptor {
//
//    @Override
//    public void apply(RequestTemplate template) {
//        // 排除在 `signup` 請求中添加 Authorization 標頭
//        if (template.url().contains("／auth/signup")) {
//            return;  // 不附加 Authorization 標頭
//        }
//
//        // 其他請求添加 Authorization 標頭
//        String token = "Bearer " + getJwtToken(); // 取得 JWT 的方法
//        template.header("Authorization", token);
//    }
//
//    private String getJwtToken() {
//        // 模擬取得 JWT 的方法，例如從上下文或服務中取得
//        return "YOUR_JWT_TOKEN";
//    }
//}
