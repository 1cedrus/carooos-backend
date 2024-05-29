package org.one_cedrus.carobackend.user.image;

import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

import jakarta.transaction.Transactional;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/image")
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/{fileName}")
    @Transactional
    public ResponseEntity<?> getImage(@PathVariable String fileName) {
        byte[] imageData = imageService.downloadImage(fileName);
        return ResponseEntity.status(HttpStatus.OK)
            .cacheControl(CacheControl.maxAge(Duration.ofDays(365)))
            .contentType(MediaType.valueOf(IMAGE_PNG_VALUE))
            .body(imageData);
    }
}
