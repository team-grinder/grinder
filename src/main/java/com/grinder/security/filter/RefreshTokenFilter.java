package com.grinder.security.filter;

import com.google.gson.Gson;
import com.grinder.security.exception.RefreshTokenException;
import com.grinder.utils.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class RefreshTokenFilter extends OncePerRequestFilter {

    private final String refreshPath;
    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        String path = request.getRequestURI();

        // 토큰 갱신 경로 아닐 경우
        if(!path.equals(refreshPath)){
            log.info("skip refresh token filter---------");
            filterChain.doFilter(request,response);
            return;
        }
        log.info("Refresh Token Filter.........run...........1");
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies){
            if (cookie.getName().equals("refresh")) {

                refreshToken = cookie.getValue();
            }
        }

        log.info("refreshToken: "+refreshToken);

        Map<String,Object> refreshClaims = null;

        try{
            // accessToken은 무조건 새로 발행
            // refreshToken은 만료일이 얼마 남지 않은 경우에 새로 발행
            refreshClaims = checkRefreshToken(refreshToken);
            log.info(refreshClaims.toString());

            Integer exp = (Integer) refreshClaims.get("exp");

            Date expTime = new Date(Instant.ofEpochMilli(exp).toEpochMilli()*1000);

            Date current = new Date(System.currentTimeMillis());

            long gapTime = (expTime.getTime()-current.getTime());

            log.info("-------------------------------------");
            log.info("current: "+current);
            log.info("expTime: "+expTime);
            log.info("gap: "+gapTime);

            String email = jwtUtil.getEmail(refreshToken);
            Map<String, Object> mid = Map.of("email", email);
            log.info("mid: "+mid);
            //이 상태 도달 시 무조건 accessToken은 새로 생성
            String accessToken = jwtUtil.generateToken(Map.of("mid",mid),1);
            //refreshToken이 3일도 안남았을때
            if(gapTime < (1000*60*60*24*3)){
                log.info("new Refresh Token required.....");
                refreshToken = jwtUtil.generateToken(Map.of("mid",mid),24*7);
            }
            log.info("Refresh Token result.......");
            log.info("nowAccessToken : "+accessToken);
            log.info("refreshToken : "+refreshToken);

            sendToken(accessToken,refreshToken,response);

        }catch (RefreshTokenException refreshTokenException){
            refreshTokenException.sendResponseError(response);
            return;
        }
    }


    // refreshToken 검사 -> 토큰이 없거나 잘못된 토큰인 경우 에러메세지 전송
    private Map<String,Object> checkRefreshToken(String refreshToken)throws RefreshTokenException{
        try{
            Map<String,Object> values = jwtUtil.validateToken(refreshToken);
            return values;
        }catch (ExpiredJwtException expiredJwtException){
            throw new RefreshTokenException(RefreshTokenException.ErrorCase.OLD_REFRESH);
        }catch (MalformedJwtException malformedJwtException){
            log.error("MalformedJwtException------------------------");
            throw new RefreshTokenException(RefreshTokenException.ErrorCase.NO_REFRESH);
        }catch (Exception exception){
            new RefreshTokenException(RefreshTokenException.ErrorCase.NO_REFRESH);
        }
        return null;
    }

    //만들어진 토큰들 전송
    private void sendToken(String accessTokenValue, String refreshTokenValue,HttpServletResponse response){
        //accessToken은 로컬 스토리지 , refreshToken은 httpOnly 쿠키에 저장
        response.addHeader("Authorization","Bearer"+accessTokenValue);
        response.addCookie(createCookie("refresh",refreshTokenValue));
        response.setStatus(HttpStatus.OK.value());
    }
    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
