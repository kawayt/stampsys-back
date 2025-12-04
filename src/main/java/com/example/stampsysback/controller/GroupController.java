package com.example.stampsysback.controller;

import com.example.stampsysback.controller.GroupController.GroupDto; // もし内部クラスなら
import com.example.stampsysback.entity.GroupEntity;
import com.example.stampsysback.repository.GroupRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class GroupController {

    private final GroupRepository groupRepository;

    public GroupController(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @GetMapping("/api/groups")
    public List<GroupDto> listGroups() {
        return groupRepository.findAll().stream()
                .map(g -> new GroupDto(g.getGroupId(), g.getGroupName()))
                .collect(Collectors.toList());
    }

    /**
     * ★追加: 所属を追加するAPI
     * POST /api/groups
     * Body: { "groupName": "大阪校" }
     */
    @PostMapping("/api/groups")
    public GroupDto createGroup(@RequestBody Map<String, String> body) {
        String name = body.get("groupName");
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("グループ名は必須です");
        }

        GroupEntity g = new GroupEntity();
        g.setGroupName(name);
        // IDはDBの自動採番に任せる設定（@GeneratedValue）が必要です
        GroupEntity saved = groupRepository.save(g);

        return new GroupDto(saved.getGroupId(), saved.getGroupName());
    }

    // DTOクラス定義 (既存のものがあればそれを使用)
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