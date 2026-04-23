package com.example.eStore.service;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.constants.Constants;
import com.example.eStore.dto.request.ProfileUpdateRequest;
import com.example.eStore.dto.response.ApiResponseFactory;
import com.example.eStore.dto.response.ProfileResponse;
import com.example.eStore.dto.response.UserResponse;
import com.example.eStore.entity.Role;
import com.example.eStore.entity.User;
import com.example.eStore.exception.AppException;
import com.example.eStore.profile.ProfileMetadataService;
import com.example.eStore.repository.UserRepository;
import com.example.eStore.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:0|\\+84)\\d{9,10}$");

    private final UserRepository userRepository;
    private final ProfileMetadataService profileMetadataService;

    public BaseResultDTO<ProfileResponse> getCurrentProfile() {
        User user = resolveCurrentUser();
        Set<String> roleNames = extractRoleNames(user);

        return ApiResponseFactory.success(
                Constants.Message.Profile.GET_SUCCESS,
                ProfileResponse.builder()
                        .user(toUserResponse(user))
                        .fields(profileMetadataService.resolveFieldMetadata(roleNames))
                        .build()
        );
    }

    @Transactional
    public BaseResultDTO<ProfileResponse> updateCurrentProfile(ProfileUpdateRequest request) {
        User user = resolveCurrentUser();
        Set<String> roleNames = extractRoleNames(user);
        Set<String> editableFields = profileMetadataService.resolveEditableFields(roleNames);

        Map<String, String> updates = request.getUpdates() == null ? Map.of() : request.getUpdates();
        Set<String> changedFields = new LinkedHashSet<>();

        updates.forEach((fieldName, rawValue) ->
                applyFieldUpdate(user, editableFields, changedFields, fieldName, rawValue)
        );

        if (!changedFields.isEmpty()) {
            userRepository.save(user);
        }

        return ApiResponseFactory.success(
                Constants.Message.Profile.UPDATE_SUCCESS,
                ProfileResponse.builder()
                        .user(toUserResponse(user))
                        .fields(profileMetadataService.resolveFieldMetadata(roleNames))
                        .build()
        );
    }

    private void applyFieldUpdate(User user,
                                  Set<String> editableFields,
                                  Set<String> changedFields,
                                  String fieldName,
                                  String rawValue) {
        String normalizedField = normalizeFieldName(fieldName);

        if (!profileMetadataService.isManagedField(normalizedField)) {
            throw new AppException(
                    "Unsupported profile field: " + normalizedField,
                    Constants.ErrorCode.Profile.FIELD_NOT_SUPPORTED
            );
        }

        if (!editableFields.contains(normalizedField)) {
            throw new AppException(
                    "Field is not editable for current role: " + normalizedField,
                    Constants.ErrorCode.Profile.FIELD_NOT_EDITABLE
            );
        }

        switch (normalizedField) {
            case "fullName" -> {
                String value = requireTrimmedText(rawValue, "fullName", 120, Constants.ErrorCode.Profile.INVALID_FULL_NAME);
                updateIfChanged(normalizedField, user.getFullName(), value, user::setFullName, changedFields);
            }
            case "phone" -> {
                String value = optionalTrimmedText(rawValue, 20, Constants.ErrorCode.Profile.INVALID_PHONE);
                if (value != null && !PHONE_PATTERN.matcher(value).matches()) {
                    throw new AppException("Phone format is invalid", Constants.ErrorCode.Profile.INVALID_PHONE);
                }
                updateIfChanged(normalizedField, user.getPhone(), value, user::setPhone, changedFields);
            }
            case "address" -> {
                String value = optionalTrimmedText(rawValue, 255, Constants.ErrorCode.Profile.INVALID_ADDRESS);
                updateIfChanged(normalizedField, user.getAddress(), value, user::setAddress, changedFields);
            }
            default -> throw new AppException(
                    "Unsupported profile field: " + normalizedField,
                    Constants.ErrorCode.Profile.FIELD_NOT_SUPPORTED
            );
        }
    }

    private void updateIfChanged(String fieldName,
                                 String oldValue,
                                 String newValue,
                                 java.util.function.Consumer<String> setter,
                                 Set<String> changedFields) {
        if (Objects.equals(oldValue, newValue)) {
            return;
        }

        setter.accept(newValue);
        changedFields.add(fieldName);
    }

    private String normalizeFieldName(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            throw new AppException(
                    "Field name is required",
                    Constants.ErrorCode.Profile.FIELD_NOT_SUPPORTED
            );
        }
        return fieldName.trim();
    }

    private String requireTrimmedText(String value, String fieldName, int maxLength, String errorCode) {
        String normalized = value == null ? null : value.trim();

        if (normalized == null || normalized.isBlank()) {
            throw new AppException(fieldName + " is required", errorCode);
        }

        if (normalized.length() > maxLength) {
            throw new AppException(fieldName + " exceeds max length " + maxLength, errorCode);
        }

        return normalized;
    }

    private String optionalTrimmedText(String value, int maxLength, String errorCode) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        if (normalized.length() > maxLength) {
            throw new AppException("Value exceeds max length " + maxLength, errorCode);
        }

        return normalized;
    }

    private User resolveCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(
                        "Current user not found",
                        Constants.ErrorCode.Profile.NOT_FOUND
                ));
    }

    private Set<String> extractRoleNames(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
