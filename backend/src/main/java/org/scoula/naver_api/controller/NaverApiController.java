package org.scoula.naver_api.controller;

import org.scoula.naver_api.dto.BlogResponseDto;
import org.scoula.naver_api.dto.ImageResponseDto;
import org.scoula.naver_api.dto.LocalResponseDto;
import org.scoula.naver_api.service.BlogService;
import org.scoula.naver_api.service.ImageService;
import org.scoula.naver_api.service.LocalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
public class NaverApiController {

    @Autowired
    private BlogService blogService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private LocalService localService;

    @GetMapping("/blog")
    @ResponseBody
    public ResponseEntity<?> getBlog(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int display,
            @RequestParam(defaultValue = "1") int start) {

        try{
            BlogResponseDto blogResponse = blogService.searchNews(query, display, start);
            return ResponseEntity.ok(blogResponse);

        }catch (Exception e){
            return ResponseEntity.internalServerError().body("응답 오류");
        }
    }

    @GetMapping("/image")
    public ResponseEntity<?> getImage(
            @RequestParam String query,
            @RequestParam(defaultValue = "4") int display,
            @RequestParam(defaultValue = "1") int start,
            @RequestParam(defaultValue = "sim") String sort,
            @RequestParam(defaultValue = "small") String filter
    ) {

        try{
            ImageResponseDto imageResponse = imageService.searchImage(query,display,start,sort,filter);
            return ResponseEntity.ok(imageResponse);

        }catch (Exception e){
            return ResponseEntity.internalServerError().body("응답 오류");
        }
    }

    @GetMapping("/local")
    public ResponseEntity<?> getLocal(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int display,
            @RequestParam(defaultValue = "1") int start,
            @RequestParam(defaultValue = "random") String sort
    ) {

        try{
            LocalResponseDto localResponseDto = localService.searchLocal(query, display, start, sort);
            return ResponseEntity.ok(localResponseDto);

        }catch (Exception e){
            return ResponseEntity.internalServerError().body("응답 오류");
        }
    }
}