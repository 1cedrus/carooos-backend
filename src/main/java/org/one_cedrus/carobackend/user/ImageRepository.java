package org.one_cedrus.carobackend.user;

import java.util.Optional;
import org.one_cedrus.carobackend.user.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, String> {
    Optional<Image> findByName(String url);
}
