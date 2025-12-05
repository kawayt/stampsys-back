package com.example.stampsysback.controller;

import com.example.stampsysback.entity.GroupEntity;
import com.example.stampsysback.repository.GroupRepository;
import com.example.stampsysback.repository.UserRepository; // 追加
import org.springframework.transaction.annotation.Transactional; // 追加
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class GroupController {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository; // 追加

    // コンストラクタで UserRepository も注入
    public GroupController(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/api/groups")
    public List<GroupDto> listGroups() {
        return groupRepository.findAll().stream()
                .map(g -> new GroupDto(g.getGroupId(), g.getGroupName()))
                .collect(Collectors.toList());
    }

    @PostMapping("/api/groups")
    public GroupDto createGroup(@RequestBody Map<String, String> body) {
        String name = body.get("groupName");
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("グループ名は必須です");
        }
        GroupEntity g = new GroupEntity();
        g.setGroupName(name);
        GroupEntity saved = groupRepository.save(g);
        return new GroupDto(saved.getGroupId(), saved.getGroupName());
    }

    @DeleteMapping("/api/groups/{groupId}")
    @Transactional
    public void deleteGroup(@PathVariable Integer groupId) {
        // 1. このグループのユーザーを「未所属(NULL)」にする
        // ※ UserRepository に setGroupIdToNull メソッドを追加しておく必要があります
        userRepository.setGroupIdToNull(groupId);

        // 2. グループを削除
        groupRepository.deleteById(groupId);
    }

    // DTOクラス (そのまま)
    public static class GroupDto {
        private Integer groupId;
        private String groupName;
        public GroupDto() {}
        public GroupDto(Integer groupId, String groupName) {
            this.groupId = groupId;
            this.groupName = groupName;
        }
        public Integer getGroupId() { return groupId; }
        public void setGroupId(Integer groupId) { this.groupId = groupId; }
        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }
    }
}