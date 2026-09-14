package com.projectmanagement.app.usergroup;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserGroupService {
    private final UserGroupRepository groupRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserGroupResponse> getAll() {
        return groupRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserGroupResponse getById(Long id) {
        return toResponse(getGroup(id));
    }

    public UserGroupResponse create(UserGroupRequest r) {
        String n = r.getName().trim();
        if (groupRepository.existsByNameIgnoreCase(n))
            throw new RuntimeException("Group name already exists: " + n);
        return toResponse(
                groupRepository.save(UserGroup.builder().name(n).description(blankToNull(r.getDescription())).build()));
    }

    public UserGroupResponse update(Long id, UserGroupRequest r) {
        UserGroup g = getGroup(id);
        String n = r.getName().trim();
        if (!g.getName().equalsIgnoreCase(n) && groupRepository.existsByNameIgnoreCase(n))
            throw new RuntimeException("Group name already exists: " + n);
        g.setName(n);
        g.setDescription(blankToNull(r.getDescription()));
        return toResponse(groupRepository.save(g));
    }

    public void delete(Long id) {
        groupRepository.delete(getGroup(id));
    }

    public UserGroupResponse addMember(Long groupId, Long userId) {
        UserGroup g = getGroup(groupId);
        User u = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        if (u.getDeletedAt() != null)
            throw new RuntimeException("Cannot add an inactive user");
        g.getMembers().add(u);
        return toResponse(g);
    }

    public UserGroupResponse removeMember(Long groupId, Long userId) {
        UserGroup g = getGroup(groupId);
        g.getMembers().removeIf(u -> u.getId().equals(userId));
        return toResponse(g);
    }

    private UserGroup getGroup(Long id) {
        return groupRepository.findById(id).orElseThrow(() -> new RuntimeException("User group not found: " + id));
    }

    private UserGroupResponse toResponse(UserGroup g) {
        return UserGroupResponse.builder().id(g.getId()).name(g.getName()).description(g.getDescription()).members(g
                .getMembers().stream()
                .map(u -> UserGroupMemberResponse.builder().id(u.getId()).name(u.getName()).email(u.getEmail())
                        .hasProfileImage(u.getProfileImageData() != null && u.getProfileImageData().length > 0).build())
                .toList()).createdAt(g.getCreatedAt()).updatedAt(g.getUpdatedAt()).build();
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
