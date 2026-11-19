package kz.iitu.spring_lab_01.author;

import jakarta.persistence.*;

@Entity
@Table(name = "author")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(length = 60)
    private String country;

    protected Author() { }

    public Author(String name, String country) {
        this.name = name;
        this.country = country;
    }

    /** For tests and mapping: an author that already has an id. */
    public Author(Long id, String name, String country) {
        this(name, country);
        this.id = id;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCountry() { return country; }
}
