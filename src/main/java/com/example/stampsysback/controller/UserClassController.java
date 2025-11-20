package com.example.stampsysback.controller;

import com.example.stampsysback.mapper.UserMapper;
import com.example.stampsysback.service.UserClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserClassController {
    private final UserClassService userClassService;
    private final UserMapper userMapper;

    // 指定クラスにユーザーを追加
    // POST/api/classes/{classId}/users/ ← @PathVariable
    // Body: { "userId": 1 } ←　@RequestBody
    @PostMapping("/classes/{classId}/users/")
    public ResponseEntity<?> addUserToClass(@PathVariable Integer classId,
                                            @RequestBody Map<String, Integer> body){
        // bodyからuserIdを取得
        Integer userId = body.get("userId");
        if(userId == null){
            // usreIdが存在しない場合、400エラーを返す
            return ResponseEntity.badRequest().body("userId is required");
        }

        try{
            // ユーザーをクラスに追加
            int rows = userClassService.addUserToClass(classId, userId);
            if(rows == 1){
                // 追加成功（一行更新）なら201 Created（status(HttpStatus.CREATED)）を返す
                return ResponseEntity.status(HttpStatus.CREATED).body("added");
            }else{
                // 既に追加されていた場合など、影響行数が0なら200 OKを返す
                return ResponseEntity.ok("already exists");
            }
            //
        } catch (IllegalArgumentException ex) {
            // サービス層でIllegalArgumentExceptionが発生した場合、404エラーを返す
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            // そのほかの例外は500エラーを返す
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed to add user to class");
        }
    }
}
