package org.one_cedrus.carobackend.user;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.one_cedrus.carobackend.user.model.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(
        ImageService.class
    );
    private final ImageRepository imageRepository;

    // TODO: Verify imageData is actually a image and also restrict how big image can be =))
    public String uploadImage(MultipartFile imageData) {
        try {
            var imageType = Objects.requireNonNull(
                imageData.getOriginalFilename()
            ).split("\\.")[1];

            var imageName = UUID.randomUUID() + "." + imageType;

            var imageToSave = Image.builder()
                .name(imageName)
                .imageData(imageData.getBytes())
                .build();

            imageRepository.save(imageToSave);

            return imageName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] downloadImage(String imageName) {
        return imageRepository
            .findByName(imageName)
            .orElseThrow(() -> new RuntimeException("Image name not found"))
            .getImageData();
    }
}
