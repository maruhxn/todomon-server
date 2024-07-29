package com.maruhxn.todomon.infra.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    Resource getImage(String storedFileName);

    void deleteFile(String storedFileName);

    String storeOneFile(MultipartFile file);

}
