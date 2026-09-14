package com.projectmanagement.app.activity;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityService {

    private final ActivityRepository activityRepository;

    @Transactional(readOnly = true)
    public List<ActivityResponse> getAll() {

        return activityRepository
                .findAllByOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getActive() {

        return activityRepository
                .findByDeletedAtIsNullOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getDefault() {

        return activityRepository
                .findByIsDefaultTrueAndDeletedAtIsNullOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ActivityResponse getById(Long id) {

        Activity activity = activityRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Activity not found with id: " + id));

        return toResponse(activity);
    }

    @Transactional(readOnly = true)
    public ActivityResponse getActiveById(Long id) {

        Activity activity = activityRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Active activity not found with id: " + id));

        return toResponse(activity);
    }

    @Transactional
    public ActivityResponse create(ActivityRequest request) {

        String name = request.getName().trim();

        if (activityRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Activity with this name already exists");
        }

        Activity activity = Activity.builder()
                .name(name)
                .description(request.getDescription())
                .isDefault(
                        request.getIsDefault() != null
                                ? request.getIsDefault()
                                : false)
                .build();

        /*
         * Only one default activity should exist.
         * If a new activity is marked default,
         * remove default from existing activities.
         */
        if (Boolean.TRUE.equals(activity.getIsDefault())) {
            clearDefaultActivities();
        }

        return toResponse(activityRepository.save(activity));
    }

    @Transactional
    public ActivityResponse update(Long id, ActivityRequest request) {

        Activity activity = activityRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Activity not found with id: " + id));

        String name = request.getName().trim();

        if (activityRepository
                .existsByNameIgnoreCaseAndIdNotAndDeletedAtIsNull(name, id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Activity with this name already exists");
        }

        activity.setName(name);
        activity.setDescription(request.getDescription());

        Boolean isDefault = request.getIsDefault() != null
                ? request.getIsDefault()
                : false;

        if (Boolean.TRUE.equals(isDefault)) {
            clearDefaultActivities(id);
        }

        activity.setIsDefault(isDefault);

        return toResponse(activityRepository.save(activity));
    }

    @Transactional
    public void delete(Long id) {

        Activity activity = activityRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Activity not found with id: " + id));

        activity.setDeletedAt(java.time.LocalDateTime.now());

        /*
         * Deleted activity cannot remain default.
         */
        activity.setIsDefault(false);

        activityRepository.save(activity);
    }

    @Transactional
    public ActivityResponse restore(Long id) {

        Activity activity = activityRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Activity not found with id: " + id));

        if (activity.getDeletedAt() == null) {
            return toResponse(activity);
        }

        if (activityRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(
                activity.getName())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An active activity with the same name already exists");
        }

        activity.setDeletedAt(null);

        return toResponse(activityRepository.save(activity));
    }

    @Transactional
    public void permanentlyDelete(Long id) {

        Activity activity = activityRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Activity not found with id: " + id));

        activityRepository.delete(activity);
    }

    private void clearDefaultActivities() {

        clearDefaultActivities(null);
    }

    private void clearDefaultActivities(Long exceptId) {

        List<Activity> defaults = activityRepository
                .findByIsDefaultTrueAndDeletedAtIsNullOrderByIdAsc();

        for (Activity activity : defaults) {

            if (exceptId == null || !activity.getId().equals(exceptId)) {
                activity.setIsDefault(false);
            }
        }

        activityRepository.saveAll(defaults);
    }

    private ActivityResponse toResponse(Activity activity) {

        return ActivityResponse.builder()
                .id(activity.getId())
                .name(activity.getName())
                .description(activity.getDescription())
                .isDefault(activity.getIsDefault())
                .deletedAt(activity.getDeletedAt())
                .createdAt(activity.getCreatedAt())
                .updatedAt(activity.getUpdatedAt())
                .build();
    }
}