package assembly.general.api.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Generic paginated response wrapper matching the spec format:
 * { content, page, size, totalElements, totalPages, last }
 */
@Getter
@Builder
public class PagedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static <E, T> PagedResponse<T> of(Page<E> pageResult, Function<E, T> mapper) {
        return PagedResponse.<T>builder()
            .content(pageResult.getContent().stream().map(mapper).toList())
            .page(pageResult.getNumber())
            .size(pageResult.getSize())
            .totalElements(pageResult.getTotalElements())
            .totalPages(pageResult.getTotalPages())
            .last(pageResult.isLast())
            .build();
    }
}
