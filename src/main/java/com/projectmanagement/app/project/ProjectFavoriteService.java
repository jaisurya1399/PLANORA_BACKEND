package com.projectmanagement.app.project;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectFavoriteService {

        private final ProjectFavoriteRepository projectFavoriteRepository;
        private final UserRepository userRepository;
        private final ProjectRepository projectRepository;

        @Transactional(readOnly = true)
        public List<ProjectFavoriteResponse> getAll() {
                return projectFavoriteRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public ProjectFavoriteResponse getById(Long id) {

                return toResponse(getEntity(id));
        }

        @Transactional(readOnly = true)
        public List<ProjectFavoriteResponse> getByUser(Long userId) {

                validateUserExists(userId);

                return projectFavoriteRepository
                                .findByUserId(userId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<ProjectFavoriteResponse> getByProject(Long projectId) {

                validateProjectExists(projectId);

                return projectFavoriteRepository
                                .findByProjectId(projectId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public ProjectFavoriteResponse getByUserAndProject(
                        Long userId,
                        Long projectId) {

                ProjectFavorite favorite = projectFavoriteRepository
                                .findByUserIdAndProjectId(userId, projectId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Project favorite not found"));

                return toResponse(favorite);
        }

        @Transactional(readOnly = true)
        public boolean exists(
                        Long userId,
                        Long projectId) {
                return projectFavoriteRepository
                                .existsByUserIdAndProjectId(
                                                userId,
                                                projectId);
        }

        public ProjectFavoriteResponse create(
                        ProjectFavoriteRequest request) {

                User user = getUser(request.getUserId());
                Project project = getProject(request.getProjectId());

                /*
                 * The original database does not define a unique constraint,
                 * but the application should not create duplicate favorites.
                 */
                if (projectFavoriteRepository
                                .existsByUserIdAndProjectId(
                                                request.getUserId(),
                                                request.getProjectId())) {

                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Project is already marked as favorite");
                }

                ProjectFavorite favorite = ProjectFavorite.builder()
                                .user(user)
                                .project(project)
                                .build();

                return toResponse(
                                projectFavoriteRepository.save(favorite));
        }

        public void delete(Long id) {

                ProjectFavorite favorite = getEntity(id);

                projectFavoriteRepository.delete(favorite);
        }

        public void deleteByUserAndProject(
                        Long userId,
                        Long projectId) {

                ProjectFavorite favorite = projectFavoriteRepository
                                .findByUserIdAndProjectId(
                                                userId,
                                                projectId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Project favorite not found"));

                projectFavoriteRepository.delete(favorite);
        }

        public void deleteByUser(Long userId) {

                validateUserExists(userId);

                projectFavoriteRepository.deleteByUserId(userId);
        }

        public void deleteByProject(Long projectId) {

                validateProjectExists(projectId);

                projectFavoriteRepository.deleteByProjectId(projectId);
        }

        @Transactional(readOnly = true)
        public long countByUser(Long userId) {

                validateUserExists(userId);

                return projectFavoriteRepository.countByUserId(userId);
        }

        @Transactional(readOnly = true)
        public long countByProject(Long projectId) {

                validateProjectExists(projectId);

                return projectFavoriteRepository.countByProjectId(projectId);
        }

        private ProjectFavorite getEntity(Long id) {

                return projectFavoriteRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Project favorite not found"));
        }

        private User getUser(Long id) {

                return userRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "User not found"));
        }

        private Project getProject(Long id) {

                return projectRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Project not found"));
        }

        private void validateUserExists(Long id) {

                if (!userRepository.existsById(id)) {
                        throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "User not found");
                }
        }

        private void validateProjectExists(Long id) {

                if (!projectRepository.existsById(id)) {
                        throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Project not found");
                }
        }

        private ProjectFavoriteResponse toResponse(
                        ProjectFavorite favorite) {

                User user = favorite.getUser();
                Project project = favorite.getProject();

                return ProjectFavoriteResponse.builder()
                                .id(favorite.getId())

                                .userId(
                                                user != null
                                                                ? user.getId()
                                                                : null)
                                .userName(
                                                user != null
                                                                ? user.getName()
                                                                : null)
                                .userEmail(
                                                user != null
                                                                ? user.getEmail()
                                                                : null)

                                .projectId(
                                                project != null
                                                                ? project.getId()
                                                                : null)
                                .projectName(
                                                project != null
                                                                ? project.getName()
                                                                : null)
                                .projectTicketPrefix(
                                                project != null
                                                                ? project.getTicketPrefix()
                                                                : null)

                                .createdAt(favorite.getCreatedAt())
                                .updatedAt(favorite.getUpdatedAt())
                                .build();
        }
}