package kz.iitu.spring_lab_01.service;

import kz.iitu.spring_lab_01.audit.Audited;
import kz.iitu.spring_lab_01.security.RequiresRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class CatalogService {

    @Autowired
    @Lazy
    private CatalogService self;

    public String findById(long id) {
        sleep(50); // имитация обращения к базе
        return "Item no. " + id;
    }

    @Audited(action = "CATALOG_LIST", logArguments = true)
    public List<String> findAll(int limit) {
        sleep(300); // намеренно медленный метод
        return IntStream.rangeClosed(1, limit)
                .mapToObj(i -> "Item no. " + i)
                .toList();
    }

    @Audited(action = "CATALOG_REMOVE")
    public String remove(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid identifier: " + id);
        }
        return "Removed item no. " + id;
    }

    public String removeTwice(long id) {
        String first = self.remove(id);      // через прокси, аспекты сработают
        String second = self.remove(id + 1); // то же самое
        return first + "; " + second;
    }

    @RequiresRole("ADMIN")
    public String clearCatalog() {
        return "Catalog cleared";
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}