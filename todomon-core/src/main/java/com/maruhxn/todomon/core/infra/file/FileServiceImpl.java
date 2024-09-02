package com.maruhxn.todomon.core.infra.file;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.maruhxn.todomon.core.global.error.ErrorCode;
import com.maruhxn.todomon.core.global.error.exception.BadRequestException;
import com.maruhxn.todomon.core.global.error.exception.InternalServerException;
import com.maruhxn.todomon.core.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final AmazonS3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Override
    public void deleteFile(String storedFileName) {
        boolean isExist = s3Client.doesObjectExist(bucket, storedFileName);

        if (!isExist) {
            throw new NotFoundException(ErrorCode.NOT_FOUND_FILE);
        }

        s3Client.deleteObject(bucket, storedFileName);
    }

    @Override
    public String storeOneFile(MultipartFile file) {
        if (file.isEmpty()) throw new BadRequestException(ErrorCode.EMPTY_FILE);

        String fileName = file.getOriginalFilename();
        String storeFileName = createStoreFileName(fileName);

        try {
            if (!file.getContentType().startsWith("image")) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST, "이미지 형식의 파일만 업로드할 수 있습니다.");
            }
            File resizedFile = getResizedImage(file, 400);
            s3Client.putObject(new PutObjectRequest(bucket, storeFileName, resizedFile));
            resizedFile.delete();
        } catch (IOException e) {
            throw new InternalServerException(ErrorCode.S3_UPLOAD_ERROR, e.getMessage());
        }

        return storeFileName;
    }

    /**
     * 이미지 리사이징 + MultipartFile -> File로 만드는 메서드
     *
     * @param multipartFile
     * @param targetWidth
     * @return
     * @throws IOException
     */
    private File getResizedImage(MultipartFile multipartFile, int targetWidth) throws IOException {
        // 시스템의 임시 디렉토리에 원본 파일 이름으로 임시 파일을 생성
        File file = new File(System.getProperty("java.io.tmpdir") + "/" + multipartFile.getOriginalFilename());
        // MultipartFile의 콘텐츠 타입에서 형식 이름을 추출 (예: "jpeg", "png").
        String formatName = multipartFile.getContentType().split("/")[1];
        // MultipartFile의 바이트를 임시 파일에 쓰기 위해 FileOutputStream을 사용
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }

        // ImageIO를 사용하여 임시 파일에서 원본 이미지를 읽어옴.
        BufferedImage originalImage = ImageIO.read(file);
        int originWidth = originalImage.getWidth();
        int originHeight = originalImage.getHeight();

        // 원본 폭이 대상 폭보다 작으면 원본 파일을 반환
        if (originWidth < targetWidth)
            return file;

        // 원본 이미지의 종횡비를 유지하면서 새로운 높이를 계산
        double ratio = (double) originHeight / (double) originWidth;
        int height = (int) Math.round(targetWidth * ratio);

        // 원본 이미지를 새로운 폭과 높이로 스케일링
        java.awt.Image scaledImage = originalImage.getScaledInstance(targetWidth, height, java.awt.Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(targetWidth, height, BufferedImage.TYPE_INT_RGB);
        resizedImage.getGraphics().drawImage(scaledImage, 0, 0, null);

        // 리사이즈된 이미지를 나타내는 새로운 임시 파일을 생성
        File resizedFile = new File(System.getProperty("java.io.tmpdir") + "/resized_" + multipartFile.getOriginalFilename());
        // ImageIO를 사용하여 리사이즈된 이미지를 새로운 임시 파일에 쓰기
        ImageIO.write(resizedImage, formatName, resizedFile);

        return resizedFile;
    }

    /**
     * 서버에 저장될 파일명 생성
     *
     * @param originalFilename
     * @return
     */
    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    /**
     * 파일 확장자 추출
     *
     * @param originalFilename
     * @return
     */
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }
}
