package com.example.stampsysback.controller;

import com.example.stampsysback.dto.ClassDto;
import com.example.stampsysback.dto.ClassRoomsResponse;
import com.example.stampsysback.dto.RoomDto;
import com.example.stampsysback.entity.RoomEntity;
import com.example.stampsysback.form.RoomForm;
import com.example.stampsysback.helper.RoomHelper;
import com.example.stampsysback.service.ClassService;
import com.example.stampsysback.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://localhost:5173")
public class RoomController {
    // DI サービスに依存
    private final RoomService roomService;
    private final ClassService classService;

    // クラスに紐づくルーム一覧 + className を返す
    @GetMapping("/{classId}")
    public ResponseEntity<ClassRoomsResponse> listByClass(@PathVariable Integer classId) {
        // classId からクラス情報を取得
        var classEntity = classService.selectClassById(classId);
        if (classEntity == null) {
            // 指定された classId のクラスが存在しない
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Entity -> ClassDto 変換
        ClassDto classDto = new ClassDto(
                classEntity.getClassId(),
                classEntity.getClassName(),
                classEntity.getCreatedAt()
        );

        // クラスに紐づくルーム一覧を取得 (Entity)
        List<RoomEntity> roomEntities = roomService.selectByClassId(classId);

        // Entity -> RoomDto 変換（RoomHelper を利用）
        List<RoomDto> roomDtos = roomEntities.stream()
                .map(RoomHelper::toDto)
                .collect(Collectors.toList());

        // DTO をまとめて返却
        ClassRoomsResponse body = new ClassRoomsResponse(classDto, roomDtos);
        return ResponseEntity.ok(body);
    }

    // ルーム新規作成
    @PostMapping
    public ResponseEntity<?> insetRoom(@Valid @RequestBody RoomForm roomForm,
                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // bindingResult.getAllErrors()はList<ObjectError>を返す
            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(msg);
        }
        try {
            // room作成
            RoomDto created = roomService.insertRoom(roomForm);
            // HTTP201 Createdと作成したroom情報を返す
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (org.springframework.dao.DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("room_id の重複により作成に失敗しました");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Room の作成に失敗しました");
        }
    }

    //ルーム終了機能
    @PatchMapping("/{roomId}/close")
    public ResponseEntity<?> closeRoom(@PathVariable Integer roomId) {
        try {
            roomService.closeRoom(roomId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            // 指定された room が存在しないなど
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (org.springframework.dao.DuplicateKeyException ex) {
            // ありえないはずだが安全対策
            return ResponseEntity.status(HttpStatus.CONFLICT).body("データの整合性エラーが発生しました");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ルームの終了に失敗しました");
        }
    }

    //ルーム削除機能
    @PatchMapping("/{roomId}/delete")
    public ResponseEntity<?> deleteRoom(@PathVariable Integer roomId) {
        try {
            roomService.deleteRoom(roomId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            // 指定された room が存在しないなど
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (org.springframework.dao.DuplicateKeyException ex) {
            // ありえないはずだが安全対策
            return ResponseEntity.status(HttpStatus.CONFLICT).body("データの整合性エラーが発生しました");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ルームの削除に失敗しました");
        }
    }
}