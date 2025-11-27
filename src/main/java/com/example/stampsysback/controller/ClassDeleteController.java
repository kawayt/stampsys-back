package com.example.stampsysback.controller;

import com.example.stampsysback.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * クラスの削除（DELETE）と復元（PUT / restore）リクエストのみを処理するコントローラー。
 * 既存の ClassController との関心事を分離。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/classes")
@CrossOrigin(origins = "http://localhost:5173")
public class ClassDeleteController {

    private final ClassService classService;

    /**
     * 論理削除: DELETE /api/classes/{classId}
     * 指定されたIDのクラスを論理削除します。
     */
    @DeleteMapping("/{classId}")
    public ResponseEntity<Void> delete(@PathVariable Integer classId){
        // サービス層の論理削除メソッドを呼び出す
        classService.deleteClass(classId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 復元: PUT /api/classes/{classId}/restore
     * 指定されたIDのクラスを復元（論理削除を取り消し）します。
     */
    @PutMapping("/{classId}/restore")
    public ResponseEntity<Void> restore(@PathVariable Integer classId){
        // サービス層の復元メソッドを呼び出す
        classService.restoreClass(classId);
        return ResponseEntity.ok().build();
    }
}