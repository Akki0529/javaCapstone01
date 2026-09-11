package assembly.general.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "books")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String isbn;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    private String genre;

    private Integer publicationYear;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String publisher;

    private Integer pageCount;

    private String language;

    @Column(nullable = false)
    private Integer totalCopies;

    @Column(nullable = false)
    private Integer availableCopies;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();
}
