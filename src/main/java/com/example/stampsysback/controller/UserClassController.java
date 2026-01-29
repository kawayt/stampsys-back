package com.example.stampsysback.controller;

import com.example.stampsysback.dto.ClassDto;
import com.example.stampsysback.dto.RoomDto;
import com.example.stampsysback.dto.UserDto;
import com.example.stampsysback.mapper.UserMapper;
import com.example.stampsysback.service.UserClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
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
    @PostMapping("/classes/{classId}/users")
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
            int rows = userClassService.addUserToClass(userId, classId);
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

    // 指定クラスからユーザーを削除する
    // DELETE/api/classes/{classId}/users/{userId}
    @DeleteMapping("/classes/{classId}/users/{userId}")
    public ResponseEntity<?> removeUserFromClass(@PathVariable Integer userId,
                                             @PathVariable Integer classId){
        try{
            // ユーザーをクラスから削除
            int rows = userClassService.removeUserFromClass(userId, classId);
            if(rows == 1){
                // 削除成功（一行削除）なら204（成功したが、返すコンテンツはないという意味） No Contentを返す
                return ResponseEntity.noContent().build();
            }else{
                // 削除対象が存在しない場合（rows == 0）、404（削除されているなど）Not Foundを返す
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("not found");
            }
        }catch (Exception ex) {
            // そのほかの例外は500エラーを返す
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed to remove user from class");
        }
    }

    // 指定ユーザーが指定クラスに所属しているか（重複しないため）
    // GET/api/classes/{classId}/users/{userId}/exists
    // レスポンス：{"inClass": true} もしくは {"inClass": false}
    @GetMapping(path = "/classes/{classId}/users/{userId}/exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> isUserInClass(@PathVariable Integer userId,
                                         @PathVariable Integer classId){
        try {
            // ユーザーがクラスに紐づいているかbooleanで取得
            boolean inClass = userClassService.isUserInClass(userId, classId);
            // Collections.singletonMap("inClass", inClass)で
            // {"inClass": true}または{"inClass": false}というJSONと200を返す
            return ResponseEntity.ok(Collections.singletonMap("inClass", inClass));
        } catch (IllegalArgumentException ex) {
            // クラスIdやユーザーIdが存在しない場合、サービス層でIllegalArgumentExceptionを投げて404とエラーメッセージを返す場合
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            // そのほかの例外は500エラーを返す
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed to check user in class");
        }
    }

    // 指定ユーザーが見えるクラス一覧を返す
    // GET/api/users/{userId}/classes
    @GetMapping("/users/{userId}/classes")
    public ResponseEntity<?> getClassForUser(@PathVariable Integer userId){
        try {
            List<ClassDto> classes = userClassService.getClassForUser(userId);
            return ResponseEntity.ok(classes);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed to get classes for user");
        }
    }

    // 指定ユーザーが見えるルーム一覧を返す
    // GET/api/users/{userId}/rooms
    @GetMapping("/users/{userId}/rooms")
    public ResponseEntity<?> getRoomForUser(@PathVariable Integer userId){
        try{
            List<RoomDto> rooms = userClassService.getRoomForUser(userId);
            return ResponseEntity.ok(rooms);
            // 例外発生
        } catch (Exception ex) {
            // 500 INTERNAL_SERVER_ERRORは「サーバー内部で予期せぬエラーが起きた」という意味
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed to get rooms for user");
        }
    }

    // 追加済みユーザー一覧
    // GET/api/classes/{classId}/users/in?limit=0&offset=0
    @GetMapping("/classes/{classId}/users/in")
    public ResponseEntity<List<UserDto>> getUserInClass(@PathVariable Integer classId,
                                            // そのパラメーターがなくてもエラーにしないでnullを許容
                                            @RequestParam(required = false) Integer limit,
                                            @RequestParam(required = false) Integer offset){
        List<UserDto> list = userMapper.selectUsersInClass(classId, limit, offset);
        // 200 OKとユーザーリストを返す
        return ResponseEntity.ok(list);
    }

    // 未追加ユーザー一覧
    // GET/api/classes/{classId}/users/not-in?limit=0&offset=0
    @GetMapping("/classes/{classId}/users/not-in")
    public ResponseEntity<List<UserDto>> getUserNotInClass(@PathVariable Integer classId,
                                                           @RequestParam(required = false) Integer limit,
                                                           @RequestParam(required = false) Integer offset){
        List<UserDto> list = userMapper.selectUsersNotInClass(classId, limit,offset);
        return ResponseEntity.ok(list);
    }

}
