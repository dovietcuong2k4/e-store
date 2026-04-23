package com.example.eStore.profile;

import com.example.eStore.dto.constants.Constants;
import com.example.eStore.dto.response.ProfileFieldMetadataResponse;
import com.example.eStore.entity.User;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProfileMetadataService {

    private static final Map<String, Set<String>> EDITABLE_FIELDS_BY_ROLE = Map.of(
            Constants.Role.ADMIN, Set.of("fullName", "phone", "address"),
            Constants.Role.STAFF, Set.of("fullName", "phone", "address"),
            Constants.Role.SHIPPER, Set.of("fullName", "phone", "address"),
            Constants.Role.CUSTOMER, Set.of("fullName", "phone", "address")
    );

    private static final Set<String> PROFILE_FIELDS = Arrays.stream(User.class.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(ProfileField.class))
            .map(Field::getName)
            .collect(Collectors.toUnmodifiableSet());

    public List<ProfileFieldMetadataResponse> resolveFieldMetadata(Set<String> roleNames) {
        Set<String> editableFields = resolveEditableFields(roleNames);

        return Arrays.stream(User.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ProfileField.class))
                .map(field -> toMetadata(field, editableFields.contains(field.getName())))
                .sorted(Comparator.comparing(ProfileFieldMetadataResponse::getOrder))
                .toList();
    }

    public Set<String> resolveEditableFields(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return Set.of();
        }

        return roleNames.stream()
                .map(role -> EDITABLE_FIELDS_BY_ROLE.getOrDefault(role, Set.of()))
                .flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public boolean isManagedField(String fieldName) {
        return PROFILE_FIELDS.contains(fieldName);
    }

    private ProfileFieldMetadataResponse toMetadata(Field field, boolean editable) {
        ProfileField profileField = field.getAnnotation(ProfileField.class);

        return ProfileFieldMetadataResponse.builder()
                .name(field.getName())
                .label(profileField.label())
                .type(profileField.type().name())
                .editable(editable)
                .required(profileField.required())
                .maxLength(profileField.maxLength())
                .pattern(profileField.pattern())
                .placeholder(profileField.placeholder())
                .order(profileField.order())
                .build();
    }
}
