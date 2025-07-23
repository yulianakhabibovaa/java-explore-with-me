package ru.practicum.ewm.category.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;

import java.util.Collection;
import java.util.List;

@UtilityClass
public class CategoryMapper {
    public Category toCategory(NewCategoryDto dto) {
        return Category.builder()
                .name(dto.getName())
                .build();
    }

    public CategoryDto toDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }

    public List<CategoryDto> toDtoList(Collection<Category> categories) {
        return categories.stream()
                .map(CategoryMapper::toDto)
                .toList();
    }
}
